package com.teambition.talk.view;

import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.TagSearchMessage;

import java.util.List;

/**
 * Created by wlanjie on 15/7/16.
 */
public interface TagSearchWithMessageView extends BaseView {

    void readTagWithMessage(List<Message> messages);

    void deleteComplete(String messageId);

    void readTagWithMessageError();
}
