package com.teambition.talk.client.data;

import com.teambition.talk.entity.Prefs;

/**
 * Created by zeatual on 15/4/1.
 */
public class RoomUpdateRequestData {

    String topic;
    String purpose;
    Boolean isPrivate;
    String color;
    Prefs prefs;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Prefs getPrefs() {
        return prefs;
    }

    public void setPrefs(Prefs prefs) {
        this.prefs = prefs;
    }
}
