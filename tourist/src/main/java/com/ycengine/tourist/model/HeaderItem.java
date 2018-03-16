package com.ycengine.tourist.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "header")
public class HeaderItem {
    @Element(name = "resultCode")
    private String resultCode = "";
    @Element(name = "resultMsg")
    private String resultMsg = "";

    public HeaderItem() {
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
}
