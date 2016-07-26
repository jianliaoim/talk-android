package com.teambition.talk.view;

import com.teambition.talk.entity.Message;

import java.util.List;

/**
 * Created by zeatual on 15/5/11.
 */
public interface ChatPhotoView extends BaseView {

    void onInitFinish(List<Message> messages);

    void onLoadOldFinish(List<Message> messages);

    void onLoadNewFinish(List<Message> messages);

    void onDeleteMessageSuccess();
}
