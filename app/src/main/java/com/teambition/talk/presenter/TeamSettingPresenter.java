package com.teambition.talk.presenter;

import com.teambition.talk.BizLogic;
import com.teambition.talk.Constant;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.rx.EmptyAction;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.client.data.TeamUpdateRequestData;
import com.teambition.talk.entity.Team;
import com.teambition.talk.realm.TeamRealm;
import com.teambition.talk.view.TeamSettingView;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by ZZQ on 4/7/15.
 */
public class TeamSettingPresenter extends BasePresenter {

    private TeamSettingView callback;

    public TeamSettingPresenter(TeamSettingView callback) {
        this.callback = callback;
    }

    public void updateTeam(final String color, final String name) {
        TeamUpdateRequestData data = new TeamUpdateRequestData();
        data.setColor(color);
        data.setName(name);
        talkApi.updateTeam(BizLogic.getTeamId(), data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Team>() {
                    @Override
                    public void call(final Team team) {
                        Team originTeam = (Team) MainApp.PREF_UTIL.getObject(Constant.TEAM, Team.class);
                        if (originTeam == null) {
                            MainApp.PREF_UTIL.putObject(Constant.TEAM, team);
                        } else {
                            originTeam.setColor(color);
                            originTeam.setName(name);
                            MainApp.PREF_UTIL.putObject(Constant.TEAM, originTeam);
                        }
                        Observable.create(new Observable.OnSubscribe<Team>() {
                            @Override
                            public void call(Subscriber<? super Team> subscriber) {
                                TeamRealm.getInstance().addAndUpdateWithCurrentThread(team);
                            }
                        }).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new EmptyAction<Team>(), new RealmErrorAction());
                        callback.onUpdateSuccess(true);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        callback.onUpdateSuccess(false);
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }

    public void leaveTeam() {
        talkApi.unsubscribeTeam(BizLogic.getTeamId()).subscribe();
        talkApi.leaveTeam(BizLogic.getTeamId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        callback.onQuitTeamFinish();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

}
