package com.teambition.talk;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.teambition.talk.event.PhoneEvent;

/**
 * Created by wlanjie on 15/9/14.
 */
public class PhoneReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
            manager.listen(mListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    final PhoneStateListener mListener = new PhoneStateListener() {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    BusProvider.getInstance().post(new PhoneEvent());
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    BusProvider.getInstance().post(new PhoneEvent());
                    break;
            }
        }
    };
}
