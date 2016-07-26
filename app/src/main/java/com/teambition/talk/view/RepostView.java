package com.teambition.talk.view;

import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Team;

/**
 * Created by wlanjie on 15/8/3.
 */
public interface RepostView extends TeamView {

    void onRepostFinish(Message message);

    void onGetTeamDetailFinish(Team data);
}
