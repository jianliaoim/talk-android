package com.teambition.talk.view;

import com.teambition.talk.entity.Message;

import java.util.List;

/**
 * Created by nlmartian on 1/30/16.
 */
public interface MentionedMessageView extends BaseView {

    void showMessages(List<Message> messageList);

}
