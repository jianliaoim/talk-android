package com.teambition.talk.event;

import com.teambition.talk.entity.LinkedAccount;

/**
 * Created by zeatual on 15/6/18.
 */
public class WechatBindEvent {

    public LinkedAccount linkedAccount;

    public WechatBindEvent(LinkedAccount linkedAccount) {
        this.linkedAccount = linkedAccount;
    }
}
