package com.teambition.talk.presenter;

import com.teambition.talk.entity.TeamActivity;
import com.teambition.talk.rx.ApiErrorAction;
import com.teambition.talk.util.Logger;
import com.teambition.talk.view.TeamActivitiesView;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by nlmartian on 2/16/16.
 */
public class TeamActivitiesPresenter extends BasePresenter {

    public static final String TAG = TeamActivitiesPresenter.class.getSimpleName();

    private TeamActivitiesView view;

    public TeamActivitiesPresenter(TeamActivitiesView view) {
        this.view = view;
    }

    public void getTeamActivities(String teamId, final String maxId) {
        view.showProgressBar();
        Observable<List<TeamActivity>> activitiesStream =
                maxId == null ? talkApi.getTeamActivities(teamId, 30)
                        : talkApi.getOldTeamActivities(teamId, 30, maxId);

        activitiesStream.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<TeamActivity>>() {
                    @Override
                    public void call(List<TeamActivity> activities) {
                        view.dismissProgressBar();
                        view.showActivities(activities, maxId == null);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        view.dismissProgressBar();
                        view.showActivitiesFailed();
                        Logger.e(TAG, "get activities error", e);
                    }
                });
    }

    public void removeTeamActivity(final String id) {
        talkApi.deleteActivity(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<TeamActivity>() {
                    @Override
                    public void call(TeamActivity activity) {
                        view.removeActivity(id);
                    }
                }, new ApiErrorAction());
    }
}
