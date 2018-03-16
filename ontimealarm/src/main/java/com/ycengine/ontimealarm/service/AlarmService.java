package com.ycengine.ontimealarm.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.ycengine.ontimealarm.AlarmController;
import com.ycengine.ontimealarm.Constants;
import com.ycengine.ontimealarm.R;
import com.ycengine.ontimealarm.SplashActivity;
import com.ycengine.ontimealarm.data.Databases;
import com.ycengine.ontimealarm.data.OnTimeItem;
import com.ycengine.yclibrary.util.CommonUtil;
import com.ycengine.yclibrary.util.DateUtil;
import com.ycengine.yclibrary.util.LogUtil;
import com.ycengine.yclibrary.util.handler.IOnHandlerMessage;
import com.ycengine.yclibrary.util.handler.WeakRefHandler;
import com.ycengine.yclibrary.widget.RingtoneOnce;

import java.util.ArrayList;

/**
 * YCNOTE - Service 주기
 * onCreate() -> onStartCommand() 순으로 이뤄 지는데요.
 * Service가 실행되고 있는 도중에 다시한번  startService()  메서드가 호출되면 onStartCommand() 주기 부터 실행하게 됩니다.
 * 마치 Activity의 onResume() 처럼 말이죠.
 * 그렇기 때문에 중요한 작업에 대해서는 onCreate() 보다는 onStartCommand() 메서드에 구현을 해줘야 합니다.
 */
public class AlarmService extends Service implements IOnHandlerMessage {
    private Context mContext;
    private WeakRefHandler mHandler;
    private Databases mDatabases;
    private ArrayList<OnTimeItem> arrOnTimeItem;
    private OnTimeItem item;
    private ArrayList<String> repeatDay = new ArrayList<>();
    private AlarmController mAlarmController;
    private RingtoneOnce mRingtone;
    private Vibrator mVibrator;

