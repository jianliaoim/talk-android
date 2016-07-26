package com.teambition.talk.event;

import com.teambition.talk.entity.Group;

/**
 * Created by zeatual on 15/12/23.
 */
public class RemoveGroupEvent {

    public Group group;

    public RemoveGroupEvent(Group group) {
        this.group = group;
    }
}
