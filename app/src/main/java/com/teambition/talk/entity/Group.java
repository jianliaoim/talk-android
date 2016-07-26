package com.teambition.talk.entity;

import org.parceler.Parcel;

import java.util.Date;
import java.util.List;

import io.realm.GroupRealmProxy;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by zeatual on 15/12/23.
 */
@Parcel(implementations = {GroupRealmProxy.class}, value = Parcel.Serialization.BEAN, analyze = {Group.class})
public class Group extends RealmObject {

    public static final String TEAM_ID = "_teamId";
    public static final String ID = "_id";
    public static final String NMAE = "name";

    @PrimaryKey
    private String _id;
    private String _teamId;
    private String name;
    private String _creatorId;
    private Date createdAt;
    @Ignore
    private long createdAtTime;
    @Ignore
    private List<String> _memberIds;
    private String _memberJsonIds;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
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

    public String get_creatorId() {
        return _creatorId;
    }

    public void set_creatorId(String _creatorId) {
        this._creatorId = _creatorId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public long getCreatedAtTime() {
        return createdAtTime;
    }

    public void setCreatedAtTime(long createdAtTime) {
        this.createdAtTime = createdAtTime;
    }

    public List<String> get_memberIds() {
        return _memberIds;
    }

    public void set_memberIds(List<String> _memberIds) {
        this._memberIds = _memberIds;
    }

    public String get_memberJsonIds() {
        return _memberJsonIds;
    }

    public void set_memberJsonIds(String _memberJsonIds) {
        this._memberJsonIds = _memberJsonIds;
    }
}
