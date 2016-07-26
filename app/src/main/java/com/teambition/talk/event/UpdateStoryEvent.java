package com.teambition.talk.event;

import com.teambition.talk.entity.Story;

/**
 * Created by wlanjie on 15/12/29.
 */
public class UpdateStoryEvent {
    public Story story;

    public UpdateStoryEvent(Story story) {
        this.story = story;
    }
}
