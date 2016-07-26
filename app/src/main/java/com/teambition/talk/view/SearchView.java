package com.teambition.talk.view;

import com.teambition.talk.entity.Message;

import java.util.List;

/**
 * Created by zeatual on 15/3/29.
 */
public interface SearchView extends BaseView {

    void onSearchFinish(List<Message> messages);

    void onDeleteMessageSuccess(String messageId);

    void onDownloadFinish(String path);

    void onDownloadProgress(Integer progress);
}
