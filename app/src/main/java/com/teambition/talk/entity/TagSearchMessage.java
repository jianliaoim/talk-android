package com.teambition.talk.entity;

import java.util.List;

/**
 * Created by wlanjie on 15/7/20.
 */
public class TagSearchMessage {
    private int total;
    private List<Message> messages;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
