package com.teambition.talk.view;

import com.teambition.talk.entity.Team;

import java.util.ArrayList;

/**
 * Created by zeatual on 14/10/28.
 */
public interface TeamView extends BaseView {

    void onEmpty();

    void onGetTeamsFinish(ArrayList<Team> teams);

}
