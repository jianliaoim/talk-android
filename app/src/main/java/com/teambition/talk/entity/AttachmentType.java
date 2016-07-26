package com.teambition.talk.entity;

/**
 * Created by zeatual on 15/7/27.
 */
public enum AttachmentType {

    QUOTE("quote"),
    RTF("rtf"),
    SNIPPET("snippet"),
    FILE("file"),
    SPEECH("speech"),
    MESSAGE("message"),
    VIDEO("video");

    private String value;

    AttachmentType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static AttachmentType getEnum(String value) {
        for (AttachmentType v : values()) {
            if (v.value.equalsIgnoreCase(value)) {
                return v;
            }
        }
        return QUOTE;
    }
}
