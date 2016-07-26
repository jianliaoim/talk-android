package com.teambition.talk.presenter;

import com.google.gson.Gson;
import com.teambition.talk.BusProvider;
import com.teambition.talk.FileDownloader;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.rx.EmptyAction;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.client.data.FileUploadResponseData;
import com.teambition.talk.client.data.MessageRequestData;
import com.teambition.talk.entity.Attachment;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Room;
import com.teambition.talk.entity.Story;
import com.teambition.talk.event.DeleteMessageEvent;
import com.teambition.talk.event.UpdateMessageEvent;
import com.teambition.talk.event.UpdateRoomEvent;
import com.teambition.talk.model.MessageModel;
import com.teambition.talk.realm.MessageDataProcess;
import com.teambition.talk.realm.MessageRealm;
import com.teambition.talk.realm.RoomRealm;
import com.teambition.talk.util.Logger;
import com.teambition.talk.view.ChatView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import retrofit.mime.TypedFile;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Created by jgzhu on 10/31/14.
 */
public class ChatPresenter extends BasePresenter {
    public static final String TAG = ChatPresenter.class.getSimpleName();

    public static final int TYPE_PRIVATE = 0;
    public static final int TYPE_PUBLIC = 1;
    public static final int TYPE_STORY = 2;

    private ChatView callback;
    private String teamId;

    private MessageModel msgModel;
    private final Gson gson;

    public ChatPresenter(ChatView callback, MessageModel msgModel, String teamId) {
        this.callback = callback;
        this.msgModel = msgModel;
        this.teamId = teamId;
        gson = GsonProvider.getGson();
    }

