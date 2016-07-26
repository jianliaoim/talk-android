package com.teambition.talk.event;

import com.teambition.talk.entity.Notification;

/**
 * Created by zeatual on 15/10/10.
 */
public class UpdateNotificationEvent {

    public Notification notification;

    public UpdateNotificationEvent(Notification notification) {
        this.notification = notification;
    }
}
