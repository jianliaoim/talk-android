package com.teambition.talk.entity.call;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by wlanjie on 15/9/15.
 */
@Root(name = "Response")
public class CreateCallMeeting {
    @Element(name = "statusCode", required = false)
    private String statusCode;
    @Element(name = "confid", required = false)
    private String confid;
    @Element(name = "voiptoconfid", required = false)
    private String voiptoconfid;

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getConfid() {
        return confid;
    }

    public void setConfid(String confid) {
        this.confid = confid;
    }

    public String getVoiptoconfid() {
        return voiptoconfid;
    }

    public void setVoiptoconfid(String voiptoconfid) {
        this.voiptoconfid = voiptoconfid;
    }
}
