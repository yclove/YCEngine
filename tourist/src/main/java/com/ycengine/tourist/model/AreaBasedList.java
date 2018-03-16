package com.ycengine.tourist.model;

import android.os.Parcel;
import android.os.Parcelable;

public class AreaBasedList implements Parcelable {
    private String addr1 = ""; // 주소1
    private String addr2 = ""; // 주소2
    private String areacode = ""; // 지역코드
    private String cat1 = ""; // 대분류코드
    private String cat2 = ""; // 대분류코드
    private String cat3 = ""; // 소분류코드
    private String contentid = ""; // 컨텐츠아이디
    private String contenttypeid = ""; // 컨텐츠타입아이디
    private String createdtime = ""; // 생성일 - 20151019152356
    private String firstimage = ""; // 대표이미지
    private String firstimage2 = ""; // 썸네일이미지
    private String mapx = ""; // 경도
    private String mapy = ""; // 위도
    private String mlevel = ""; // 지도레벨
    private String modifiedtime = ""; // 수정일
    private String readcount = ""; // 조회수
    private String sigungucode = ""; // 시군구코드
    private String tel = ""; // 전화
    private String title = ""; // 타이틀
    private String zipcode = "";
    private String booktour = ""; // 교과서여행지여부 - 교과서속여행지여부(1=여행지, 0=해당없음)

    public AreaBasedList(Parcel in) {
        addr1 = in.readString();
        addr2 = in.readString();
        areacode = in.readString();
        cat1 = in.readString();
        cat2 = in.readString();
        cat3 = in.readString();
        contentid = in.readString();
        contenttypeid = in.readString();
        createdtime = in.readString();
        firstimage = in.readString();
        firstimage2 = in.readString();
        mapx = in.readString();
        mapy = in.readString();
        mlevel = in.readString();
        modifiedtime = in.readString();
        readcount = in.readString();
        sigungucode = in.readString();
        tel = in.readString();
        title = in.readString();
        zipcode = in.readString();
        booktour = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(addr1);
        dest.writeString(addr2);
        dest.writeString(areacode);
        dest.writeString(cat1);
        dest.writeString(cat2);
        dest.writeString(cat3);
        dest.writeString(contentid);
        dest.writeString(contenttypeid);
        dest.writeString(createdtime);
        dest.writeString(firstimage);
        dest.writeString(firstimage2);
        dest.writeString(mapx);
        dest.writeString(mapy);
        dest.writeString(mlevel);
        dest.writeString(modifiedtime);
        dest.writeString(readcount);
        dest.writeString(sigungucode);
        dest.writeString(tel);
        dest.writeString(title);
        dest.writeString(zipcode);
        dest.writeString(booktour);
    }

    public static final Creator<AreaBasedList> CREATOR = new Creator<AreaBasedList>() {
        public AreaBasedList createFromParcel(Parcel in) {
            return new AreaBasedList(in);
        }

        public AreaBasedList[] newArray(int size) {
            return new AreaBasedList[size];
        }
    };

    public AreaBasedList() {
    }

    public String getAddr1() {
        return addr1;
    }

    public void setAddr1(String addr1) {
        this.addr1 = addr1;
    }

    public String getAddr2() {
        return addr2;
    }

    public void setAddr2(String addr2) {
        this.addr2 = addr2;
    }

    public String getAreacode() {
        return areacode;
    }

    public void setAreacode(String areacode) {
        this.areacode = areacode;
    }

    public String getCat1() {
        return cat1;
    }

    public void setCat1(String cat1) {
        this.cat1 = cat1;
    }

    public String getCat2() {
        return cat2;
    }

    public void setCat2(String cat2) {
        this.cat2 = cat2;
    }

    public String getCat3() {
        return cat3;
    }

    public void setCat3(String cat3) {
        this.cat3 = cat3;
    }

    public String getContentid() {
        return contentid;
    }

    public void setContentid(String contentid) {
        this.contentid = contentid;
    }

    public String getContenttypeid() {
        return contenttypeid;
    }

    public void setContenttypeid(String contenttypeid) {
        this.contenttypeid = contenttypeid;
    }

    public String getCreatedtime() {
        return createdtime;
    }

    public void setCreatedtime(String createdtime) {
        this.createdtime = createdtime;
    }

    public String getFirstimage() {
        return firstimage;
    }

    public void setFirstimage(String firstimage) {
        this.firstimage = firstimage;
    }

    public String getFirstimage2() {
        return firstimage2;
    }

    public void setFirstimage2(String firstimage2) {
        this.firstimage2 = firstimage2;
    }

    public String getMapx() {
        return mapx;
    }

    public void setMapx(String mapx) {
        this.mapx = mapx;
    }

    public String getMapy() {
        return mapy;
    }

    public void setMapy(String mapy) {
        this.mapy = mapy;
    }

    public String getMlevel() {
        return mlevel;
    }

    public void setMlevel(String mlevel) {
        this.mlevel = mlevel;
    }

    public String getModifiedtime() {
        return modifiedtime;
    }

    public void setModifiedtime(String modifiedtime) {
        this.modifiedtime = modifiedtime;
    }

    public String getReadcount() {
        return readcount;
    }

    public void setReadcount(String readcount) {
        this.readcount = readcount;
    }

    public String getSigungucode() {
        return sigungucode;
    }

    public void setSigungucode(String sigungucode) {
        this.sigungucode = sigungucode;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getBooktour() {
        return booktour;
    }

    public void setBooktour(String booktour) {
        this.booktour = booktour;
    }
}
