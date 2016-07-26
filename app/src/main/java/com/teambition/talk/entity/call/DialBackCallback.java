package com.teambition.talk.entity.call;

import org.simpleframework.xml.Element;

/**
 * Created by wlanjie on 15/9/14.
 */
public class DialBackCallback {
    @Element(name = "customerSerNum")
    private String customerSerNum;
    @Element(name = "fromSerNum")
    private String fromSerNum;
    @Element(name = "orderId")
    private String orderId;
    @Element(name = "appId", required = false)
    private String appId;
    @Element(name = "callSid", required = false)
    private String callSid;
    @Element(name = "dateCreated", required = false)
    private String dateCreated;

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

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getCustomerSerNum() {
        return customerSerNum;
    }

    public void setCustomerSerNum(String customerSerNum) {
        this.customerSerNum = customerSerNum;
    }

    public String getFromSerNum() {
        return fromSerNum;
    }

    public void setFromSerNum(String fromSerNum) {
        this.fromSerNum = fromSerNum;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
