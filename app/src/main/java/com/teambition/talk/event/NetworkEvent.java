package com.teambition.talk.event;

/**
 * Created by zeatual on 15/8/21.
 */
public class NetworkEvent {

    public static final int STATE_CONNECTED = 0;
    public static final int STATE_DISCONNECTED = 1;
    public static final int STATE_CONNECTING = 2;

    public int state;

    public NetworkEvent(int state) {
        this.state = state;
    }
}
