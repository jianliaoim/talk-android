package com.teambition.talk.entity;

import org.parceler.Parcel;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by zeatual on 15/9/14.
 */
@Parcel(implementations = {}, value = Parcel.Serialization.BEAN, analyze = {Invitation.class})
public class Invitation extends RealmObject {

    //realm key
    public static final String KEY = "key";
    public static final String TEAM_ID = "_teamId";

    @PrimaryKey
    private String key;
    private String _teamId;
    private String name;
    private String mobile;
    private String email;
    private String _id;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public Invitation(){}

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String get_teamId() {
        return _teamId;
    }

    public void set_teamId(String _teamId) {
        this._teamId = _teamId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
