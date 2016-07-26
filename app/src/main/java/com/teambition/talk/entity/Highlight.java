package com.teambition.talk.entity;

import org.parceler.Parcel;

/**
 * Created by zeatual on 15/4/28.
 */
@Parcel
public class Highlight {

    String body;
    String text;
    String title;
    String fileName;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}