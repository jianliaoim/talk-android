package com.teambition.talk.entity;

import org.parceler.Parcel;

/**
 * Created by zeatual on 15/2/2.
 */
@Parcel(Parcel.Serialization.BEAN)
public class Preference {

    private String _id;
    private String user;
    private boolean emailNotification;
    private boolean notifyOnRelated;
    private boolean muteWhenWebOnline;
    private boolean pushOnWorkTime;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean isEmailNotification() {
        return emailNotification;
    }

    public void setEmailNotification(boolean emailNotification) {
        this.emailNotification = emailNotification;
    }

    public boolean isNotifyOnRelated() {
        return notifyOnRelated;
    }

    public void setNotifyOnRelated(boolean notifyOnRelated) {
        this.notifyOnRelated = notifyOnRelated;
    }

    public boolean isMuteWhenWebOnline() {
        return muteWhenWebOnline;
    }

    public void setMuteWhenWebOnline(boolean muteWhenWebOnline) {
        this.muteWhenWebOnline = muteWhenWebOnline;
    }

    public boolean isPushOnWorkTime() {
        return pushOnWorkTime;
    }

    public void setPushOnWorkTime(boolean pushOnWorkTime) {
        this.pushOnWorkTime = pushOnWorkTime;
    }
}
