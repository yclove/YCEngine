package com.ycengine.rainsound;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.ycengine.rainsound.data.CoreItem;
import com.ycengine.rainsound.data.Databases;
import com.ycengine.rainsound.transformer.ParallaxPagerTransformer;
import com.ycengine.rainsound.transformer.ScalePageTransformer;
import com.ycengine.yclibrary.util.DateUtil;
import com.ycengine.yclibrary.util.DeviceUtil;
import com.ycengine.yclibrary.util.LogUtil;
import com.ycengine.yclibrary.util.SPUtil;
import com.ycengine.yclibrary.util.handler.IOnHandlerMessage;
import com.ycengine.yclibrary.util.handler.WeakRefHandler;
import com.ycengine.yclibrary.widget.CircularSeekBar;
import com.ycengine.yclibrary.widget.MultiViewPager;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.ycengine.yclibrary.Constants.getAdRequest;

public class MainActivityBak extends BaseActivity implements View.OnClickListener, IOnHandlerMessage, AudioManager.OnAudioFocusChangeListener {
    private static final int PLAYER_TYPE_RAIN = 0;
    private static final int PLAYER_TYPE_THUNDER = 1;
    private static final int PLAYER_TYPE_PIANO = 2;

    private static final int HANDLE_MAIN_TIMER = 3;
    private static final int HANDLE_RAIN_TIMER = 4;
    private static final int HANDLE_THUNDER_TIMER = 5;
    private static final int HANDLE_PIANO_TIMER = 6;
    private static final int HANDLE_RAIN_LOOP_TIMER = 7;
    private static final int HANDLE_THUNDER_LOOP_TIMER = 8;
    private static final int HANDLE_PIANO_LOOP_TIMER = 9;

    private WeakRefHandler mWeakRefHandler;
    private FirebaseAnalytics mFirebaseAnalytics;
    private InterstitialAd mInterstitialAd;
    private boolean isInterstitialAdShow = false;
    private int restInterstitialAdCount = 3;

    private FinishConfirmDialog mFinishConfirmDialog;
    private RequestManager mGlideRequestManager;

    private Databases mDatabases;
    private ArrayList<CoreItem> arrCoreItem;

    private View vStatusBar;

    private MultiViewPager mHeaderViewPager;
    private HeaderAdapter mHeaderAdapter;

    private ViewPager mMainViewPager;
    private MainAdapter mMainAdapter;

    private CircularSeekBar mRainSeekBar, mThunderSeekBar, mPianoSeekBar;
    private float mRainSeekBarVolume = 0f, mThunderSeekBarVolume = 0f, mPianoSeekBarVolume = 0f;
    private RelativeLayout mPlayBtn, mThunderBtn, mPianoBtn;
    private ImageView mPlayImage, mTimerImage, mThunderImage, mPianoImage, mVolumeImage;
    private MediaPlayer mMediaPlayer, mMediaPlayerLoop, mThunderPlayer, mThunderPlayerLoop, mPianoPlayer, mPianoPlayerLoop;
    private boolean isRain = false, isTimer = false, isThunder = false, isPiano = false, isVolume = false;

    private Timer mTimer;
    private MainTask mMainTask;
    private TextView tvTimerFormat;
    private int leftTotalSecond = 0, rainDuration = 0, thunderDuration = 0, pianoDuration = 0;

