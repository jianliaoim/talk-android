package com.teambition.talk.realm;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.client.data.FileUploadResponseData;
import com.teambition.talk.entity.AttachmentType;
import com.teambition.talk.entity.File;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Quote;
import com.teambition.talk.entity.RTF;
import com.teambition.talk.entity.Room;
import com.teambition.talk.entity.Snippet;
import com.teambition.talk.event.UpdateMemberEvent;
import com.teambition.talk.event.UpdateRoomEvent;
import com.teambition.talk.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by wlanjie on 15/9/29.
 */
public class MessageDataProcess {

    private static MessageDataProcess messageDataProcess;

    private final Gson mGson;

    public MessageDataProcess() {
        mGson = new GsonProvider.Builder().setDateAdapter().create();
    }

    public static MessageDataProcess getInstance() {
        if (messageDataProcess == null) {
            messageDataProcess = new MessageDataProcess();
        }
        return messageDataProcess;
    }

    public void updateUnreadNum(final Message message) {
        if (message == null) return;
        if (StringUtil.isNotBlank(message.getForeignId()) && !BizLogic.isMe(message.get_creatorId())) {
            Observable.create(new Observable.OnSubscribe<Object>() {
                @Override
                public void call(Subscriber<? super Object> subscriber) {
                    if (MainApp.globalMembers.containsKey(message.getForeignId())) {
                        Member member = MainApp.globalMembers.get(message.getForeignId());
                        member.setUnread(member.getUnread() + 1);
                        MemberRealm.getInstance().addOrUpdateWithCurrentThread(member);
                        MainApp.IS_MEMBER_CHANGED = true;
                        MainApp.globalMembers.put(message.getForeignId(), member);
                    }
                    if (MainApp.globalRooms.containsKey(message.getForeignId())) {
                        Room room = MainApp.globalRooms.get(message.getForeignId());
                        room.setUnread(room.getUnread() + 1);
                        RoomRealm.getInstance().addOrUpdateWithCurrentThread(room);
                        MainApp.IS_ROOM_CHANGED = true;
                        MainApp.globalRooms.put(message.getForeignId(), room);
                    }
                }
            }).subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object o) {
                            BusProvider.getInstance().post(new UpdateRoomEvent());
                            BusProvider.getInstance().post(new UpdateMemberEvent());
                        }
                    });
        }
    }

    public void processForPersistent(Message message) {
        if (message == null) return;
        if (MainApp.globalMembers.containsKey(message.get_toId())) {
            message.getTo().setAlias(MainApp.globalMembers.get(message.get_toId()).getAlias());
        }
        if (MainApp.globalMembers.containsKey(message.get_creatorId())) {
            message.getCreator().setAlias(MainApp.globalMembers.get(message.get_creatorId()).getAlias());
        }
        if (message.get_roomId() != null) {
            message.setForeignId(message.get_roomId());
            message.setChatTitle(message.getRoom().getIsGeneral() ? MainApp.CONTEXT.getString(R.string.general) :
                    message.getRoom().getTopic());
        } else if (message.get_storyId() != null) {
            message.setForeignId(message.get_storyId());
            message.setChatTitle(message.getStory().getTitle());
        } else {
            if (BizLogic.isMe(message.get_creatorId())) {
                message.setForeignId(message.get_toId());
                message.setChatTitle(BizLogic.isXiaoai(message.getTo()) ? MainApp.CONTEXT.getString(R.string.talk_ai) :
                        message.getTo().getAlias());
            } else {
                message.setForeignId(message.get_creatorId());
                message.setChatTitle(BizLogic.isXiaoai(message.getCreator()) ? MainApp.CONTEXT.getString(R.string.talk_ai) :
                        message.getCreator().getAlias());
            }
        }
        if (StringUtil.isNotBlank(message.getAuthorName())) {
            message.setCreatorName(message.getAuthorName());
        } else if (BizLogic.isMe(message.get_creatorId())) {
            message.setCreatorName(MainApp.CONTEXT.getString(R.string.me));
        } else if (message.getCreator() != null) {
            message.setCreatorName(BizLogic.isXiaoai(message.getCreator()) ? MainApp.CONTEXT.getString(R.string.talk_ai) :
                    message.getCreator().getAlias());
        } else {
            message.setCreatorName(MainApp.CONTEXT.getString(R.string.anonymous_user));
        }
        if (StringUtil.isNotBlank(message.getAuthorAvatarUrl())) {
            message.setCreatorAvatar(message.getAuthorAvatarUrl());
        } else if (message.getCreator() != null) {
            message.setCreatorAvatar(message.getCreator().getAvatarUrl());
        }
        if (message.getTags() != null && !message.getTags().isEmpty()) {
            message.setTagToJson(mGson.toJson(message.getTags()));
        }
        if (message.getReceiptors() != null && !message.getReceiptors().isEmpty()) {
            message.setReceiptorsStr(mGson.toJson(message.getReceiptors()));
        }
    }

    public List<File> getImages(final Message message) {
        List<File> images = new ArrayList<>();
        JsonArray attachments = mGson.fromJson(message.getAttachments(), JsonArray.class);
        for (JsonElement attachment : attachments) {
            String type = attachment.getAsJsonObject().get("category").getAsString();
            if (AttachmentType.FILE.equals(AttachmentType.getEnum(type))) {
                File file = mGson.fromJson(attachment.getAsJsonObject().get("data"), File.class);
                if (BizLogic.isImg(file)) {
                    images.add(file);
                }

            }
        }
        return images;
    }

    public Quote getQuote(final Message message) {
        JsonArray attachments = mGson.fromJson(message.getAttachments(), JsonArray.class);
        for (JsonElement attachment : attachments) {
            String type = attachment.getAsJsonObject().get("category").getAsString();
            if (AttachmentType.QUOTE.equals(AttachmentType.getEnum(type))) {
                return mGson.fromJson(attachment.getAsJsonObject().get("data"), Quote.class);
            }
        }
        return null;
    }

    public RTF getRTF(final Message message) {
        JsonArray attachments = mGson.fromJson(message.getAttachments(), JsonArray.class);
        for (JsonElement attachment : attachments) {
            String type = attachment.getAsJsonObject().get("category").getAsString();
            if (AttachmentType.RTF.equals(AttachmentType.getEnum(type))) {
                return mGson.fromJson(attachment.getAsJsonObject().get("data"), RTF.class);
            }
        }
        return null;
    }

    public Snippet getSnippet(final Message message) {
        JsonArray attachments = mGson.fromJson(message.getAttachments(), JsonArray.class);
        for (JsonElement attachment : attachments) {
            String type = attachment.getAsJsonObject().get("category").getAsString();
            if (AttachmentType.SNIPPET.equals(AttachmentType.getEnum(type))) {
                return mGson.fromJson(attachment.getAsJsonObject().get("data"), Snippet.class);
            }
        }
        return null;
    }

    /**
     * @return the first file attachment of message
     */
    public File getFile(final Message message) {
        JsonArray attachments = mGson.fromJson(message.getAttachments(), JsonArray.class);
        for (JsonElement attachment : attachments) {
            String type = attachment.getAsJsonObject().get("category").getAsString();
            if (AttachmentType.FILE.equals(AttachmentType.getEnum(type))
                    || AttachmentType.SPEECH.equals(AttachmentType.getEnum(type))
                    || AttachmentType.VIDEO.equals(AttachmentType.getEnum(type))) {
                return mGson.fromJson(attachment.getAsJsonObject().get("data"), File.class);
            }
        }
        return null;
    }

    public void setImage(FileUploadResponseData file, final Message message) {
        message.setAttachments("[{\"category\":\"file\",\"data\":" + mGson.toJson(file) + "}]");
    }

    public void setSpeech(FileUploadResponseData speech, final Message message) {
        message.setAttachments("[{\"category\":\"speech\",\"data\":" + mGson.toJson(speech) + "}]");
    }

    public void setVideo(FileUploadResponseData video, final Message message) {
        message.setAttachments("[{\"category\":\"video\",\"data\":" + mGson.toJson(video) + "}]");
    }

    public void setVideoDuration(int duration, final Message message) {
        JsonArray attachments = mGson.fromJson(message.getAttachments(), JsonArray.class);
        for (JsonElement attachment : attachments) {
            String type = attachment.getAsJsonObject().get("category").getAsString();
            if (AttachmentType.VIDEO.equals(AttachmentType.getEnum(type))) {
                FileUploadResponseData data = mGson.fromJson(attachment.getAsJsonObject().get("data"), FileUploadResponseData.class);
                data.setDuration(duration);
                setVideo(data, message);
            }
        }
    }

    public FileUploadResponseData getUploadData(final Message message) {
        JsonArray attachments = mGson.fromJson(message.getAttachments(), JsonArray.class);
        for (JsonElement attachment : attachments) {
            String type = attachment.getAsJsonObject().get("category").getAsString();
            if (AttachmentType.FILE.equals(AttachmentType.getEnum(type))) {
                return mGson.fromJson(attachment.getAsJsonObject().get("data"),
                        FileUploadResponseData.class);
            }
        }
        return null;
    }

    public Message copy(final Message msg) {
        Message message = new Message();
        message.set_id(msg.get_id());
        message.setBody(msg.getBody());
        message.setAttachments(msg.getAttachments());
        message.setIsSystem(msg.isSystem());
        message.setCreatedAt(msg.getCreatedAt());
        message.set_teamId(msg.get_teamId());
        message.set_toId(msg.get_toId());
        message.set_roomId(msg.get_roomId());
        message.set_creatorId(msg.get_creatorId());
        message.set_storyId(msg.get_storyId());
        message.setDisplayMode(msg.getDisplayMode());
        message.setIsRead(msg.isRead());
        message.setAudioLocalPath(msg.getAudioLocalPath());
        message.setForeignId(msg.getForeignId());
        message.setCreatorName(msg.getCreatorName());
        message.setCreatorAvatar(msg.getCreatorAvatar());
        message.setChatTitle(msg.getChatTitle());
        message.setStatus(msg.getStatus());
        message.setTagToJson(msg.getTagToJson());
        message.setReceiptorsStr(msg.getReceiptorsStr());
        return message;
    }


    public enum Status {

        NONE("none"),
        SENDING("sending"),
        SEND_FAILED("send_failed"),
        UPLOADING("uploading"),
        UPLOAD_FAILED("upload_failed");

        private String value;

        Status(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        public static Status getEnum(int ordinal) {
            for (Status v : values()) {
                if (v.ordinal() == ordinal) {
                    return v;
                }
            }
            return NONE;
        }
    }

    public enum DisplayMode {

        MESSAGE("message"),
        FILE("file"),
        IMAGE("image"),
        RTF("rtf"),
        SYSTEM("system"),
        INTEGRATION("integration"),
        SPEECH("speech"),
        VIDEO("video"),
        SNIPPET("snippet");

        private String value;

        DisplayMode(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        public static DisplayMode getEnum(String value) {
            for (DisplayMode v : values()) {
                if (v.value.equalsIgnoreCase(value)) {
                    return v;
                }
            }
            return MESSAGE;
        }

        public String value() {
            return value;
        }
    }

    public enum ReservedType {

        NONE("none"),
        VOICECALL("voice-call");

        private String value;

        ReservedType(String value) {
            this.value = value;
        }

        public static ReservedType getEnum(String value) {
            for (ReservedType v : values()) {
                if (v.value.equalsIgnoreCase(value)) {
                    return v;
                }
            }
            return NONE;
        }
    }

}
