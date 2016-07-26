package com.teambition.talk.entity;

import org.parceler.Parcel;

import java.util.List;

/**
 * Created by zeatual on 14/11/5.
 */
@Parcel(Parcel.Serialization.BEAN)
public class Content {

    public static String CONTENT_TEXT = "content_text";
    public static String CONTENT_DATA = "content_data";

    public String type;
    public Object value;

    public Content(){}

    public Content(String type, Object value) {
        this.type = type;
        this.value = value;
    }

    public static String getContentString(List<Content> contents) {
        String result = "";
        if (contents == null) {
            return result;
        }
        for (int i = 0; i < contents.size(); i++) {
            Content content = contents.get(i);
            if (CONTENT_TEXT.equals(content.type)) {
                result += content.value.toString();
            } else if (CONTENT_DATA.equals(content.type)) {
                result += ((ContentData) content.value).getText();
            }
        }
        return result;
    }
}
