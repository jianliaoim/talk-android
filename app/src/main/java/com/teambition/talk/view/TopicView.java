package com.teambition.talk.view;

import com.teambition.talk.entity.Room;

import java.util.List;

/**
 * Created by zeatual on 14/10/31.
 */
public interface TopicView extends BaseView {

    void onLoadRoomsFinish(List<Room> roomsJoined, List<Room> roomsToJoin,
                           List<Room> roomsPinned);

}
