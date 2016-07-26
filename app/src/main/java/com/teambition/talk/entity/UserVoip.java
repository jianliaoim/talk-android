package com.teambition.talk.entity;

import org.parceler.Parcel;

/**
 * Created by wlanjie on 15/9/14.
 */
@Parcel(Parcel.Serialization.BEAN)
public class UserVoip {
    private String _id;
    private String user;
    private String voipAccount;
    private String voipPwd;
    private String subToken;
    private String subAccountSid;
    private String _userId;
    private String id;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getVoipAccount() {
        return voipAccount;
    }

    public void setVoipAccount(String voipAccount) {
        this.voipAccount = voipAccount;
    }

    public String getVoipPwd() {
        return voipPwd;
    }

    public void setVoipPwd(String voipPwd) {
        this.voipPwd = voipPwd;
    }

    public String getSubToken() {
        return subToken;
    }

    public void setSubToken(String subToken) {
        this.subToken = subToken;
    }

    public String getSubAccountSid() {
        return subAccountSid;
    }

    public void setSubAccountSid(String subAccountSid) {
        this.subAccountSid = subAccountSid;
    }

    public String get_userId() {
        return _userId;
    }

    public void set_userId(String _userId) {
        this._userId = _userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
