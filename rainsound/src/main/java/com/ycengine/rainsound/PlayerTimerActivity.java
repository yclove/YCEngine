package com.ycengine.rainsound;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdView;
import com.ycengine.rainsound.library.wheel.OnWheelScrollListener;
import com.ycengine.rainsound.library.wheel.WheelView;
import com.ycengine.yclibrary.util.DeviceUtil;
import com.ycengine.yclibrary.util.SPUtil;
import com.ycengine.yclibrary.util.handler.IOnHandlerMessage;
import com.ycengine.yclibrary.util.handler.WeakRefHandler;

import static com.ycengine.yclibrary.Constants.getAdRequest;

public class PlayerTimerActivity extends BaseActivity implements IOnHandlerMessage {
    private WeakRefHandler mHandler;

    private View vStatusBar;
    private int backgroundResourceID;
    private RelativeLayout rootLayout;
    private ImageButton mCloseBtn;

    private WheelView mPlayerTimerGridWheelView, mPlayerTimerHourWheelView, mPlayerTimerMinuteWheelView;
    private PlayerTimerGridAdapter mPlayerTimerGridAdapter;
    private PlayerTimerAdapter mPlayerTimerHourAdapter, mPlayerTimerMinuteAdapter;
    private RelativeLayout rlPlayerTimerToggleBtn;
    private int mPlayerTimerTotalSecond = 0;

    @Override
    protected int layoutResId() {
        return R.layout.activity_player_timer;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new WeakRefHandler(this);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        mAdView.loadAd(getAdRequest());
        mAdView.setAdListener(mAdListener);

        mPlayerTimerTotalSecond = SPUtil.getIntSharedPreference(mContext, Constants.SP_PLAYER_TIMER);

        Intent intent = getIntent();
        backgroundResourceID = intent.getIntExtra("backgroundResourceID", -1);

        setDisplay();
    }

    private void setDisplay() {
        vStatusBar = findViewById(R.id.vStatusBar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            vStatusBar.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, DeviceUtil.getStatusBarHeightPx(mContext)));
        }

        if (backgroundResourceID != -1) {
            rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);
            rootLayout.setBackgroundResource(backgroundResourceID);
        }

        mCloseBtn = (ImageButton)findViewById(R.id.mCloseBtn);
        mCloseBtn.setOnClickListener(onClickListener);

        mPlayerTimerGridAdapter = new PlayerTimerGridAdapter(mContext);
        mPlayerTimerGridWheelView = (WheelView) findViewById(R.id.mPlayerTimerGridWheelView);
        mPlayerTimerGridWheelView.setViewAdapter(mPlayerTimerGridAdapter);
        mPlayerTimerGridWheelView.setDrawShadows(false);
        mPlayerTimerGridWheelView.setCyclic(true);

        mPlayerTimerHourAdapter = new PlayerTimerAdapter(mContext, "HOUR");
        mPlayerTimerHourWheelView = (WheelView) findViewById(R.id.mPlayerTimerHourWheelView);
        mPlayerTimerHourWheelView.setViewAdapter(mPlayerTimerHourAdapter);
        mPlayerTimerHourWheelView.setDrawShadows(false);
        mPlayerTimerHourWheelView.setCyclic(true);

        mPlayerTimerHourWheelView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                mPlayerTimerGridWheelView.dispatchTouchEvent(event);
                return false;
            }
        });

        mPlayerTimerHourWheelView.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelView wheel) {
            }

            @Override
            public void onScrollingFinished(WheelView wheel) {
                mPlayerTimerHourAdapter.setCurrentItem("HOUR", wheel.getCurrentItem());
                updateTriggerUI();
            }
        });

        mPlayerTimerMinuteAdapter = new PlayerTimerAdapter(mContext, "MINUTE");
        mPlayerTimerMinuteWheelView = (WheelView) findViewById(R.id.mPlayerTimerMinuteWheelView);
        mPlayerTimerMinuteWheelView.setViewAdapter(mPlayerTimerMinuteAdapter);
        mPlayerTimerMinuteWheelView.setDrawShadows(false);
        mPlayerTimerMinuteWheelView.setCyclic(true);

        mPlayerTimerMinuteWheelView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                mPlayerTimerGridWheelView.dispatchTouchEvent(event);
                return false;
            }
        });

        mPlayerTimerMinuteWheelView.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelView wheel) {
            }

            @Override
            public void onScrollingFinished(WheelView wheel) {
                mPlayerTimerMinuteAdapter.setCurrentItem("MINUTE", wheel.getCurrentItem());
                updateTriggerUI();
            }
        });

        // Footer 시작 버튼
        rlPlayerTimerToggleBtn = (RelativeLayout) findViewById(R.id.rlPlayerTimerToggleBtn);
        rlPlayerTimerToggleBtn.setOnClickListener(onClickListener);

        int playerTimerHour = mPlayerTimerTotalSecond / 3600;
        int playerTimerMinute = (mPlayerTimerTotalSecond % 3600) / 60;
        mPlayerTimerHourWheelView.setCurrentItem(playerTimerHour);
        mPlayerTimerHourAdapter.setCurrentItem("HOUR", mPlayerTimerHourWheelView.getCurrentItem());
        mPlayerTimerMinuteWheelView.setCurrentItem(playerTimerMinute);
        mPlayerTimerMinuteAdapter.setCurrentItem("MINUTE", mPlayerTimerMinuteWheelView.getCurrentItem());

        updateTriggerUI();
    }

    private void updateTriggerUI() {
        if (mPlayerTimerHourWheelView.getCurrentItem() == 0 && mPlayerTimerMinuteWheelView.getCurrentItem() == 0) {
            rlPlayerTimerToggleBtn.setAlpha(0.2f);
            rlPlayerTimerToggleBtn.setOnClickListener(null);
        } else {
            rlPlayerTimerToggleBtn.setAlpha(1.0f);
            rlPlayerTimerToggleBtn.setOnClickListener(onClickListener);
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.mCloseBtn:
                    setResult(RESULT_CANCELED);
                    finish();
                    break;
                case R.id.rlPlayerTimerToggleBtn:
                    int hourPosition = mPlayerTimerHourWheelView.getCurrentItem();
                    int minutePosition = mPlayerTimerMinuteWheelView.getCurrentItem();
                    int totalSecond = (hourPosition * 3600) + (minutePosition * 60);
                    SPUtil.setSharedPreference(mContext, Constants.SP_PLAYER_TIMER, totalSecond);

                    Intent intent = getIntent();
                    intent.putExtra("hourPosition", hourPosition);
                    intent.putExtra("minutePosition", minutePosition);
                    setResult(RESULT_OK, intent);
                    finish();
                    break;
            }
        }
    };

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            default:
                break;
        }
    }
}
