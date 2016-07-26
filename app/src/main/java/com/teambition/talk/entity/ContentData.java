package com.teambition.talk.entity;

import org.parceler.Parcel;

/**
 * Created by zeatual on 14/11/5.
 */
@Parcel(Parcel.Serialization.BEAN)
public class ContentData {

    public String type;
    public String text;
    public String href;
    public Data data;

    @Parcel(Parcel.Serialization.BEAN)
    public static class Data {

        public String id;

        public Data(){}

        public Data(String id) {
            this.id = id;
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getText() {
//        if ("text".equals(type)) {
//            return text;
//        } else if ("mention".equals(type)) {
//            return text;
//        } else if ("link".equals(type)) {
//            return "<a href=\"" + href + "\">" + text + "</a>";
//        } else if ("highlight".equals(type)) {
//            return "<em>" + text + "</em>";
//        } else {
//            return text;
//        }
        return text;
    }
}
