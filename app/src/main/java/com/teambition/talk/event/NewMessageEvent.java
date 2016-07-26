package com.teambition.talk.event;

import com.teambition.talk.entity.Message;

/**
 * Created by jgzhu on 11/4/14.
 */
public class NewMessageEvent {
    public Message message;

    public NewMessageEvent(Message message) {
        this.message = message;
    }
}
