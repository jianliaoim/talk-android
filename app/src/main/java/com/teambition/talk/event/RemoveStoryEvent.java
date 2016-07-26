package com.teambition.talk.event;

import com.teambition.talk.entity.Story;

/**
 * Created by wlanjie on 15/12/29.
 */
public class RemoveStoryEvent {
    public Story story;

    public RemoveStoryEvent(Story story) {
        this.story = story;
    }
}
