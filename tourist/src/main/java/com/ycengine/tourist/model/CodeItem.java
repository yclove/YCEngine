package com.ycengine.tourist.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "item")
public class CodeItem implements Parcelable {
    @Element(name = "code")
    private String code = ""; // 코드
    @Element(name = "name")
    private String name = ""; // 코드명
    @Element(name = "rnum")
    private int rnum; // 일련번호

    private String type = ""; // 타입
    private boolean isSelected = false;

    public CodeItem(Parcel in) {
        type = in.readString();
        code = in.readString();
        name = in.readString();
        rnum = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(code);
        dest.writeString(name);
        dest.writeInt(rnum);
    }

    public static final Creator<CodeItem> CREATOR = new Creator<CodeItem>() {
        public CodeItem createFromParcel(Parcel in) {
            return new CodeItem(in);
        }

        public CodeItem[] newArray(int size) {
            return new CodeItem[size];
        }
    };

    public CodeItem() {
    }

    public CodeItem(String _type, String _code, String _name, int _rnum, boolean _selected) {
        this.type = _type;
        this.code = _code;
        this.name = _name;
        this.rnum = _rnum;
        this.isSelected = _selected;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRnum() {
        return rnum;
    }

    public void setRnum(int rnum) {
        this.rnum = rnum;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
