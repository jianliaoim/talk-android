package com.teambition.talk.entity;

import com.teambition.talk.ui.MessageFormatter;
import com.teambition.talk.util.StringUtil;

import org.parceler.Parcel;

/**
 * Created by zeatual on 15/7/30.
 */
@Parcel(Parcel.Serialization.BEAN)
public class RTF {

    String title;
    String text;

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

    public String getThumbnailUrl() {
        if (StringUtil.isNotBlank(MessageFormatter.filterImage(text))) {
            return MessageFormatter.filterImage(getText());
        }
        return null;
    }
}
