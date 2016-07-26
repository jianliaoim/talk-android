package com.teambition.talk.client.data.call;

import org.simpleframework.xml.Attribute;

/**
 * Created by wlanjie on 15/9/23.
 */
public class DismissConf {

    @Attribute(name = "confid", required = false)
    private String confid;

    public String getConfid() {
        return confid;
    }

    public void setConfid(String confid) {
        this.confid = confid;
    }
}
