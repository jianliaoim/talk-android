package com.teambition.talk.realm;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.teambition.talk.BizLogic;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.entity.Group;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by zeatual on 15/12/23.
 */
public class GroupRealm extends AbstractRealm {

    private static GroupRealm realm;

    private final Gson gson;

    private GroupRealm() {
        gson = GsonProvider.getGson();
    }

    public static GroupRealm getInstance() {
        if (realm == null) {
            realm = new GroupRealm();
        }
        return realm;
    }

    public Observable<List<Group>> getAllGroups() {
        return Observable.create(new OnSubscribeRealm<List<Group>>() {
            @Override
            public List<Group> get(Realm realm) {
                List<Group> results = new ArrayList<>();
                RealmResults<Group> groups = realm.where(Group.class)
                        .equalTo(Group.TEAM_ID, BizLogic.getTeamId())
                        .findAll();
                for (Group group : groups) {
                    Group result = new Group();
                    copy(result, group);
                    results.add(result);
                }
                return results;
            }
        }).subscribeOn(Schedulers.io());
    }

    public List<Group> getAllGroupsWithCurrentThread() {
        List<Group> results = new ArrayList<>();
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            RealmResults<Group> groups = realm.where(Group.class)
                    .equalTo(Group.TEAM_ID, BizLogic.getTeamId())
                    .findAll();
            for (Group group : groups) {
                Group result = new Group();
                copy(result, group);
                results.add(result);
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
        return results;
    }

    public Observable<Object> remove(final Group group) {
        return Observable.create(new OnSubscribeRealm<Object>() {
            @Override
            public Object get(Realm realm) {
                Group realmGroup = realm.where(Group.class)
                        .equalTo(Group.ID, group.get_id())
                        .findFirst();
                if (realmGroup != null) {
                    realmGroup.removeFromRealm();
                }
                return realmGroup;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Group> addOrUpdate(final Group group) {
        return Observable.create(new OnSubscribeRealm<Group>() {
            @Override
            public Group get(Realm realm) {
                copy(group, group);
                realm.copyToRealmOrUpdate(group);
                return group;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<List<Group>> batchAdd(final List<Group> groups) {
        return Observable.create(new OnSubscribeRealm<List<Group>>() {
            @Override
            public List<Group> get(Realm realm) {
                List<Group> realmRooms = new ArrayList<>(groups.size());
                for (Group group : groups) {
                    Group realmGroup = new Group();
                    copy(realmGroup, group);
                    if (group.getCreatedAt() != null) {
                        group.setCreatedAtTime(group.getCreatedAt().getTime());
                    }
                    realmRooms.add(realmGroup);
                }

                realm.copyToRealmOrUpdate(realmRooms);
                return groups;
            }
        }).subscribeOn(Schedulers.io());
    }

    public void copy(Group a, Group b) {
        if (b.get_id() != null) {
            a.set_id(b.get_id());
        }
        if (b.get_teamId() != null) {
            a.set_teamId(b.get_teamId());
        }
        if (b.getName() != null) {
            a.setName(b.getName());
        }
        if (b.get_creatorId() != null) {
            a.set_creatorId(b.get_creatorId());
        }
        if (b.getCreatedAt() != null) {
            a.setCreatedAtTime(b.getCreatedAt().getTime());
        }
        if (b.getCreatedAtTime() != 0) {
            a.setCreatedAt(new Date(b.getCreatedAtTime()));
        }
        if (b.get_memberJsonIds() != null) {
            final List<String> memberIds = gson.fromJson(b.get_memberJsonIds(),
                    new TypeToken<List<String>>() {
                    }.getType());
            a.set_memberIds(memberIds);
        }
        if (b.get_memberIds() != null) {
            a.set_memberJsonIds(gson.toJson(b.get_memberIds()));
        }
    }

}
