package com.teambition.talk.model;

import com.teambition.talk.entity.Message;

import java.util.List;

import rx.Observable;

/**
 * Created by nlmartian on 8/4/15.
 */
public interface MessageModel {

    Observable<Object> createFavoriteMessageObservable(String messageId);

    Observable<List<Message>> getStoryMessagesBeside(String storyId, String teamId, String besideId, int limit);

    Observable<List<Message>> getPreviousUserMessages(String userId, String teamId, String maxId, int limit);

    Observable<List<Message>> getLaterUserMessages(String userId, String teamId, String minId, int limit);

    Observable<List<Message>> getPreviousRoomMessages(String roomId, String teamId, String maxId, int limit);

    Observable<List<Message>> getLaterRoomMessages(String roomId, String teamId, String minId, int limit);

    Observable<List<Message>> getPreviousStoryMessages(String storyId, String teamId, String maxId, int limit);

    Observable<List<Message>> getLaterStoryMessages(String storyId, String teamId, String minId, int limit);

    Observable<List<Message>> getUserMessages(String userId, String teamId, int limit);

    Observable<List<Message>> getRoomMessages(String roomId, String teamId, int limit);

    Observable<List<Message>> getStoryMessages(String storyId, String teamId, int limit);

    List<Message> getLocalUnreadMessages(String foreignId);

    void deleteLocalMessageWith(String foreignId);

    Observable<Object> deleteMessage(String msgId);

    Observable<Message> updateMessage(String messageId, String content);

    void saveMessageInDb(Message message);

    List<Message> getUnSendMessage(String foreignId);
}
