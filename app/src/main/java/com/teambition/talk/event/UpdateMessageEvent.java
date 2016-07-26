package com.teambition.talk.event;

import com.teambition.talk.entity.Message;

/**
 * Created by zeatual on 14/11/13.
 */
public class UpdateMessageEvent {
    public Message message;

    public UpdateMessageEvent() {
    }

    public UpdateMessageEvent(Message message) {
        this.message = message;
    }
}
