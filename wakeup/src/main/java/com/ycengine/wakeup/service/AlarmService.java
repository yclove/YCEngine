package com.ycengine.wakeup.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;

import com.ycengine.wakeup.AlarmController;
import com.ycengine.wakeup.Constants;
import com.ycengine.wakeup.data.Databases;
import com.ycengine.wakeup.data.NotifyItem;
import com.ycengine.wakeup.game.CalculationActivity;
import com.ycengine.wakeup.game.CalculationSimpleActivity;
import com.ycengine.wakeup.game.NonActivity;
import com.ycengine.wakeup.game.PuzzleActivity;
import com.ycengine.yclibrary.util.LogUtil;
import com.ycengine.yclibrary.util.handler.IOnHandlerMessage;
import com.ycengine.yclibrary.util.handler.WeakRefHandler;

import java.util.ArrayList;
import java.util.Calendar;

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
    private ArrayList<NotifyItem> arrNotifyItem;
    private NotifyItem item;
    private ArrayList<String> repeatDay = new ArrayList<>();
    private AlarmController mAlarmController;

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

            // action = com.ycengine.wakeup.ALARM_START
            if (idx != -1) {
                if (mDatabases == null) mDatabases = new Databases(mContext);
                mDatabases.open();
                item = mDatabases.getNotifyItem(idx);
                mDatabases.close();

                if (item != null) {
                    if (item.getNotiSpecialDay() != -1) {
                        Bundle data = new Bundle();
                        Message msg = new Message();
                        data.putInt("idx", idx);
                        msg.setData(data);
                        msg.what = item.getNotiGame();
                        mHandler.sendMessage(msg);
                    } else {
                        String notiDay = item.getNotiDay();
                        repeatDay.clear();
                        for (int i = 0; i < notiDay.length(); i++) {
                            repeatDay.add(notiDay.substring(i, i + 1));
                        }

                        Calendar cal = Calendar.getInstance();
                        int nWeek = cal.get(Calendar.DAY_OF_WEEK); /* 1 : 일요일 ~ 7 : 토요일 */

                        if ("1".equals(repeatDay.get(nWeek - 1))) {
                            Bundle data = new Bundle();
                            Message msg = new Message();
                            data.putInt("idx", idx);
                            msg.setData(data);
                            msg.what = item.getNotiGame();
                            mHandler.sendMessage(msg);
                        }
                        mAlarmController.save(item);
                    }
                }
            }
            // action = android.intent.action.BOOT_COMPLETED
            else {
                if (mDatabases == null) mDatabases = new Databases(mContext);
                mDatabases.open();
                arrNotifyItem = mDatabases.getNotifyItemList();
                mDatabases.close();

                for (NotifyItem item : arrNotifyItem) {
                    if (item.getNotiEnabled() == Constants.NOTI_ENABLED)
                        mAlarmController.save(item);
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
    }

    @Override
    public void handleMessage(Message msg) {
        Intent intent;
        Bundle data = msg.getData();
        int idx = data.getInt("idx", -1);

        switch(msg.what) {
            case Constants.NOTI_GAME_NON:
                intent = new Intent(mContext, NonActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("idx", idx);
                startActivity(intent);
                break;
            case Constants.NOTI_GAME_CALCULATION:
                intent = new Intent(mContext, CalculationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("idx", idx);
                startActivity(intent);
                break;
            case Constants.NOTI_GAME_CALCULATION_SIMPLE:
                intent = new Intent(mContext, CalculationSimpleActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("idx", idx);
                startActivity(intent);
                break;
            case Constants.NOTI_GAME_PUZZLE:
                intent = new Intent(mContext, PuzzleActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("idx", idx);
                startActivity(intent);
                break;
        }
    }
}
