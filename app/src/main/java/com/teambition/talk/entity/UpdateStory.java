package com.teambition.talk.entity;

/**
 * Created by wlanjie on 15/11/18.
 */
public class UpdateStory {

    public String fileKey;
    public String fileName;

    public UpdateStory(){}

    public UpdateStory(String fileKey, String fileName) {
        this.fileKey = fileKey;
        this.fileName = fileName;
    }
}
