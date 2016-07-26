package com.teambition.talk.view;

import com.teambition.talk.entity.Notification;

import java.util.List;

/**
 * Created by zeatual on 15/10/13.
 */
public interface NotificationView extends BaseView {

    void onInitNotifications(List<Notification> notifications, int pinNum);

    void onInitNotificationsFailed();

    void onLoadMoreNotifications(List<Notification> notifications);

    void onLoadMoreNotificationsFailed();

    void onPinSucceed(Notification notification);

    void onMuteSucceed(Notification notification);

    void onClearUnreadSucceed(Notification notification);

    void onGetWebState(boolean webOnline);

    void onUpdatePreference();
}
