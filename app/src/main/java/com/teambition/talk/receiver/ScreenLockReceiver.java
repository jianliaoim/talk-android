package com.teambition.talk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.teambition.talk.MainApp;

/**
 * Created by zeatual on 14/12/10.
 */
public class ScreenLockReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
            MainApp.IS_SCREEN_LOCK = false;
        } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            MainApp.IS_SCREEN_ON = true;
        } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
            MainApp.IS_SCREEN_LOCK = true;
            MainApp.IS_SCREEN_ON = false;
        }
    }
}
