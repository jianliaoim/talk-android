package com.teambition.talk.client.data.call;

import org.simpleframework.xml.Attribute;

/**
 * Created by wlanjie on 15/9/15.
 */
public class CreateCallMeetingConf {
    @Attribute(name = "maxmember", required = false)
    private String maxmember;

    public CreateCallMeetingConf(String maxmember) {
        this.maxmember = maxmember;
    }

    public String getMaxmember() {
        return maxmember;
    }

    public void setMaxmember(String maxmember) {
        this.maxmember = maxmember;
    }
}
