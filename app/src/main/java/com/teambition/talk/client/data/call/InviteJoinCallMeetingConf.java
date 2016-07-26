package com.teambition.talk.client.data.call;

import org.simpleframework.xml.Attribute;

/**
 * Created by wlanjie on 15/9/15.
 */
public class InviteJoinCallMeetingConf {

    @Attribute(name = "number", required = false)
    private String number;

    @Attribute(name = "confid", required = false)
    private String confid;

    public InviteJoinCallMeetingConf(String number, String confid) {
        this.number = number;
        this.confid = confid;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getConfid() {
        return confid;
    }

    public void setConfid(String confid) {
        this.confid = confid;
    }
}
