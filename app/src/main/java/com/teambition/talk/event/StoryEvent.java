package com.teambition.talk.event;

import java.util.List;

/**
 * Created by wlanjie on 15/11/18.
 */
public class StoryEvent {

    public List<String> memberIds;

    public String memberId;

    public StoryEvent(List<String> memberIds) {
        this.memberIds = memberIds;
    }

    public StoryEvent(String memberId) {
        this.memberId = memberId;
    }
}
