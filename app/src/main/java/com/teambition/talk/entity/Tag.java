package com.teambition.talk.entity;

import org.parceler.Parcel;

/**
 * Created by wlanjie on 15/7/14.
 */
@Parcel(Parcel.Serialization.BEAN)
public class Tag {

    private String id;
    private String updatedAt;
    private String _id;
    private String _teamId;
    private String createdAt;
    private String _creatorId;
    private String name;
    private int __v;
    private String team;
    private String creator;
    private boolean isSelect;

    public void setId(String id) {
        this.id = id;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public void set_teamId(String _teamId) {
        this._teamId = _teamId;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void set_creatorId(String _creatorId) {
        this._creatorId = _creatorId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void set__v(int __v) {
        this.__v = __v;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getId() {
        return id;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String get_id() {
        return _id;
    }

    public String get_teamId() {
        return _teamId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String get_creatorId() {
        return _creatorId;
    }

    public String getName() {
        return name;
    }

    public int get__v() {
        return __v;
    }

    public String getTeam() {
        return team;
    }

    public String getCreator() {
        return creator;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setIsSelect(boolean isSelect) {
        this.isSelect = isSelect;
    }
}
