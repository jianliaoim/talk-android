package com.teambition.talk.client.data.call;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by wlanjie on 15/9/23.
 */
@Root(name = "Request")
public class QuitCallMeeting {

    @Element(name = "Appid", required = false)
    private String appId;

    @Element(name = "DismissConf", required = false)
    private DismissConf dismissConf;

    public QuitCallMeeting(String appId, DismissConf dismissConf) {
        this.appId = appId;
        this.dismissConf = dismissConf;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public DismissConf getDismissConf() {
        return dismissConf;
    }

    public void setDismissConf(DismissConf dismissConf) {
        this.dismissConf = dismissConf;
    }
}
