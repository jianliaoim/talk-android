package com.teambition.talk.event;

import com.teambition.talk.entity.TeamActivity;

/**
 * Created by nlmartian on 3/9/16.
 */
public class RemoveTeamActivityEvent {
    public TeamActivity activity;

    public RemoveTeamActivityEvent(TeamActivity activity) {
        this.activity = activity;
    }
}
