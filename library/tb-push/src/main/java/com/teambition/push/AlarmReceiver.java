package com.teambition.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.teambition.push.util.Logger;

/**
 * Created by nlmartian on 7/30/15.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";

    public static final String ACTION_ALARM = "com.teambition.push.ACTION_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_ALARM.equals(intent.getAction())) {
            context.startService(PushMsgService.startIntent(context));
        } else {
            if (!Whisper.isServiceRun(context, "com.teambition.push.MessageService")) {
                context.startService(PushMsgService.startIntent(context));
            } else {
                Logger.d(TAG, "Whisper is already initiated :)");
            }
        }
    }
}
