package com.teambition.talk.entity.call;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by nlmartian on 2/4/16.
 */
@Root(name = "Response")
public class CallResultRes {
    @Element(name = "statusMsg", required = false)
    private String statusMsg;
    @Element(name = "statusCode", required = false)
    private String statusCode;
    @Element(name = "CallResult",  required = false)
    private CallResult callResult;

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public CallResult getCallResult() {
        return callResult;
    }

    public void setCallResult(CallResult callResult) {
        this.callResult = callResult;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }
}
