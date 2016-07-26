package com.teambition.talk.entity.call;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by wlanjie on 15/9/12.
 */
@Root(name = "Response")
public class DialBack {

    @Element(name = "statusMsg", required = false)
    private String statusMsg;

    @Element(name = "statusCode", required = false)
    private String statusCode;

    @Element(name = "CallBack", required = false)
    private DialBackCallback callback;

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public DialBackCallback getCallback() {
        return callback;
    }

    public void setCallback(DialBackCallback callback) {
        this.callback = callback;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }
}
