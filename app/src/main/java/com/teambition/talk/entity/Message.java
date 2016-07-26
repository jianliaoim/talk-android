package com.teambition.talk.entity;

import android.media.MediaPlayer;
import android.net.Uri;

import com.teambition.talk.BizLogic;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.client.data.FileUploadResponseData;
import com.teambition.talk.realm.MessageDataProcess;
import com.teambition.talk.yuv.MediaController;

import org.parceler.Parcel;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.realm.MessageRealmProxy;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * Created by zeatual on 15/7/27.
 */
@Parcel(implementations = {MessageRealmProxy.class}, value = Parcel.Serialization.BEAN, analyze = {Message.class})
@RealmClass
public class Message extends RealmObject {

    //realm key
    public static final String ID = "_id";
    public static final String FOREIGN_ID = "foreignId";
    public static final String STATUS = "status";
    public static final String IS_READ = "isRead";
    public static final String TEAM_ID = "_teamId";
    public static final String CREATE_AT_TIME = "createAtTime";

    public static final String TAG = Message.class.getSimpleName();
    public static final String SCHEME_FILE = "file://";

    //realm
    @PrimaryKey
    private String _id;
    private String body;
    private String attachments;
    private boolean isSystem;
    private String _teamId;
    private String _toId;
    private String _roomId;
    private String _storyId;
    private String _creatorId;
    private String displayMode;
    private long createAtTime;

    @Ignore
    private Date createdAt;
    @Ignore
    private String authorName;
    @Ignore
    private String authorAvatarUrl;
    @Ignore
    private Member creator;
    @Ignore
    private Member to;
    @Ignore
    private Room room;
    @Ignore
    private Story story;
    @Ignore
    private float audioProgress;
    @Ignore
    private int audioProgressSec;
    @Ignore
    private Highlight highlight;
    @Ignore
    private List<Tag> tags;
    @Ignore
    private List<String> receiptors;
    @Ignore
    private String tempId;

    // useful when it's treated as favorite
    @Ignore
    private String _messageId;

    // for audio message
    private boolean isRead = true;
    private String audioLocalPath;

    // for persistent & pojo specially
    private String foreignId;
    private String creatorName;
    private String creatorAvatar;
    private String chatTitle;
    private int status;
    private String tagToJson;
    private String markId;
    private String receiptorsStr;

    public Message() {
    }

    public Message(int status) {
        this._id = UUID.randomUUID().toString();
        this._teamId = BizLogic.getTeamId();
        this.createdAt = new Date(System.currentTimeMillis());
        this.createAtTime = System.currentTimeMillis();
        this.creatorName = MainApp.CONTEXT.getString(R.string.me);
        this._creatorId = BizLogic.getUserInfo().get_id();
        this.creator = MainApp.globalMembers.get(BizLogic.getUserInfo().get_id());
        this.status = status;
    }

    public static Message newPreSendTextInstance(String body) {
        Message message = new Message(MessageDataProcess.Status.SENDING.ordinal());
        message.setBody(body);
        return message;
    }

    public static Message newPreSendImageInstance(java.io.File file) {
        Message message = new Message(MessageDataProcess.Status.UPLOADING.ordinal());
        message.setDisplayMode(MessageDataProcess.DisplayMode.IMAGE.value());
        FileUploadResponseData data = new FileUploadResponseData();
        data.setThumbnailUrl(SCHEME_FILE + file.getAbsolutePath());
        String fileType;
        if (file.getName().lastIndexOf(".") != -1 && file.getName().lastIndexOf(".") != 0) {
            fileType = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        } else {
            fileType = "jpg";
        }
        data.setFileType(fileType);
        MessageDataProcess.getInstance().setImage(data, message);
        return message;
    }

    public static Message newPreSendSpeechInstance(String path, int duration) {
        Message message = new Message(MessageDataProcess.Status.UPLOADING.ordinal());
        message.setDisplayMode(MessageDataProcess.DisplayMode.SPEECH.value());
        message.setAudioLocalPath(path);
        FileUploadResponseData data = new FileUploadResponseData();
        data.setSpeech(true);
        data.setFileType("amr");
        data.setDuration(duration);
        MessageDataProcess.getInstance().setSpeech(data, message);
        return message;
    }

    public static Message newPreSendFileInstance(java.io.File file, String mimeType) {
        Message message = new Message(MessageDataProcess.Status.UPLOADING.ordinal());
        message.setDisplayMode(MessageDataProcess.DisplayMode.FILE.value());
        FileUploadResponseData data = new FileUploadResponseData();
        data.setThumbnailUrl(SCHEME_FILE + file.getAbsolutePath());
        data.setFileType(mimeType);
        data.setFileSize((int) file.length());
        data.setFileName(file.getName());
        MessageDataProcess.getInstance().setImage(data, message);
        return message;
    }

