package com.teambition.talk.presenter;

import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.client.data.RoomArchiveRequestData;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Notification;
import com.teambition.talk.entity.Room;
import com.teambition.talk.event.RemoveNotificationEvent;
import com.teambition.talk.event.RoomRemoveEvent;
import com.teambition.talk.event.UpdateRoomEvent;
import com.teambition.talk.realm.MemberRealm;
import com.teambition.talk.realm.NotificationRealm;
import com.teambition.talk.realm.RoomRealm;
import com.teambition.talk.view.TopicSettingView;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by zeatual on 15/3/12.
 */
public class TopicSettingPresenter extends BasePresenter {

    private TopicSettingView callback;

    public TopicSettingPresenter(TopicSettingView callback) {
        this.callback = callback;
    }

    public void getTopicMembers(final String roomId) {
        talkApi.readOneRoom(roomId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Room>() {
                    @Override
                    public void call(final Room room) {
                        if (room != null && room.getMembers() != null && !room.getMembers().isEmpty()) {
                            Observable.create(new Observable.OnSubscribe<List<Member>>() {
                                @Override
                                public void call(Subscriber<? super List<Member>> subscriber) {
                                    List<Member> members = new ArrayList<>(room.getMembers().size());
                                    for (Member member : room.getMembers()) {
                                        if (member != null) {
                                            Member m = MemberRealm.getInstance().getMemberWithCurrentThread(member.get_id());
                                            members.add(m == null ? member : m);
                                        }
                                    }
                                    subscriber.onNext(members);
                                }
                            }).subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Action1<List<Member>>() {
                                        @Override
                                        public void call(List<Member> members) {
                                            room.setMembers(members);
                                            callback.onLoadMembersFinish(room.getMembers());
                                        }
                                    }, new RealmErrorAction());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        System.out.println();
                    }
                });
    }

    public void updateRoom(String roomId, String topicName, String topicGoal, String color) {
        talkApi.updateRoom(roomId, topicName, topicGoal, color)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Room>() {
                    @Override
                    public void call(Room room) {
                        if (room != null) {
                            RoomRealm.getInstance().addOrUpdate(room)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Action1<Room>() {
                                        @Override
                                        public void call(Room room) {
                                            MainApp.IS_ROOM_CHANGED = true;
                                            BusProvider.getInstance().post(new UpdateRoomEvent());
                                        }
                                    }, new RealmErrorAction());
                            callback.onUpdateTopic(room);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        callback.onUpdateTopic(null);
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }

    public void updateVisibility(String roomId, boolean isPrivate) {
        talkApi.updateRoom(roomId, isPrivate)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Room>() {
                    @Override
                    public void call(Room room) {
                        if (room != null) {
                            RoomRealm.getInstance().addOrUpdate(room)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Action1<Room>() {
                                        @Override
                                        public void call(Room room) {
                                            MainApp.IS_ROOM_CHANGED = true;
                                            BusProvider.getInstance().post(new UpdateRoomEvent());
                                        }
                                    }, new RealmErrorAction());
                            callback.onUpdateVisibility(true);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        callback.onUpdateVisibility(false);
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }

    public void leaveRoom(final String roomId) {
        talkApi.leaveRoom(roomId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        Room room = MainApp.globalRooms.get(roomId);
                        room.setIsQuit(true);
                        RoomRealm.getInstance().leave(room, BizLogic.getUserInfo().get_id())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Room>() {
                                    @Override
                                    public void call(Room room) {
                                        MainApp.IS_ROOM_CHANGED = true;
                                        BusProvider.getInstance().post(new UpdateRoomEvent(room));
                                    }
                                }, new RealmErrorAction());
                        updateNotification(roomId);
                        callback.onDropTopic();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }

    public void deleteRoom(final String roomId) {
        talkApi.deleteRoom(roomId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Room>() {
                    @Override
                    public void call(final Room room) {
                        if (room != null) {
                            MainApp.globalRooms.remove(room.get_id());
                            RoomRealm.getInstance().remove(room)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Action1<Room>() {
                                        @Override
                                        public void call(Room r) {
                                            BusProvider.getInstance().post(new RoomRemoveEvent(room.get_id()));
                                        }
                                    }, new RealmErrorAction());
                            updateNotification(roomId);
                            callback.onDropTopic();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }

    public void archiveRoom(final String roomId) {
        talkApi.archiveRoom(roomId, new RoomArchiveRequestData(true))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Room>() {
                    @Override
                    public void call(Room room) {
                        RoomRealm.getInstance().archive(room)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Room>() {
                                    @Override
                                    public void call(Room room) {
                                        MainApp.IS_ROOM_CHANGED = true;
                                    }
                                }, new RealmErrorAction());
                        updateNotification(roomId);
                        callback.onDropTopic();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }

    public void removeMember(final String roomId, final String memberId) {
        talkApi.removeMemberFromRoom(roomId, memberId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        RoomRealm.getInstance().removeRoomMember(roomId, memberId)
                                .subscribe(new Action1<Object>() {
                                    @Override
                                    public void call(Object o) {

                                    }
                                }, new RealmErrorAction());
                        callback.onMemberRemove(memberId);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }

    private void updateNotification(String id) {
        NotificationRealm.getInstance()
                .removeByTargetId(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Notification>() {
                    @Override
                    public void call(Notification notification) {
                        if (notification != null) {
                            BusProvider.getInstance().post(new RemoveNotificationEvent(notification));
                        }
                    }
                }, new RealmErrorAction());
    }
}
