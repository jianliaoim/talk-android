package com.teambition.talk.realm;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.teambition.talk.BizLogic;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Tag;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by wlanjie on 15/9/28.
 */
public class MessageRealm extends AbstractRealm {

    private static MessageRealm realm;

    private Gson mGson;

    private MessageRealm() {
        mGson = new GsonProvider.Builder().setDateAdapter().create();
    }

    public static MessageRealm getInstance() {
        if (realm == null) {
            realm = new MessageRealm();
        }
        return realm;
    }

    public Message updateUploadFileSuccessWithCurrentThread(final String msgId) {
        Message message = new Message();
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            Message msg = realm.where(Message.class)
                    .equalTo(Message.TEAM_ID, BizLogic.getTeamId())
                    .equalTo(Message.ID, msgId)
                    .findFirst();
            msg.setStatus(MessageDataProcess.Status.NONE.ordinal());
            copy(message, msg);
            if (message.getTagToJson() != null) {
                final List<Tag> tags = mGson.fromJson(message.getTagToJson(), new TypeToken<List<Tag>>() {
                }.getType());
                message.setTags(tags);
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
        return message;
    }

    public Observable<Message> updateUploadFileSuccess(final String msgId) {
        return Observable.create(new OnSubscribeRealm<Message>() {
                @Override
                public Message get(Realm realm) {
                Message realmMsg = realm.where(Message.class)
                        .equalTo(Message.TEAM_ID, BizLogic.getTeamId())
                        .equalTo(Message.ID, msgId)
                        .findFirst();
                if (realmMsg == null) return null;
                realmMsg.setStatus(MessageDataProcess.Status.NONE.ordinal());
                Message msg = new Message();
                copy(msg, realmMsg);
                if (msg.getTagToJson() != null) {
                    final List<Tag> tags = mGson.fromJson(msg.getTagToJson(), new TypeToken<List<Tag>>(){}.getType());
                    msg.setTags(tags);
                }
                return msg;
            }
        }).subscribeOn(Schedulers.io());

    }

    public Observable<Message> updateSendFailedMessage(final String msgId, final int messageStatus) {
        return Observable.create(new OnSubscribeRealm<Message>() {
            @Override
            public Message get(Realm realm) {
                Message msg = realm.where(Message.class)
                        .equalTo(Message.TEAM_ID, BizLogic.getTeamId())
                        .equalTo(Message.ID, msgId)
                        .findFirst();
                if (msg == null) return null;
                Message message = new Message();
                copy(message, msg);
                if (!TextUtils.isEmpty(msg.getTagToJson())) {
                    final List<Tag> tags = mGson.fromJson(msg.getTagToJson(), new TypeToken<List<Tag>>(){}.getType());
                    message.setTags(tags);
                }
                message.setStatus(messageStatus);
                return message;
            }
        }).subscribeOn(Schedulers.io());
    }

    public List<Message> getUnSendMessageWithCurrentThread(final String foreignId) {
        final List<Message> messages = new ArrayList<>();
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            RealmResults<Message> realmResults = realm.where(Message.class)
                    .equalTo(Message.TEAM_ID, BizLogic.getTeamId())
                    .equalTo(Message.FOREIGN_ID, foreignId)
                    .notEqualTo(Message.STATUS, 0)
                    .findAll();
            realmResults.sort(Message.CREATE_AT_TIME, Sort.ASCENDING);
            for (Message realmResult : realmResults) {
                final Message message = new Message();
                copy(message, realmResult);
                messages.add(message);
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
        return messages;
    }

    public List<Message> getLocalUnreadMessagesWithCurrentThread(final String foreignId) {
        List<Message> messages = new ArrayList<>();
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            RealmResults<Message> realmResults = realm.where(Message.class)
                    .equalTo(Message.TEAM_ID, BizLogic.getTeamId())
                    .equalTo(Message.FOREIGN_ID, foreignId)
                    .equalTo(Message.IS_READ, false)
                    .findAll();
            realmResults.sort(Message.CREATE_AT_TIME, Sort.ASCENDING);
            for (Message realmResult : realmResults) {
                Message message = new Message();
                copy(message, realmResult);
                messages.add(message);
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
        return messages;
    }

    public Observable<List<Message>> getLocalMessage(final String id, final String teamId) {
        return getLocalMessage(id, teamId, null, 0, 0)
                .flatMap(new Func1<List<Message>, Observable<Message>>() {
                    @Override
                    public Observable<Message> call(List<Message> messages) {
                        return Observable.from(messages);
                    }
                })
                .takeLast(30)
                .toList();
    }

    public Observable<List<Message>> getLocalMessage(final String id, final String teamId, final String boundaryId, final long begin, final long end) {
        return Observable.create(new OnSubscribeRealm<List<Message>>() {
            @Override
            public List<Message> get(Realm realm) {
                RealmQuery<Message> query = realm.where(Message.class)
                        .equalTo(Message.FOREIGN_ID, id)
                        .equalTo(Message.TEAM_ID, teamId);
                if (begin != 0) {
                    query.greaterThan(Message.CREATE_AT_TIME, begin);
                    query.notEqualTo(Message.ID, boundaryId);
                }
                if (end != 0) {
                    query.lessThan(Message.CREATE_AT_TIME, end);
                    query.notEqualTo(Message.ID, boundaryId);
                }
                RealmResults<Message> realmResults = query.findAll();
                realmResults.sort(Message.CREATE_AT_TIME, Sort.ASCENDING);
                List<Message> messageList = new ArrayList<>(realmResults.size());
                for (Message message : realmResults) {
                    Message msg = new Message();
                    copy(msg, message);
                    messageList.add(msg);
                }
                return messageList;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Message> getMessage(final String msgId) {
        return Observable.create(new OnSubscribeRealm<Message>() {
            @Override
            public Message get(Realm realm) {
                Message realmMsg = realm.where(Message.class)
                        .equalTo(Message.ID, msgId)
                        .equalTo(Message.TEAM_ID, BizLogic.getTeamId())
                        .findFirst();
                if (realmMsg == null) return null;
                Message msg = new Message();
                copy(msg, realmMsg);
                return msg;
            }
        }).subscribeOn(Schedulers.io());
    }

    public void deleteLocalMessageWithCurrentThread(final String foreignId) {
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            RealmResults<Message> realmResults = realm.where(Message.class)
                    .equalTo(Message.TEAM_ID, BizLogic.getTeamId())
                    .equalTo(Message.FOREIGN_ID, foreignId)
                    .findAll();
            for (Message realmResult : realmResults) {
                realmResult.removeFromRealm();
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
    }

    public Observable<Message> deleteMessage(final String messageId) {
        return Observable.create(new OnSubscribeRealm<Message>() {
            @Override
            public Message get(Realm realm) {
                Message msg = realm.where(Message.class)
                        .equalTo(Message.TEAM_ID, BizLogic.getTeamId())
                        .equalTo(Message.ID, messageId)
                        .findFirst();
                if (msg == null) return null;
                Message message = new Message();
                copy(message, msg);
                msg.removeFromRealm();
                return message;
            }
        }) .subscribeOn(Schedulers.io());
    }

    public Observable<Void> deleteTeamMessage(final String teamId) {
        return Observable.create(new OnSubscribeRealm<Void>() {
            @Override
            public Void get(Realm realm) {
                RealmResults<Message> realmResults = realm.where(Message.class)
                        .equalTo(Message.TEAM_ID, teamId)
                        .findAll();
                for (int i = realmResults.size() - 1; i >= 0; i--) {
                    Message msg = realmResults.get(i);
                    msg.removeFromRealm();
                }
                return null;
            }
        }).subscribeOn(Schedulers.io());
    }

    /**
     * remove from db and post event
     */
    public Observable<Message> remove(final Message message) {
        return Observable.create(new OnSubscribeRealm<Message>() {
            @Override
            public Message get(Realm realm) {
                Message msg = realm.where(Message.class)
                        .equalTo(Message.TEAM_ID, BizLogic.getTeamId())
                        .equalTo(Message.ID, message.get_id())
                        .findFirst();
                if (msg == null) return null;
                msg.removeFromRealm();
                return message;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<List<Message>> batchAdd(final List<Message> messages) {
        return Observable.create(new OnSubscribeRealm<List<Message>>() {
            @Override
            public List<Message> get(Realm realm) {
                final List<Message> realmMessages = new ArrayList<>(messages.size());
                for (Message message : messages) {
                    Message realmMessage = new Message();
                    copy(realmMessage, message);
                    realmMessages.add(realmMessage);
                }
                realm.copyToRealmOrUpdate(realmMessages);
                return realmMessages;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Message> addOrUpdate(final Message message, final String... msgId) {
        return Observable.create(new OnSubscribeRealm<Message>() {
            @Override
            public Message get(Realm realm) {
                copy(message, message);
                realm.copyToRealmOrUpdate(message);
                return message;
            }
        }).subscribeOn(Schedulers.io());
    }

    public void addOrUpdateEventWithCurrentThread(Message message, final String... msgId) {
        final Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            copy(message, message);
            realm.copyToRealmOrUpdate(message);
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
    }

    void copy(Message realmMsg, Message msg) {
        if (msg.get_id() != null) {
            realmMsg.set_id(msg.get_id());
        }
        if (msg.getBody() != null) {
            realmMsg.setBody(msg.getBody());
        }
        if (msg.getAttachments() != null) {
            realmMsg.setAttachments(msg.getAttachments());
        }
        realmMsg.setIsSystem(msg.isSystem());
        realmMsg.setStatus(msg.getStatus());
        if (msg.getCreatedAt() != null) {
            realmMsg.setCreateAtTime(msg.getCreatedAt().getTime());
            realmMsg.setCreatedAt(msg.getCreatedAt());
        } else {
            realmMsg.setCreateAtTime(msg.getCreateAtTime());
            realmMsg.setCreatedAt(new Date(msg.getCreateAtTime()));
        }
        if (msg.get_teamId() != null) {
            realmMsg.set_teamId(msg.get_teamId());
        }
        if (msg.get_toId() != null) {
            realmMsg.set_toId(msg.get_toId());
        }
        if (msg.get_roomId() != null) {
            realmMsg.set_roomId(msg.get_roomId());
        }
        if (msg.get_creatorId() != null) {
            realmMsg.set_creatorId(msg.get_creatorId());
        }
        if (msg.getDisplayMode() != null) {
            realmMsg.setDisplayMode(msg.getDisplayMode());
        }
        realmMsg.setIsRead(msg.isRead());
        if (msg.getAudioLocalPath() != null) {
            realmMsg.setAudioLocalPath(msg.getAudioLocalPath());
        }
        if (msg.getForeignId() != null) {
            realmMsg.setForeignId(msg.getForeignId());
        }
        if (msg.getCreatorName() != null) {
            realmMsg.setCreatorName(msg.getCreatorName());
        }
        if (msg.getCreatorAvatar() != null) {
            realmMsg.setCreatorAvatar(msg.getCreatorAvatar());
        }
        if (msg.getChatTitle() != null) {
            realmMsg.setChatTitle(msg.getChatTitle());
        }
        if (msg.getTagToJson() != null) {
            realmMsg.setTagToJson(msg.getTagToJson());
            final List<Tag> tags = mGson.fromJson(msg.getTagToJson(), new TypeToken<List<Tag>>(){}.getType());
            realmMsg.setTags(tags);
        }
        if (msg.getReceiptorsStr() != null) {
            realmMsg.setReceiptorsStr(msg.getReceiptorsStr());
            final List<String> receiptors = mGson.fromJson(msg.getReceiptorsStr(), new TypeToken<List<String>>(){}.getType());
            realmMsg.setReceiptors(receiptors);
        }
    }
}
