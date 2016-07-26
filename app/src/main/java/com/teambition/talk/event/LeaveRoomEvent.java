package com.teambition.talk.event;

/**
 * Created by nlmartian on 9/2/15.
 */
public class LeaveRoomEvent {
    public String roomId;

    public LeaveRoomEvent(String roomId) {
        this.roomId = roomId;
    }
}
