package com.teambition.talk.view;

import com.teambition.talk.entity.Room;

import java.util.List;

/**
 * Created by zeatual on 15/3/16.
 */
public interface ArchivedTopicView extends BaseView {

    void onLoadArchivedRoomsFinish(List<Room> rooms);

    void onUndoArchiveFinish(Room room);

}
