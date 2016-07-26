package com.teambition.talk.entity;

import org.parceler.Parcel;

/**
 * Created by zeatual on 15/4/1.
 */
@Parcel(Parcel.Serialization.BEAN)
public class Prefs {
    Boolean isMute;
    Boolean hideMobile;
    String alias;

    public Prefs() {
    }

    public Prefs(Boolean isMute, String alias) {
        this.isMute = isMute;
        this.alias = alias;
    }

    public Prefs(Boolean hideMobile) {
        this.hideMobile = hideMobile;
    }

    public Boolean getIsMute() {
        return isMute;
    }

    public boolean isMute() {
        if (isMute == null) {
            return false;
        } else {
            return isMute;
        }
    }

    public Boolean getHideMobile() {
        return hideMobile;
    }

    public void setHideMobile(Boolean hideMobile) {
        this.hideMobile = hideMobile;
    }

    public void setIsMute(Boolean isMute) {
        this.isMute = isMute;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
