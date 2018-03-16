package com.ycengine.ontimealarm;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.ycengine.ontimealarm.data.Databases;
import com.ycengine.ontimealarm.data.OnTimeItem;
import com.ycengine.yclibrary.util.DeviceUtil;
import com.ycengine.yclibrary.util.DurationUtil;
import com.ycengine.yclibrary.util.LogUtil;
import com.ycengine.yclibrary.util.handler.IOnHandlerMessage;
import com.ycengine.yclibrary.util.handler.WeakRefHandler;

import java.util.ArrayList;

import static com.ycengine.yclibrary.Constants.getAdRequest;

public class MainActivity extends BaseActivity implements View.OnClickListener, IOnHandlerMessage {
    private WeakRefHandler mWeakRefHandler;
    private FinishConfirmDialog mFinishConfirmDialog;

    private InterstitialAd mInterstitialAd;

    private AlarmController mAlarmController;

    private Databases mDatabases;
    private OnTimeAdapter mOnTimeAdapter;
    private ArrayList<OnTimeItem> arrOnTimeItem;

    private RelativeLayout mEmptyLayout;
    private ListView mListView;

    @Override
    public void onBackPressed() {
        if (mFinishConfirmDialog != null && !mFinishConfirmDialog.isShowing()) {
            mFinishConfirmDialog.show();
        } else {
            finish();
        }
    }

    @Override
    protected int layoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final View rootView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        // http://stackoverflow.com/a/24035591/5055032, http://stackoverflow.com/a/3948036/5055032
        // 레이아웃의 뷰가 그리기 시작했습니다. (The views in our layout have begun drawing.)
        // 레이아웃이 끝나는 시점을 알려주는 라이프 사이클 콜백은 없습니다. (There is no lifecycle callback that tells us when our layout finishes drawing.)
        // 내 자신의 테스트에서 여전히 드로잉은 onResume ()에 의해 완료되지 않았습니다. (in my own test, drawing still isn't finished by onResume.)
        // 드로잉이 완료된 후에 UI 이벤트 큐에 메시지를 게시하여 크기를 얻을 수 있도록 게시하십시오. (Post a message in the UI events queue to be executed after drawing is complete, so that we may get their dimensions.)
        rootView.post(new Runnable() {
            @Override
            public void run() {
            }
        });

