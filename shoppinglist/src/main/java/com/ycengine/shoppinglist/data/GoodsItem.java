package com.ycengine.shoppinglist.data;

import android.os.Parcel;
import android.os.Parcelable;

public class GoodsItem implements Parcelable {
    private String goodsName = "";
    private String goodsRegDate = "";
    private String goodsEventType = ""; /* 0 : 구매물품, 1 : 취소물품, 2 : 구매완료 */
    private String goodsEventDate = "";

    public GoodsItem(Parcel in) {
        goodsName = in.readString();
        goodsRegDate = in.readString();
        goodsEventType = in.readString();
        goodsEventDate = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(goodsName);
        dest.writeString(goodsRegDate);
        dest.writeString(goodsEventType);
        dest.writeString(goodsEventDate);
    }

    public static final Creator<GoodsItem> CREATOR = new Creator<GoodsItem>() {
        public GoodsItem createFromParcel(Parcel in) {
            return new GoodsItem(in);
        }

        public GoodsItem[] newArray(int size) {
            return new GoodsItem[size];
        }
    };

    public GoodsItem() {
    }

    public GoodsItem(String name, String regDate, String eventType, String eventDate) {
        this.goodsName = name;
        this.goodsRegDate = regDate;
        this.goodsEventType = eventType;
        this.goodsEventDate = eventDate;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getGoodsRegDate() {
        return goodsRegDate;
    }

    public void setGoodsRegDate(String goodsRegDate) {
        this.goodsRegDate = goodsRegDate;
    }

    public String getGoodsEventType() {
        return goodsEventType;
    }

    public void setGoodsEventType(String goodsEventType) {
        this.goodsEventType = goodsEventType;
    }

    public String getGoodsEventDate() {
        return goodsEventDate;
    }

    public void setGoodsEventDate(String goodsEventDate) {
        this.goodsEventDate = goodsEventDate;
    }
}
