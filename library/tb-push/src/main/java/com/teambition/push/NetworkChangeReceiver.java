package com.teambition.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.teambition.push.util.Network;

/**
 * Created by nlmartian on 6/19/15.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    final static String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if (CONNECTIVITY_ACTION.equalsIgnoreCase(intent.getAction())) {
                if (Network.isNetworkConnected(context)) {
                    context.startService(PushMsgService.startIntent(context));
                }
            }
        }
    }
}
