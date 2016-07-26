package com.teambition.talk.event;

import com.teambition.talk.entity.Member;

import java.util.List;

/**
 * Created by zeatual on 14/11/13.
 */
public class UpdateMemberEvent {

    public List<Member> members;

    public Member member;

    public UpdateMemberEvent() {}

    public UpdateMemberEvent(Member member) {
        this.member = member;
    }

    public UpdateMemberEvent(List<Member> members) {
        this.members = members;
    }
}
