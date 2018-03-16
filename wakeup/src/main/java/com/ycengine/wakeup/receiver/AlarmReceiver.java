package com.ycengine.wakeup.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ycengine.wakeup.service.AlarmService;
import com.ycengine.yclibrary.util.LogUtil;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // 재부팅일 경우
        }

        Intent mAlarmService = new Intent(context, AlarmService.class);
        int idx = intent.getIntExtra("idx", -1);
        mAlarmService.putExtra("idx", idx);

        LogUtil.e("BroadcastReceiver :::: " + intent.getAction() + " :: IDX :: " + idx);

        context.startService(mAlarmService);
    }
}