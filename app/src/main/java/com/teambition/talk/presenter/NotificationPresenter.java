package com.teambition.talk.presenter;

import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.Constant;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.client.data.UpdateNotificationRequestData;
import com.teambition.talk.entity.Notification;
import com.teambition.talk.entity.Preference;
import com.teambition.talk.entity.User;
import com.teambition.talk.entity.WebState;
import com.teambition.talk.event.RemoveNotificationEvent;
import com.teambition.talk.realm.NotificationRealm;
import com.teambition.talk.rx.ApiErrorAction;
import com.teambition.talk.rx.EmptyAction;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.util.Connectivity;
import com.teambition.talk.util.Logger;
import com.teambition.talk.view.NotificationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by zeatual on 15/10/13.
 */
public class NotificationPresenter extends BasePresenter {
    public static final int WEB_ONLINE_STATE_INTERVAL = 60;

    private NotificationView callback;

    public NotificationPresenter(NotificationView callback) {
        this.callback = callback;
    }

    public void initNotifications(final boolean forceRefresh) {
        if (MainApp.globalMembers.isEmpty()) {
            initNotificationsAfterMemberInit(forceRefresh);
            return;
        }
        Observable<List<Notification>> networkStream = talkApi.getNotifications(BizLogic.getTeamId(), null, null)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<List<Notification>>() {
                    @Override
                    public void call(List<Notification> notifications) {
                        NotificationRealm.getInstance().batchAdd(notifications)
                                .subscribe(new EmptyAction<List<Notification>>(),
                                        new RealmErrorAction());
                    }
                });
        Observable<List<Notification>> cachedStream =
                forceRefresh ? Observable.<List<Notification>>empty() : NotificationRealm.getInstance().getNotifications(20);
        cachedStream.onErrorReturn(new Func1<Throwable, List<Notification>>() {
            @Override
            public List<Notification> call(Throwable throwable) {
                return Collections.emptyList();
            }
        });
        cachedStream.concatWith(networkStream)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Notification>>() {
                    @Override
                    public void call(List<Notification> notifications) {
                        try {
                            int pinNum = 0;
                            for (Notification notification : notifications) {
                                if (notification.getIsPinned() != null && notification.getIsPinned()) {
                                    pinNum++;
                                }
                            }
                            callback.onInitNotifications(notifications, pinNum);
                        } catch (Exception e) {
                        }
                    }
                }, new ApiErrorAction() {
                    @Override
                    protected void call() {
                        callback.onInitNotificationsFailed();
                    }
                });
    }

    private void initNotificationsAfterMemberInit(final boolean forceRefresh) {
        BizLogic.initGlobalMembers(new Action1<Object>() {
            @Override
            public void call(Object o) {
                initNotifications(forceRefresh);
            }
        });
    }

    public void getMoreNotifications(Date maxDate) {
        Observable<List<Notification>> networkStream = talkApi.getNotifications(BizLogic.getTeamId(), maxDate, 20)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<List<Notification>>() {
                    @Override
                    public void call(List<Notification> notifications) {
                        NotificationRealm.getInstance().batchAdd(notifications)
                                .subscribe(new EmptyAction<List<Notification>>(),
                                        new RealmErrorAction());
                    }
                });
        Observable<List<Notification>> cachedStream = BizLogic.isNetworkConnected()
                ? Observable.<List<Notification>>empty() : NotificationRealm.getInstance().getMoreNotifications(maxDate, 20);

        cachedStream.concatWith(networkStream)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Notification>>() {
                    @Override
                    public void call(List<Notification> notifications) {

                        callback.onLoadMoreNotifications(notifications);
                    }
                }, new ApiErrorAction() {
                    @Override
                    protected void call() {
                        callback.onLoadMoreNotificationsFailed();
                    }
                });
    }

    public void pinNotification(String id, boolean isPin) {
        talkApi.updateNotification(id, new UpdateNotificationRequestData(isPin, null, null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Notification>() {
                    @Override
                    public void call(Notification notification) {
                        callback.onPinSucceed(notification);
                    }
                }, new ApiErrorAction());
    }

    public void muteNotification(String id, boolean isMute) {
        talkApi.updateNotification(id, new UpdateNotificationRequestData(null, isMute, null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Notification>() {
                    @Override
                    public void call(Notification notification) {
                        callback.onMuteSucceed(notification);
                    }
                }, new ApiErrorAction());
    }

    public void clearUnread(Notification notification) {
        talkApi.updateNotification(notification.get_id(), new UpdateNotificationRequestData(notification.get_emitterId()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Notification>() {
                    @Override
                    public void call(Notification notification) {
                        callback.onClearUnreadSucceed(notification);
                    }
                }, new ApiErrorAction());
    }

    public void removeNotification(final Notification notification) {
        talkApi.updateNotification(notification.get_id(), new UpdateNotificationRequestData(true))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object object) {
                        NotificationRealm.getInstance()
                                .remove(notification.get_id())
                                .subscribe(new EmptyAction<Notification>(), new RealmErrorAction());
                        BusProvider.getInstance().post(new RemoveNotificationEvent(notification));
                    }
                }, new ApiErrorAction());
    }

    public Subscription getWebOnlineState() {
        return talkApi.getWebState().repeatWhen(new Func1<Observable<? extends rx.Notification<?>>, Observable<?>>() {
            @Override
            public Observable<?> call(Observable<? extends rx.Notification<?>> observable) {
                return observable.delay(WEB_ONLINE_STATE_INTERVAL, TimeUnit.SECONDS);
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<WebState>() {
                    @Override
                    public void call(WebState webState) {
                        callback.onGetWebState(webState.getOnlineweb() == 1);
                    }
                }, new ApiErrorAction());
    }

    public void updateMuteConfig(final boolean mute) {
        talkApi.updateMuteWhenWebOnline(mute)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Preference>() {
                    @Override
                    public void call(Preference preference) {
                        User user = BizLogic.getUserInfo();
                        user.setPreference(preference);
                        if (user.getPreference() == null) {
                            User u = (User) MainApp.PREF_UTIL.getObject(Constant.USER, User.class);
                            if (u != null) {
                                user.setPreference(u.getPreference());
                            }
                        }
                        MainApp.PREF_UTIL.putObject(Constant.USER, user);
                        callback.onUpdatePreference();
                        if (mute) {
                            MainApp.showToastMsg(R.string.already_mute);
                        } else {
                            MainApp.showToastMsg(R.string.already_unmute);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }
}
