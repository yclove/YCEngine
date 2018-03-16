package com.ycengine.tourist.model;

import com.ycengine.tourist.model.CodeItem;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "body")
public class BodyItem {
    @ElementList(name = "items")
    List<CodeItem> items;

    @Element(name = "numOfRows")
    String numOfRows;

    @Element(name = "pageNo")
    String pageNo;

    @Element(name = "totalCount")
    String totalCount;

    public List<CodeItem> getItems() {
        return items;
    }

    public void setItems(List<CodeItem> items) {
        this.items = items;
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

    public BodyItem() {
    }
}
