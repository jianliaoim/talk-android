package com.teambition.talk.presenter;

import com.teambition.talk.BusProvider;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.client.data.RoomUpdateRequestData;
import com.teambition.talk.entity.Prefs;
import com.teambition.talk.entity.Room;
import com.teambition.talk.event.UpdateRoomEvent;
import com.teambition.talk.realm.RoomRealm;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by ZZQ on 4/2/15.
 */
public class MutePresenter extends BasePresenter {

    public void updateMute(String roomId, final boolean isMute) {
        Prefs prefs = new Prefs(isMute, null);
        RoomUpdateRequestData data = new RoomUpdateRequestData();
        data.setPrefs(prefs);
        talkApi.updateRoom(roomId, data)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Room>() {
                    @Override
                    public void call(Room room) {
                        if (room != null) {
                            room.setIsMute(isMute);
                            RoomRealm.getInstance().addOrUpdate(room)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Action1<Room>() {
                                        @Override
                                        public void call(Room room) {
                                            MainApp.IS_ROOM_CHANGED = true;
                                            BusProvider.getInstance().post(new UpdateRoomEvent());
                                        }
                                    }, new RealmErrorAction());
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