        mWeakRefHandler = new WeakRefHandler(this);
        mAlarmController = new AlarmController(this);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.banner_ad_fullscreen_id));
        mInterstitialAd.loadAd(getAdRequest());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
            }
        });

        AdView mAdView = (AdView) findViewById(R.id.adView);
        mAdView.loadAd(getAdRequest());
        mAdView.setAdListener(mAdListener);

        mFinishConfirmDialog = new FinishConfirmDialog(this);
        //mFinishConfirmDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        mFinishConfirmDialog.setListener(new FinishConfirmDialog.OnDialogEventListener() {
            @Override
            public void onClickDialog(Dialog dialog, int index) {
                if (index == FinishConfirmDialog.dialog_result_cancel) {
                    if (mFinishConfirmDialog != null && mFinishConfirmDialog.isShowing()) {
                        mFinishConfirmDialog.dismiss();
                    }
                } else if (index == FinishConfirmDialog.dialog_result_confirm) {
                    mFinishConfirmDialog.hide();
                    mFinishConfirmDialog.dismiss();
                    finish();
                }
            }
        });

        mDatabases = new Databases(mContext);
        mDatabases.open();
        arrOnTimeItem = mDatabases.getOnTimeItemList();
        mDatabases.close();

        mOnTimeAdapter = new OnTimeAdapter(mContext, arrOnTimeItem, mWeakRefHandler);
        mEmptyLayout = (RelativeLayout) findViewById(R.id.mEmptyLayout);
        mListView = (ListView)findViewById(R.id.mListView);
        mListView.setAdapter(mOnTimeAdapter);
        mWeakRefHandler.sendEmptyMessage(Constants.REQUEST_UPDATE_LISTVIEW_UI);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(final View v) {
        final int id = v.getId();

        switch (id) {
            case R.id.mPlusBtn:
                Intent intent = new Intent(mContext, SettingActivity.class);
                startActivityForResult(intent, Constants.REQUEST_SETTING);
                break;
        }
    }

    @Override
    public void handleMessage(Message msg) {
        Bundle data = msg.getData();
        int position;

        switch (msg.what) {
            case Constants.REQUEST_NOTIFY_CHANGE_ENABLED:
                position = data.getInt("position");
                boolean enabled = data.getBoolean("enabled");

                arrOnTimeItem.get(position).setOnTimeEnabled((enabled) ? Constants.ONTIME_ENABLED : Constants.ONTIME_DISABLED);
                mOnTimeAdapter.notifyDataSetChanged();

                if (mDatabases == null) mDatabases = new Databases(mContext);
                mDatabases.open();
                mDatabases.updateOnTimeItem(arrOnTimeItem.get(position));
                mDatabases.close();

                if (enabled) {
                    long setTime = mAlarmController.save(arrOnTimeItem.get(position), false);
                    if (setTime > 0) {
                        String message = mContext.getString(R.string.alarm_set_for, DurationUtil.toString(mContext, setTime - System.currentTimeMillis(), false));
                        DeviceUtil.showToast(mContext, message);
                    }
                } else {
                    mAlarmController.cancelAlarm(arrOnTimeItem.get(position));
                }
                break;
            case Constants.REQUEST_NOTIFY_CHANGE_DATA:
                position = data.getInt("position");

                OnTimeItem item = arrOnTimeItem.get(position);

                Intent intent = new Intent(mContext, SettingActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("OnTimeItem", item);
                startActivityForResult(intent, Constants.REQUEST_NOTIFY_CHANGE_DATA);
                break;
            case Constants.REQUEST_FULL_SCREEN_AD_SHOW:
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
                break;
            case Constants.REQUEST_UPDATE_LISTVIEW_UI:
                if (arrOnTimeItem.size() > 0) {
                    mEmptyLayout.setVisibility(View.GONE);
                    mListView.setVisibility(View.VISIBLE);
                } else {
                    mEmptyLayout.setVisibility(View.VISIBLE);
                    mListView.setVisibility(View.GONE);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.e("Request Code(" + requestCode + "), Result Code(" + resultCode + ")");
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.REQUEST_SETTING:
                if (resultCode == RESULT_OK) {
                    OnTimeItem onTimeItem = data.getParcelableExtra("item");

                    if (mDatabases == null) mDatabases = new Databases(mContext);
                    mDatabases.open();
                    mDatabases.setOnTimeItemList(onTimeItem);
                    arrOnTimeItem = mDatabases.getOnTimeItemList();
                    mDatabases.close();

                    mOnTimeAdapter.addAllItems(arrOnTimeItem);
                    mWeakRefHandler.sendEmptyMessage(Constants.REQUEST_UPDATE_LISTVIEW_UI);

                    long setTime = mAlarmController.save(arrOnTimeItem.get(arrOnTimeItem.size() - 1), false);
                    if (setTime > 0) {
                        String message = mContext.getString(R.string.alarm_set_for, DurationUtil.toString(mContext, setTime - System.currentTimeMillis(), false));
                        DeviceUtil.showToast(mContext, message);
                    }

                    mWeakRefHandler.sendEmptyMessage(Constants.REQUEST_FULL_SCREEN_AD_SHOW);
                }
                break;
            case Constants.REQUEST_NOTIFY_CHANGE_DATA:
                if (resultCode == RESULT_OK) {
                    int position = data.getIntExtra("position", -1);

                    if (position != -1) {
                        OnTimeItem onTimeItem = data.getParcelableExtra("item");

                        if (onTimeItem == null) {
                            mAlarmController.cancelAlarm(arrOnTimeItem.get(position));

                            int cancelIdx = arrOnTimeItem.get(position).getOnTimeIdx();
                            if (mDatabases == null) mDatabases = new Databases(mContext);
                            mDatabases.open();
                            mDatabases.deleteOnTimeItem(cancelIdx);
                            arrOnTimeItem = mDatabases.getOnTimeItemList();
                            mDatabases.close();

                            mOnTimeAdapter.addAllItems(arrOnTimeItem);
                            mWeakRefHandler.sendEmptyMessage(Constants.REQUEST_UPDATE_LISTVIEW_UI);
                        } else {
                            long setTime = mAlarmController.save(onTimeItem, false);
                            if (setTime > 0) {
                                String message = mContext.getString(R.string.alarm_set_for, DurationUtil.toString(mContext, setTime - System.currentTimeMillis(), false));
                                DeviceUtil.showToast(mContext, message);
                            }

                            if (mDatabases == null) mDatabases = new Databases(mContext);
                            mDatabases.open();
                            mDatabases.updateOnTimeItem(onTimeItem);
                            mDatabases.close();

                            mOnTimeAdapter.setItem(position, onTimeItem);

                            mWeakRefHandler.sendEmptyMessage(Constants.REQUEST_FULL_SCREEN_AD_SHOW);
                        }
                    }
                }
                break;
        }
    }
}
