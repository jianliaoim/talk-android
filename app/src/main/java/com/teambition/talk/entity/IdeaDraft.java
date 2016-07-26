package com.teambition.talk.entity;

import org.parceler.Parcel;

import io.realm.DraftRealmProxy;
import io.realm.RealmObject;

/**
 * Created by wlanjie on 16/3/7.
 */
@Parcel(implementations = {DraftRealmProxy.class}, value = Parcel.Serialization.BEAN, analyze = {Draft.class})
public class IdeaDraft extends RealmObject {

    private String title;
    private String description;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
