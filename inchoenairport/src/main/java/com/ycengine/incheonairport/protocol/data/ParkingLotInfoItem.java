package com.ycengine.incheonairport.protocol.data;

import android.os.Parcel;
import android.os.Parcelable;

public class ParkingLotInfoItem implements Parcelable {
    private String resultCode = "";
    private String resultMsg = "";
    private String numOfRows = ""; // 한 페이지 결과 수
    private String pageNo = ""; // 페이지 번호
    private String totalCount = ""; // 전체 결과 수
    private String datetm = ""; // 주차 현황 시간
    private String floor = ""; // 주차장 구분
    private String parking = ""; // 주차구역 주차수
    private String parkingarea = ""; // 주차구역 총주차면수

    public ParkingLotInfoItem(Parcel in) {
        resultCode = in.readString();
        resultMsg = in.readString();
        numOfRows = in.readString();
        pageNo = in.readString();
        totalCount = in.readString();
        datetm = in.readString();
        floor = in.readString();
        parking = in.readString();
        parkingarea = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(resultCode);
        dest.writeString(resultMsg);
        dest.writeString(numOfRows);
        dest.writeString(pageNo);
        dest.writeString(totalCount);
        dest.writeString(datetm);
        dest.writeString(floor);
        dest.writeString(parking);
        dest.writeString(parkingarea);
    }

    public static final Creator<ParkingLotInfoItem> CREATOR = new Creator<ParkingLotInfoItem>() {
        public ParkingLotInfoItem createFromParcel(Parcel in) {
            return new ParkingLotInfoItem(in);
        }

        public ParkingLotInfoItem[] newArray(int size) {
            return new ParkingLotInfoItem[size];
        }
    };

    public ParkingLotInfoItem() {
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

    public String getNumOfRows() {
        return numOfRows;
    }

    public void setNumOfRows(String numOfRows) {
        this.numOfRows = numOfRows;
    }

    public String getPageNo() {
        return pageNo;
    }

    public void setPageNo(String pageNo) {
        this.pageNo = pageNo;
    }

    public String getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(String totalCount) {
        this.totalCount = totalCount;
    }

    public String getDatetm() {
        return datetm;
    }

    public void setDatetm(String datetm) {
        this.datetm = datetm;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getParking() {
        return parking;
    }

    public void setParking(String parking) {
        this.parking = parking;
    }

    public String getParkingarea() {
        return parkingarea;
    }

    public void setParkingarea(String parkingarea) {
        this.parkingarea = parkingarea;
    }
}
