package com.teambition.talk.event;

import com.teambition.talk.entity.Group;

/**
 * Created by zeatual on 15/12/23.
 */
public class UpdateGroupEvent {

    public Group group;

    public UpdateGroupEvent(Group group) {
        this.group = group;
    }
}
