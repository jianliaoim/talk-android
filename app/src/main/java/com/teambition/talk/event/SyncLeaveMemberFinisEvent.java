package com.teambition.talk.event;

import com.teambition.talk.entity.Member;

import java.util.List;

/**
 * Created by wlanjie on 15/8/6.
 */
public class SyncLeaveMemberFinisEvent {

    public List<Member> members;

    public boolean isSuccess;

    public Member member;

    public SyncLeaveMemberFinisEvent(List<Member> members) {
        this.members = members;
    }

    public SyncLeaveMemberFinisEvent(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public SyncLeaveMemberFinisEvent(Member member) {
        this.member = member;
    }
}
