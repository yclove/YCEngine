package com.ycengine.incheonairport.protocol.data;

import android.os.Parcel;
import android.os.Parcelable;

public class AirportGateInfoItem implements Parcelable {
    private String resultCode = "";
    private String resultMsg = "";
    private String areadiv = ""; // 지역구분
    private String cgtdt = ""; // 조회일자 YYYYMMDD
    private String cgthm = ""; // 업데이트시간 HHMM
    private String cgtlvlg2 = ""; // 2번출국장혼잡도
    private String cgtlvlg3 = ""; // 3번출국장혼잡도
    private String cgtlvlg4 = ""; // 4번출국장혼잡도
    private String cgtlvlg5 = ""; // 5번출국장혼잡도
    private String pwcntg2 = ""; // 2번출국장대기인수
    private String pwcntg3 = ""; // 3번출국장대기인수
    private String pwcntg4 = ""; // 4번출국장대기인수
    private String pwcntg5 = ""; // 5번출국장대기인수
    private String terno = ""; // 터미널 구분 1: 1터미널 2: 2터미널

    public AirportGateInfoItem(Parcel in) {
        resultCode = in.readString();
        resultMsg = in.readString();
        areadiv = in.readString();
        cgtdt = in.readString();
        cgthm = in.readString();
        cgtlvlg2 = in.readString();
        cgtlvlg3 = in.readString();
        cgtlvlg4 = in.readString();
        cgtlvlg5 = in.readString();
        pwcntg2 = in.readString();
        pwcntg3 = in.readString();
        pwcntg4 = in.readString();
        pwcntg5 = in.readString();
        terno = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(resultCode);
        dest.writeString(resultMsg);
        dest.writeString(areadiv);
        dest.writeString(cgtdt);
        dest.writeString(cgthm);
        dest.writeString(cgtlvlg2);
        dest.writeString(cgtlvlg3);
        dest.writeString(cgtlvlg4);
        dest.writeString(cgtlvlg5);
        dest.writeString(pwcntg2);
        dest.writeString(pwcntg3);
        dest.writeString(pwcntg4);
        dest.writeString(pwcntg5);
        dest.writeString(terno);
    }

    public static final Creator<AirportGateInfoItem> CREATOR = new Creator<AirportGateInfoItem>() {
        public AirportGateInfoItem createFromParcel(Parcel in) {
            return new AirportGateInfoItem(in);
        }

        public AirportGateInfoItem[] newArray(int size) {
            return new AirportGateInfoItem[size];
        }
    };

    public AirportGateInfoItem() {
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public String getAreadiv() {
        return areadiv;
    }

    public void setAreadiv(String areadiv) {
        this.areadiv = areadiv;
    }

    public String getCgtdt() {
        return cgtdt;
    }

    public void setCgtdt(String cgtdt) {
        this.cgtdt = cgtdt;
    }

    public String getCgthm() {
        return cgthm;
    }

    public void setCgthm(String cgthm) {
        this.cgthm = cgthm;
    }

    public String getCgtlvlg2() {
        return cgtlvlg2;
    }

    public void setCgtlvlg2(String cgtlvlg2) {
        this.cgtlvlg2 = cgtlvlg2;
    }

    public String getCgtlvlg3() {
        return cgtlvlg3;
    }

    public void setCgtlvlg3(String cgtlvlg3) {
        this.cgtlvlg3 = cgtlvlg3;
    }

    public String getCgtlvlg4() {
        return cgtlvlg4;
    }

    public void setCgtlvlg4(String cgtlvlg4) {
        this.cgtlvlg4 = cgtlvlg4;
    }

    public String getCgtlvlg5() {
        return cgtlvlg5;
    }

    public void setCgtlvlg5(String cgtlvlg5) {
        this.cgtlvlg5 = cgtlvlg5;
    }

    public String getPwcntg2() {
        return pwcntg2;
    }

    public void setPwcntg2(String pwcntg2) {
        this.pwcntg2 = pwcntg2;
    }

    public String getPwcntg3() {
        return pwcntg3;
    }

    public void setPwcntg3(String pwcntg3) {
        this.pwcntg3 = pwcntg3;
    }

    public String getPwcntg4() {
        return pwcntg4;
    }

    public void setPwcntg4(String pwcntg4) {
        this.pwcntg4 = pwcntg4;
    }

    public String getPwcntg5() {
        return pwcntg5;
    }

    public void setPwcntg5(String pwcntg5) {
        this.pwcntg5 = pwcntg5;
    }

    public String getTerno() {
        return terno;
    }

    public void setTerno(String terno) {
        this.terno = terno;
    }
}
