package com.teambition.talk.client.data;

/**
 * Created by zeatual on 15/10/14.
 */
public class UpdateNotificationRequestData {

    Boolean isPinned;
    Boolean isMute;
    Boolean isHidden;
    Integer unreadNum;
    String _latestReadMessageId;

    public UpdateNotificationRequestData(Boolean isPinned, Boolean isMute, Integer unreadNum) {
        this.isPinned = isPinned;
        this.isMute = isMute;
        this.unreadNum = unreadNum;
    }

    public UpdateNotificationRequestData(Boolean isHidden) {
        this.isHidden = isHidden;
    }

    public UpdateNotificationRequestData(String _latestReadMessageId) {
        this.unreadNum = 0;
        this._latestReadMessageId = _latestReadMessageId;
    }
}
