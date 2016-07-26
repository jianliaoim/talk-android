package com.teambition.talk.entity.call;

import org.simpleframework.xml.Element;

/**
 * Created by nlmartian on 2/4/16.
 */
public class CallResult {
    // 呼叫结果，0 成功，1未接听，2失败
    @Element(name = "state", required = false)
    private String state;
    @Element(name = "callTime", required = false)
    private String callTime;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCallTime() {
        return callTime;
    }

    public void setCallTime(String callTime) {
        this.callTime = callTime;
    }
}
