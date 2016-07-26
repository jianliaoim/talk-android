package com.teambition.talk.entity;

import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.ThemeUtil;

import org.parceler.Parcel;

/**
 * Created by zeatual on 15/6/25.
 */
@Parcel(Parcel.Serialization.BEAN)
public class Contact implements Comparable<Contact>, Cloneable {

    private String name;
    private String phoneNum;
    private String emailAddress;
    private String index;
    private String avatar;
    private Boolean isInTeam;
    private String userId;

    public Contact(){}

    public Contact(String name, String phoneNum, String index, String avatar, String userId) {
        this.name = name;
        this.phoneNum = phoneNum;
        this.index = index;
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = StringUtil.isNotBlank(avatar) ? avatar : ImageLoaderConfig.PREFIX_DRAWABLE +
                ThemeUtil.getTopicRoundDrawableId(ThemeUtil.TopicColor.random());
    }

    public Boolean getIsInTeam() {
        return isInTeam;
    }

    public void setIsInTeam(Boolean isInTeam) {
        this.isInTeam = isInTeam;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public int compareTo(Contact another) {
        if (another.index.equals("#")) {
            return -1;
        } else if (this.index.equals("#")) {
            return 1;
        } else {
            return this.index.compareTo(another.index);
        }
    }

    @Override
    public Object clone() {
        Contact contact = null;
        try {
            contact = (Contact) super.clone();
        } catch (CloneNotSupportedException e) {

        }
        return contact;
    }
}
