package com.teambition.talk.client.data;

import com.teambition.talk.entity.Member;

/**
 * Created by nlmartian on 3/17/15.
 */
public class MemberRequestData {
    public String _userId;
    public String role;

    public MemberRequestData() {
    }

    public MemberRequestData(Member member) {
        this._userId = member.get_id();
        this.role = member.getRole();
    }

    public static MemberRequestData adminMember(String userId) {
        MemberRequestData requestData = new MemberRequestData();
        requestData._userId = userId;
        requestData.role = "admin";
        return requestData;
    }
}
