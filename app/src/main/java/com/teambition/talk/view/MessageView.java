package com.teambition.talk.view;

/**
 * Created by wlanjie on 15/7/30.
 */
public interface MessageView {

    void onDownloadProgress(int progress);

    void onDownloadFinish(String path);

    void onDeleteMessageSuccess(String messageId);
}
