package com.teambition.talk.presenter;

import android.util.Base64;

import com.google.gson.JsonObject;
import com.teambition.talk.BizLogic;
import com.teambition.talk.Constant;
import com.teambition.talk.MainApp;
import com.teambition.talk.NotificationConfig;
import com.teambition.talk.client.ApiConfig;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.data.StrikerTokenResponseData;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Room;
import com.teambition.talk.entity.Team;
import com.teambition.talk.entity.User;
import com.teambition.talk.realm.MemberRealm;
import com.teambition.talk.realm.RoomRealm;
import com.teambition.talk.realm.TeamRealm;
import com.teambition.talk.rx.EmptyAction;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.util.Logger;
import com.teambition.talk.view.HomeView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zeatual on 14/10/30.
 */
public class HomePresenter extends BasePresenter {
    public static final String TAG = HomePresenter.class.getSimpleName();

    private HomeView callback;

    public HomePresenter(HomeView callback) {
        this.callback = callback;
    }

    public void syncUser() {
        talkApi.getUser().observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        if (user.getPreference() == null) {
                            User u = (User) MainApp.PREF_UTIL.getObject(Constant.USER, User.class);
                            if (u != null) {
                                user.setPreference(u.getPreference());
                            }
                        }
                        MainApp.PREF_UTIL.putObject(Constant.USER, user);
                        if (user.getPreference().isNotifyOnRelated()) {
                            MainApp.PREF_UTIL.putInt(Constant.NOTIFY_PREF,
                                    NotificationConfig.NOTIFICATION_ONLY_MENTION);
                        }
                        callback.onLoadUserFinish(user);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                    }
                });
    }

    public void getStrikerToken() {
        talkApi.getStrikerToken().observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<StrikerTokenResponseData>() {
                    @Override
                    public void call(StrikerTokenResponseData responseData) {
                        if (responseData != null) {
                            MainApp.PREF_UTIL.putString(Constant.STRIKER_TOKEN, responseData.getToken());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.e(TAG, "fail to get file auth key", throwable);
                    }
                });
    }

    public void getUser() {
        User user = BizLogic.getUserInfo();
        if (user != null) {
            callback.onLoadUserFinish(user);
        }
    }

    public void getTeams() {
        TeamRealm.getInstance().getAllTeams()
                .flatMap(new Func1<List<Team>, Observable<Team>>() {
                    @Override
                    public Observable<Team> call(List<Team> teamInfos) {
                        return Observable.from(teamInfos);
                    }
                }).filter(new Func1<Team, Boolean>() {
                    @Override
                    public Boolean call(Team teamInfo) {
                        return teamInfo != null;
                    }
                })
                .toList()
                .onErrorReturn(new Func1<Throwable, List<Team>>() {
                    @Override
                    public List<Team> call(Throwable throwable) {
                        return Collections.emptyList();
                    }
                }).concatWith(talkApi.getTeams().doOnNext(new Action1<List<Team>>() {
                    @Override
                    public void call(List<Team> teams) {
                        TeamRealm.getInstance()
                            .batchAdd(teams)
                            .subscribe(new EmptyAction(), new RealmErrorAction());
                }
                }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<List<Team>>() {
                    @Override
                    public void call(List<Team> teams) {
                        if (!teams.isEmpty()) {
                            Collections.sort(teams, new Comparator<Team>() {
                                @Override
                                public int compare(Team lhs, Team rhs) {
                                    return rhs.getUnread() - lhs.getUnread();
                                }
                            });
                            callback.onLoadTeamFinish(teams);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.d("get teams", "failed");
                    }
                });
    }

    public void getCachedTeams() {
        TeamRealm.getInstance().getAllTeams()
                .flatMap(new Func1<List<Team>, Observable<Team>>() {
                    @Override
                    public Observable<Team> call(List<Team> teamInfos) {
                        return Observable.from(teamInfos);
                    }
                }).filter(new Func1<Team, Boolean>() {
            @Override
            public Boolean call(Team teamInfo) {
                return teamInfo != null;
            }
        }).toList()
            .onErrorReturn(new Func1<Throwable, List<Team>>() {
                @Override
                public List<Team> call(Throwable throwable) {
                    return Collections.emptyList();
                }
            }).observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(new Action1<List<Team>>() {
                @Override
                public void call(List<Team> teams) {
                    if (!teams.isEmpty()) {
                        Collections.sort(teams, new Comparator<Team>() {
                            @Override
                            public int compare(Team lhs, Team rhs) {
                                return rhs.getUnread() - lhs.getUnread();
                            }
                        });
                        callback.onLoadTeamFinish(teams);
                    }
                }
            });
    }

    public void unsubscribeTeam(String teamId) {
        talkApi.unsubscribeTeam(teamId).subscribeOn(Schedulers.io()).subscribe();
    }
}
