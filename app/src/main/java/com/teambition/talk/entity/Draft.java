package com.teambition.talk.entity;

import org.parceler.Parcel;

import java.util.Date;

import io.realm.DraftRealmProxy;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by wlanjie on 16/1/5.
 */
@Parcel(implementations = {DraftRealmProxy.class}, value = Parcel.Serialization.BEAN, analyze = {Draft.class})
public class Draft extends RealmObject {

    public final static String ID = "_id";
    public final static String TEAM_ID = "teamId";

    @PrimaryKey
    private String _id;
    private String content;
    private String teamId;
    @Ignore
    private Date updatedAt;
    private long updatedAtTime;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getUpdatedAtTime() {
        return updatedAtTime;
    }

    public void setUpdatedAtTime(long updatedAtTime) {
        this.updatedAtTime = updatedAtTime;
    }
}
