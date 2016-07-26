package com.teambition.talk.event;

/**
 * Created by nlmartian on 4/28/15.
 */
public class AudioRecordStopEvent {
    public boolean tooShort;

    public AudioRecordStopEvent() {

    }

    public AudioRecordStopEvent(boolean tooShort) {
        this.tooShort = tooShort;
    }
}
