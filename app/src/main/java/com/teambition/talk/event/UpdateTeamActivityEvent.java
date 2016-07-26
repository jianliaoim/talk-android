package com.teambition.talk.event;

import com.teambition.talk.entity.TeamActivity;

/**
 * Created by nlmartian on 3/9/16.
 */
public class UpdateTeamActivityEvent {
    public TeamActivity activity;

    public UpdateTeamActivityEvent(TeamActivity activity) {
        this.activity = activity;
    }
}
