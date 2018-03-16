package com.ycengine.shoppinglist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StrikethroughSpan;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.ycengine.shoppinglist.data.Databases;
import com.ycengine.shoppinglist.data.GoodsItem;
import com.ycengine.yclibrary.library.helper.OnStartDragListener;
import com.ycengine.yclibrary.library.helper.SimpleItemTouchHelperCallback;
import com.ycengine.yclibrary.library.recyclerviewanim.adapter.AlphaAnimatorAdapter;
import com.ycengine.yclibrary.library.recyclerviewanim.itemanimator.SlideInOutLeftItemAnimator;
import com.ycengine.yclibrary.util.SPUtil;
import com.ycengine.yclibrary.util.handler.IOnHandlerMessage;
import com.ycengine.yclibrary.util.handler.WeakRefHandler;
import com.ycengine.yclibrary.widget.SimpleDividerItemDecoration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.ycengine.yclibrary.Constants.getAdRequest;

public class MainActivity extends BaseActivity implements View.OnClickListener, IOnHandlerMessage, OnStartDragListener {
    public static final int NOTIFICATION_ID = 7777;

    private WeakRefHandler mWeakRefHandler;
    private FirebaseAnalytics mFirebaseAnalytics;

    private FinishConfirmDialog mFinishConfirmDialog;

    private Switch mSwitch;
    private boolean mNotificationFlag = false;

    private InputMethodManager imm;
    private RelativeLayout mRootLayout, mEmptyLayout, mPlusBtn;
    private EditText mEditText;
    private ImageButton mChangeBtn, mCloseBtn;

    private Databases mDatabases;
    private GoodsAdapter mGoodsAdapter;
    private ArrayList<GoodsItem> arrGoodsItems;

    private RecyclerView mRecyclerView;
    private ItemTouchHelper mItemTouchHelper;