    private View vBackground;
    private RelativeLayout mLicensedLayout;

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
        mGlideRequestManager = Glide.with(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.banner_ad_fullscreen_id));
        mInterstitialAd.loadAd(getAdRequest());
        mInterstitialAd.setAdListener(mAdListener);

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

        vBackground = findViewById(R.id.vBackground);
        mLicensedLayout = (RelativeLayout) findViewById(R.id.mLicensedLayout);

        mDatabases = new Databases(mContext);
        mDatabases.open();
        arrCoreItem = mDatabases.getCoreItemList();
        mDatabases.close();

        int latestSoundPosition = SPUtil.getIntSharedPreference(mContext, Constants.SP_LATEST_SOUND_POSITION, 0);
        if (latestSoundPosition >= arrCoreItem.size()) {
            latestSoundPosition = 0;
        }

        mHeaderViewPager = (MultiViewPager) findViewById(R.id.mHeaderViewPager);
        ScalePageTransformer scalePageTransformer = new ScalePageTransformer();
        mHeaderViewPager.setPageTransformer(true, scalePageTransformer);
        mHeaderAdapter = new HeaderAdapter(mContext, getLayoutInflater(), mWeakRefHandler);
        mHeaderViewPager.setAdapter(mHeaderAdapter);
        mHeaderViewPager.setCurrentItem(arrCoreItem.size() * 1000 + latestSoundPosition);
        mHeaderAdapter.addAllItems(arrCoreItem);

        mMainViewPager = (ViewPager) findViewById(R.id.mMainViewPager);
        ParallaxPagerTransformer parallaxPagerTransformer = new ParallaxPagerTransformer(R.id.ivRoot);
        parallaxPagerTransformer.setSpeed(0.5f);
        mMainViewPager.setPageTransformer(false, parallaxPagerTransformer);
        mMainViewPager.addOnPageChangeListener(mPageChangeListener);
        mMainAdapter = new MainAdapter(mContext, getLayoutInflater(), mWeakRefHandler, mGlideRequestManager);
        mMainViewPager.setAdapter(mMainAdapter);
        mMainViewPager.setCurrentItem(arrCoreItem.size() * 1000 + latestSoundPosition);
        mMainAdapter.addAllItems(arrCoreItem);

        mMainViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                MotionEvent motionEvent = MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), event.getX() / 2.0f, event.getY(), event.getPressure(), event.getSize(), event.getMetaState(), event.getXPrecision(), event.getYPrecision(), event.getDeviceId(), event.getEdgeFlags());
                mHeaderViewPager.onTouchEvent(motionEvent);
                return false;
            }
        });

        vStatusBar = findViewById(R.id.vStatusBar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            vStatusBar.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, DeviceUtil.getStatusBarHeightPx(mContext)));
        }

        mRainSeekBar = (CircularSeekBar) findViewById(R.id.mRainSeekBar);
        mThunderSeekBar = (CircularSeekBar) findViewById(R.id.mThunderSeekBar);
        mPianoSeekBar = (CircularSeekBar) findViewById(R.id.mPianoSeekBar);
        mPlayBtn = (RelativeLayout) findViewById(R.id.mPlayBtn);
        mThunderBtn = (RelativeLayout) findViewById(R.id.mThunderBtn);
        mPianoBtn = (RelativeLayout) findViewById(R.id.mPianoBtn);
        mPlayImage = (ImageView) findViewById(R.id.mPlayImage);
        mTimerImage = (ImageView) findViewById(R.id.mTimerImage);
        tvTimerFormat = (TextView) findViewById(R.id.tvTimerFormat);
        mThunderImage = (ImageView) findViewById(R.id.mThunderImage);
        mPianoImage = (ImageView) findViewById(R.id.mPianoImage);
        mVolumeImage = (ImageView) findViewById(R.id.mVolumeImage);

        mRainSeekBarVolume = SPUtil.getFloatSharedPreference(mContext, Constants.SP_RAIN_VOLUME, 0.5f);
        mThunderSeekBarVolume = SPUtil.getFloatSharedPreference(mContext, Constants.SP_THUNDER_VOLUME, 0.5f);
        mPianoSeekBarVolume = SPUtil.getFloatSharedPreference(mContext, Constants.SP_PIANO_VOLUME, 0.5f);

        mRainSeekBar.setMax(100);
        mRainSeekBar.setProgress((int)(mRainSeekBarVolume * 100));
        mThunderSeekBar.setMax(100);
        mThunderSeekBar.setProgress((int)(mThunderSeekBarVolume * 100));
        mPianoSeekBar.setMax(100);
        mPianoSeekBar.setProgress((int)(mPianoSeekBarVolume * 100));

        mRainSeekBar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
                mRainSeekBarVolume = (float)progress / 100f;
                if (mMediaPlayer != null && mMediaPlayerLoop != null) {
                    mMediaPlayer.setVolume(mRainSeekBarVolume, mRainSeekBarVolume);
                    mMediaPlayerLoop.setVolume(mRainSeekBarVolume / 2f, mRainSeekBarVolume / 2f);
                }
            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {
                SPUtil.setSharedPreference(mContext, Constants.SP_RAIN_VOLUME, mRainSeekBarVolume);
            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {}
        });

        mThunderSeekBar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
                mThunderSeekBarVolume = (float)progress / 100f;
                if (mThunderPlayer != null && mThunderPlayerLoop != null) {
                    mThunderPlayer.setVolume(mThunderSeekBarVolume, mThunderSeekBarVolume);
                    mThunderPlayerLoop.setVolume(mThunderSeekBarVolume / 2f, mThunderSeekBarVolume / 2f);
                }
            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {
                SPUtil.setSharedPreference(mContext, Constants.SP_THUNDER_VOLUME, mThunderSeekBarVolume);
            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {}
        });

        mPianoSeekBar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
                mPianoSeekBarVolume = (float)progress / 100f;
                if (mPianoPlayer != null && mPianoPlayerLoop != null) {
                    mPianoPlayer.setVolume(mPianoSeekBarVolume, mPianoSeekBarVolume);
                    mPianoPlayerLoop.setVolume(mPianoSeekBarVolume / 2f, mPianoSeekBarVolume / 2f);
                }
            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {
                SPUtil.setSharedPreference(mContext, Constants.SP_PIANO_VOLUME, mPianoSeekBarVolume);
            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {}
        });
    }

    @Override
    public void onResume() {
        LogUtil.e("onResume");
        super.onResume();

        int rainSeekBarWidthSize = mRainSeekBar.getLayoutParams().width;
        mRainSeekBar.setLayoutParams(new RelativeLayout.LayoutParams(rainSeekBarWidthSize, rainSeekBarWidthSize));

        int thunderSeekBarWidthSize = mThunderSeekBar.getLayoutParams().width;
        mThunderSeekBar.setLayoutParams(new RelativeLayout.LayoutParams(thunderSeekBarWidthSize, thunderSeekBarWidthSize));

        int pianoSeekBarWidthSize = mPianoSeekBar.getLayoutParams().width;
        mPianoSeekBar.setLayoutParams(new RelativeLayout.LayoutParams(pianoSeekBarWidthSize, pianoSeekBarWidthSize));
    }

    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            if (mMainAdapter.isLoop) {
                position = position % arrCoreItem.size();
            }

            SPUtil.setSharedPreference(mContext, Constants.SP_LATEST_SOUND_POSITION, position);

            mHeaderViewPager.setCurrentItem(mMainViewPager.getCurrentItem(), true);
            playMusic(PLAYER_TYPE_RAIN);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                if (!isInterstitialAdShow) {
                    restInterstitialAdCount--;
                    if (restInterstitialAdCount <= 0) {
                        if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                            isInterstitialAdShow = true;
                        }
                    }
                }
            }
        }
    };

    @Override
    public void onClick(final View v) {
        final int id = v.getId();

        switch (id) {
            case R.id.mPlayBtn:
                isRain = !isRain;
                if (!isRain) {
                    mPlayImage.setImageResource(R.drawable.btn_play);
                    stopMusic(PLAYER_TYPE_RAIN);
                } else {
                    mPlayImage.setImageResource(R.drawable.btn_stop);
                    playMusic(PLAYER_TYPE_RAIN);
                }
                break;
            case R.id.mTimerBtn:
                if (isTimer) {
                    isTimer = false;
                    mTimerImage.setImageResource(R.drawable.btn_timer);
                    tvTimerFormat.setVisibility(View.GONE);
                    stopTimer();
                } else {
                    Intent intent = new Intent(mContext, PlayerTimerActivity.class);
                    int position = mMainViewPager.getCurrentItem();
                    if (mMainAdapter.isLoop) {
                        position = position % arrCoreItem.size();
                    }
                    intent.putExtra("backgroundResourceID", arrCoreItem.get(position).getCorePhoto());
                    startActivityForResult(intent, Constants.REQUEST_TIMER);
                }
                break;
            case R.id.mThunderBtn:
                isThunder = !isThunder;
                if (!isThunder) {
                    mThunderImage.setImageResource(R.drawable.btn_thunder);
                    stopMusic(PLAYER_TYPE_THUNDER);
                } else {
                    mThunderImage.setImageResource(R.drawable.btn_thunder_on);
                    playMusic(PLAYER_TYPE_THUNDER);
                }
                break;
            case R.id.mPianoBtn:
                isPiano = !isPiano;
                if (!isPiano) {
                    mPianoImage.setImageResource(R.drawable.btn_piano);
                    stopMusic(PLAYER_TYPE_PIANO);
                } else {
                    mPianoImage.setImageResource(R.drawable.btn_piano_on);
                    playMusic(PLAYER_TYPE_PIANO);
                }
                break;
            case R.id.mVolumeBtn:
                isVolume = !isVolume;
                if (!isVolume) {
                    mVolumeImage.setImageResource(R.drawable.btn_volume);
                    updateVolumeUI(false);
                } else {
                    mVolumeImage.setImageResource(R.drawable.btn_volume_on);
                    updateVolumeUI(true);
                }
                break;
            case R.id.mLicensedBtn:
                vBackground.setBackgroundColor(Color.parseColor("#E6000000"));
                Animation showAnimation = new AlphaAnimation(0.0f, 1.0f);
                showAnimation.setDuration(500);
                mLicensedLayout.setVisibility(View.VISIBLE);
                mLicensedLayout.setAnimation(showAnimation);
                break;
            case R.id.mLicensedCloseBtn:
                vBackground.setBackgroundColor(Color.parseColor("#4D000000"));
                Animation hideAnimation = new AlphaAnimation(1.0f, 0.0f);
                hideAnimation.setDuration(500);
                mLicensedLayout.setVisibility(View.GONE);
                mLicensedLayout.setAnimation(hideAnimation);
                break;
        }
    }

    private void updateVolumeUI(boolean isShow) {
        if (isShow) {
            mPlayBtn.setClickable(false);
            mThunderBtn.setClickable(false);
            mPianoBtn.setClickable(false);

            mRainSeekBar.setVisibility(View.VISIBLE);
            mThunderSeekBar.setVisibility(View.VISIBLE);
            mPianoSeekBar.setVisibility(View.VISIBLE);
        } else {
            mPlayBtn.setClickable(true);
            mThunderBtn.setClickable(true);
            mPianoBtn.setClickable(true);

            mRainSeekBar.setVisibility(View.GONE);
            mThunderSeekBar.setVisibility(View.GONE);
            mPianoSeekBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.e("Request Code(" + requestCode + "), Result Code(" + resultCode + ")");
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.REQUEST_TIMER:
                if (resultCode == RESULT_OK) {
                    isTimer = true;
                    mTimerImage.setImageResource(R.drawable.btn_timer_on);
                    leftTotalSecond = SPUtil.getIntSharedPreference(mContext, Constants.SP_PLAYER_TIMER, 0);
                    startTimer();

                    int hourPosition = data.getIntExtra("hourPosition", 0);
                    int minutePosition = data.getIntExtra("minutePosition", 0);
                    DeviceUtil.showToast(mContext, getString(R.string.msg_timer, hourPosition, minutePosition));
                }
                break;
        }
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case HANDLE_MAIN_TIMER:
                if (leftTotalSecond > 0) {
                    leftTotalSecond--;
                    tvTimerFormat.setVisibility(View.VISIBLE);
                    tvTimerFormat.setText(DateUtil.getConvertMsToFormat(leftTotalSecond));
                } else {
                    finish();
                }
                break;
            case HANDLE_RAIN_TIMER:
                if (mMediaPlayer != null) {
                    if (mMediaPlayer.isPlaying()) {
                        LogUtil.e("Rain MediaPlayer :::: Seek To");
                        mMediaPlayer.seekTo(0);
                        mWeakRefHandler.sendEmptyMessageDelayed(HANDLE_RAIN_TIMER, rainDuration);
                        mWeakRefHandler.sendEmptyMessageDelayed(HANDLE_RAIN_LOOP_TIMER, rainDuration - 200);
                    } else {
                        LogUtil.e("Rain MediaPlayer :::: 재생중이 아니므로 Handler Remove");
                        mWeakRefHandler.removeMessages(HANDLE_RAIN_TIMER);
                        mWeakRefHandler.removeMessages(HANDLE_RAIN_LOOP_TIMER);
                    }
                }
                break;
            case HANDLE_RAIN_LOOP_TIMER:
                if (mMediaPlayer != null && mMediaPlayerLoop != null) {
                    if (mMediaPlayer.isPlaying()) {
                        if (mMediaPlayerLoop.isPlaying()) {
                            LogUtil.e("Rain MediaPlayer :::: Stop");
                            mMediaPlayerLoop.pause();
                            mMediaPlayerLoop.seekTo(0);
                        } else {
                            LogUtil.e("Rain MediaPlayer :::: Start");
                            mMediaPlayerLoop.start();
                            mWeakRefHandler.sendEmptyMessageDelayed(HANDLE_RAIN_LOOP_TIMER, 400);
                        }
                    } else {
                        mWeakRefHandler.removeMessages(HANDLE_RAIN_LOOP_TIMER);
                    }
                }
                break;
            case HANDLE_THUNDER_TIMER:
                if (mThunderPlayer != null) {
                    if (mThunderPlayer.isPlaying()) {
                        LogUtil.e("Thunder MediaPlayer :::: Seek To");
                        mThunderPlayer.seekTo(0);
                        mWeakRefHandler.sendEmptyMessageDelayed(HANDLE_THUNDER_TIMER, thunderDuration);
                        mWeakRefHandler.sendEmptyMessageDelayed(HANDLE_THUNDER_LOOP_TIMER, thunderDuration - 200);
                    } else {
                        LogUtil.e("Thunder MediaPlayer :::: 재생중이 아니므로 Handler Remove");
                        mWeakRefHandler.removeMessages(HANDLE_THUNDER_TIMER);
                        mWeakRefHandler.removeMessages(HANDLE_THUNDER_LOOP_TIMER);
                    }
                }
                break;
            case HANDLE_THUNDER_LOOP_TIMER:
                if (mThunderPlayer != null && mThunderPlayerLoop != null) {
                    if (mThunderPlayer.isPlaying()) {
                        if (mThunderPlayerLoop.isPlaying()) {
                            LogUtil.e("Thunder MediaPlayer :::: Stop");
                            mThunderPlayerLoop.pause();
                            mThunderPlayerLoop.seekTo(0);
                        } else {
                            LogUtil.e("Thunder MediaPlayer :::: Start");
                            mThunderPlayerLoop.start();
                            mWeakRefHandler.sendEmptyMessageDelayed(HANDLE_THUNDER_LOOP_TIMER, 400);
                        }
                    } else {
                        mWeakRefHandler.removeMessages(HANDLE_THUNDER_LOOP_TIMER);
                    }
                }
                break;
            case HANDLE_PIANO_TIMER:
                if (mPianoPlayer != null) {
                    if (mPianoPlayer.isPlaying()) {
                        LogUtil.e("Piano MediaPlayer :::: Seek To");
                        mPianoPlayer.seekTo(0);
                        mWeakRefHandler.sendEmptyMessageDelayed(HANDLE_PIANO_TIMER, pianoDuration);
                        mWeakRefHandler.sendEmptyMessageDelayed(HANDLE_PIANO_LOOP_TIMER, pianoDuration - 200);
                    } else {
                        LogUtil.e("Piano MediaPlayer :::: 재생중이 아니므로 Handler Remove");
                        mWeakRefHandler.removeMessages(HANDLE_PIANO_TIMER);
                        mWeakRefHandler.removeMessages(HANDLE_PIANO_LOOP_TIMER);
                    }
                }
                break;
            case HANDLE_PIANO_LOOP_TIMER:
                if (mPianoPlayer != null && mPianoPlayerLoop != null) {
                    if (mPianoPlayer.isPlaying()) {
                        if (mPianoPlayerLoop.isPlaying()) {
                            LogUtil.e("Piano MediaPlayer :::: Stop");
                            mPianoPlayerLoop.pause();
                            mPianoPlayerLoop.seekTo(0);
                        } else {
                            LogUtil.e("Piano MediaPlayer :::: Start");
                            mPianoPlayerLoop.start();
                            mWeakRefHandler.sendEmptyMessageDelayed(HANDLE_PIANO_LOOP_TIMER, 400);
                        }
                    } else {
                        mWeakRefHandler.removeMessages(HANDLE_PIANO_LOOP_TIMER);
                    }
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        LogUtil.e("♬ MusicService ▶ onDestroy");
        super.onDestroy();
        stopMusic(PLAYER_TYPE_RAIN);
        stopMusic(PLAYER_TYPE_THUNDER);
        stopMusic(PLAYER_TYPE_PIANO);
        stopTimer();
    }

    private void playMusic(int playType) {
        stopMusic(playType);

        if (playType == PLAYER_TYPE_RAIN) {
            if (isRain) {
                int position = mMainViewPager.getCurrentItem();
                if (mMainAdapter.isLoop) {
                    position = position % arrCoreItem.size();
                }

                if (arrCoreItem.get(position).getCoreMusic() != -1) {
                    mMediaPlayer = MediaPlayer.create(mContext, arrCoreItem.get(position).getCoreMusic());
                    rainDuration = mMediaPlayer.getDuration() - 200;
                    LogUtil.e("Rain Duration :::: " + (rainDuration + 200));

                    mWeakRefHandler.removeMessages(HANDLE_RAIN_TIMER);
                    mWeakRefHandler.removeMessages(HANDLE_RAIN_LOOP_TIMER);

                    mWeakRefHandler.sendEmptyMessageDelayed(HANDLE_RAIN_TIMER, rainDuration);

                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mMediaPlayer.setVolume(mRainSeekBarVolume, mRainSeekBarVolume);
                    mMediaPlayer.start();

                    mMediaPlayerLoop = MediaPlayer.create(mContext, arrCoreItem.get(position).getCoreMusic());
                    mMediaPlayerLoop.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mMediaPlayerLoop.setVolume(mRainSeekBarVolume / 2f, mRainSeekBarVolume / 2f);

                    mWeakRefHandler.sendEmptyMessageDelayed(HANDLE_RAIN_LOOP_TIMER, rainDuration - 200);
                }
            }
        } else if (playType == PLAYER_TYPE_THUNDER) {
            if (isThunder) {
                mThunderPlayer = MediaPlayer.create(mContext, R.raw.thunderstorm);
                thunderDuration = mThunderPlayer.getDuration() - 200;
                LogUtil.e("Thunder Duration :::: " + (thunderDuration + 200));

                mWeakRefHandler.removeMessages(HANDLE_THUNDER_TIMER);
                mWeakRefHandler.sendEmptyMessageDelayed(HANDLE_THUNDER_TIMER, thunderDuration);

                mThunderPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mThunderPlayer.setVolume(mThunderSeekBarVolume, mThunderSeekBarVolume);
                mThunderPlayer.start();

                mThunderPlayerLoop = MediaPlayer.create(mContext, R.raw.thunderstorm);
                mThunderPlayerLoop.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mThunderPlayerLoop.setVolume(mThunderSeekBarVolume / 2f, mThunderSeekBarVolume / 2f);
                mWeakRefHandler.sendEmptyMessageDelayed(HANDLE_THUNDER_LOOP_TIMER, thunderDuration - 200);
            }
        } else if (playType == PLAYER_TYPE_PIANO) {
            if (isPiano) {
                mPianoPlayer = MediaPlayer.create(mContext, R.raw.piano);
                pianoDuration = mPianoPlayer.getDuration() - 200;
                LogUtil.e("Piano Duration :::: " + (pianoDuration + 200));

                mWeakRefHandler.removeMessages(HANDLE_PIANO_TIMER);
                mWeakRefHandler.sendEmptyMessageDelayed(HANDLE_PIANO_TIMER, pianoDuration);

                mPianoPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mPianoPlayer.setVolume(mPianoSeekBarVolume, mPianoSeekBarVolume);
                mPianoPlayer.start();

                mPianoPlayerLoop = MediaPlayer.create(mContext, R.raw.piano);
                mPianoPlayerLoop.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mPianoPlayerLoop.setVolume(mPianoSeekBarVolume / 2f, mPianoSeekBarVolume / 2f);
                mWeakRefHandler.sendEmptyMessageDelayed(HANDLE_PIANO_LOOP_TIMER, pianoDuration - 200);
            }
        }
    }

    private void stopMusic(int playType) {
        if (playType == PLAYER_TYPE_RAIN) {
            if (mMediaPlayer != null) {
                if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        } else if (playType == PLAYER_TYPE_THUNDER) {
            if (mThunderPlayer != null) {
                if (mThunderPlayer.isPlaying()) mThunderPlayer.stop();
                mThunderPlayer.release();
                mThunderPlayer = null;
            }
        } else if (playType == PLAYER_TYPE_PIANO) {
            if (mPianoPlayer != null) {
                if (mPianoPlayer.isPlaying()) mPianoPlayer.stop();
                mPianoPlayer.release();
                mPianoPlayer = null;
            }
        }
    }

    private void startTimer() {
        LogUtil.e("♬ MusicService ▶ 타이머 생성");

        if (mTimer == null) mTimer = new Timer();
        if (mMainTask == null) mMainTask = new MainTask();
        mTimer.scheduleAtFixedRate(mMainTask, 0, 1000);
    }

    private void stopTimer() {
        LogUtil.e("♬ MusicService ▶ 타이머 제거");

        if (mMainTask != null) {
            mMainTask.cancel(); //타이머 task를 timer 큐에서 지워버린다.
            mMainTask = null;
        }

        if (mTimer != null) {
            // 스케쥴 Task와 타이머를 취소한다.
            mTimer.cancel();
            // Task큐의 모든 Task를 제거한다.
            mTimer.purge();
            mTimer = null;
        }
    }

    private class MainTask extends TimerTask {
        public void run() {
            mWeakRefHandler.sendEmptyMessage(HANDLE_MAIN_TIMER);
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        LogUtil.e(String.format(Locale.US, "♬ MusicService ▶ onAudioFocusChange : %d", focusChange));

        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN: // 1
                LogUtil.e("AudioManager.AUDIOFOCUS_GAIN");

                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(mRainSeekBarVolume, mRainSeekBarVolume);
                if (mMediaPlayerLoop != null && mMediaPlayerLoop.isPlaying()) mMediaPlayerLoop.setVolume(mRainSeekBarVolume / 2f, mRainSeekBarVolume / 2f);
                if (mThunderPlayer != null && mThunderPlayer.isPlaying()) mThunderPlayer.setVolume(mThunderSeekBarVolume, mThunderSeekBarVolume);
                if (mThunderPlayerLoop != null && mThunderPlayerLoop.isPlaying()) mThunderPlayerLoop.setVolume(mThunderSeekBarVolume / 2f, mThunderSeekBarVolume / 2f);
                if (mPianoPlayer != null && mPianoPlayer.isPlaying()) mPianoPlayer.setVolume(mPianoSeekBarVolume, mPianoSeekBarVolume);
                if (mPianoPlayerLoop != null && mPianoPlayerLoop.isPlaying()) mPianoPlayerLoop.setVolume(mPianoSeekBarVolume / 2f, mPianoSeekBarVolume / 2f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS: // -1
                LogUtil.e("AudioManager.AUDIOFOCUS_LOSS");

                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0f, 0f);
                if (mMediaPlayerLoop != null && mMediaPlayerLoop.isPlaying()) mMediaPlayerLoop.setVolume(0f, 0f);
                if (mThunderPlayer != null && mThunderPlayer.isPlaying()) mThunderPlayer.setVolume(0f, 0f);
                if (mThunderPlayerLoop != null && mThunderPlayerLoop.isPlaying()) mThunderPlayerLoop.setVolume(0f, 0f);
                if (mPianoPlayer != null && mPianoPlayer.isPlaying()) mPianoPlayer.setVolume(0f, 0f);
                if (mPianoPlayerLoop != null && mPianoPlayerLoop.isPlaying()) mPianoPlayerLoop.setVolume(0f, 0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: // -2
                LogUtil.e("AudioManager.AUDIOFOCUS_LOSS_TRANSIENT");

                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0f, 0f);
                if (mMediaPlayerLoop != null && mMediaPlayerLoop.isPlaying()) mMediaPlayerLoop.setVolume(0f, 0f);
                if (mThunderPlayer != null && mThunderPlayer.isPlaying()) mThunderPlayer.setVolume(0f, 0f);
                if (mThunderPlayerLoop != null && mThunderPlayerLoop.isPlaying()) mThunderPlayerLoop.setVolume(0f, 0f);
                if (mPianoPlayer != null && mPianoPlayer.isPlaying()) mPianoPlayer.setVolume(0f, 0f);
                if (mPianoPlayerLoop != null && mPianoPlayerLoop.isPlaying()) mPianoPlayerLoop.setVolume(0f, 0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: // -3
                LogUtil.e("AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");

                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
                if (mMediaPlayerLoop != null && mMediaPlayerLoop.isPlaying()) mMediaPlayerLoop.setVolume(0.1f, 0.1f);
                if (mThunderPlayer != null && mThunderPlayer.isPlaying()) mThunderPlayer.setVolume(0.1f, 0.1f);
                if (mThunderPlayerLoop != null && mThunderPlayerLoop.isPlaying()) mThunderPlayerLoop.setVolume(0.1f, 0.1f);
                if (mPianoPlayer != null && mPianoPlayer.isPlaying()) mPianoPlayer.setVolume(0.1f, 0.1f);
                if (mPianoPlayerLoop != null && mPianoPlayerLoop.isPlaying()) mPianoPlayerLoop.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private boolean requestFocus() {
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }
}
