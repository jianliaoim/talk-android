package com.teambition.talk.event;

import com.teambition.talk.entity.Message;

/**
 * Created by nlmartian on 4/27/15.
 */
public class AudioProgressChangeEvent {

    public Message message;

    public AudioProgressChangeEvent(Message message) {
        this.message = message;
    }
}
