package com.teambition.talk.entity;

import org.parceler.Parcel;

/**
 * Created by zeatual on 15/6/18.
 */
@Parcel(Parcel.Serialization.BEAN)
public class LinkedAccount {

    public static final String WECHAT = "open_wx_service";

    public String _id;
    public String showname;
    public String refer;

}