    public static Message newPreSendVideoInstance(java.io.File file) {
        Message message = new Message(MessageDataProcess.Status.UPLOADING.ordinal());
        message.setDisplayMode(MessageDataProcess.DisplayMode.VIDEO.value());
        FileUploadResponseData data = new FileUploadResponseData();
        MediaPlayer mediaPlayer = MediaPlayer.create(MainApp.CONTEXT, Uri.fromFile(file));
        data.setDuration(mediaPlayer.getDuration());
        mediaPlayer.release();
        data.setFileSize((int) file.length());
        data.setFileName(file.getName());
        data.setDownloadUrl(file.getAbsolutePath());
        MessageDataProcess.getInstance().setVideo(data, message);
        return message;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setIsSystem(boolean isSystem) {
        this.isSystem = isSystem;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public long getCreateAtTime() {
        return createAtTime;
    }

    public void setCreateAtTime(long createAtTime) {
        this.createAtTime = createAtTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String get_teamId() {
        return _teamId;
    }

    public void set_teamId(String _teamId) {
        this._teamId = _teamId;
    }

    public String get_toId() {
        return _toId;
    }

    public void set_toId(String _toId) {
        this._toId = _toId;
    }

    public String get_roomId() {
        return _roomId;
    }

    public void set_roomId(String _roomId) {
        this._roomId = _roomId;
    }

    public String get_creatorId() {
        return _creatorId;
    }

    public void set_creatorId(String _creatorId) {
        this._creatorId = _creatorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorAvatarUrl() {
        return authorAvatarUrl;
    }

    public void setAuthorAvatarUrl(String authorAvatarUrl) {
        this.authorAvatarUrl = authorAvatarUrl;
    }

    public Member getCreator() {
        return creator;
    }

    public void setCreator(Member creator) {
        this.creator = creator;
    }

    public Member getTo() {
        return to;
    }

    public void setTo(Member to) {
        this.to = to;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getForeignId() {
        return foreignId;
    }

    public void setForeignId(String foreignId) {
        this.foreignId = foreignId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatorAvatar() {
        return creatorAvatar;
    }

    public void setCreatorAvatar(String creatorAvatar) {
        this.creatorAvatar = creatorAvatar;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public String getAudioLocalPath() {
        return audioLocalPath;
    }

    public void setAudioLocalPath(String audioLocalPath) {
        this.audioLocalPath = audioLocalPath;
    }

    public float getAudioProgress() {
        return audioProgress;
    }

    public void setAudioProgress(float audioProgress) {
        this.audioProgress = audioProgress;
    }

    public int getAudioProgressSec() {
        return audioProgressSec;
    }

    public void setAudioProgressSec(int audioProgressSec) {
        this.audioProgressSec = audioProgressSec;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public String get_messageId() {
        return _messageId;
    }

    public void set_messageId(String _messageId) {
        this._messageId = _messageId;
    }

    public String getTagToJson() {
        return tagToJson;
    }

    public void setTagToJson(String tagToJson) {
        this.tagToJson = tagToJson;
    }

    public String getChatTitle() {
        return chatTitle;
    }

    public void setChatTitle(String chatTitle) {
        this.chatTitle = chatTitle;
    }

    public Story getStory() {
        return story;
    }

    public void setStory(Story story) {
        this.story = story;
    }

    public String get_storyId() {
        return _storyId;
    }

    public void set_storyId(String _storyId) {
        this._storyId = _storyId;
    }

    public String getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(String displayMode) {
        this.displayMode = displayMode;
    }

    public Highlight getHighlight() {
        return highlight;
    }

    public void setHighlight(Highlight highlight) {
        this.highlight = highlight;
    }

    public String getMarkId() {
        return markId;
    }

    public void setMarkId(String markId) {
        this.markId = markId;
    }

    public List<String> getReceiptors() {
        return receiptors;
    }

    public void setReceiptors(List<String> receiptors) {
        this.receiptors = receiptors;
    }

    public String getReceiptorsStr() {
        return receiptorsStr;
    }

    public void setReceiptorsStr(String receiptorsStr) {
        this.receiptorsStr = receiptorsStr;
    }

    public String getTempId() {
        return tempId;
    }

    public void setTempId(String tempId) {
        this.tempId = tempId;
    }
}