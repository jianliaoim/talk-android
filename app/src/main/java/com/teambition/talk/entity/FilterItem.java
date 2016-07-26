package com.teambition.talk.entity;

import org.parceler.Parcel;

/**
 * Created by zeatual on 15/5/5.
 */
@Parcel(Parcel.Serialization.BEAN)
public class FilterItem {

    public static final int TYPE_MEMBER = 0;
    public static final int TYPE_ROOM = 1;
    public static final int TYPE_KEYWORD = 2;
    public static final int TYPE_ALL = 3;

    public FilterItem(){}

    public FilterItem(int type, String key, String name) {
        this.type = type;
        this.key = key;
        this.name = name;
    }

    int type;
    String key;
    String name;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
