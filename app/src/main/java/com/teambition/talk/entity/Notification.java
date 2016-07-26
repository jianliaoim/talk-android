package com.teambition.talk.entity;

import org.parceler.Parcel;

import java.io.Serializable;
import java.util.Date;

import io.realm.NotificationRealmProxy;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by zeatual on 15/9/30.
 */
@Parcel(implementations = {NotificationRealmProxy.class}, value = Parcel.Serialization.BEAN, analyze = {Notification.class})
public class Notification extends RealmObject implements Serializable {

    //realm key
    public static final String ID = "_id";
    public static final String TARGET_ID = "_targetId";
    public static final String TEAM_ID = "_teamId";
    public static final String IS_PINNED = "isPinned";
    public static final String UPDATED_AT = "updateAtTime";

    @PrimaryKey
    private String _id;
    private String _userId;
    private String _teamId;
    private String _targetId;
    private String _creatorId;
    private String type;
    private String _emitterId;
    private String _latestReadMessageId;
    @Ignore
    private Member creator;
    private String event;
    private String text;
    private Integer unreadNum;
    @Ignore
    private Integer oldUnreadNum;
    private Boolean isPinned;
    private Boolean isMute;
    private Boolean isHidden;
    private int status;
    private String authorName;
    @Ignore
    private Date updatedAt;
    private long updateAtTime;
    @Ignore
    private Date createdAt;
    private long createdAtTime;
    private Member member;
    private Room room;
    private Story story;
    @Ignore
    private String outlineText;
    @Ignore
    private int oldPosition;
    @Ignore
    private Date draftTempUpdateAt;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String get_userId() {
        return _userId;
    }

    public void set_userId(String _userId) {
        this._userId = _userId;
    }

    public String get_teamId() {
        return _teamId;
    }

    public void set_teamId(String _teamId) {
        this._teamId = _teamId;
    }

    public String get_targetId() {
        return _targetId;
    }

    public void set_targetId(String _targetId) {
        this._targetId = _targetId;
    }

    public String get_creatorId() {
        return _creatorId;
    }

    public void set_creatorId(String _creatorId) {
        this._creatorId = _creatorId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String get_emitterId() {
        return _emitterId;
    }

    public void set_emitterId(String _emitterId) {
        this._emitterId = _emitterId;
    }

    public String get_latestReadMessageId() {
        return _latestReadMessageId;
    }

    public void set_latestReadMessageId(String _latestReadMessageId) {
        this._latestReadMessageId = _latestReadMessageId;
    }

    public Member getCreator() {
        return creator;
    }

    public void setCreator(Member creator) {
        this.creator = creator;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getUnreadNum() {
        return unreadNum;
    }

    public void setUnreadNum(Integer unreadNum) {
        this.unreadNum = unreadNum;
    }

    public Integer getOldUnreadNum() {
        return oldUnreadNum;
    }

    public void setOldUnreadNum(Integer oldUnreadNum) {
        this.oldUnreadNum = oldUnreadNum;
    }

    public Boolean getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Boolean isPinned) {
        this.isPinned = isPinned;
    }

    public Boolean getIsMute() {
        return isMute;
    }

    public void setIsMute(Boolean isMute) {
        this.isMute = isMute;
    }

    public Boolean getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(Boolean isHidden) {
        this.isHidden = isHidden;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Story getStory() {
        return story;
    }

    public void setStory(Story story) {
        this.story = story;
    }

    public long getUpdateAtTime() {
        return updateAtTime;
    }

    public void setUpdateAtTime(long updateAtTime) {
        this.updateAtTime = updateAtTime;
    }

    public long getCreatedAtTime() {
        return createdAtTime;
    }

    public void setCreatedAtTime(long createdAtTime) {
        this.createdAtTime = createdAtTime;
    }

    public String getOutlineText() {
        return outlineText;
    }

    public void setOutlineText(String outlineText) {
        this.outlineText = outlineText;
    }

    public int getOldPosition() {
        return oldPosition;
    }

    public void setOldPosition(int oldPosition) {
        this.oldPosition = oldPosition;
    }

    public Date getDraftTempUpdateAt() {
        return draftTempUpdateAt;
    }

    public void setDraftTempUpdateAt(Date draftTempUpdateAt) {
        this.draftTempUpdateAt = draftTempUpdateAt;
    }
}
