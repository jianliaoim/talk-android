package com.teambition.talk.entity;

import android.support.annotation.IntDef;


import org.parceler.Parcel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;
import java.util.List;

import io.realm.RealmObject;
import io.realm.RoomRealmProxy;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by zeatual on 14/10/29.
 */
@Parcel(implementations = {RoomRealmProxy.class}, value = Parcel.Serialization.BEAN, analyze = {Room.class})
public class Room extends RealmObject {

    //realm key
    public static final String ID = "_id";
    public static final String IS_GENERAL = "isGeneral";
    public static final String IS_QUIT = "isQuit";
    public static final String IS_ARCHIVED = "isArchived";
    public static final String PINNED_AT = "pinnedAt";
    public static final String CREATED_AT = "createdAt";
    public static final String IS_PRIVATE = "isPrivate";
    public static final String PINYIN = "pinyin";
    public static final String TEAM_ID = "_teamId";

    //states
    public static final int UPDATE = 0;
    public static final int REMOVE = 1;
    public static final int ARCHIVE = 2;
    public static final int LEAVE = 3;

    @IntDef({UPDATE, REMOVE, ARCHIVE, LEAVE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {}

    @PrimaryKey
    private String _id;
    private String topic;
    private String _creatorId;
    private String _teamId;
    @Ignore
    private Date createdAt;
    private long createdAtTime;
    private Boolean isPrivate;
    private Boolean isArchived;
    private Boolean isGeneral;
    private Boolean isQuit;
    private String purpose;
    private Integer unread;
    private String color;
    private String pinyin;
    @Ignore
    private Date pinnedAt;
    private long pinnedAtTime;
    private Boolean isMute;
    @Ignore
    private int state;
    @Ignore
    private List<Member> members;
    @Ignore
    private List<String> _memberIds;
    private String _memberJsonIds;
    @Ignore
    private Prefs prefs;

    public long getCreatedAtTime() {
        return createdAtTime;
    }

    public void setCreatedAtTime(long createdAtTime) {
        this.createdAtTime = createdAtTime;
    }

    public long getPinnedAtTime() {
        return pinnedAtTime;
    }

    public void setPinnedAtTime(long pinnedAtTime) {
        this.pinnedAtTime = pinnedAtTime;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String get_creatorId() {
        return _creatorId;
    }

    public void set_creatorId(String _creatorId) {
        this._creatorId = _creatorId;
    }

    public String get_teamId() {
        return _teamId;
    }

    public void set_teamId(String _teamId) {
        this._teamId = _teamId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public Boolean getIsArchived() {
        return isArchived;
    }

    public void setIsArchived(Boolean isArchived) {
        this.isArchived = isArchived;
    }

    public Boolean getIsGeneral() {
        return isGeneral == null ? false : isGeneral;
    }

    public void setIsGeneral(Boolean isGeneral) {
        this.isGeneral = isGeneral;
    }

    public Boolean getIsQuit() {
        return isQuit == null ? false : isQuit;
    }

    public void setIsQuit(Boolean isQuit) {
        this.isQuit = isQuit;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Integer getUnread() {
        return unread == null ? 0 : unread;
    }

    public void setUnread(Integer unread) {
        this.unread = unread;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Date getPinnedAt() {
        return pinnedAt;
    }

    public void setPinnedAt(Date pinnedAt) {
        this.pinnedAt = pinnedAt;
    }

    @State
    public int getState() {
        return state;
    }

    public void setState(@State int state) {
        this.state = state;
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    public Boolean getIsMute() {
        return isMute == null ? false : isMute;
    }

    public void setIsMute(Boolean isMute) {
        this.isMute = isMute;
    }

    public Prefs getPrefs() {
        return prefs;
    }

    public void setPrefs(Prefs prefs) {
        this.prefs = prefs;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
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
