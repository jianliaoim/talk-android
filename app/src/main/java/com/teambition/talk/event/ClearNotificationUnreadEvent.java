package com.teambition.talk.event;

/**
 * Created by zeatual on 15/10/10.
 */
public class ClearNotificationUnreadEvent {

    public String targetId;

    public ClearNotificationUnreadEvent(String targetId) {
        this.targetId= targetId;
    }
}
