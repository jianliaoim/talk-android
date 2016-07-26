package com.teambition.talk.event;

import com.teambition.talk.entity.Message;

/**
 * Created by nlmartian on 4/27/15.
 */
public class AudioResetEvent {
    public Message message;

    public AudioResetEvent(Message message) {
        this.message = message;
    }
}
