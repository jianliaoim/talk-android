package com.teambition.talk.view;

import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Room;

import java.util.List;

/**
 * Created by zeatual on 15/3/12.
 */
public interface TopicSettingView extends BaseView {

    void onUpdateTopic(Room room);

    void onUpdateVisibility(boolean isSuccess);

    void onMemberRemove(String memberId);

    // delete, archive, quit
    void onDropTopic();
    void onLoadMembersFinish(List<Member> members);

}
