package com.teambition.talk.presenter;

import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.MainApp;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.client.data.RoomArchiveRequestData;
import com.teambition.talk.entity.Room;
import com.teambition.talk.event.UpdateRoomEvent;
import com.teambition.talk.realm.RoomRealm;
import com.teambition.talk.view.ArchivedTopicView;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by zeatual on 15/3/16.
 */
public class ArchivedTopicPresenter extends BasePresenter {

    private ArchivedTopicView callback;

    public ArchivedTopicPresenter(ArchivedTopicView callback) {
        this.callback = callback;
    }

    public void getArchivedRooms() {
        Observable.create(new Observable.OnSubscribe<List<Room>>() {
            @Override
            public void call(Subscriber<? super List<Room>> subscriber) {
                List<Room> rooms = RoomRealm.getInstance().getRoomOnArchivedWithCurrentThread();
                subscriber.onNext(rooms);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Room>>() {
                    @Override
                    public void call(List<Room> rooms) {
                        callback.onLoadArchivedRoomsFinish(rooms);
                    }
                });
    }

    public void syncArchivedRooms() {
        talkApi.getArchivedRoom(BizLogic.getTeamId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Room>>() {
                    @Override
                    public void call(List<Room> rooms) {
                        for (Room room : rooms) {
                            MainApp.globalRooms.put(room.get_id(), room);
                        }
                        RoomRealm.getInstance().batchAdd(rooms);
                        callback.onLoadArchivedRoomsFinish(rooms);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    public void undoArchive(final Room room) {
        talkApi.archiveRoom(room.get_id(), new RoomArchiveRequestData(false))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Room>() {
                    @Override
                    public void call(Room room) {
                        RoomRealm.getInstance().addOrUpdate(room)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Room>() {
                                    @Override
                                    public void call(Room room) {
                                        MainApp.IS_ROOM_CHANGED = true;
                                        BusProvider.getInstance().post(new UpdateRoomEvent());
                                    }
                                }, new RealmErrorAction());
                        callback.onUndoArchiveFinish(room);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }
}