    public void getSearchResult(final Message msg, boolean isPrivate, Story story) {
        if (story != null) {
            final Observable<List<Message>> messageOb = msgModel.getStoryMessagesBeside(story.get_id(), teamId, msg.get_id(), 15);
            messageOb.observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<Message>>() {
                        @Override
                        public void call(List<Message> messages) {
                            for (int i = 0; i < messages.size(); i++) {
                                Message message = messages.get(i);
                                if (message.get_id().compareTo(msg.get_id()) == 0) {
                                    break;
                                } else if (message.get_id().compareTo(msg.get_id()) < 0) {
                                    messages.add(i - 1, msg);
                                    break;
                                }
                            }
                            Collections.reverse(messages);
                            callback.dismissProgressBar();
                            callback.showSearchResult(messages);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            callback.dismissProgressBar();
                        }
                    });
        } else {
            Observable<List<Message>> previousMessagesOb = null;
            Observable<List<Message>> laterMessagesOb = null;
            if (isPrivate) {
                Member member = msg.getCreator();
                if (member != null) {
                    previousMessagesOb = msgModel.getPreviousUserMessages(member.get_id(), teamId, msg.get_id(), 15);
                    laterMessagesOb = msgModel.getLaterUserMessages(member.get_id(), teamId, msg.get_id(), 15);
                }
            } else {
                Room room = msg.getRoom();
                if (room != null) {
                    previousMessagesOb = msgModel.getPreviousRoomMessages(room.get_id(), teamId, msg.get_id(), 15);
                    laterMessagesOb = msgModel.getLaterRoomMessages(room.get_id(), teamId, msg.get_id(), 15);

                }
            }
            if (previousMessagesOb != null && laterMessagesOb != null) {
                Observable.combineLatest(previousMessagesOb, laterMessagesOb,
                        new Func2<List<Message>, List<Message>, List<Message>>() {
                            @Override
                            public List<Message> call(List<Message> messages, List<Message> messages2) {
                                List<Message> result = new ArrayList<>();
                                Collections.reverse(messages);
                                result.addAll(messages);
                                result.add(msg);
                                result.addAll(messages2);
                                return result;
                            }
                        }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<List<Message>>() {
                            @Override
                            public void call(List<Message> messages) {
                                callback.dismissProgressBar();
                                callback.showSearchResult(messages);
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                callback.dismissProgressBar();
                            }
                        });
            }
        }
    }

    public void getMessages(final String id, final int type) {
        callback.showProgressBar();
        MessageRealm.getInstance()
                .getLocalMessage(id, teamId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Message>>() {
                    @Override
                    public void call(List<Message> messages) {
                        if (messages.isEmpty()) {
                            switch (type) {
                                case TYPE_PRIVATE:
                                    syncPrivateMessages(id);
                                    break;
                                case TYPE_PUBLIC:
                                    syncPublicMessages(id);
                                    break;
                                case TYPE_STORY:
                                    syncStoryMessages(id);
                                    break;
                            }
                        } else {
                            callback.dismissProgressBar();
                            callback.showLocalMessages(messages);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        callback.dismissProgressBar();
                    }
                });
    }

    private void getMoreOldPrivateMessages(final String userId, String maxId) {
        msgModel.getPreviousUserMessages(userId, teamId, maxId, 30)
                .map(new UnreadFunc(userId))
                .doOnNext(saveMsgAction)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Message>>() {
                    @Override
                    public void call(List<Message> messages) {
                        Collections.reverse(messages);
                        callback.showMoreOldMessages(messages, false);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    public void getMoreOldPrivateMessages(final String userId, final String maxId, Date maxCreateTime) {
        MessageRealm.getInstance()
                .getLocalMessage(userId, teamId, maxId, 0, maxCreateTime.getTime())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Message>>() {
                    @Override
                    public void call(List<Message> messages) {
                        if (messages.isEmpty()) {
                            getMoreOldPrivateMessages(userId, maxId);
                        } else {
                            callback.showMoreOldMessages(messages, true);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    private void getMoreNewPrivateMessages(final String userId, String minId) {
        msgModel.getLaterUserMessages(userId, teamId, minId, 30)
                .map(new UnreadFunc(userId))
                .doOnNext(saveMsgAction)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Message>>() {
                    @Override
                    public void call(List<Message> messages) {
                        callback.showMoreNewMessages(messages, false);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    public void getMoreNewPrivateMessages(final String userId, final String minId, Date minCreateTime) {
        MessageRealm.getInstance()
                .getLocalMessage(userId, teamId, minId, minCreateTime.getTime(), 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Message>>() {
                    @Override
                    public void call(List<Message> messages) {
                        if (messages.isEmpty()) {
                            getMoreNewPrivateMessages(userId, minId);
                        } else {
                            callback.showMoreNewMessages(messages, true);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    /**
     * get older room message from API
     * @param roomId
     * @param maxId
     */
    private void getMoreOldPublicMessages(final String roomId, String maxId) {
        msgModel.getPreviousRoomMessages(roomId, teamId, maxId, 30)
                .map(new UnreadFunc(roomId))
                .doOnNext(saveMsgAction)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Message>>() {
                    @Override
                    public void call(List<Message> messages) {
                        Collections.reverse(messages);
                        callback.showMoreOldMessages(messages, false);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.d("debug", throwable.toString());
                    }
                });
    }

    public void getMoreOldPublicMessages(final String roomId, final String maxId, Date maxCreateTime) {
        MessageRealm.getInstance()
                .getLocalMessage(roomId, teamId, maxId, 0, maxCreateTime.getTime())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Message>>() {
                    @Override
                    public void call(List<Message> messages) {
                        if (messages.isEmpty()) {
                            getMoreOldPublicMessages(roomId, maxId);
                        } else {
                            callback.showMoreOldMessages(messages, true);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    private void getMoreNewPublicMessages(final String roomId, String minId) {
        msgModel.getLaterRoomMessages(roomId, teamId, minId, 30)
                .map(new UnreadFunc(roomId))
                .doOnNext(saveMsgAction)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Message>>() {
                    @Override
                    public void call(List<Message> messages) {
                        callback.showMoreNewMessages(messages, false);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.d("debug", throwable.toString());
                    }
                });
    }

    public void getMoreNewPublicMessages(final String roomId, final String minId, Date minCreateTime) {
        MessageRealm.getInstance()
                .getLocalMessage(roomId, teamId, minId, minCreateTime.getTime(), 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Message>>() {
                    @Override
                    public void call(List<Message> messages) {
                        if (messages.isEmpty()) {
                            getMoreNewPublicMessages(roomId, minId);
                        } else {
                            callback.showMoreNewMessages(messages, true);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    private void getMoreOldStoryMessages(final String storyId, String maxId) {
        msgModel.getPreviousStoryMessages(storyId, teamId, maxId, 30)
                .map(new UnreadFunc(storyId))
                .doOnNext(saveMsgAction)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Message>>() {
                    @Override
                    public void call(List<Message> messages) {
                        Collections.reverse(messages);
                        callback.showMoreOldMessages(messages, false);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.d("debug", throwable.toString());
                    }
                });
    }

    public void getMoreOldStoryMessages(final String storyId, final String maxId, Date maxCreateTime) {
        MessageRealm.getInstance()
                .getLocalMessage(storyId, teamId, maxId, 0, maxCreateTime.getTime())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Message>>() {
                    @Override
                    public void call(List<Message> messages) {
                        if (messages.isEmpty()) {
                            getMoreOldStoryMessages(storyId, maxId);
                        } else {
                            callback.showMoreOldMessages(messages, true);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    private void getMoreNewStoryMessages(final String storyId, String minId) {
        msgModel.getLaterStoryMessages(storyId, teamId, minId, 30)
                .map(new UnreadFunc(storyId))
                .doOnNext(saveMsgAction)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Message>>() {
                    @Override
                    public void call(List<Message> messages) {
                        callback.showMoreNewMessages(messages, false);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.d("debug", throwable.toString());
                    }
                });
    }

    public void getMoreNewStoryMessages(final String storyId, final String minId, Date minCreateTime) {
        MessageRealm.getInstance()
                .getLocalMessage(storyId, teamId, minId, minCreateTime.getTime(), 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Message>>() {
                    @Override
                    public void call(List<Message> messages) {
                        if (messages.isEmpty()) {
                            getMoreNewStoryMessages(storyId, minId);
                        } else {
                            callback.showMoreNewMessages(messages, true);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    private class UnreadFunc implements Func1<List<Message>, List<Message>> {
        private String foreignId;

        public UnreadFunc(String foreignId) {
            this.foreignId = foreignId;
        }
        @Override
        public List<Message> call(List<Message> messages) {
            List<Message> unread = msgModel.getLocalUnreadMessages(foreignId);
            for (Message message : messages) {
                message.setStatus(MessageDataProcess.Status.NONE.ordinal());
                for (Message unreadMsg : unread) {
                    if (unreadMsg.get_id().equals(message.get_id())) {
                        message.setIsRead(false);
                        break;
                    }
                }
            }
            Collections.reverse(messages);
            return messages;
        }
    }

    private class UnsentFunc implements Func1<List<Message>, List<Message>> {
        private String foreignId;

        public UnsentFunc(String foreignId) {
            this.foreignId = foreignId;
        }

        @Override
        public List<Message> call(List<Message> messages) {
            List<Message> messageUnSend = msgModel.getUnSendMessage(foreignId);
            for (Message msg : messageUnSend) {
                messages.add(MessageDataProcess.getInstance().copy(msg));
            }
            return messages;
        }
    }

    private Action1<List<Message>> saveMsgAction = new Action1<List<Message>>() {
        @Override
        public void call(List<Message> messages) {
            MessageRealm.getInstance().batchAdd(messages)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<Message>>() {
                        @Override
                        public void call(List<Message> messages) { }
                    }, new RealmErrorAction());
        }
    };

    public void syncPrivateMessages(final String userId) {
        callback.showProgressBar();
        msgModel.getUserMessages(userId, teamId, 30)
                .map(new UnreadFunc(userId))
                .doOnNext(saveMsgAction)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Message>>() {
                    @Override
                    public void call(final List<Message> messages) {
                        callback.dismissProgressBar();
                        callback.showLatestMessages(messages);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        callback.dismissProgressBar();
                    }
                });
    }

    public void syncPublicMessages(final String roomId) {
        callback.showProgressBar();
        msgModel.getRoomMessages(roomId, teamId, 30)
                .map(new UnreadFunc(roomId))
                .doOnNext(saveMsgAction)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Message>>() {
                    @Override
                    public void call(final List<Message> messages) {
                        callback.dismissProgressBar();
                        callback.showLatestMessages(messages);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        callback.dismissProgressBar();
                    }
                });
    }

    public void syncStoryMessages(final String storyId) {
        callback.showProgressBar();
        msgModel.getStoryMessages(storyId, teamId, 30)
                .map(new UnreadFunc(storyId))
                .doOnNext(saveMsgAction)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Message>>() {
                    @Override
                    public void call(final List<Message> messages) {
                        callback.dismissProgressBar();
                        callback.showLatestMessages(messages);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        callback.dismissProgressBar();
                    }
                });
    }

    public void sendPrivateMessage(String toId, String body, final String msgId) {
        MessageRequestData data = new MessageRequestData(teamId, null, toId, null, body, null);
        sendMessage(data, msgId);
    }

    public void sendPublicMessage(String roomId, String body, final String msgId) {
        MessageRequestData data = new MessageRequestData(teamId, roomId, null, null, body, null);
        sendMessage(data, msgId);
    }

    public void sendStoryMessage(String storyId, String body, final String msgId) {
        MessageRequestData data = new MessageRequestData(teamId, null, null, storyId, body, null);
        sendMessage(data, msgId);
    }

    public void deleteMessage(final String messageId) {
        msgModel.deleteMessage(messageId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        MessageRealm.getInstance().deleteMessage(messageId)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Message>() {
                                    @Override
                                    public void call(Message message) {
                                        bus.post(new DeleteMessageEvent(messageId));
                                    }
                                }, new RealmErrorAction());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }

    public void updateMessage(String messageId, String body) {
        msgModel.updateMessage(messageId, body)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Message>() {
                    @Override
                    public void call(Message message) {
                        msgModel.saveMessageInDb(message);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }

    public Subscription uploadFile(final String mimeType, String path, final String tempMsgId) {
        return uploadFile(mimeType, path, tempMsgId, 0);
    }

    public Subscription uploadFile(final String mimeType, String path, final String tempMsgId, final int duration) {
        return uploadFile(mimeType, path, tempMsgId, duration, 0, 0);
    }

    public Subscription uploadFile(final String mimeType, String path, final String tempMsgId, final int duration, final int videoWidth, final int videoHeight) {
        File originFile = new File(path);
        if (originFile.length() <= 0) {
            return null;
        }
        if ("audio/amr".equals(mimeType) && originFile.length() < 100) {
            callback.onUploadFileInvalid(tempMsgId);
            return null;
        }
        TypedFile file = new TypedFile(mimeType, originFile);
        return uploadApi.uploadFile(originFile.getName(), mimeType, originFile.length(), file)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<FileUploadResponseData>() {
                    @Override
                    public void call(FileUploadResponseData fileUploadResponseData) {
                        if (mimeType.equals("audio/amr")) {
                            fileUploadResponseData.setSpeech(true);
                            fileUploadResponseData.setDuration(duration);
                        } else if (mimeType.equals("video")) {
                            fileUploadResponseData.setWidth(videoWidth);
                            fileUploadResponseData.setHeight(videoHeight);
                            fileUploadResponseData.setDuration(duration);
                        }
                        callback.onUploadFileSuccess(fileUploadResponseData, tempMsgId);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        callback.onUploadFileFailed(tempMsgId);
                    }
                });
    }

    public void sendPublicMsgWithFile(List<Attachment> attachments, String roomId, final String msgId) {
        MessageRequestData data = new MessageRequestData(teamId, roomId, null, null, null, attachments);
        sendMessage(data, msgId);
    }

    public void sendPrivateMsgWithFile(List<Attachment> attachments, String toId, final String msgId) {
        MessageRequestData data = new MessageRequestData(teamId, null, toId, null, null, attachments);
        sendMessage(data, msgId);
    }

    public void sendStoryMsgWithFile(List<Attachment> attachments, String storyId, final String msgId) {
        MessageRequestData data = new MessageRequestData(teamId, null, null, storyId, null, attachments);
        sendMessage(data, msgId);
    }

    private void sendMessage(MessageRequestData data, final String msgId) {
        talkApi.sendMessage(data)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Message>() {
                    @Override
                    public void call(final Message message) {
                        message.setStatus(MessageDataProcess.Status.NONE.ordinal());
                        BusProvider.getInstance().post(new UpdateMessageEvent(message));
                        MessageRealm.getInstance().addOrUpdate(message, msgId)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Message>() {
                                    @Override
                                    public void call(Message msg) {
                                        MessageRealm.getInstance().addOrUpdate(message)
                                                .subscribe(new EmptyAction<Message>(), new RealmErrorAction());
                                        MessageRealm.getInstance().deleteMessage(msgId)
                                                .subscribe(new EmptyAction<Message>(), new RealmErrorAction());
                                    }
                                }, new RealmErrorAction());
                        callback.onSendMessageSuccess(msgId, message);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        callback.onSendMessageFailed(msgId);
                    }
                });
    }

    public void downloadFile(final String url, final String path) {
        FileDownloader.getInstance().startDownload(url, path, new Action1<Integer>() {
            @Override
            public void call(Integer progress) {
                if (progress == FileDownloader.FINISH) {
                    callback.onDownloadFinish(path);
                } else {
                    callback.onDownloadProgress(progress);
                }
            }
        }, null);
    }

    public void joinRoom(final String roomId) {
        talkApi.joinRoom(roomId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Room>() {
                    @Override
                    public void call(Room room) {
                        if (room != null) {
                            room.setIsQuit(false);
                            RoomRealm.getInstance().addOrUpdate(room)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Action1<Room>() {
                                        @Override
                                        public void call(Room room) {
                                            MainApp.IS_ROOM_CHANGED = true;
                                            BusProvider.getInstance().post(new UpdateRoomEvent(room));
                                        }
                                    }, new RealmErrorAction());
                            callback.onJoinTopic(room);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }

    public void favoriteMessage(String messageId) {
        talkApi.favoriteMessage(messageId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        MainApp.showToastMsg(R.string.favorite_success);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        Logger.e(TAG, "favorite error", e);
                    }
                });
    }

    public void sendMessageReceipt(final String messageId) {
        talkApi.sendMessageReceipt(messageId)
                .flatMap(new Func1<Message, Observable<Message>>() {
                    @Override
                    public Observable<Message> call(Message message) {
                        return MessageRealm.getInstance().getMessage(messageId);
                    }
                })
                .filter(new Func1<Message, Boolean>() {
                    @Override
                    public Boolean call(Message message) {
                        return message != null;
                    }
                })
                .subscribe(new Action1<Message>() {
                    @Override
                    public void call(Message message) {
                        MessageRealm.getInstance().addOrUpdateEventWithCurrentThread(message);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }
}
