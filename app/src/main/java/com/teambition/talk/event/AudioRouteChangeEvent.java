package com.teambition.talk.event;

/**
 * Created by nlmartian on 5/8/15.
 */
public class AudioRouteChangeEvent {
    public boolean frontSpeaker;

    public AudioRouteChangeEvent(boolean frontSpeaker) {
        this.frontSpeaker = frontSpeaker;
    }
}
