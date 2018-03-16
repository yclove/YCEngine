package com.ycengine.wakeup.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.ycengine.wakeup.Constants;

public class NotifyItem implements Parcelable {
    private int notiIdx;
    private int notiEnabled = Constants.NOTI_ENABLED;
    private String notiName = "";
    private String notiMessage = "";
    private int notiType = Constants.NOTI_TYPE_VIBRATE_SOUND;
    private String notiRingTone = "";
    private int notiVolume = 100;
    private int notiGame = Constants.NOTI_GAME_NON;
    private int notiHour = 7;
    private int notiMinute = 30;
    private String notiDay = "0000000";
    private int notiSpecialDay = -1;
    private String notiRegDate = "";

    public NotifyItem(Parcel in) {
        notiIdx = in.readInt();
        notiEnabled = in.readInt();
        notiName = in.readString();
        notiMessage = in.readString();
        notiType = in.readInt();
        notiRingTone = in.readString();
        notiVolume = in.readInt();
        notiGame = in.readInt();
        notiHour = in.readInt();
        notiMinute = in.readInt();
        notiDay = in.readString();
        notiSpecialDay = in.readInt();
        notiRegDate = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(notiIdx);
        dest.writeInt(notiEnabled);
        dest.writeString(notiName);
        dest.writeString(notiMessage);
        dest.writeInt(notiType);
        dest.writeString(notiRingTone);
        dest.writeInt(notiVolume);
        dest.writeInt(notiGame);
        dest.writeInt(notiHour);
        dest.writeInt(notiMinute);
        dest.writeString(notiDay);
        dest.writeInt(notiSpecialDay);
        dest.writeString(notiRegDate);
    }

    public static final Creator<NotifyItem> CREATOR = new Creator<NotifyItem>() {
        public NotifyItem createFromParcel(Parcel in) {
            return new NotifyItem(in);
        }

        public NotifyItem[] newArray(int size) {
            return new NotifyItem[size];
        }
    };

    public NotifyItem() {
    }

    public NotifyItem(int idx, int enabled, String name, String message, int type, String ringTone, int volume, int game, int hour, int minute, String day, int specialDay, String regDate) {
        this.notiIdx = idx;
        this.notiEnabled = enabled;
        this.notiName = name;
        this.notiMessage = message;
        this.notiType = type;
        this.notiRingTone = ringTone;
        this.notiVolume = volume;
        this.notiGame = game;
        this.notiHour = hour;
        this.notiMinute = minute;
        this.notiDay = day;
        this.notiSpecialDay = specialDay;
        this.notiRegDate = regDate;
    }

    public int getNotiIdx() {
        return notiIdx;
    }

    public void setNotiIdx(int notiIdx) {
        this.notiIdx = notiIdx;
    }

    public int getNotiEnabled() {
        return notiEnabled;
    }

    public void setNotiEnabled(int notiEnabled) {
        this.notiEnabled = notiEnabled;
    }

    public String getNotiName() {
        return notiName;
    }

    public void setNotiName(String notiName) {
        this.notiName = notiName;
    }

    public String getNotiMessage() {
        return notiMessage;
    }

    public void setNotiMessage(String notiMessage) {
        this.notiMessage = notiMessage;
    }

    public int getNotiType() {
        return notiType;
    }

    public void setNotiType(int notiType) {
        this.notiType = notiType;
    }

    public String getNotiRingTone() {
        return notiRingTone;
    }

    public void setNotiRingTone(String notiRingTone) {
        this.notiRingTone = notiRingTone;
    }

    public int getNotiVolume() {
        return notiVolume;
    }

    public void setNotiVolume(int notiVolume) {
        this.notiVolume = notiVolume;
    }

    public int getNotiGame() {
        return notiGame;
    }

    public void setNotiGame(int notiGame) {
        this.notiGame = notiGame;
    }

    public int getNotiHour() {
        return notiHour;
    }

    public void setNotiHour(int notiHour) {
        this.notiHour = notiHour;
    }

    public int getNotiMinute() {
        return notiMinute;
    }

    public void setNotiMinute(int notiMinute) {
        this.notiMinute = notiMinute;
    }

    public String getNotiDay() {
        return notiDay;
    }

    public void setNotiDay(String notiDay) {
        this.notiDay = notiDay;
    }

    public int getNotiSpecialDay() {
        return notiSpecialDay;
    }

    public void setNotiSpecialDay(int notiSpecialDay) {
        this.notiSpecialDay = notiSpecialDay;
    }

    public String getNotiRegDate() {
        return notiRegDate;
    }

    public void setNotiRegDate(String notiRegDate) {
        this.notiRegDate = notiRegDate;
    }
}
