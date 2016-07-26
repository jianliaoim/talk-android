package com.teambition.talk.entity;

import org.parceler.Parcel;

import java.util.Date;
import java.util.List;

import io.realm.RealmObject;
import io.realm.StoryRealmProxy;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by zeatual on 15/9/25.
 */
@Parcel(implementations = {StoryRealmProxy.class}, value = Parcel.Serialization.BEAN, analyze = {Story.class})
public class Story extends RealmObject {

    public static final String ID = "_id";
    public static final String TEAM_ID = "_teamId";

    @PrimaryKey
    private String _id;
    private String _teamId;
    private String _creatorId;
    private String category;
    private String title;
    private String data;
    @Ignore
    private Date createdAt;
    private long createdAtTime;
    @Ignore
    private Date activedAt;
    private long activedAtTime;
    @Ignore
    private Date updatedAt;
    private long updateAtTime;
    @Ignore
    private List<String> _memberIds;
    //memberid数组,需要使用gson解析成List<String>
    private String memberIds;

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

    public String get_creatorId() {
        return _creatorId;
    }

    public void set_creatorId(String _creatorId) {
        this._creatorId = _creatorId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getActivedAt() {
        return activedAt;
    }

    public void setActivedAt(Date activedAt) {
        this.activedAt = activedAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getUpdateAtTime() {
        return updateAtTime;
    }

    public void setUpdateAtTime(long updateAtTime) {
        this.updateAtTime = updateAtTime;
    }

    public List<String> get_memberIds() {
        return _memberIds;
    }

    public void set_memberIds(List<String> _memberIds) {
        this._memberIds = _memberIds;
    }

    public String getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(String memberIds) {
        this.memberIds = memberIds;
    }

    public long getCreatedAtTime() {
        return createdAtTime;
    }

    public void setCreatedAtTime(long createdAtTime) {
        this.createdAtTime = createdAtTime;
    }

    public long getActivedAtTime() {
        return activedAtTime;
    }

    public void setActivedAtTime(long activedAtTime) {
        this.activedAtTime = activedAtTime;
    }

}
