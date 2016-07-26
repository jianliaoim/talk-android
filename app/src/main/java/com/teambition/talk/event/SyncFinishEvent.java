package com.teambition.talk.event;

/**
 * Created by zeatual on 14/11/6.
 */
public class SyncFinishEvent {
    public boolean isSuccess;

    public SyncFinishEvent(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }
}
