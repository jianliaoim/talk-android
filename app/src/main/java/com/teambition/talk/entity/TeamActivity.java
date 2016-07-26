package com.teambition.talk.entity;

import java.util.Date;
import java.util.List;

/**
 * Created by nlmartian on 2/16/16.
 */
public class TeamActivity {
    private String _id;
    private String _creatorId;
    private String _teamId;
    private String _targetId;
    private String type;
    private String text;
    private boolean isPublic;
    private List<String> members;
    private Date createdAt;
    private Date updatedAt;
    private Member creator;

    private Room room;
    private Story story;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
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

    public String get_targetId() {
        return _targetId;
    }

    public void set_targetId(String _targetId) {
        this._targetId = _targetId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
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

    public Member getCreator() {
        return creator;
    }

    public void setCreator(Member creator) {
        this.creator = creator;
    }
}
