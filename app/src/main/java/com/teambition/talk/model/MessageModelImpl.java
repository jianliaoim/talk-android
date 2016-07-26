package com.teambition.talk.model;


import com.teambition.talk.BusProvider;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.apis.TalkApi;
import com.teambition.talk.entity.Message;
import com.teambition.talk.event.UpdateMessageEvent;
import com.teambition.talk.realm.MessageRealm;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by nlmartian on 8/4/15.
 */
public class MessageModelImpl implements MessageModel {

    private TalkApi api = null;

    public MessageModelImpl() {
        api = TalkClient.getInstance().getTalkApi();
    }

    @Override
    public Observable<List<Message>> getPreviousUserMessages(String userId, String teamId, String maxId, int limit) {
        return api.getMoreOldMsgWithUser(userId, teamId, maxId, limit);
    }

    @Override
    public Observable<List<Message>> getLaterUserMessages(String userId, String teamId, String minId, int limit) {
        return api.getMoreNewMsgWithUser(userId, teamId, minId, limit);
    }

    @Override
    public Observable<List<Message>> getPreviousRoomMessages(String roomId, String teamId, String maxId, int limit) {
        return api.getMoreOldMsgOfRoom(roomId, teamId, maxId, limit);
    }

    @Override
    public Observable<List<Message>> getLaterRoomMessages(String roomId, String teamId, String minId, int limit) {
        return api.getMoreNewMsgOfRoom(roomId, teamId, minId, limit);
    }

    @Override
    public Observable<List<Message>> getPreviousStoryMessages(String storyId, String teamId, String maxId, int limit) {
        return api.getMoreOldMsgOfStory(storyId, teamId, maxId, limit);
    }

    @Override
    public Observable<List<Message>> getLaterStoryMessages(String storyId, String teamId, String minId, int limit) {
        return api.getMoreNewMsgOfStory(storyId, teamId, minId, limit);
    }

    @Override
    public Observable<List<Message>> getUserMessages(String userId, String teamId, int limit) {
        return api.getMsgWithUser(userId, teamId, limit);
    }

    @Override
    public Observable<List<Message>> getRoomMessages(String roomId, String teamId, int limit) {
        return api.getMsgOfRoom(roomId, teamId, limit);
    }

    @Override
    public Observable<List<Message>> getStoryMessages(String storyId, String teamId, int limit) {
        return api.getMsgOfStory(storyId, teamId, limit);
    }

    @Override
    public List<Message> getLocalUnreadMessages(String foreignId) {
        return MessageRealm.getInstance().getLocalUnreadMessagesWithCurrentThread(foreignId);
    }

    @Override
    public void deleteLocalMessageWith(String foreignId) {
        MessageRealm.getInstance().deleteLocalMessageWithCurrentThread(foreignId);
    }

    @Override
    public Observable<Object> deleteMessage(String messageId) {
        return api.deleteMessage(messageId);
    }

    @Override
    public Observable<Message> updateMessage(String messageId, String body) {
        return api.updateMessage(messageId, body);
    }

    @Override
    public void saveMessageInDb(Message message) {
        MessageRealm.getInstance().addOrUpdate(message)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Message>() {
                    @Override
                    public void call(Message message) {
                        if (message != null) {
                            BusProvider.getInstance().post(new UpdateMessageEvent(message));
                        }
                    }
                }, new RealmErrorAction());
    }

    @Override
    public Observable<Object> createFavoriteMessageObservable(String messageId) {
        return null;
    }

    @Override
    public List<Message> getUnSendMessage(String foreignId) {
        return MessageRealm.getInstance().getUnSendMessageWithCurrentThread(foreignId);
    }

    @Override
    public Observable<List<Message>> getStoryMessagesBeside(String storyId, String teamId, String besideId, int limit) {
        return api.getMsgOfStoryBeside(storyId, teamId, besideId, limit);
    }
}
