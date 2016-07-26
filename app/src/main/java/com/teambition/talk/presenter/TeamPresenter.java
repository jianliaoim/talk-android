package com.teambition.talk.presenter;

import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.client.data.RepostData;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Team;
import com.teambition.talk.view.RepostView;
import com.teambition.talk.view.TeamView;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zeatual on 14/10/28.
 */
public class TeamPresenter extends BasePresenter {

    private TeamView callback;

    public TeamPresenter(TeamView callback) {
        this.callback = callback;
    }

    public void getTeams() {
        talkApi.getTeams()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Team>>() {
                    @Override
                    public void call(final List<Team> teams) {
                        ArrayList<Team> result = new ArrayList<>();
                        result.addAll(teams);
                        if (teams.isEmpty()) {
                            callback.onEmpty();
                        } else {
                            callback.onGetTeamsFinish(result);
                        }
                    }

                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    public void repostMessage(String messageId, String teamId, String roomId, String userId) {
        talkApi.repostMessage(messageId, new RepostData(teamId, roomId, userId, null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Message>() {
                    @Override
                    public void call(Message message) {
                        if (callback instanceof RepostView) {
                            ((RepostView) callback).onRepostFinish(message);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }

    public void repostFavoritesMessage(String teamId, String roomId, String userId, String[] favoritesId) {
        talkApi.favoritesRepostsMessage(new RepostData(teamId, roomId, userId, favoritesId))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Message>>() {
                    @Override
                    public void call(List<Message> messages) {
                        if (callback instanceof RepostView) {
                            ((RepostView) callback).onRepostFinish(null);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }

    public void getTeamDetail(String teamId) {
        talkApi.getTeamDetail(teamId, "members,rooms")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Team>() {
                    @Override
                    public void call(Team teamInfoResponseData) {
                        if (callback instanceof RepostView) {
                            ((RepostView) callback).onGetTeamDetailFinish(teamInfoResponseData);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }
}
