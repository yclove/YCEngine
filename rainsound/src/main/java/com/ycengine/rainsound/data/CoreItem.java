package com.ycengine.rainsound.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.ycengine.rainsound.Constants;
import com.ycengine.rainsound.R;

public class CoreItem implements Parcelable {
    private int corePosition = -1;
    private String coreStatus = Constants.CORE_STATUS_INOPERATIVE;
    private int coreTitle = R.string.app_name;
    private int corePhoto = -1;
    private String corePhotographer = "";
    private String corePhotoReferrer = "";
    private int coreMusic = -1;
    private String coreMusician = "";
    private String coreMusicReferrer = "";
    private String coreRegDate = "";

    public CoreItem(Parcel in) {
        corePosition = in.readInt();
        coreStatus = in.readString();
        coreTitle = in.readInt();
        corePhoto = in.readInt();
        corePhotographer = in.readString();
        corePhotoReferrer = in.readString();
        coreMusic = in.readInt();
        coreMusician = in.readString();
        coreMusicReferrer = in.readString();
        coreRegDate = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(corePosition);
        dest.writeString(coreStatus);
        dest.writeInt(coreTitle);
        dest.writeInt(corePhoto);
        dest.writeString(corePhotographer);
        dest.writeString(corePhotoReferrer);
        dest.writeInt(coreMusic);
        dest.writeString(coreMusician);
        dest.writeString(coreMusicReferrer);
        dest.writeString(coreRegDate);
    }

    public static final Creator<CoreItem> CREATOR = new Creator<CoreItem>() {
        public CoreItem createFromParcel(Parcel in) {
            return new CoreItem(in);
        }

        public CoreItem[] newArray(int size) {
            return new CoreItem[size];
        }
    };

    public CoreItem() {
    }

    public CoreItem(int position, String status, int title, int photo, String photographer, String photoreferrer, int music, String musician, String musicreferrer, String regdate) {
        this.corePosition = position;
        this.coreStatus = status;
        this.coreTitle = title;
        this.corePhoto = photo;
        this.corePhotographer = photographer;
        this.corePhotoReferrer = photoreferrer;
        this.coreMusic = music;
        this.coreMusician = musician;
        this.coreMusicReferrer = musicreferrer;
        this.coreRegDate = regdate;
    }

    public int getCorePosition() {
        return corePosition;
    }

    public void setCorePosition(int corePosition) {
        this.corePosition = corePosition;
    }

    public String getCoreStatus() {
        return coreStatus;
    }

    public void setCoreStatus(String coreStatus) {
        this.coreStatus = coreStatus;
    }

    public int getCoreTitle() {
        return coreTitle;
    }

    public void setCoreTitle(int coreTitle) {
        this.coreTitle = coreTitle;
    }

    public int getCorePhoto() {
        return corePhoto;
    }

    public void setCorePhoto(int corePhoto) {
        this.corePhoto = corePhoto;
    }

    public String getCorePhotographer() {
        return corePhotographer;
    }

    public void setCorePhotographer(String corePhotographer) {
        this.corePhotographer = corePhotographer;
    }

    public String getCorePhotoReferrer() {
        return corePhotoReferrer;
    }

    public void setCorePhotoReferrer(String corePhotoReferrer) {
        this.corePhotoReferrer = corePhotoReferrer;
    }

    public int getCoreMusic() {
        return coreMusic;
    }

    public void setCoreMusic(int coreMusic) {
        this.coreMusic = coreMusic;
    }

    public String getCoreMusician() {
        return coreMusician;
    }

    public void setCoreMusician(String coreMusician) {
        this.coreMusician = coreMusician;
    }

    public String getCoreMusicReferrer() {
        return coreMusicReferrer;
    }

    public void setCoreMusicReferrer(String coreMusicReferrer) {
        this.coreMusicReferrer = coreMusicReferrer;
    }

    public String getCoreRegDate() {
        return coreRegDate;
    }

    public void setCoreRegDate(String coreRegDate) {
        this.coreRegDate = coreRegDate;
    }
}
