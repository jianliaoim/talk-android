package com.teambition.talk.presenter;

import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.Preference;
import com.teambition.talk.view.SettingView;

import java.util.TimeZone;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by zeatual on 15/2/2.
 */
public class SettingPresenter extends BasePresenter {

    private SettingView callback;

    public SettingPresenter(SettingView callback) {
        this.callback = callback;
    }

    public void updateEmailNotification(boolean isOn) {
        talkApi.updateEmailNotification(isOn)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Preference>() {
                    @Override
                    public void call(Preference preference) {
                        callback.onEmailNotificationUpdate(true);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                        callback.onEmailNotificationUpdate(false);
                    }
                });
    }

    public void updateNotifyOnRelated(final boolean isOn) {
        talkApi.updateNotifyOnRelated(isOn)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Preference>() {
                    @Override
                    public void call(Preference preference) {
                        callback.onNotifyOnRelatedUpdate(isOn, true);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                        callback.onNotifyOnRelatedUpdate(isOn, false);
                    }
                });
    }

    public void updatePushOnWorkTime(final boolean isOn) {
        TimeZone timeZone = TimeZone.getDefault();
        talkApi.updatePushOnWorkTime(isOn, timeZone.getID())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Preference>() {
                    @Override
                    public void call(Preference preference) {
                        callback.onPushOnWorkTimeUpdate(true);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                        callback.onPushOnWorkTimeUpdate(false);
                    }
                });
    }
}
