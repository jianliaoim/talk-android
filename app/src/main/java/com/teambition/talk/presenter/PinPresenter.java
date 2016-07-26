package com.teambition.talk.presenter;

import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Room;
import com.teambition.talk.event.UpdateMemberEvent;
import com.teambition.talk.event.UpdateRoomEvent;
import com.teambition.talk.realm.MemberRealm;
import com.teambition.talk.realm.RoomRealm;

import java.util.Date;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by ZZQ on 3/20/15.
 */
public class PinPresenter extends BasePresenter {

    public void pin(final String targetId) {
        talkApi.pin(BizLogic.getTeamId(), targetId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if (MainApp.globalRooms.containsKey(targetId)) {
                            Room room = MainApp.globalRooms.get(targetId);
                            room.setPinnedAt(new Date());
                            RoomRealm.getInstance().addOrUpdate(room)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Action1<Room>() {
                                        @Override
                                        public void call(Room room) {
                                            MainApp.IS_ROOM_CHANGED = true;
                                            BusProvider.getInstance().post(new UpdateRoomEvent());
                                        }
                                    }, new RealmErrorAction());
                            BusProvider.getInstance().post(new UpdateRoomEvent());
                            MainApp.IS_ROOM_CHANGED = true;
                            MainApp.globalRooms.put(targetId, room);
                        } else if (MainApp.globalMembers.containsKey(targetId)) {
                            Member member = MainApp.globalMembers.get(targetId);
                            member.setPinnedAt(new Date());
                            MemberRealm.getInstance().addOrUpdate(member)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Action1<Member>() {
                                        @Override
                                        public void call(Member member) {

                                        }
                                    }, new RealmErrorAction());
                            BusProvider.getInstance().post(new UpdateMemberEvent());
                            MainApp.globalMembers.put(targetId, member);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }

    public void unpin(final String targetId) {
        talkApi.unpin(BizLogic.getTeamId(), targetId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if (MainApp.globalRooms.containsKey(targetId)) {
                            Room room = MainApp.globalRooms.get(targetId);
                            room.setPinnedAt(null);
                            RoomRealm.getInstance().addOrUpdate(room)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Action1<Room>() {
                                        @Override
                                        public void call(Room room) {
                                            MainApp.IS_ROOM_CHANGED = true;
                                            BusProvider.getInstance().post(new UpdateRoomEvent());
                                        }
                                    }, new RealmErrorAction());
                            BusProvider.getInstance().post(new UpdateRoomEvent());
                            MainApp.IS_ROOM_CHANGED = true;
                            MainApp.globalRooms.put(targetId, room);
                        } else if (MainApp.globalMembers.containsKey(targetId)) {
                            Member member = MainApp.globalMembers.get(targetId);
                            member.setPinnedAt(null);
                            MemberRealm.getInstance().addOrUpdate(member)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Action1<Member>() {
                                        @Override
                                        public void call(Member member) {

                                        }
                                    }, new RealmErrorAction());
                            BusProvider.getInstance().post(new UpdateMemberEvent());
                            MainApp.globalMembers.put(targetId, member);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }

}
