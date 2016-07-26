package com.teambition.talk.entity;

import com.teambition.talk.client.data.FileUploadResponseData;

import org.parceler.Parcel;

/**
 * Created by zeatual on 15/7/27.
 */
@Parcel(Parcel.Serialization.BEAN)
public class Attachment {

    private String category;

    private FileUploadResponseData data;

    public Attachment(){}

    public Attachment(FileUploadResponseData data) {
        if (data.isSpeech()) {
            this.category = "speech";
        } else if ("video".equals(data.getFileCategory())) {
          this.category = "video";
        } else {
            this.category = "file";
        }
        this.data = data;
    }
}
