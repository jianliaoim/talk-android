package com.teambition.talk.event;

import com.teambition.talk.entity.Room;

/**
 * Created by zeatual on 11/13/14.
 */
public class UpdateRoomEvent {

    public Room room;

    public UpdateRoomEvent() {
    }

    public UpdateRoomEvent(Room room) {
        this.room = room;
    }
}
