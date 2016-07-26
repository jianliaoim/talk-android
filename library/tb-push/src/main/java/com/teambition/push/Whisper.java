package com.teambition.push;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;

import com.teambition.push.util.Daemon;

import java.util.List;

/**
 * Created by nlmartian on 7/28/15.
 */
public class Whisper {

    public static final String TAG = "Whisper";

    public static final long ALARM_REPEAT_INTERVAL = 15 * 60 * 1000;

    public static final int ALARM_REQUEST_CODE = 10221;

    public static void init(final Context context, boolean debugMode) {
        Intent serviceIntent = PushMsgService.startIntent(context, PushMsgService.ACTION_START_SERVICE);
        serviceIntent.putExtra(PushMsgService.EXTRA_DEBUG, debugMode);
        context.startService(serviceIntent);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Daemon.run(context, PushMsgService.class, Daemon.INTERVAL_ONE_MINUTE * 2);
            }
        }, 3000);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_ALARM);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                    ALARM_REPEAT_INTERVAL, pendingIntent);
        } else {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                    ALARM_REPEAT_INTERVAL, pendingIntent);
        }
    }

    public static void init(final Context context) {
        init(context, false);
    }

    public static void stop(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_ALARM);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        Daemon.kill(context);
        Intent closeIntent = new Intent(context, PushMsgService.class);
        closeIntent.setAction(PushMsgService.ACTION_DISCONNECT);
        context.startService(closeIntent);
    }

    public static boolean isServiceRun(Context mContext, String className) {
        boolean isRun = false;
        ActivityManager activityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(500);
        int size = serviceList.size();
        for (int i = 0; i < size; i++) {
            if (serviceList.get(i).service.getClassName().equals(className)) {
                isRun = true;
                break;
            }
        }
        return isRun;
    }
}
