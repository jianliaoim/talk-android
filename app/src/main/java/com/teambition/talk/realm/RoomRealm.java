package com.teambition.talk.realm;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.teambition.common.PinyinUtil;
import com.teambition.talk.BizLogic;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Room;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by wlanjie on 15/10/13.
 */
public class RoomRealm extends AbstractRealm {

    private static RoomRealm realm;

    private final Gson gson;

    private RoomRealm() {
        gson = GsonProvider.getGson();
    }

    public static RoomRealm getInstance() {
        if (realm == null) {
            realm = new RoomRealm();
        }
        return realm;
    }

    public List<Room> getRoomWithCurrentThread() {
        final List<Room> rooms = new ArrayList<>();
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            RealmResults<Room> realmResults = realm.where(Room.class)
                    .equalTo(Room.TEAM_ID, BizLogic.getTeamId())
                    .findAll();
            realmResults.sort(Room.PINYIN, Sort.ASCENDING);
            for (int i = 0; i < realmResults.size(); i++) {
                Room room = new Room();
                copy(room, realmResults.get(i));
                rooms.add(room);
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
        return rooms;
    }

    /**
     * 查询不是退出,不是归档
     *
     * @return 查询到的所有数据
     */
    public List<Room> getRoomOnNotQuitOnNotArchivedWithCurrentThread() {
        final List<Room> rooms = new ArrayList<>();
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            RealmResults<Room> realmResults = realm.where(Room.class)
                    .equalTo(Room.TEAM_ID, BizLogic.getTeamId())
                    .equalTo(Room.IS_QUIT, false)
                    .equalTo(Room.IS_ARCHIVED, false)
                    .findAll();
            realmResults.sort(Room.PINYIN, Sort.ASCENDING);
            for (int i = 0; i < realmResults.size(); i++) {
                Room room = new Room();
                copy(room, realmResults.get(i));
                rooms.add(room);
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
        return rooms;
    }

    /**
     * 查询所有不是归档的Room
     *
     * @return 查询到的所有数据
     */
    public List<Room> getRoomOnNotArchivedWithCurrentThread() {
        List<Room> rooms = new ArrayList<>();
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            RealmResults<Room> realmResults = realm.where(Room.class)
                    .equalTo(Room.TEAM_ID, BizLogic.getTeamId())
                    .equalTo(Room.IS_ARCHIVED, false)
                    .findAll();
            realmResults.sort(Room.PINYIN, Sort.ASCENDING);
            for (int i = 0; i < realmResults.size(); i++) {
                Room room = new Room();
                copy(room, realmResults.get(i));
                rooms.add(room);
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
        return rooms;
    }

    /**
     * 查询所有归档的Room
     *
     * @return 查询到的所有数据
     */
    public List<Room> getRoomOnArchivedWithCurrentThread() {
        List<Room> rooms = new ArrayList<>();
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            RealmResults<Room> realmResults = realm.where(Room.class)
                    .equalTo(Room.TEAM_ID, BizLogic.getTeamId())
                    .equalTo(Room.IS_ARCHIVED, true)
                    .findAll();
            realmResults.sort(Room.PINYIN, Sort.ASCENDING);
            for (int i = 0; i < realmResults.size(); i++) {
                Room room = new Room();
                copy(room, realmResults.get(i));
                rooms.add(room);
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
        return rooms;
    }

    public Observable<List<Room>> getJoinedRooms() {
        return Observable.create(new OnSubscribeRealm<List<Room>>() {
            @Override
            public List<Room> get(Realm realm) {
                List<Room> rooms = new ArrayList<>();
                RealmResults<Room> realmResults = realm.where(Room.class)
                        .equalTo(Room.TEAM_ID, BizLogic.getTeamId())
                        .equalTo(Room.IS_ARCHIVED, false)
                        .equalTo(Room.IS_QUIT, false)
                        .findAll();
                realmResults.sort(Room.PINYIN, Sort.ASCENDING);
                for (int i = 0; i < realmResults.size(); i++) {
                    Room room = new Room();
                    copy(room, realmResults.get(i));
                    rooms.add(room);
                }
                return rooms;
            }
        });
    }

    public Observable<List<Room>> getToJoinRooms() {
        return Observable.create(new OnSubscribeRealm<List<Room>>() {
            @Override
            public List<Room> get(Realm realm) {
                List<Room> rooms = new ArrayList<>();
                RealmResults<Room> realmResults = realm.where(Room.class)
                        .equalTo(Room.TEAM_ID, BizLogic.getTeamId())
                        .equalTo(Room.IS_ARCHIVED, false)
                        .equalTo(Room.IS_QUIT, true)
                        .findAll();
                realmResults.sort(Room.PINYIN, Sort.ASCENDING);
                for (int i = 0; i < realmResults.size(); i++) {
                    Room room = new Room();
                    copy(room, realmResults.get(i));
                    rooms.add(room);
                }
                return rooms;
            }
        });
    }

    public Observable<Object> removeRoomMember(final String roomId, final String memberId) {
        return Observable.create(new OnSubscribeRealm<Object>() {
            @Override
            public Object get(Realm realm) {
                Room realmRoom = realm.where(Room.class)
                        .equalTo(Room.TEAM_ID, BizLogic.getTeamId())
                        .equalTo(Room.ID, roomId)
                        .findFirst();
                if (realmRoom == null) return null;
                final List<String> memberIds = gson.fromJson(realmRoom.get_memberJsonIds(), new TypeToken<List<String>>() {
                }.getType());
                if (memberIds != null && !memberIds.isEmpty()) {
                    Iterator<String> iterator = memberIds.iterator();
                    while (iterator.hasNext()) {
                        String mId = iterator.next();
                        if (mId.equalsIgnoreCase(memberId)) {
                            iterator.remove();
                            break;
                        }
                    }
                }
                return realmRoom;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Room> updateGeneralRoom(final Member member) {
        return Observable.create(new OnSubscribeRealm<Room>() {
            @Override
            public Room get(Realm realm) {
                Room realmRoom = realm.where(Room.class)
                        .equalTo(Room.TEAM_ID, BizLogic.getTeamId())
                        .equalTo(Room.IS_GENERAL, true)
                        .findFirst();
                if (realmRoom == null) return null;
                List<String> memberIds = gson.fromJson(realmRoom.get_memberJsonIds(), new TypeToken<List<String>>() {
                }.getType());
                memberIds.add(member.get_id());
                realmRoom.set_memberJsonIds(gson.toJson(memberIds));
                return realmRoom;
            }
        }).subscribeOn(Schedulers.io());
    }

    public void addOrUpdateWithCurrentThread(final Room room) {
        if (room == null) return;
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            copy(room, room);
            if (room.getIsGeneral()) {
                room.setTopic(MainApp.CONTEXT.getString(R.string.general));
            }
            realm.copyToRealmOrUpdate(room);
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
    }

    public Room getRoomWithCurrentThread(final String roomId) {
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            Room realmRoom = realm.where(Room.class)
                    .equalTo(Room.TEAM_ID, BizLogic.getTeamId())
                    .equalTo(Room.ID, roomId)
                    .findFirst();
            if (realmRoom == null) return null;
            Room room = new Room();
            copy(room, realmRoom);
            realm.commitTransaction();
            return room;
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
        return null;
    }

    public void leaveWithCurrentThread(final Room room, final String memberId) {
        if (room == null) return;
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            if (BizLogic.isMe(memberId)) {
                Room realmRoom = realm.where(Room.class)
                        .equalTo(Room.TEAM_ID, BizLogic.getTeamId())
                        .equalTo(Room.ID, room.get_id())
                        .findFirst();
                realmRoom.setIsArchived(true);
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
    }

    public Observable<Room> getRoom(final String roomId) {
        return Observable.create(new OnSubscribeRealm<Room>() {
            @Override
            public Room get(Realm realm) {
                Room realmRoom = realm.where(Room.class)
                        .equalTo(Room.TEAM_ID, BizLogic.getTeamId())
                        .equalTo(Room.ID, roomId)
                        .findFirst();
                if (realmRoom == null) return null;
                Room room = new Room();
                copy(room, realmRoom);
                return room;
            }
        }).subscribeOn(Schedulers.io());
    }

    public void batchAddWithCurrentThread(final List<Room> rooms) {
        final List<Room> realmRooms = new ArrayList<>(rooms.size());
        Realm realm = RealmProvider.getInstance();
        try {
            for (Room room : rooms) {
                Room realmRoom = new Room();
                copy(realmRoom, room);
                realmRooms.add(realmRoom);
            }
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(realmRooms);
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
    }

    public Observable<List<Room>> batchAdd(final List<Room> rooms) {
        return Observable.create(new OnSubscribeRealm<List<Room>>() {
            @Override
            public List<Room> get(Realm realm) {
                List<Room> realmRooms = new ArrayList<>(rooms.size());
                for (Room room : rooms) {
                    Room realmRoom = new Room();
                    copy(realmRoom, room);
                    if (room.getCreatedAt() != null) {
                        room.setCreatedAtTime(room.getCreatedAt().getTime());
                    }
                    if (room.getPinnedAt() != null) {
                        room.setPinnedAtTime(room.getPinnedAt().getTime());
                    }
                    if (room.getIsGeneral() != null && room.getIsGeneral()) {
                        room.setTopic(MainApp.CONTEXT.getString(R.string.general));
                    }
                    realmRooms.add(realmRoom);
                }
                realm.copyToRealmOrUpdate(realmRooms);
                return rooms;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Room> addOrUpdate(final Room room) {
        return Observable.create(new OnSubscribeRealm<Room>() {
            @Override
            public Room get(Realm realm) {
                copy(room, room);
                if (room.getIsGeneral()) {
                    room.setTopic(MainApp.CONTEXT.getString(R.string.general));
                }
                realm.copyToRealmOrUpdate(room);
                return room;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Room> updateRoomMemberIds(final String roomId, final List<String> memberIds) {
        return Observable.create(new OnSubscribeRealm<Room>() {
            @Override
            public Room get(Realm realm) {
                final Room realmRoom = realm.where(Room.class)
                        .equalTo(Room.ID, roomId)
                        .equalTo(Room.TEAM_ID, BizLogic.getTeamId())
                        .findFirst();
                if (realmRoom == null) return null;
                List<String> realmMemberIds = realmRoom.get_memberIds();
                if (realmMemberIds != null) {
                    realmMemberIds.addAll(memberIds);
                    realmRoom.set_memberJsonIds(gson.toJson(realmMemberIds));
                } else {
                    realmRoom.set_memberJsonIds(gson.toJson(memberIds));
                }
                return realmRoom;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Object> clearUnread(final String roomId) {
        return Observable.create(new OnSubscribeRealm<Object>() {
            @Override
            public Object get(Realm realm) {
                Room realmRoom = realm.where(Room.class)
                        .equalTo(Room.ID, roomId)
                        .equalTo(Room.TEAM_ID, BizLogic.getTeamId())
                        .findFirst();
                if (realmRoom == null) return null;
                realmRoom.setUnread(0);
                return realmRoom;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Room> remove(final String roomId) {
        return Observable.create(new OnSubscribeRealm<Room>() {
            @Override
            public Room get(Realm realm) {
                Room realmRoom = realm.where(Room.class)
                        .equalTo(Room.TEAM_ID, BizLogic.getTeamId())
                        .equalTo(Room.ID, roomId)
                        .findFirst();
                if (realmRoom == null) return null;
                realmRoom.removeFromRealm();
                Room room = new Room();
                copy(room, realmRoom);
                return room;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Room> remove(final Room room) {
        return Observable.create(new OnSubscribeRealm<Room>() {
            @Override
            public Room get(Realm realm) {
                Room realmRoom = realm.where(Room.class)
                        .equalTo(Room.TEAM_ID, BizLogic.getTeamId())
                        .equalTo(Room.ID, room.get_id())
                        .findFirst();
                if (realmRoom == null) return null;
                Room r = new Room();
                copy(r, realmRoom);
                realmRoom.removeFromRealm();
                return r;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Room> archive(final Room room) {
        return Observable.create(new OnSubscribeRealm<Room>() {
            @Override
            public Room get(Realm realm) {
                Room realmRoom = realm.where(Room.class)
                        .equalTo(Room.TEAM_ID, BizLogic.getTeamId())
                        .equalTo(Room.ID, room.get_id())
                        .findFirst();
                if (realmRoom == null) return null;
                realmRoom.setIsArchived(true);
                return room;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Room> leave(final Room room, final String memberId) {
        return Observable.create(new OnSubscribeRealm<Room>() {
            @Override
            public Room get(Realm realm) {
                if (BizLogic.isMe(memberId)) {
                    Room realmRoom = realm.where(Room.class)
                            .equalTo(Room.TEAM_ID, BizLogic.getTeamId())
                            .equalTo(Room.ID, room.get_id())
                            .findFirst();
                    if (realmRoom == null) return null;
                    realmRoom.setIsQuit(true);
                    return room;
                }
                return null;
            }
        }).subscribeOn(Schedulers.io());
    }

    public void copy(Room realmRoom, Room room) {
        if (realmRoom == null || room == null) return;
        if (room.get_id() != null) {
            realmRoom.set_id(room.get_id());
        }
        if (room.getTopic() != null) {
            realmRoom.setTopic(room.getTopic());
        }
        if (room.get_creatorId() != null) {
            realmRoom.set_creatorId(room.get_creatorId());
        }
        if (room.get_teamId() != null) {
            realmRoom.set_teamId(room.get_teamId());
        }
        if (room.getCreatedAt() != null) {
            realmRoom.setCreatedAtTime(room.getCreatedAt().getTime());
        }
        if (room.getCreatedAtTime() != 0) {
            realmRoom.setCreatedAt(new Date(room.getCreatedAtTime()));
        }
        if (room.getIsPrivate() != null) {
            realmRoom.setIsPrivate(room.getIsPrivate());
        }
        if (room.getIsArchived() != null) {
            realmRoom.setIsArchived(room.getIsArchived());
        }
        if (room.getIsGeneral() != null) {
            realmRoom.setIsGeneral(room.getIsGeneral());
        }
        if (room.getIsQuit() != null) {
            realmRoom.setIsQuit(room.getIsQuit());
        }
        if (room.getPurpose() != null) {
            realmRoom.setPurpose(room.getPurpose());
        }
        if (room.getUnread() != null) {
            realmRoom.setUnread(room.getUnread());
        }
        if (room.getColor() != null) {
            realmRoom.setColor(room.getColor());
        }
        if (room.getPinnedAt() != null) {
            realmRoom.setPinnedAtTime(room.getPinnedAt().getTime());
        }
        if (room.getPinnedAtTime() != 0) {
            realmRoom.setPinnedAt(new Date(room.getPinnedAtTime()));
        }
        if (room.getIsMute() != null) {
            realmRoom.setIsMute(room.getIsMute());
        }
        if (room.getIsGeneral() != null && room.getIsGeneral()) {
            realmRoom.setTopic(MainApp.CONTEXT.getString(R.string.general));
        }
        if (room.get_memberJsonIds() != null) {
            final List<String> memberIds = gson.fromJson(room.get_memberJsonIds(),
                    new TypeToken<List<String>>() {
                    }.getType());
            realmRoom.set_memberIds(memberIds);
        }
        if (room.get_memberIds() != null) {
            realmRoom.set_memberJsonIds(gson.toJson(room.get_memberIds()));
        }
    }
}
