package com.teambition.talk.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;

import com.teambition.talk.BizLogic;
import com.teambition.talk.Constant;
import com.teambition.talk.MainApp;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.ui.activity.HomeActivity;
import com.teambition.talk.ui.activity.MainActivity;
import com.teambition.talk.util.Logger;
import com.teambition.talk.util.NotificationUtil;
import com.teambition.talk.util.StringUtil;
import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageHelper;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import java.util.List;
import java.util.Map;

import rx.android.concurrency.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by zeatual on 15/2/4.
 */
public class XiaomiPushReceiver extends PushMessageReceiver {

    public static final String TAG = "XiaomiPush";
    public static final String DMS = "dms";
    public static final String ROOM = "room";
    public static final String STORY = "story";
    public static final String MESSAGE_TYPE = "message_type";
    public static final String _TARGET_ID = "_targetId";
    public static final String _TEAM_ID = "_teamId";

    @Override
    public void onNotificationMessageClicked(Context context, MiPushMessage miPushMessage) {
        Intent intent = new Intent(context, HomeActivity.class);
        final Map<String, String> extra = miPushMessage.getExtra();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(PushMessageHelper.KEY_MESSAGE, miPushMessage);
        TaskStackBuilder.create(context)
                .addParentStack(MainActivity.class)
                .addNextIntent(intent)
                .startActivities();
    }

    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage miPushMessage) {
        String msg = miPushMessage.getDescription();
        Logger.d(TAG, msg);
        NotificationUtil.showNotification(context, miPushMessage.getDescription(), miPushMessage);
    }

    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage miPushCommandMessage) {
        Logger.d(TAG, "command result");
        String command = miPushCommandMessage.getCommand();
        List<String> arguments = miPushCommandMessage.getCommandArguments();
        final String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);

        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (miPushCommandMessage.getResultCode() == ErrorCode.SUCCESS && BizLogic.isLogin()) {
                if (StringUtil.isBlank(MainApp.PREF_UTIL.getString(Constant.XIAOMI_TOKEN))) {
                    MainApp.PREF_UTIL.putString(Constant.XIAOMI_TOKEN, cmdArg1);
                }
                TalkClient.getInstance().getTalkApi()
                        .postToken(cmdArg1)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Object>() {
                            @Override
                            public void call(Object o) {
                                MainApp.PREF_UTIL.putString(Constant.XIAOMI_TOKEN, cmdArg1);
                                Logger.d(TAG, "xiaomi push register success: " + cmdArg1);
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Logger.e(TAG, "xiaomi push  register", throwable);
                            }
                        });
            }
        }
    }
}
