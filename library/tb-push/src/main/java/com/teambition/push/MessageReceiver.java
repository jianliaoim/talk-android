package com.teambition.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by nlmartian on 7/28/15.
 */
public abstract class MessageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (PushMsgService.ACTION_RECEIVE.equals(intent.getAction())) {
            String message = intent.getStringExtra("message");
            onMessageReceive(context, message);
        } else if (PushMsgService.ACTION_CONNECT.equals(intent.getAction())) {
            int error = intent.getIntExtra("error", 0);
            String userId = intent.getStringExtra("userId");
            onRegister(context, error, userId);
        }
    }

    abstract protected void onRegister(Context context, int errorCode, String userId);

    abstract protected void onMessageReceive(Context context, String message);
}
