package com.ycengine.tourist.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "response")
public class ResponseItem {
    @Element(name = "header")
    HeaderItem header;

    @Element(name = "body")
    BodyItem body;

    public HeaderItem getHeader() {
        return header;
    }

    public void setHeader(HeaderItem header) {
        this.header = header;
    }

    public BodyItem getBody() {
        return body;
    }

    public void setBody(BodyItem body) {
        this.body = body;
    }

    public ResponseItem() {
    }
}
