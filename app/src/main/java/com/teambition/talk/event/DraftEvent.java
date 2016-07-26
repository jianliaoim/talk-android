package com.teambition.talk.event;

/**
 * Created by wlanjie on 16/1/5.
 */
public class DraftEvent {

    public String id;
    public String content;

    public DraftEvent(String id, String content) {
        this.id = id;
        this.content = content;
    }
}
