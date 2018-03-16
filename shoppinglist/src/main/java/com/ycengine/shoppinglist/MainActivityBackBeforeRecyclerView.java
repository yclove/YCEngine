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
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.ycengine.shoppinglist.data.Databases;
import com.ycengine.shoppinglist.data.GoodsItem;
import com.ycengine.shoppinglist.library.dslv.DragSortListView;
import com.ycengine.yclibrary.util.CommonUtil;
import com.ycengine.yclibrary.util.SPUtil;
import com.ycengine.yclibrary.util.handler.IOnHandlerMessage;
import com.ycengine.yclibrary.util.handler.WeakRefHandler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.ycengine.yclibrary.Constants.getAdRequest;

public class MainActivityBackBeforeRecyclerView extends BaseActivity implements View.OnClickListener, IOnHandlerMessage {
    public static final int NOTIFICATION_ID = 7777;

    private WeakRefHandler mWeakRefHandler;
    private FirebaseAnalytics mFirebaseAnalytics;

    private FinishConfirmDialog mFinishConfirmDialog;

    private Switch mSwitch;
    private boolean mNotificationFlag = false;

    private InputMethodManager imm;
    private RelativeLayout mRootLayout, mPlusBtn;
    private EditText mEditText;
    private DragSortListView mDragSortListView;
    private ListView mListView;
    private ImageButton mChangeBtn, mCloseBtn;

    private Databases mDatabases;
    private GoodsAdapterBackBeforeRecyclerView mGoodsAdapter;
    private ArrayList<GoodsItem> arrGoodsItems;

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

        mGoodsAdapter = new GoodsAdapterBackBeforeRecyclerView(mContext, arrGoodsItems, mWeakRefHandler);

        mDragSortListView = (DragSortListView) findViewById(R.id.mDragSortListView);
        mDragSortListView.setAdapter(mGoodsAdapter);
        mDragSortListView.setDropListener(onDrop);

        mListView = (ListView) findViewById(R.id.mListView);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
            }
        });

        /*SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.parseColor("#005a9e")));
                // set item width
                deleteItem.setWidth(DeviceUtil.dp2px(mContext, 50));
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };
        // set creator
        mListView.setMenuCreator(creator);

        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                GoodsItem item = mGoodsAdapter.getItem(position);
                switch (index) {
                    case 0:
                        // update delete event
                        if (mDatabases == null) mDatabases = new Databases(mContext);
                        mDatabases.open();
                        mDatabases.deleteGoodsItem(arrGoodsItems.get(position).getGoodsRegDate());
                        mDatabases.close();

                        arrGoodsItems.remove(position);
                        mGoodsAdapter.notifyDataSetChanged();
                        updateNotification();
                        break;
                }
                return false;
            }
        });

        // set SwipeListener
        mListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {
            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });

        // set MenuStateChangeListener
        mListView.setOnMenuStateChangeListener(new SwipeMenuListView.OnMenuStateChangeListener() {
            @Override
            public void onMenuOpen(int position) {
            }

            @Override
            public void onMenuClose(int position) {
            }
        });

        // other setting
        mListView.setCloseInterpolator(new BounceInterpolator());*/

        // test item long click
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder ad = new AlertDialog.Builder(new ContextThemeWrapper(mContext, R.style.AlertDialogCustom));

                ad.setTitle(R.string.app_name);

                final EditText et = new EditText(mContext);
                et.setImeOptions(EditorInfo.IME_ACTION_DONE);
                et.setText(arrGoodsItems.get(position).getGoodsName());
                et.setPadding(50, 50, 50, 50);
                et.setSelection(arrGoodsItems.get(position).getGoodsName().length());
                ad.setView(et);

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

                ad.show();
                return true;
            }
        });

        mListView.setAdapter(mGoodsAdapter);

        updateNotification();
    }

    private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
        @Override
        public void drop(int from, int to) {
            GoodsItem item = mGoodsAdapter.getItem(from);

            arrGoodsItems.remove(item);
            arrGoodsItems.add(to, item);
            mGoodsAdapter.notifyDataSetChanged();

            if (mDatabases == null) mDatabases = new Databases(mContext);
            mDatabases.open();
            mDatabases.deleteGoodsItemList();
            mDatabases.setGoodsItemList(arrGoodsItems);
            mDatabases.close();

            updateNotification();
        }
    };

    private DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener() {
        @Override
        public void remove(int which) {
            mGoodsAdapter.getAdapterData().remove(mGoodsAdapter.getItem(which));
        }
    };

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
                if (mGoodsAdapter.getCount() > 0) {
                    mListView.setSelection(mGoodsAdapter.getCount() - 1);
                    mDragSortListView.setSelection(mGoodsAdapter.getCount() - 1);
                }
                break;
            case R.id.mChangeBtn:
                mChangeBtn.setVisibility(View.GONE);
                mCloseBtn.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);
                mDragSortListView.setVisibility(View.VISIBLE);

                if (mGoodsAdapter.getCount() > 0) {
                    mListView.setSelection(0);
                    mDragSortListView.setSelection(0);
                }
                break;
            case R.id.mCloseBtn:
                mChangeBtn.setVisibility(View.VISIBLE);
                mCloseBtn.setVisibility(View.GONE);
                mDragSortListView.setVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);

                if (mGoodsAdapter.getCount() > 0) {
                    mListView.setSelection(0);
                    mDragSortListView.setSelection(0);
                }
                break;
        }
    }

    public void updateNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (mNotificationFlag && arrGoodsItems.size() > 0) {
            Intent notificationIntent = new Intent(this, SplashActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            notificationIntent.putExtra("notificationId", NOTIFICATION_ID);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

            StringBuilder contentText = null;
            for (GoodsItem item : arrGoodsItems) {
                String goodsName = "";
                if (Constants.FLAG_GOODS_EVENT_TYPE_AFTER.equals(item.getGoodsEventType())) {
                    goodsName = "<font color='#888888'>•<s>" + item.getGoodsName() + "</s></font>";
                } else {
                    goodsName = "<font color='#000000'>•" + item.getGoodsName() + "</font>";
                }

                goodsName = "•" + item.getGoodsName();

                if (contentText == null) {
                    contentText = new StringBuilder(goodsName);
                } else {
                    contentText.append(" " + goodsName);
                }
            }

            Spannable spannableString = new SpannableString(contentText.toString());
            ArrayList<int[]> hashtagSpans = CommonUtil.getSpans(contentText.toString(), '•');
            for (int i = 0; i < hashtagSpans.size(); i++) {
                if (Constants.FLAG_GOODS_EVENT_TYPE_AFTER.equals(arrGoodsItems.get(i).getGoodsEventType())) {
                    int[] span = hashtagSpans.get(i);
                    int hashTagStart = span[0];
                    int hashTagEnd = span[1];

                    if (hashTagStart >= 0 && hashTagEnd >= 0)
                        spannableString.setSpan(new StrikethroughSpan(), hashTagStart, hashTagEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
            inboxStyle.addLine(spannableString);

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

                arrGoodsItems.remove(position);
                mGoodsAdapter.notifyDataSetChanged();
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
        }
    }

    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }
}
