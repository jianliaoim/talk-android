package com.teambition.talk.view;

import com.teambition.talk.entity.Member;

import java.util.List;

/**
 * Created by zeatual on 14/10/31.
 */
public interface MemberView extends BaseView {

    void onLoadMembersFinish(List<Member> members);

    void onLoadLeaveMembersFinish(List<Member> members);

}
