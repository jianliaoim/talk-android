package com.teambition.talk.entity;

import org.parceler.Parcel;

/**
 * Created by zeatual on 15/6/8.
 */
@Parcel
public class AddonsItem {

    public int img;
    public String text;
    public String url;

    public AddonsItem() {
    }

    public AddonsItem(int img, String text, String url) {
        this.img = img;
        this.text = text;
        this.url = url;
    }
}
