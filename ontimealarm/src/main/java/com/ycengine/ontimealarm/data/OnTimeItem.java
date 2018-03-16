package com.ycengine.ontimealarm.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.ycengine.ontimealarm.Constants;

public class OnTimeItem implements Parcelable {
    private int onTimeIdx;
    private int onTimeEnabled = Constants.ONTIME_ENABLED;
    private String onTimeName = "";
    private String onTimeMessage = "";
    private int onTimeType = Constants.ONTIME_TYPE_VIBRATE_SOUND;
    private String onTimeRingTone = "";
    private int onTimeVolume = 100;
    private int onTimeEtqEnabled = Constants.ONTIME_ETQ_ENABLED;
    private int onTimeBeginHour = 21;
    private int onTimeBeginMinute = 0;
    private int onTimeEndHour = 8;
    private int onTimeEndMinute = 0;
    private String onTimeDay = "0000000";
    private String onTimeHour = "000000000000000000000000";
    private int onTimeStatusBar = Constants.ONTIME_STATUS_BAR_ENABLED;
    private String onTimeRegDate = "";

    public OnTimeItem(Parcel in) {
        onTimeIdx = in.readInt();
        onTimeEnabled = in.readInt();
        onTimeName = in.readString();
        onTimeMessage = in.readString();
        onTimeType = in.readInt();
        onTimeRingTone = in.readString();
        onTimeVolume = in.readInt();
        onTimeEtqEnabled = in.readInt();
        onTimeBeginHour = in.readInt();
        onTimeBeginMinute = in.readInt();
        onTimeEndHour = in.readInt();
        onTimeEndMinute = in.readInt();
        onTimeDay = in.readString();
        onTimeHour = in.readString();
        onTimeStatusBar = in.readInt();
        onTimeRegDate = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(onTimeIdx);
        dest.writeInt(onTimeEnabled);
        dest.writeString(onTimeName);
        dest.writeString(onTimeMessage);
        dest.writeInt(onTimeType);
        dest.writeString(onTimeRingTone);
        dest.writeInt(onTimeVolume);
        dest.writeInt(onTimeEtqEnabled);
        dest.writeInt(onTimeBeginHour);
        dest.writeInt(onTimeBeginMinute);
        dest.writeInt(onTimeEndHour);
        dest.writeInt(onTimeEndMinute);
        dest.writeString(onTimeDay);
        dest.writeString(onTimeHour);
        dest.writeInt(onTimeStatusBar);
        dest.writeString(onTimeRegDate);
    }

    public static final Creator<OnTimeItem> CREATOR = new Creator<OnTimeItem>() {
        public OnTimeItem createFromParcel(Parcel in) {
            return new OnTimeItem(in);
        }

        public OnTimeItem[] newArray(int size) {
            return new OnTimeItem[size];
        }
    };

    public OnTimeItem() {
    }

    public OnTimeItem(int idx, int enabled, String name, String message, int type, String ringTone, int volume, int etqEnabled, int beginHour, int beginMinute, int endHour, int endMinute, String day, String hour, int statusBar, String regDate) {
        this.onTimeIdx = idx;
        this.onTimeEnabled = enabled;
        this.onTimeName = name;
        this.onTimeMessage = message;
        this.onTimeType = type;
        this.onTimeRingTone = ringTone;
        this.onTimeVolume = volume;
        this.onTimeEtqEnabled = etqEnabled;
        this.onTimeBeginHour = beginHour;
        this.onTimeBeginMinute = beginMinute;
        this.onTimeEndHour = endHour;
        this.onTimeEndMinute = endMinute;
        this.onTimeDay = day;
        this.onTimeHour = hour;
        this.onTimeStatusBar = statusBar;
        this.onTimeRegDate = regDate;
    }

    public int getOnTimeIdx() {
        return onTimeIdx;
    }

    public void setOnTimeIdx(int onTimeIdx) {
        this.onTimeIdx = onTimeIdx;
    }

    public int getOnTimeEnabled() {
        return onTimeEnabled;
    }

    public void setOnTimeEnabled(int onTimeEnabled) {
        this.onTimeEnabled = onTimeEnabled;
    }

    public String getOnTimeName() {
        return onTimeName;
    }

    public void setOnTimeName(String onTimeName) {
        this.onTimeName = onTimeName;
    }

    public String getOnTimeMessage() {
        return onTimeMessage;
    }

    public void setOnTimeMessage(String onTimeMessage) {
        this.onTimeMessage = onTimeMessage;
    }

    public int getOnTimeType() {
        return onTimeType;
    }

    public void setOnTimeType(int onTimeType) {
        this.onTimeType = onTimeType;
    }

    public String getOnTimeRingTone() {
        return onTimeRingTone;
    }

    public void setOnTimeRingTone(String onTimeRingTone) {
        this.onTimeRingTone = onTimeRingTone;
    }

    public int getOnTimeVolume() {
        return onTimeVolume;
    }

    public void setOnTimeVolume(int onTimeVolume) {
        this.onTimeVolume = onTimeVolume;
    }

    public int getOnTimeEtqEnabled() {
        return onTimeEtqEnabled;
    }

    public void setOnTimeEtqEnabled(int onTimeEtqEnabled) {
        this.onTimeEtqEnabled = onTimeEtqEnabled;
    }

    public int getOnTimeBeginHour() {
        return onTimeBeginHour;
    }

    public void setOnTimeBeginHour(int onTimeBeginHour) {
        this.onTimeBeginHour = onTimeBeginHour;
    }

    public int getOnTimeBeginMinute() {
        return onTimeBeginMinute;
    }

    public void setOnTimeBeginMinute(int onTimeBeginMinute) {
        this.onTimeBeginMinute = onTimeBeginMinute;
    }

    public int getOnTimeEndHour() {
        return onTimeEndHour;
    }

    public void setOnTimeEndHour(int onTimeEndHour) {
        this.onTimeEndHour = onTimeEndHour;
    }

    public int getOnTimeEndMinute() {
        return onTimeEndMinute;
    }

    public void setOnTimeEndMinute(int onTimeEndMinute) {
        this.onTimeEndMinute = onTimeEndMinute;
    }

    public String getOnTimeDay() {
        return onTimeDay;
    }

    public void setOnTimeDay(String onTimeDay) {
        this.onTimeDay = onTimeDay;
    }

    public String getOnTimeHour() {
        return onTimeHour;
    }

    public void setOnTimeHour(String onTimeHour) {
        this.onTimeHour = onTimeHour;
    }

    public int getOnTimeStatusBar() {
        return onTimeStatusBar;
    }

    public void setOnTimeStatusBar(int onTimeStatusBar) {
        this.onTimeStatusBar = onTimeStatusBar;
    }

    public String getOnTimeRegDate() {
        return onTimeRegDate;
    }

    public void setOnTimeRegDate(String onTimeRegDate) {
        this.onTimeRegDate = onTimeRegDate;
    }
}
