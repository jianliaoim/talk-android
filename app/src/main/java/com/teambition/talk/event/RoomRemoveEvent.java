package com.teambition.talk.event;

/**
 * Created by nlmartian on 9/8/15.
 */
public class RoomRemoveEvent {
    public String roomId;

    public RoomRemoveEvent(String roomId) {
        this.roomId = roomId;
    }
}
