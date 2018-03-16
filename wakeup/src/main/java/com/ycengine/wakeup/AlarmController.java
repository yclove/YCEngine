/*
 * Copyright 2017 Phillip Hsu
 *
 * This file is part of ClockPlus.
 *
 * ClockPlus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ClockPlus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ClockPlus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ycengine.wakeup;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.ycengine.wakeup.data.NotifyItem;
import com.ycengine.yclibrary.util.CastUtil;
import com.ycengine.yclibrary.util.DateUtil;
import com.ycengine.yclibrary.util.LogUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getActivity;

public final class AlarmController {
    private final Context mContext;
    private ArrayList<String> repeatDay = new ArrayList<>();

    public AlarmController(Context context) {
        mContext = context.getApplicationContext();
    }

    public void scheduleAlarm(NotifyItem alarm, boolean showSnackbar) {
        if (alarm.getNotiEnabled() == Constants.NOTI_DISABLED) {
            return;
        }

        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        /*final long ringAt = alarm.isSnoozed() ? alarm.snoozingUntil() : alarm.ringsAt();
        final PendingIntent alarmIntent = alarmIntent(alarm, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            PendingIntent showIntent = ContentIntentUtils.create(mAppContext, MainActivity.PAGE_ALARMS, alarm.getId());
            AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(ringAt, showIntent);
            am.setAlarmClock(info, alarmIntent);
        } else {
            // WAKEUP alarm types wake the CPU up, but NOT the screen;
            // you would handle that yourself by using a wakelock, etc..
            am.setExact(AlarmManager.RTC_WAKEUP, ringAt, alarmIntent);
            // Show alarm in the status bar
            Intent alarmChanged = new Intent("android.intent.action.ALARM_CHANGED");
            alarmChanged.putExtra("alarmSet", true*//*enabled*//*);
            mAppContext.sendBroadcast(alarmChanged);
        }

        final int hoursToNotifyInAdvance = AlarmPreferences.hoursBeforeUpcoming(mAppContext);
        if (hoursToNotifyInAdvance > 0 || alarm.isSnoozed()) {
            // If snoozed, upcoming note posted immediately.
            long upcomingAt = ringAt - HOURS.toMillis(hoursToNotifyInAdvance);
            // We use a WAKEUP alarm to send the upcoming alarm notification so it goes off even if the
            // device is asleep. Otherwise, it will not go off until the device is turned back on.
            am.set(AlarmManager.RTC_WAKEUP, upcomingAt, notifyUpcomingAlarmIntent(alarm, false));
        }

        if (showSnackbar) {
            String message = mAppContext.getString(R.string.alarm_set_for, DurationUtil.toString(mAppContext, alarm.ringsIn(), false*//*abbreviate*//*));
            showSnackbar(message);
        }*/
    }

    public void cancelAlarm(NotifyItem item) {
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent("com.ycengine.wakeup.ALARM_START");
        PendingIntent pi = PendingIntent.getBroadcast(mContext, item.getNotiIdx(), intent, FLAG_UPDATE_CURRENT);

        LogUtil.e("AlarmManager Cancel :: IDX :: " + item.getNotiIdx());

        if (pi != null) {
            am.cancel(pi);
            pi.cancel();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                // Remove alarm in the status bar
                Intent alarmChanged = new Intent("android.intent.action.ALARM_CHANGED");
                alarmChanged.putExtra("alarmSet", false/*enabled*/);
                mContext.sendBroadcast(alarmChanged);
            }
        }
    }

    private boolean isPastTime(NotifyItem item) {
        // 요일 반복 시
        if (item.getNotiSpecialDay() == -1)
            return false;

        String selectDateTime = String.valueOf(item.getNotiSpecialDay()) + CastUtil.getFillString(item.getNotiHour(), 2) + CastUtil.getFillString(item.getNotiMinute(), 2);
        String currentDateTime = DateUtil.getCurrentDate("yyyyMMddHHmm");

        if (Long.valueOf(selectDateTime) > Long.valueOf(currentDateTime))
            return false;

        return true;
    }

    // YCNOTE - adb shell dumpsys alarm > dump.txt
    public long save(final NotifyItem item) {
        Calendar calendar = Calendar.getInstance();
        String selectDateTime = "", currentDateTime = "";
        long delay = 0;

        // 요일 반복 시
        if (item.getNotiSpecialDay() == -1) {
            selectDateTime = DateUtil.getCurrentDate("yyyyMMdd") + CastUtil.getFillString(item.getNotiHour(), 2) + CastUtil.getFillString(item.getNotiMinute(), 2);
            currentDateTime = DateUtil.getCurrentDate("yyyyMMddHHmm");

            String notiDay = item.getNotiDay();
            repeatDay.clear();
            for (int i = 0; i < notiDay.length(); i++) {
                repeatDay.add(notiDay.substring(i, i + 1));
            }

            int nWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; /* 0 : 일요일 ~ 6 : 토요일 */

            boolean isPossibleCalculation = false;
            int calculationLimit = 0;
            while (!isPossibleCalculation) {
                calculationLimit++;
                if (calculationLimit > 100) {
                    return 0;
                } else {
                    if (nWeek > 6) nWeek = 0;
                    if ("1".equals(repeatDay.get(nWeek))) {
                        // 오늘일 경우
                        if (calculationLimit == 1) {
                            // 지난 시간일 경우 지나친다.
                            if (Long.valueOf(selectDateTime) <= Long.valueOf(currentDateTime)) {
                                nWeek += 1;
                                delay += AlarmManager.INTERVAL_DAY;
                            } else {
                                isPossibleCalculation = true;
                            }
                        } else {
                            isPossibleCalculation = true;
                        }
                    } else {
                        nWeek += 1;
                        delay += AlarmManager.INTERVAL_DAY;
                    }
                }
            }
        }

        if (!isPastTime(item) && item.getNotiEnabled() == Constants.NOTI_ENABLED) {
            int notiSpecialDay = (item.getNotiSpecialDay() != -1) ? item.getNotiSpecialDay() : Integer.valueOf(DateUtil.getCurrentDate("yyyyMMdd"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
            selectDateTime = String.valueOf(notiSpecialDay) + CastUtil.getFillString(item.getNotiHour(), 2) + CastUtil.getFillString(item.getNotiMinute(), 2) + "00000";
            calendar = Calendar.getInstance();
            try {
                calendar.setTime(sdf.parse(selectDateTime));
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }

            Intent intent = new Intent("com.ycengine.wakeup.ALARM_START");
            intent.putExtra("idx", item.getNotiIdx());
            PendingIntent pi = PendingIntent.getBroadcast(mContext, item.getNotiIdx(), intent, FLAG_UPDATE_CURRENT);

            /*
                RTC_WAKEUP :  인자로 넘겨진, 시간을 기준으로 알람이 알람이 동작하여, pendingIntent를 전달합니다.
                RTC : 이름에서 볼 수 있듯이, 위와 똑같지만, sleep모드에 들어간 기계를 깨우지는 않습니다.
                ELAPSED_REALTIME_WAKEUP :  안드로이드 기계가 부팅된 시점을 기준으로 알람이 울립니다.
                ELAPSED_REALTIME : ELAPSED_REALTIME_WAKEUP과 같지만, sleep모드에 들어갔다면 깨우지는 않습니다.
            */
            AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

            /*
            * Doze 상태에 대한 처리
            */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis() + delay, pi);
                am.setAlarmClock(info, pi);
            } else {
                // WAKEUP alarm types wake the CPU up, but NOT the screen;
                // you would handle that yourself by using a wakelock, etc..
                am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + delay, pi);
                // Show alarm in the status bar
                Intent alarmChanged = new Intent("android.intent.action.ALARM_CHANGED");
                alarmChanged.putExtra("alarmSet", true/*enabled*/);
                mContext.sendBroadcast(alarmChanged);
            }
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + delay, pi);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + delay, pi);
            } else {
                am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + delay, pi);
            }*/
            String dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(calendar.getTimeInMillis() + delay));
            LogUtil.e("AlarmManager Save :: IDX :: " + item.getNotiIdx() + " :: DATE :: " + dateString);
        } else {
            return 0;
        }

        return calendar.getTimeInMillis() + delay;
    }

    private PendingIntent alarmIntent(NotifyItem item) {
        Intent intent = new Intent("com.ycengine.wakeup.ALARM_START");
        /*
            FLAG_UPDATE_CURRENT : Pending인텐트가 이미 존재할 경우, Extra Data를 모두 대체한다.
            FLAG_CANCEL_CURRENT : Pending인텐트가 이미 존재할 경우, 기존 Pending인텐트를 cancel하고 다시 생성한다.
            FLAG_IMMUTABLE : 이름에서 알 수 있듯이, 기존 PendingIntent 는 변경되지 않구요, 새로 데이터를 추가한 PendingIntent를 보내도 무시한다 입니다.
            FLAG_NO_CREATE: Pending인텐트가 기존에 존재하지 않으면, Null을 return한다.
            FLAG_ONE_SHOT : 이름이 아주 직관적인데요, 한번만 사용할 수 있는 PendingIntent란 뜻 입니다.
        */
        return getActivity(mContext, item.getNotiIdx(), intent, FLAG_UPDATE_CURRENT);
    }

    private void cancelAlarmIfExists(Context mContext,int requestCode,Intent intent){
        try {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, requestCode, intent, 0);
            AlarmManager am=(AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
            am.cancel(pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
