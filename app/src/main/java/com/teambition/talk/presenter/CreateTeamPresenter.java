package com.teambition.talk.presenter;

import com.teambition.talk.entity.Team;
import com.teambition.talk.view.CreateTeamView;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by zeatual on 14/11/25.
 */
public class CreateTeamPresenter extends BasePresenter {

    private CreateTeamView callback;

    public CreateTeamPresenter(CreateTeamView callback) {
        this.callback = callback;
    }

    public void createTeam(String name) {
        talkApi.createTeam(name)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Team>() {
                    @Override
                    public void call(Team team) {
                        if (team != null) {
                            callback.onCreateTeamFinish(team);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }
}
