package com.teambition.talk.entity;

import com.google.gson.Gson;
import com.teambition.talk.GsonProvider;

import org.parceler.Parcel;

/**
 * Created by zeatual on 15/6/15.
 */
@Parcel(Parcel.Serialization.BEAN)
public class QRCodeData {

    public String _id;
    public String name;
    public String color;
    public String signCode;

    public QRCodeData(){}

    public QRCodeData(String _id, String name, String color, String signCode) {
        this._id = _id;
        this.name = name;
        this.color = color;
        this.signCode = signCode;
    }

    @Override
    public String toString() {
        Gson gson = new GsonProvider.Builder().create();
        return gson.toJson(this);
    }

    public boolean verify() {
        return _id != null && name != null && color != null && signCode != null;
    }
}
