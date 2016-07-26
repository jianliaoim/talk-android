package com.teambition.talk.view;

import com.teambition.talk.entity.Team;
import com.teambition.talk.entity.User;

import java.util.List;

/**
 * Created by zeatual on 14/10/30.
 */
public interface HomeView extends BaseView {

    void onLoadUserFinish(User user);

    void onLoadTeamFinish(List<Team> teams);

    void onValidateFail();
}
