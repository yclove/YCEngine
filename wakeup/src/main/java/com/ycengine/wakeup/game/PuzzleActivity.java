package com.ycengine.wakeup.game;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ycengine.wakeup.BaseActivity;
import com.ycengine.wakeup.Constants;
import com.ycengine.wakeup.MainActivity;
import com.ycengine.wakeup.R;
import com.ycengine.wakeup.data.Databases;
import com.ycengine.wakeup.data.NotifyItem;
import com.ycengine.yclibrary.util.CommonUtil;
import com.ycengine.yclibrary.util.DeviceUtil;
import com.ycengine.yclibrary.util.LogUtil;
import com.ycengine.yclibrary.util.handler.IOnHandlerMessage;
import com.ycengine.yclibrary.util.handler.WeakRefHandler;
import com.ycengine.yclibrary.widget.RingtoneLoop;

import java.util.Random;

public class PuzzleActivity extends BaseActivity implements View.OnClickListener, IOnHandlerMessage {
    private WeakRefHandler mWeakRefHandler;
    private RingtoneLoop mRingtone;
    private Vibrator mVibrator;

    private Databases mDatabases;
    private NotifyItem mNotifyItem;

    private boolean isPreview = false;
    private int mIdx;
    private TextView mNotifyName;
    private ImageButton mObjectStBtn, mObjectNdBtn, mObjectRdBtn;

    private int mCompleteCount = 0;
    private int screenWidth, objectWidth, possibleRange;

    @Override
    public void onBackPressed() {}

    @Override
    protected int layoutResId() {
        return R.layout.activity_game_puzzle;
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
                try {
                    screenWidth = DeviceUtil.getScreenWidthInPXs(mContext);
                    objectWidth = mObjectStBtn.getWidth();
                    possibleRange = (screenWidth / 2) - (objectWidth / 2);

                    LogUtil.e("screenWidth :::: " + screenWidth + " :::: objectWidth :::: " + objectWidth + " :::: possibleRange :::: " + possibleRange);

                    Random generator = new Random();
                    int mObjectStBtnMargin = generator.nextInt(possibleRange - 1);
                    int mObjectNdBtnMargin = generator.nextInt(possibleRange - 1);
                    int mObjectRdBtnMargin = generator.nextInt(possibleRange - 1);

                    if (generator.nextInt(2) == 1) mObjectStBtnMargin = -mObjectStBtnMargin;
                    if (generator.nextInt(2) == 1) mObjectNdBtnMargin = -mObjectNdBtnMargin;
                    if (generator.nextInt(2) == 1) mObjectRdBtnMargin = -mObjectRdBtnMargin;

                    LogUtil.e("mObjectStBtnMargin :::: " + mObjectStBtnMargin + " :::: mObjectNdBtnMargin :::: " + mObjectNdBtnMargin + " :::: mObjectRdBtnMargin :::: " + mObjectRdBtnMargin);

                    RelativeLayout.LayoutParams paramsSt = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    paramsSt.setMargins(mObjectStBtnMargin, 0, 0, 0);
                    mObjectStBtn.setLayoutParams(paramsSt);

                    RelativeLayout.LayoutParams paramsNd = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    paramsNd.setMargins(mObjectNdBtnMargin, 0, 0, 0);
                    mObjectNdBtn.setLayoutParams(paramsNd);

                    RelativeLayout.LayoutParams paramsRd = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    paramsRd.setMargins(mObjectRdBtnMargin, 0, 0, 0);
                    mObjectRdBtn.setLayoutParams(paramsRd);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mWeakRefHandler = new WeakRefHandler(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        Intent intent = getIntent();
        mIdx = intent.getIntExtra("idx", -1);
        isPreview = intent.getBooleanExtra("preview", false);

        mObjectStBtn = (ImageButton)findViewById(R.id.mObjectStBtn);
        mObjectNdBtn = (ImageButton)findViewById(R.id.mObjectNdBtn);
        mObjectRdBtn = (ImageButton)findViewById(R.id.mObjectRdBtn);

        if (!isPreview) {
            if (mIdx == -1) {
                LogUtil.e("전달 된 데이터(IDX)에 문제가 발생하였습니다.");
                finish();
            } else {
                if (mDatabases == null) mDatabases = new Databases(mContext);
                mDatabases.open();
                mNotifyItem = mDatabases.getNotifyItem(mIdx);
                mDatabases.close();

                if (mNotifyItem == null) {
                    LogUtil.e("전달 된 데이터(ITEM)에 문제가 발생하였습니다.");
                    finish();
                } else {
                    mNotifyName = (TextView)findViewById(R.id.mNotifyName);

                    if (CommonUtil.isNotNull(mNotifyItem.getNotiName())) {
                        mNotifyName.setVisibility(View.VISIBLE);
                        mNotifyName.setText(mNotifyItem.getNotiName());
                    } else {
                        mNotifyName.setVisibility(View.GONE);
                    }

                    if (mNotifyItem.getNotiType() != Constants.NOTI_TYPE_VIBRATE) {
                        mRingtone = new RingtoneLoop(mContext, getRingtoneUri());
                        if (mNotifyItem.getNotiVolume() > 0)
                            mRingtone.setmVolume(mNotifyItem.getNotiVolume());
                        mRingtone.play();
                    }

                    if (mNotifyItem.getNotiType() != Constants.NOTI_TYPE_SOUND) {
                        Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                        vibrator.vibrate(300);

                        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                        mVibrator.vibrate(new long[] { // apply pattern
                                0, // millis to wait before turning vibrator on
                                500, // millis to keep vibrator on before turning off
                                500, // millis to wait before turning back on
                                500 // millis to keep on before turning off
                        }, 2 /* start repeating at this index of the array, after one cycle */);
                    }
                }
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Set the content to appear under the system bars so that the
            // content doesn't resize when the system bars hide and show.
            // The system bars will remain hidden on user interaction;
            // however, they can be revealed using swipe gestures along
            // the region where they normally appear.
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;

            // Make status bar translucent, which automatically adds
            // SYSTEM_UI_FLAG_LAYOUT_STABLE and SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            // Looks too light on the current background..
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        }
    }

    protected Uri getRingtoneUri() {
        String ringtone = mNotifyItem.getNotiRingTone();
        if (ringtone.isEmpty()) {
            return Settings.System.DEFAULT_ALARM_ALERT_URI;
        }
        return Uri.parse(ringtone);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRingtone != null) {
            mRingtone.stop();
        }
        if (mVibrator != null) {
            mVibrator.cancel();
        }

        if (!isPreview) {
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("fullScreenAdShow", true);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(final View v) {
        final int id = v.getId();

        switch (id) {
            case R.id.mObjectStBtn:
            case R.id.mObjectNdBtn:
            case R.id.mObjectRdBtn:
                mCompleteCount++;
                v.setVisibility(View.GONE);
                if (mCompleteCount >= 3) {
                    finish();
                }
                break;
        }
    }

    @Override
    public void handleMessage(Message msg) {
        Bundle data = msg.getData();
        int position;

        switch (msg.what) {
            default:
                break;
        }
    }
}
