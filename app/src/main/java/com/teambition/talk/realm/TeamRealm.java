package com.teambition.talk.realm;

import com.google.gson.Gson;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.entity.Team;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by wlanjie on 15/10/15.
 */
public class TeamRealm extends AbstractRealm {

    private static TeamRealm realm;
    private final Gson gson;

    private TeamRealm() {
        gson = GsonProvider.getGson();
    }

    public static TeamRealm getInstance() {
        if (realm == null) {
            realm = new TeamRealm();
        }
        return realm;
    }

    public Observable<Team> deleteTeam(final String teamId) {
        return Observable.create(new OnSubscribeRealm<Team>() {
            @Override
            public Team get(Realm realm) {
                Team result = new Team();
                Team localTeam = realm.where(Team.class)
                        .equalTo(Team.ID, teamId)
                        .findFirst();
                if (localTeam != null) {
                    copy(result, localTeam);
                }
                localTeam.removeFromRealm();
                return result;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Team> addOrUpdate(final Team team) {
        return Observable.create(new OnSubscribeRealm<Team>() {
            @Override
            public Team get(Realm realm) {
                copy(team, team);
                realm.copyToRealmOrUpdate(team);
                return team;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<List<Team>> getAllTeams() {
        return Observable.create(new OnSubscribeRealm<List<Team>>() {
            @Override
            public List<Team> get(Realm realm) {
                RealmResults<Team> realmResults = realm.where(Team.class)
                        .findAll();
                List<Team> teamInfoLst = new ArrayList<>();
                for (Team realmResult : realmResults) {
                    Team teamInfo = new Team();
                    copy(teamInfo, realmResult);
                    teamInfoLst.add(teamInfo);
                }
                return teamInfoLst;
            }
        }).subscribeOn(Schedulers.io());
    }

    public List<Team> getTeamWithCurrentThread() {
        final List<Team> teams = new ArrayList<>();
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            RealmResults<Team> realmResults = realm.where(Team.class).findAll();
            for (int i = 0; i < realmResults.size(); i++) {
                Team teamInfo = new Team();
                copy(teamInfo, realmResults.get(i));
                teams.add(teamInfo);
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
        return teams;
    }

    public void addWithCurrentThread(final Team team) {
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            Team teamInfo = realm.createObject(Team.class);
            copy(teamInfo, team);
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
    }

    public void batchAddWithCurrentThread(final List<Team> teams) {
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            final String json = gson.toJson(teams);
            realm.createOrUpdateAllFromJson(Team.class, json);
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
    }

    public Observable<List<Team>> batchAdd(final List<Team> teams) {
        return Observable.create(new OnSubscribeRealm<List<Team>>() {
            @Override
            public List<Team> get(Realm realm) {
                final String json = gson.toJson(teams);
                realm.createOrUpdateAllFromJson(Team.class, json);
                return teams;
            }
        }).subscribeOn(Schedulers.io());
    }

    public void addAndUpdateWithCurrentThread(final Team team) {
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            Team realmTeam = realm.where(Team.class).equalTo(Team.ID, team.get_id()).findFirst();
            if (realmTeam == null) {
                realmTeam = realm.createObject(Team.class);
            }
            copy(realmTeam, team);
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
    }

    public void copy(Team realTeam, Team team) {
        if (team == null) {
            realTeam = null;
            return;
        }
        if (team.get_id() != null) {
            realTeam.set_id(team.get_id());
        }
        if (team.getColor() != null) {
            realTeam.setColor(team.getColor());
        }
        if (team.getName() != null) {
            realTeam.setName(team.getName());
        }
        if (team.getSource() != null) {
            realTeam.setSource(team.getSource());
        }
        if (team.getSourceId() != null) {
            realTeam.setSourceId(team.getSourceId());
        }
        if (team.getSignCode() != null) {
            realTeam.setSignCode(team.getSignCode());
        }
        if (team.getInviteCode() != null) {
            realTeam.setInviteCode(team.getInviteCode());
        }
        if (team.getInviteUrl() != null) {
            realTeam.setInviteUrl(team.getInviteUrl());
        }
        realTeam.setIsQuit(team.isQuit());
        realTeam.setNonJoinable(team.isNonJoinable());
        realTeam.setHasUnread(team.isHasUnread());
        realTeam.setUnread(team.getUnread());
    }
}
