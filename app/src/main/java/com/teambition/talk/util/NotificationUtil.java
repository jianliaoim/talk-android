package com.teambition.talk.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.teambition.talk.BizLogic;
import com.teambition.talk.Constant;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.ui.activity.HomeActivity;
import com.teambition.talk.ui.activity.MainActivity;
import com.teambition.talk.client.adapter.ISODateAdapter;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zeatual on 15/2/5.
 */
public class NotificationUtil {

    private static final String CATEGORY_NOTIFICATION = "category_notification";
    public static final boolean USE_XIAOMI = true; //BuildConfig.BUILD_TYPE_INT == 0 || BuildConfig.BUILD_TYPE_INT == 1;

    public static void startPush(Context context) {
        if (USE_XIAOMI) {
            MiPushClient.registerPush(context, Constant.XIAOMI_APP_ID, Constant.XIAOMI_APP_KEY);
        }
    }

    public static void stopPush(Context context) {
        if (USE_XIAOMI) {
            MiPushClient.unregisterPush(context);
        }
    }

    public static void showNotification(Context context, String message, MiPushMessage miPushMessage) {
        if (BizLogic.isNotificationOn()) {
            if (!BizLogic.isApplicationShowing(MainApp.CONTEXT) || MainApp.IS_SCREEN_LOCK) {

                int numMessages = MainApp.PREF_UTIL.getInt(Constant.NOTIFICATION_COUNT, 0) + 1;
                MainApp.PREF_UTIL.putInt(Constant.NOTIFICATION_COUNT, numMessages);
                List<String> msgList = jsonToList(MainApp.PREF_UTIL.getString(Constant.NOTIFICATION_CONTENT,
                        ""), String.class);
                if (msgList == null) {
                    msgList = new ArrayList<>();
                }
                msgList.add(0, message);
                MainApp.PREF_UTIL.putString(Constant.NOTIFICATION_CONTENT, listToJson(msgList));

                /* Creates an explicit intent for an Activity in your app */
                Intent resultIntent = new Intent(context, HomeActivity.class);
                resultIntent.putExtra(PushMessageHelper.KEY_MESSAGE, miPushMessage);
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                resultIntent.addCategory(CATEGORY_NOTIFICATION);

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addParentStack(MainActivity.class);

                /* Adds the Intent that starts the Activity to the top of the stack */
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                inboxStyle.setBigContentTitle(context.getString(R.string.app_name));
                inboxStyle.setSummaryText(context.getString(R.string.new_message));
                for (String mMsg : msgList) {
                    inboxStyle.addLine(mMsg);
                }
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
                mBuilder.setContentTitle(context.getString(R.string.app_name)) //设置通知栏标题
                        .setStyle(inboxStyle)
                        .setContentText(context.getString(R.string.new_message))
                        .setAutoCancel(true)
                        .setContentIntent(resultPendingIntent) //设置通知栏点击意图
                        .setNumber(numMessages) //设置通知集合的数量
                        .setWhen(System.currentTimeMillis()) //通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                        .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS) //向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
                        .setSound(Uri.parse("android.resource://com.teambition.talk/" + R.raw.add), AudioManager.STREAM_NOTIFICATION)
                        .setSmallIcon(R.drawable.ic_notification); //设置通知小ICON
                mNotificationManager.notify(Constant.NOTIFICATION_ID, mBuilder.build());
            }
        }
    }

    private static <V> ArrayList<V> jsonToList(String strJSON, Class<V> clazz) {

        Gson gson = new GsonBuilder().setDateFormat(DateUtil.DATE_FORMAT_JSON)
                .registerTypeAdapter(Date.class, new ISODateAdapter())
                .create();

        Type listType = new TypeToken<List<V>>() {
        }.getType();
        ArrayList<V> list = new ArrayList<>();
        String result = null;
        try {
            List<V> vList = gson.fromJson(strJSON, listType);
            if (vList != null) {
                for (V v : vList) {
                    result = gson.toJson(v);
                    list.add(gson.fromJson(result, clazz));
                }
            }
        } catch (JsonSyntaxException i) {
            Log.d("teambition", result);
            i.printStackTrace();
        }

        return list;
    }

    private static <V> String listToJson(List<V> obj) {
        String strToJSON;
        Gson gson = new GsonBuilder().setDateFormat(DateUtil.DATE_FORMAT_JSON).create();
        Type listType = new TypeToken<List<V>>() {
        }.getType();
        strToJSON = gson.toJson(obj, listType);
        return strToJSON;
    }

    public static boolean isMIUI() {
        String propName = "ro.miui.ui.version.name";
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            return false;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
        return line != null && line.length() > 0;
    }
}
