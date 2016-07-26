package com.teambition.talk.event;

import com.teambition.talk.entity.Member;

/**
 * Created by wlanjie on 15/9/17.
 */
public class CallPhoneEvent {

    public Member member;

    public CallPhoneEvent() {}

    public CallPhoneEvent(Member member) {
        this.member = member;
    }
}
