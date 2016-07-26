package com.teambition.talk.view;

import com.teambition.talk.entity.TeamActivity;

import java.util.List;

/**
 * Created by nlmartian on 2/16/16.
 */
public interface TeamActivitiesView extends BaseView {

    void showActivities(List<TeamActivity> activities, boolean refresh);

    void showActivitiesFailed();

    void removeActivity(String id);
}
