package com.teambition.talk.entity;

import com.teambition.talk.util.StringUtil;

import org.parceler.Parcel;

/**
 * Created by zeatual on 14/11/5.
 */
@Parcel(Parcel.Serialization.BEAN)
public class Quote {

    String openId;
    String text;
    String authorName;
    String authorAvatarUrl;
    String redirectUrl;
    String title;
    String category;
    String thumbnailPicUrl;
    String imageUrl;

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorAvatarUrl() {
        return authorAvatarUrl;
    }

    public void setAuthorAvatarUrl(String authorAvatarUrl) {
        this.authorAvatarUrl = authorAvatarUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getThumbnailPicUrl() {
        return thumbnailPicUrl;
    }

    public void setThumbnailPicUrl(String thumbnailPicUrl) {
        this.thumbnailPicUrl = thumbnailPicUrl;
    }

    public String getThumbnailUrl() {
        if (StringUtil.isNotBlank(getThumbnailPicUrl())) {
            return getThumbnailPicUrl();
        } else if (StringUtil.isNotBlank(imageUrl)) {
            return imageUrl;
        }
        return null;
    }

}
