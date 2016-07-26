package com.teambition.talk.view;

import com.teambition.talk.client.data.FileUploadResponseData;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Room;

import java.util.List;

/**
 * Created by alfa7055 on 14-11-2.
 */
public interface ChatView extends BaseView {

    void onSendMessageSuccess(String tempMsgId, Message message);

    void onSendMessageFailed(String tempMsgId);

    void showLocalMessages(List<Message> messages);

    void showLatestMessages(List<Message> messages);

    void showSearchResult(List<Message> messages);

    void showMoreOldMessages(List<Message> messages, boolean isLocal);

    void showMoreNewMessages(List<Message> messages, boolean isLocal);

    void onUploadFileSuccess(FileUploadResponseData file, String tempMsgId);

    void onUploadFileFailed(String tempMsgId);

    void onUploadFileInvalid(String tempMsgId);

    void onDownloadProgress(int progress);

    void onDownloadFinish(String path);

    void onJoinTopic(Room room);

}
