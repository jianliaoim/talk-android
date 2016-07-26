package com.teambition.talk.util;

import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Tag;

import java.util.List;

/**
 * Created by zeatual on 15/8/11.
 */
public class SimpleMessageActionCallback implements MessageDialogBuilder.MessageActionCallback {
    @Override
    public void deleteMessage(Message msg) {

    }

    @Override
    public void editMessage(Message msg, String text) {

    }

    @Override
    public void saveFile(String fileName, String fileType, String downloadUrl) {

    }

    @Override
    public void copyText(CharSequence text) {

    }

    @Override
    public void favorite(String msgId) {

    }

    @Override
    public void tag(String msgId, List<Tag> tags) {

    }

    @Override
    public void addTag(Message msg) {

    }

    @Override
    public void forward(String msgId) {

    }
}