    /**
     * 다른 컴포넌트가 bindService()를 호출해서 서비스와 연결을 시도하면 이 메소드가 호출됩니다.
     * 이 메소드에서 IBinder를 반환해서 서비스와 컴포넌트가 통신하는데 사용하는 인터페이스를 제공해야 합니다.
     * 만약 시작 타입의 서비스를 구현한다면 null을 반환하면 됩니다.
     */
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    /**
     * 서비스가 처음으로 생성되면 호출됩니다.
     * 이 메소드 안에서 초기의 설정 작업을 하면되고 서비스가 이미 실행중이면 이 메소드는 호출되지 않습니다.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mHandler = new WeakRefHandler(this);
        mAlarmController = new AlarmController(this);
        LogUtil.e("♬ AlarmService 생성");
    }

    /**
     * YCNOTE - Service onStartCommand
     * 다른 컴포넌트가 startService()를 호출해서 서비스가 시작되면 이 메소드가 호출됩니다. 만약 연결된 타입의 서비스를 구현한다면 이 메소드는 재정의 할 필요가 없습니다.
     * onStartCommand() 메서드는 3가지 리턴 타입을 갖게 되는데요.
     * START_STICKY
     * Service가 강제 종료되었을 경우 시스템이 다시 Service를 재시작 시켜 주지만 intent 값을 null로 초기화 시켜서 재시작 합니다.
     * Service 실행시 startService(Intent service) 메서드를 호출 하는데 onStartCommand(Intent intent, int flags, int startId) 메서드에 intent로 value를 넘겨 줄 수 있습니다.
     * 기존에 intent에 value값이 설정이 되있다고 하더라도 Service 재시작시 intent 값이 null로 초기화 되서 재시작 됩니다.
     *
     * START_NOT_STICKY
     * 이 Flag를 리턴해 주시면, 강제로 종료 된 Service가 재시작 하지 않습니다. 시스템에 의해 강제 종료되어도 괸찮은 작업을 진행 할 때 사용해 주시면 됩니다.
     *
     * START_REDELIVER_INTENT
     * START_STICKY와 마찬가지로 Service가 종료 되었을 경우 시스템이 다시 Service를 재시작 시켜 주지만 intent 값을 그대로 유지 시켜 줍니다.
     * startService() 메서드 호출시 Intent value값을 사용한 경우라면 해당 Flag를 사용해서 리턴값을 설정해 주면 됩니다.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.e("♬ AlarmService ▶ onStartCommand");
        try {
            int idx = intent.getIntExtra("idx", -1);
            LogUtil.e("IDX :::: " + idx);

            // action = com.ycengine.ontimealarm.ALARM_START
            if (idx != -1) {
                if (mDatabases == null) mDatabases = new Databases(mContext);
                mDatabases.open();
                item = mDatabases.getOnTimeItem(idx);
                mDatabases.close();

                if (item != null) {
                    mAlarmController.save(item, true);
                    mHandler.sendEmptyMessage(Constants.REQUEST_ON_TIME_ALARM);
                }
            }
            // action = android.intent.action.BOOT_COMPLETED
            else {
                if (mDatabases == null) mDatabases = new Databases(mContext);
                mDatabases.open();
                arrOnTimeItem = mDatabases.getOnTimeItemList();
                mDatabases.close();

                for (OnTimeItem item : arrOnTimeItem) {
                    if (item.getOnTimeEnabled() == Constants.ONTIME_ENABLED)
                        mAlarmController.save(item, false);
                }
            }
        } catch (Exception e) {
            LogUtil.e("♬ AlarmService ▶ " + e.getMessage());
        }

        return START_NOT_STICKY;
    }

    /**
     * 서비스가 소멸되는 도중에 이 메소드가 호출되며 주로 Thread, Listener, BroadcastReceiver와 같은 자원들을 정리하는데 사용하면 됩니다.
     * TaskKiller에 의해 서비스가 강제종료될 경우에는 이 메소드가 호출되지 않습니다
     */
    @Override
    public void onDestroy() {
        LogUtil.e("♬ AlarmService ▶ onDestroy");
        super.onDestroy();

        if (mRingtone != null) {
            mRingtone.stop();
        }
        if (mVibrator != null) {
            mVibrator.cancel();
        }
    }

    @Override
    public void handleMessage(Message msg) {
        switch(msg.what) {
            case Constants.REQUEST_ON_TIME_ALARM:
                if (item.getOnTimeType() != Constants.ONTIME_TYPE_VIBRATE) {
                    mRingtone = new RingtoneOnce(mContext, getRingtoneUri());
                    if (item.getOnTimeVolume() > 0)
                        mRingtone.setmVolume(item.getOnTimeVolume());
                    mRingtone.play();
                }

                if (item.getOnTimeType() != Constants.ONTIME_TYPE_SOUND) {
                    mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    mVibrator.vibrate(500);
                }

                if (item.getOnTimeStatusBar() == Constants.ONTIME_STATUS_BAR_ENABLED) {
                    updateNotification();
                }
                break;
        }
    }

    public void updateNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, SplashActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra("notificationId", item.getOnTimeIdx());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String contentTextVal;
        if (CommonUtil.isNotNull(item.getOnTimeName())) {
            contentTextVal = item.getOnTimeName();
        } else {
            contentTextVal = getString(R.string.app_name);
        }

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
        builder
                .setContentTitle(getString(R.string.app_name))
                .setContentText(contentTextVal + "-" + DateUtil.getCurrentDate("yyyy.MM.dd HH:mm:ss.SSS"))
                .setTicker(contentTextVal)
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

        nm.notify(item.getOnTimeIdx(), builder.build());
    }

    protected Uri getRingtoneUri() {
        String ringtone = item.getOnTimeRingTone();
        if (ringtone.isEmpty()) {
            return Settings.System.DEFAULT_NOTIFICATION_URI;
        }
        return Uri.parse(ringtone);
    }
}
