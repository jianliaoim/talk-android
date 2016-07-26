package com.teambition.talk.client.data.call;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by wlanjie on 15/9/15.
 */
@Root(name = "Request")
public class InviteJoinCallMeetingData {

    @Element(name = "Appid", required = false)
    private String appId;

    @Element(name = "InviteJoinConf", required = false)
    private InviteJoinCallMeetingConf conf;

    public InviteJoinCallMeetingData(String appId, InviteJoinCallMeetingConf conf) {
        this.appId = appId;
        this.conf = conf;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public InviteJoinCallMeetingConf getConf() {
        return conf;
    }

    public void setConf(InviteJoinCallMeetingConf conf) {
        this.conf = conf;
    }
}