    private int mZoomLevel = 0;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWeakRefHandler = new WeakRefHandler(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

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

        mNotificationFlag = SPUtil.getBooleanSharedPreference(mContext, Constants.SP_NOTIFICATION_FLAG, true);
        mSwitch = (Switch) findViewById(R.id.mSwitch);
        mSwitch.setChecked(mNotificationFlag);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SPUtil.setSharedPreference(mContext, Constants.SP_NOTIFICATION_FLAG, isChecked);
                mNotificationFlag = isChecked;
                updateNotification();
            }
        });

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mRootLayout = (RelativeLayout) findViewById(R.id.mRootLayout);
        mEmptyLayout = (RelativeLayout) findViewById(R.id.mEmptyLayout);
        mPlusBtn = (RelativeLayout) findViewById(R.id.mPlusBtn);

        mEditText = (EditText) findViewById(R.id.mEditText);
        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Constants.DB_GOODS_MAX_LENGTH)});
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                        mPlusBtn.performClick();
                        return true;
                    default:
                        return false;
                }
            }
        });

        mChangeBtn = (ImageButton) findViewById(R.id.mChangeBtn);
        mCloseBtn = (ImageButton) findViewById(R.id.mCloseBtn);

        mDatabases = new Databases(mContext);
        mDatabases.open();
        arrGoodsItems = mDatabases.getGoodsItemList();
        mDatabases.close();

        mZoomLevel = SPUtil.getIntSharedPreference(mContext, Constants.SP_TXT_ZOOM_LEVEL, 0);
        mGoodsAdapter = new GoodsAdapter(mContext, arrGoodsItems, mWeakRefHandler, this, mZoomLevel);

        mRecyclerView = (RecyclerView) findViewById(R.id.mRecyclerView);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(mContext));

        // 각 Item 들이 RecyclerView 의 전체 크기를 변경하지 않는 다면 setHasFixedSize() 함수를 사용해서 성능을 개선할 수 있습니다. 변경될 가능성이 있다면 false 로 , 없다면 true를 설정해주세요.
        mRecyclerView.setHasFixedSize(true);

        AlphaAnimatorAdapter animatorAdapter = new AlphaAnimatorAdapter(mGoodsAdapter, mRecyclerView);
        mRecyclerView.setAdapter(animatorAdapter);
        mRecyclerView.setItemAnimator(new SlideInOutLeftItemAnimator(mRecyclerView));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        // ItemAnimator 는 adapter 가 notify 를 받았을 때 ViewGroup 을 어떻게 animate 할지 결정한다. 기본적으로 add, remove 의 animation 은 제공된다. 이 녀석도 확장해서 쓰는 것이 쉽지 않는데, DefaultItemAnimator 가 대부분의 기초를 서포트한다.
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mGoodsAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        updateNotification();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onClick(final View v) {
        final int id = v.getId();

        switch (id) {
            case R.id.mPlusBtn:
                String mEditTextVal = mEditText.getText().toString();
                if (mEditTextVal == null || mEditTextVal.length() == 0) {
                    Toast.makeText(getBaseContext(), R.string.hint_add_goods_name, Toast.LENGTH_LONG).show();
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US);
                    String date = sdf.format(new Date());

                    GoodsItem item = new GoodsItem();
                    item.setGoodsName(mEditTextVal);
                    item.setGoodsRegDate(date);
                    item.setGoodsEventType(Constants.FLAG_GOODS_EVENT_TYPE_BEFORE);
                    item.setGoodsEventDate(date);

                    mDatabases.open();
                    mDatabases.setGoodsItemList(item);
                    mDatabases.close();

                    mGoodsAdapter.addItem(item);
                    updateNotification();
                }

                // 입력박스 초기화
                mEditText.setText("", TextView.BufferType.EDITABLE);
                // 소프트키보드 가림
                //imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                // 포커스를 루트뷰로 이동
                //mRootLayout.requestFocus();
                // 리스트 최하단으로 이동
                if (mGoodsAdapter.getItemCount() > 0) {
                    mRecyclerView.smoothScrollToPosition(mGoodsAdapter.getItemCount() -1 );
                }
                break;
            case R.id.mChangeBtn:
                mChangeBtn.setVisibility(View.GONE);
                mCloseBtn.setVisibility(View.VISIBLE);
                mGoodsAdapter.setChangeMode(true);
                break;
            case R.id.mCloseBtn:
                mChangeBtn.setVisibility(View.VISIBLE);
                mCloseBtn.setVisibility(View.GONE);
                mGoodsAdapter.setChangeMode(false);
                break;
            case R.id.mTxtPlusBtn:
                if (mZoomLevel < 10) {
                    mZoomLevel++;
                    SPUtil.setSharedPreference(mContext, Constants.SP_TXT_ZOOM_LEVEL, mZoomLevel);
                    mGoodsAdapter.setZoomLevel(mZoomLevel);
                }
                break;
            case R.id.mTxtMinusBtn:
                if (mZoomLevel > -5) {
                    mZoomLevel--;
                    SPUtil.setSharedPreference(mContext, Constants.SP_TXT_ZOOM_LEVEL, mZoomLevel);
                    mGoodsAdapter.setZoomLevel(mZoomLevel);
                }
                break;
        }
    }

    public void updateNotification() {
        if (arrGoodsItems.size() > 0) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyLayout.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyLayout.setVisibility(View.VISIBLE);
        }

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (mNotificationFlag && arrGoodsItems.size() > 0) {
            Intent notificationIntent = new Intent(this, SplashActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            notificationIntent.putExtra("notificationId", NOTIFICATION_ID);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

            CharSequence[] spans = new CharSequence[arrGoodsItems.size()];
            StringBuilder contentText = new StringBuilder();
            for (int i = 0; i < arrGoodsItems.size(); i++) {
                GoodsItem item = arrGoodsItems.get(i);
                String goodsName = "•" + item.getGoodsName() + " ";
                contentText.append(goodsName);
                if (Constants.FLAG_GOODS_EVENT_TYPE_AFTER.equals(item.getGoodsEventType())) {
                    Spannable spannableString = new SpannableString(goodsName);
                    spannableString.setSpan(new StrikethroughSpan(), 0, item.getGoodsName().length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spans[i] = spannableString;
                } else {
                    spans[i] = goodsName;
                }
            }

            /**
             * YCNOTE - Notification
             *
             * setSmallIcon : 아이콘입니다 구 소스의 icon이랑 같습니다
             * setTicker : 알림이 뜰때 잠깐 표시되는 Text이며, 구 소스의 tickerText이랑 같습니다
             * setWhen : 알림이 표시되는 시간이며, 구 소스의 when이랑 같습니다
             * setNumber : 미확인 알림의 개수이며, 구 소스의 notification.number랑 같습니다
             * setContentTitle : 상단바 알림 제목이며, 구 소스의 contentTitle랑 같습니다
             * setContentText : 상단바 알림 내용이며, 구 소스의 contentText랑 같습니다
             * setDefaults : 기본 설정이며, 구 소스의 notification.defaults랑 같습니다
             * setContentIntent : 실행할 작업이 담긴 PendingIntent이며, 구 소스의 contentIntent랑 같습니다.
             * setAutoCancel : 터치하면 자동으로 지워지도록 설정하는 것이며, 구 소스의 FLAG_AUTO_CANCEL랑 같습니다
             * setPriority : 우선순위입니다, 구 소스의 notification.priority랑 같습니다만 구글 개발자 API를 보면 API 16이상부터 사용이 가능하다고 합니다
             * setOngoing : 진행중알림 이며, 구 소스의 FLAG_ONGOING_EVENT랑 같습니다
             * 	- 알림의 지속성 : 알림 리스트에서 사용자가 그것을 클릭하거나 좌우로 드래그해도 사라지지 않음
             * addAction : 알림에서 바로 어떤 활동을 할지 선택하는 것이며, 스샷찍은다음 삭제/공유 같은것이 이에 해당합니다
             */
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            inboxStyle.addLine(TextUtils.concat(spans));

            builder.setContentTitle(getString(R.string.app_name))
                    .setContentText(contentText.toString())
                    .setSmallIcon(R.drawable.ico_notification_small)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setStyle(inboxStyle)
                    .setWhen(System.currentTimeMillis());

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                builder.setCategory(Notification.CATEGORY_MESSAGE)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setVisibility(Notification.VISIBILITY_PUBLIC);
            }

            nm.notify(NOTIFICATION_ID, builder.build());
        } else {
            nm.cancel(NOTIFICATION_ID);
        }
    }

    @Override
    public void handleMessage(Message msg) {
        Bundle data = msg.getData();
        final int position = data.getInt("position");

        switch (msg.what) {
            case Constants.QUERY_UPDATE_GOODS:
                GoodsItem item = arrGoodsItems.get(position);
                String updateEventType = Constants.FLAG_GOODS_EVENT_TYPE_AFTER;

                if (Constants.FLAG_GOODS_EVENT_TYPE_AFTER.equals(item.getGoodsEventType())) {
                    updateEventType = Constants.FLAG_GOODS_EVENT_TYPE_BEFORE;
                }

                if (mDatabases == null) mDatabases = new Databases(mContext);
                mDatabases.open();
                mDatabases.updateGoodsItem(arrGoodsItems.get(position).getGoodsRegDate(), updateEventType);
                mDatabases.close();

                arrGoodsItems.get(position).setGoodsEventType(updateEventType);
                mGoodsAdapter.notifyDataSetChanged();
                updateNotification();
                break;
            case Constants.QUERY_DELETE_GOODS:
                // update delete event
                if (mDatabases == null) mDatabases = new Databases(mContext);
                mDatabases.open();
                mDatabases.deleteGoodsItem(arrGoodsItems.get(position).getGoodsRegDate());
                mDatabases.close();

                mGoodsAdapter.onItemDismiss(position);
                /*arrGoodsItems.remove(position);
                mGoodsAdapter.notifyDataSetChanged();*/
                updateNotification();
                break;
            case Constants.QUERY_RENAME_GOODS:
                AlertDialog.Builder ad = new AlertDialog.Builder(new ContextThemeWrapper(mContext, R.style.AlertDialogCustom));
                ad.setTitle(R.string.app_name);
                ad.setMessage("");

                final EditText et = new EditText(mContext);
                et.setImeOptions(EditorInfo.IME_ACTION_DONE);
                et.setSingleLine();
                et.setText(arrGoodsItems.get(position).getGoodsName());
                et.setSelection(arrGoodsItems.get(position).getGoodsName().length());

                FrameLayout container = new FrameLayout(mContext);
                FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = getResources().getDimensionPixelSize(R.dimen.section_div_height);
                params.rightMargin = getResources().getDimensionPixelSize(R.dimen.section_div_height);
                et.setLayoutParams(params);
                container.addView(et);

                ad.setView(container);

                ad.setPositiveButton(R.string.dialog_alert_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String value = et.getText().toString();

                        if (mDatabases == null) mDatabases = new Databases(mContext);
                        mDatabases.open();
                        mDatabases.updateGoodsItemName(arrGoodsItems.get(position).getGoodsRegDate(), value);
                        mDatabases.close();

                        arrGoodsItems.get(position).setGoodsName(value);
                        mGoodsAdapter.notifyDataSetChanged();
                        updateNotification();

                        dialog.dismiss();
                    }
                });

                ad.setNegativeButton(R.string.dialog_alert_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = ad.create();
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                dialog.show();
                break;
            case Constants.QUERY_DRAG_SORT:
                if (mDatabases == null) mDatabases = new Databases(mContext);
                mDatabases.open();
                mDatabases.deleteGoodsItemList();
                mDatabases.setGoodsItemList(arrGoodsItems);
                mDatabases.close();

                updateNotification();
                break;
        }
    }
}
