package com.teambition.talk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.teambition.talk.BusProvider;
import com.teambition.talk.event.NetworkEvent;
import com.teambition.talk.service.MessageService;

/**
 * Created by zeatual on 14/11/6.
 */
public class NetworkStateReceiver extends BroadcastReceiver {

    private static boolean preNetworkState = true;

    @Override
    public void onReceive(Context context, Intent intent) {

        NetworkInfo info;
        ConnectivityManager connectivityManager;

        String action = intent.getAction();
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            connectivityManager = (ConnectivityManager) context.getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            info = connectivityManager.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                if (!preNetworkState) {
                    context.startService(MessageService.startIntent(context));

                    preNetworkState = true;
                    BusProvider.getInstance().post(new NetworkEvent(NetworkEvent.STATE_CONNECTING));
                }
            } else {
                preNetworkState = false;
                BusProvider.getInstance().post(new NetworkEvent(NetworkEvent.STATE_DISCONNECTED));
            }
        }
    }
}
