package com.teambition.talk.entity;

import org.parceler.Parcel;

/**
 * Created by zeatual on 15/7/30.
 */
@Parcel(Parcel.Serialization.BEAN)
public class Snippet {

    String title;
    String text;
    String codeType;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCodeType() {
        return codeType;
    }

    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }
}
