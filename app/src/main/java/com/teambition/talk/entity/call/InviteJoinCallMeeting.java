package com.teambition.talk.entity.call;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by wlanjie on 15/9/15.
 */
@Root(name = "Response")
public class InviteJoinCallMeeting {

    @Element(name = "statusMsg", required = false)
    private String statusMsg;

    @Element(name = "statusCode", required = false)
    private String statusCode;

    @Element(name = "callSid", required = false)
    private String callSid;

    @Element(name = "orderId", required = false)
    private String orderId;

    public String getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getCallSid() {
        return callSid;
    }

    public void setCallSid(String callSid) {
        this.callSid = callSid;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
