package com.teambition.talk.event;

/**
 * Created by zeatual on 14/11/13.
 */
public class DeleteMessageEvent {
    public String msgId;

    public DeleteMessageEvent(String msgId) {
        this.msgId = msgId;
    }
}
