package com.teambition.talk.view;

/**
 * Created by zeatual on 15/2/2.
 */
public interface SettingView extends BaseView {

    void onEmailNotificationUpdate(boolean result);

    void onNotifyOnRelatedUpdate(boolean value ,boolean result);

    void onPushOnWorkTimeUpdate(boolean result);
}
