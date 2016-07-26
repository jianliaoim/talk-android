package com.teambition.talk.realm;

import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.Member;

/**
 * Created by wlanjie on 15/10/12.
 */
public class MemberDataProcess {

    private static MemberDataProcess memberDataProcess;

    public static MemberDataProcess getInstance() {
        if (memberDataProcess == null) {
            memberDataProcess = new MemberDataProcess();
        }
        return memberDataProcess;
    }

    public void processNewMember(final Member member) {
        member.setRole(Member.MEMBER);
        member.setIsQuit(false);
        member.setUnread(0);
    }

    public void processPrefers(final Member member) {
        if (member.getPrefs() != null) {
            member.setAlias(member.getPrefs().getAlias());
            member.setHideMobile(member.getPrefs().getHideMobile() == null ? false : member.getPrefs().getHideMobile());
        }
    }

    public boolean isPinned(final Member member) {
        return member.getPinnedAt() != null;
    }

    public static Member getAnonymousInstance() {
        Member member = new Member();
        member.setName(MainApp.CONTEXT.getString(R.string.anonymous_user));
        return member;
    }
}
