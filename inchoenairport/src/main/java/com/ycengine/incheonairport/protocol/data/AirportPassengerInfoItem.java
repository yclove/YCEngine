package com.ycengine.incheonairport.protocol.data;

import android.os.Parcel;
import android.os.Parcelable;

public class AirportPassengerInfoItem implements Parcelable {
    private String adate = ""; // 표출 일자
    private String atime = ""; // 시간대 표시
    private String sum1 = ""; // 승객예고 입국장 동편(A,B) 현황
    private String sum2 = ""; // 승객예고 입국장 서편(E,F) 현황
    private String sum3 = ""; // 승객예고 입국심사(C) 현황
    private String sum4 = ""; // 승객예고 입국심사(D) 현황
    private String sum5 = ""; // 승객예고 출국장1,2 현황
    private String sum6 = ""; // 승객예고 출국장3 현황
    private String sum7 = ""; // 승객예고 출국장4 현황
    private String sum8 = ""; // 승객예고 출국장5,6 현황
    private String sumset1 = ""; // 승객예고 입국장 합계
    private String sumset2 = ""; // 승객예고 출국장 합계

    public AirportPassengerInfoItem(Parcel in) {
        adate = in.readString();
        atime = in.readString();
        sum1 = in.readString();
        sum2 = in.readString();
        sum3 = in.readString();
        sum4 = in.readString();
        sum5 = in.readString();
        sum6 = in.readString();
        sum7 = in.readString();
        sum8 = in.readString();
        sumset1 = in.readString();
        sumset2 = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(adate);
        dest.writeString(atime);
        dest.writeString(sum1);
        dest.writeString(sum2);
        dest.writeString(sum3);
        dest.writeString(sum4);
        dest.writeString(sum5);
        dest.writeString(sum6);
        dest.writeString(sum7);
        dest.writeString(sum8);
        dest.writeString(sumset1);
        dest.writeString(sumset2);
    }

    public static final Creator<AirportPassengerInfoItem> CREATOR = new Creator<AirportPassengerInfoItem>() {
        public AirportPassengerInfoItem createFromParcel(Parcel in) {
            return new AirportPassengerInfoItem(in);
        }

        public AirportPassengerInfoItem[] newArray(int size) {
            return new AirportPassengerInfoItem[size];
        }
    };

    public AirportPassengerInfoItem() {
    }

    public String getAdate() {
        return adate;
    }

    public void setAdate(String adate) {
        this.adate = adate;
    }

    public String getAtime() {
        return atime;
    }

    public void setAtime(String atime) {
        this.atime = atime;
    }

    public String getSum1() {
        return sum1;
    }

    public void setSum1(String sum1) {
        this.sum1 = sum1;
    }

    public String getSum2() {
        return sum2;
    }

    public void setSum2(String sum2) {
        this.sum2 = sum2;
    }

    public String getSum3() {
        return sum3;
    }

    public void setSum3(String sum3) {
        this.sum3 = sum3;
    }

    public String getSum4() {
        return sum4;
    }

    public void setSum4(String sum4) {
        this.sum4 = sum4;
    }

    public String getSum5() {
        return sum5;
    }

    public void setSum5(String sum5) {
        this.sum5 = sum5;
    }

    public String getSum6() {
        return sum6;
    }

    public void setSum6(String sum6) {
        this.sum6 = sum6;
    }

    public String getSum7() {
        return sum7;
    }

    public void setSum7(String sum7) {
        this.sum7 = sum7;
    }

    public String getSum8() {
        return sum8;
    }

    public void setSum8(String sum8) {
        this.sum8 = sum8;
    }

    public String getSumset1() {
        return sumset1;
    }

    public void setSumset1(String sumset1) {
        this.sumset1 = sumset1;
    }

    public String getSumset2() {
        return sumset2;
    }

    public void setSumset2(String sumset2) {
        this.sumset2 = sumset2;
    }
}
