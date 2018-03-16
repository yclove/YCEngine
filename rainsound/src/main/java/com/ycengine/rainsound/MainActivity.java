package com.ycengine.rainsound;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.android.gms.ads.AdListener;
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

public class MainActivity extends BaseActivity implements View.OnClickListener, IOnHandlerMessage, AudioManager.OnAudioFocusChangeListener {
    public static final int NOTIFICATION_ID = 7777;
    private static final int PLAYER_TYPE_RAIN = 0;
    private static final int PLAYER_TYPE_THUNDER = 1;
    private static final int PLAYER_TYPE_PIANO = 2;

    private static final int HANDLE_MAIN_TIMER = 3;

    private WeakRefHandler mWeakRefHandler;
    private FirebaseAnalytics mFirebaseAnalytics;
    private InterstitialAd mInterstitialAd;
    private boolean isInterstitialAdShow = false;
    private int restInterstitialAdCount = 3;
    private AdView mAdView;
    private LinearLayout mController;

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
    private RelativeLayout mPlayBtn, mThunderBtn, mPianoBtn, mVolumeBtn;
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
        mGlideRequestManager = Glide.with(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.banner_ad_fullscreen_id));
        mInterstitialAd.loadAd(getAdRequest());
        mInterstitialAd.setAdListener(mAdListener);

        mAdView = (AdView) findViewById(R.id.adView);
        mAdView.loadAd(getAdRequest());
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                updateLicensedUI();
            }
        });

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
        mController = (LinearLayout) findViewById(R.id.mController);

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
        mVolumeBtn = (RelativeLayout) findViewById(R.id.mVolumeBtn);

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
                    mMediaPlayerLoop.setVolume(mRainSeekBarVolume, mRainSeekBarVolume);
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
                    mThunderPlayerLoop.setVolume(mThunderSeekBarVolume, mThunderSeekBarVolume);
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
                    mPianoPlayerLoop.setVolume(mPianoSeekBarVolume, mPianoSeekBarVolume);
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

    private void updateLicensedUI() {
        mMainAdapter.updateLicensedUI(mAdView.getHeight() + mController.getHeight());
        mMainAdapter.notifyDataSetChanged();
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
                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    Intent notificationIntent = new Intent(this, SplashActivity.class);
                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    notificationIntent.putExtra("notificationId", NOTIFICATION_ID);
                    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

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
                    builder.setContentTitle(getString(R.string.app_name))
                            .setContentText(getString(R.string.msg_finish_by_timer))
                            .setSmallIcon(R.drawable.ico_notification_small)
                            .setContentIntent(contentIntent)
                            .setAutoCancel(true)
                            .setOngoing(false)
                            .setWhen(System.currentTimeMillis());

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        builder.setCategory(Notification.CATEGORY_MESSAGE)
                                .setPriority(Notification.PRIORITY_HIGH)
                                .setVisibility(Notification.VISIBILITY_PUBLIC);
                    }

                    nm.notify(NOTIFICATION_ID, builder.build());
                    finish();
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

    private void createNextMediaPlayer(final int playType, final int resId) {
        if (playType == PLAYER_TYPE_RAIN) {
            mMediaPlayerLoop = MediaPlayer.create(mContext, resId);
            mMediaPlayer.setNextMediaPlayer(mMediaPlayerLoop);
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.release();
                    mMediaPlayer = mMediaPlayerLoop;
                    mMediaPlayer.setVolume(mRainSeekBarVolume, mRainSeekBarVolume);

                    createNextMediaPlayer(playType, resId);

                    LogUtil.e(String.format("Loop!!!"));
                }
            });
        } else if (playType == PLAYER_TYPE_THUNDER) {
            mThunderPlayerLoop = MediaPlayer.create(mContext, resId);
            mThunderPlayer.setNextMediaPlayer(mThunderPlayerLoop);
            mThunderPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.release();
                    mThunderPlayer = mThunderPlayerLoop;
                    mThunderPlayer.setVolume(mThunderSeekBarVolume, mThunderSeekBarVolume);

                    createNextMediaPlayer(playType, resId);

                    LogUtil.e(String.format("Loop!!!"));
                }
            });
        } else if (playType == PLAYER_TYPE_PIANO) {
            mPianoPlayerLoop = MediaPlayer.create(mContext, resId);
            mPianoPlayer.setNextMediaPlayer(mPianoPlayerLoop);
            mPianoPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.release();
                    mPianoPlayer = mPianoPlayerLoop;
                    mPianoPlayer.setVolume(mPianoSeekBarVolume, mPianoSeekBarVolume);

                    createNextMediaPlayer(playType, resId);

                    LogUtil.e(String.format("Loop!!!"));
                }
            });
        }
    }

    private void playMusic(int playType) {
        stopMusic(playType);

        if (playType == PLAYER_TYPE_RAIN) {
            if (isRain && requestFocus()) {
                int position = mMainViewPager.getCurrentItem();
                if (mMainAdapter.isLoop) {
                    position = position % arrCoreItem.size();
                }

                if (arrCoreItem.get(position).getCoreMusic() != -1) {
                    mMediaPlayer = MediaPlayer.create(mContext, arrCoreItem.get(position).getCoreMusic());
                    rainDuration = mMediaPlayer.getDuration();
                    LogUtil.e("Rain Duration :::: " + rainDuration);

                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mMediaPlayer.setVolume(mRainSeekBarVolume, mRainSeekBarVolume);
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            mMediaPlayer.start();
                        }
                    });

                    createNextMediaPlayer(playType, arrCoreItem.get(position).getCoreMusic());
                }
            }
        } else if (playType == PLAYER_TYPE_THUNDER) {
            if (isThunder && requestFocus()) {
                mThunderPlayer = MediaPlayer.create(mContext, R.raw.thunderstorm);
                thunderDuration = mThunderPlayer.getDuration();
                LogUtil.e("Thunder Duration :::: " + thunderDuration);

                mThunderPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mThunderPlayer.setVolume(mThunderSeekBarVolume, mThunderSeekBarVolume);
                mThunderPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mThunderPlayer.start();
                    }
                });

                createNextMediaPlayer(playType, R.raw.thunderstorm);
            }
        } else if (playType == PLAYER_TYPE_PIANO) {
            if (isPiano && requestFocus()) {
                mPianoPlayer = MediaPlayer.create(mContext, R.raw.piano);
                pianoDuration = mPianoPlayer.getDuration();
                LogUtil.e("Piano Duration :::: " + pianoDuration);

                mPianoPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mPianoPlayer.setVolume(mPianoSeekBarVolume, mPianoSeekBarVolume);
                mPianoPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mPianoPlayer.start();
                    }
                });

                createNextMediaPlayer(playType, R.raw.piano);
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
            if (mMediaPlayerLoop != null) {
                if (mMediaPlayerLoop.isPlaying()) mMediaPlayerLoop.stop();
                mMediaPlayerLoop.release();
                mMediaPlayerLoop = null;
            }
        } else if (playType == PLAYER_TYPE_THUNDER) {
            if (mThunderPlayer != null) {
                if (mThunderPlayer.isPlaying()) mThunderPlayer.stop();
                mThunderPlayer.release();
                mThunderPlayer = null;
            }
            if (mThunderPlayerLoop != null) {
                if (mThunderPlayerLoop.isPlaying()) mThunderPlayerLoop.stop();
                mThunderPlayerLoop.release();
                mThunderPlayerLoop = null;
            }
        } else if (playType == PLAYER_TYPE_PIANO) {
            if (mPianoPlayer != null) {
                if (mPianoPlayer.isPlaying()) mPianoPlayer.stop();
                mPianoPlayer.release();
                mPianoPlayer = null;
            }
            if (mPianoPlayerLoop != null) {
                if (mPianoPlayerLoop.isPlaying()) mPianoPlayerLoop.stop();
                mPianoPlayerLoop.release();
                mPianoPlayerLoop = null;
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
                if (mMediaPlayerLoop != null && mMediaPlayerLoop.isPlaying()) mMediaPlayerLoop.setVolume(mRainSeekBarVolume, mRainSeekBarVolume);
                if (mThunderPlayer != null && mThunderPlayer.isPlaying()) mThunderPlayer.setVolume(mThunderSeekBarVolume, mThunderSeekBarVolume);
                if (mThunderPlayerLoop != null && mThunderPlayerLoop.isPlaying()) mThunderPlayerLoop.setVolume(mThunderSeekBarVolume, mThunderSeekBarVolume);
                if (mPianoPlayer != null && mPianoPlayer.isPlaying()) mPianoPlayer.setVolume(mPianoSeekBarVolume, mPianoSeekBarVolume);
                if (mPianoPlayerLoop != null && mPianoPlayerLoop.isPlaying()) mPianoPlayerLoop.setVolume(mPianoSeekBarVolume, mPianoSeekBarVolume);
                break;
            case AudioManager.AUDIOFOCUS_LOSS: // -1
                LogUtil.e("AudioManager.AUDIOFOCUS_LOSS");

                if (isVolume) mVolumeBtn.performClick();
                if (isRain) mPlayBtn.performClick();
                if (isThunder) mThunderBtn.performClick();
                if (isPiano) mPianoBtn.performClick();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: // -2
                LogUtil.e("AudioManager.AUDIOFOCUS_LOSS_TRANSIENT");

                if (isVolume) mVolumeBtn.performClick();
                if (isRain) mPlayBtn.performClick();
                if (isThunder) mThunderBtn.performClick();
                if (isPiano) mPianoBtn.performClick();
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
