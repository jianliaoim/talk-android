package com.teambition.talk.ui.row;

/**
 * Created by zeatual on 15/7/27.
 */
public enum RowType {

    TEXT_ROW("text_row"),
    TEXT_SELF_ROW("text_self_row"),
    QUOTE_ROW("quote_row"),
    QUOTE_SELF_ROW("quote_self_row"),
    FILE_ROW("file_row"),
    FILE_SELF_ROW("file_self_row"),
    IMAGE_ROW("image_row"),
    IMAGE_SELF_ROW("image_self_row"),
    SPEECH_ROW("speech_row"),
    SPEECH_SELF_ROW("speech_self_row"),
    SPEECH_RECORD_ROW("speech_record_row"),
    INFO_SELF_ROW("info_self_row"),
    INFO_ROW("info_row"),
    MENTION_ROW("mention_row"),
    MENTION_SELF_ROW("mention_self_row"),
    SYSTEM_ROW("system_row");


    private String value;

    RowType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static RowType getEnum(String value) {
        for (RowType v : values()) {
            if (v.value.equalsIgnoreCase(value)) {
                return v;
            }
        }
        return TEXT_ROW;
    }
}
