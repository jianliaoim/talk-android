package com.teambition.talk.event;

/**
 * Created by nlmartian on 2/18/16.
 */
public class MentionReadEvent {
    private String messageId;

    public MentionReadEvent(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
