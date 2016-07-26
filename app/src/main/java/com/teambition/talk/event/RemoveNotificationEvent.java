package com.teambition.talk.event;

import com.teambition.talk.entity.Notification;

/**
 * Created by zeatual on 15/10/10.
 */
public class RemoveNotificationEvent {

    public Notification notification;

    public RemoveNotificationEvent(Notification notification) {
        this.notification = notification;
    }
}
