package com.teambition.talk.event;

import com.teambition.talk.entity.Message;

/**
 * Created by zeatual on 14/11/13.
 */
public class RePostEvent {

    public static final int RE_UPLOAD = 0;
    public static final int RE_SEND = 1;

    private int action;
    private Message message;

    public RePostEvent(int action, Message message) {
        this.action = action;
        this.message = message;
    }

    public int getAction() {
        return action;
    }

    public Message getMessage() {
        return message;
    }
}
