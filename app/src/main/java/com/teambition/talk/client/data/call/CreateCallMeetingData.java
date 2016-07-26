package com.teambition.talk.client.data.call;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by wlanjie on 15/9/15.
 */
@Root(name = "Request")
public class CreateCallMeetingData {
    @Element(name = "Appid", required = false)
    private String appId;

    @Element(name = "CreateConf", required = false)
    private CreateCallMeetingConf conf;

    public CreateCallMeetingData(String appId, CreateCallMeetingConf conf) {
        this.appId = appId;
        this.conf = conf;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public CreateCallMeetingConf getConf() {
        return conf;
    }

    public void setConf(CreateCallMeetingConf conf) {
        this.conf = conf;
    }
}
