package com.teambition.talk.client.data.call;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by wlanjie on 15/9/14.
 */
@Root(name = "CallCancel")
public class CallCancelData {
    @Element(name = "appId", required = false)
    private String appId;
    @Element(name = "callSid", required = false)
    private String callSid;
    @Element(name = "type", required = false)
    private int type;

    public CallCancelData(String appId, String callSid, int type) {
        this.appId = appId;
        this.callSid = callSid;
        this.type = type;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getCallSid() {
        return callSid;
    }

    public void setCallSid(String callSid) {
        this.callSid = callSid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
