package com.teambition.talk.event;

import com.teambition.talk.entity.TeamActivity;

/**
 * Created by nlmartian on 3/9/16.
 */
public class NewTeamActivityEvent {
    public TeamActivity activity;

    public NewTeamActivityEvent(TeamActivity activity) {
        this.activity = activity;
    }
}
