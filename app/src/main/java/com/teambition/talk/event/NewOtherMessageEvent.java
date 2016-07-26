package com.teambition.talk.event;

/**
 * Created by jgzhu on 11/4/14.
 */
public class NewOtherMessageEvent {
    public String teamId;
    public boolean isMute;

    public NewOtherMessageEvent(String teamId, boolean isMute) {
        this.teamId = teamId;
        this.isMute = isMute;
    }
}
