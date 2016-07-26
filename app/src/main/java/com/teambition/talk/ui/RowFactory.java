package com.teambition.talk.ui;

import android.media.MediaMetadataRetriever;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.teambition.talk.BizLogic;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.entity.AttachmentType;
import com.teambition.talk.entity.File;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Notification;
import com.teambition.talk.entity.Quote;
import com.teambition.talk.entity.RTF;
import com.teambition.talk.entity.Snippet;
import com.teambition.talk.ui.row.FileRow;
import com.teambition.talk.ui.row.ImageRow;
import com.teambition.talk.ui.row.InfoRow;
import com.teambition.talk.ui.row.MentionRow;
import com.teambition.talk.ui.row.MessageRow;
import com.teambition.talk.ui.row.NotificationRow;
import com.teambition.talk.ui.row.QuoteRow;
import com.teambition.talk.ui.row.Row;
import com.teambition.talk.ui.row.SpeechRow;
import com.teambition.talk.ui.row.SystemRow;
import com.teambition.talk.ui.row.TextRow;
import com.teambition.talk.ui.row.VideoRow;
import com.teambition.talk.util.MessageDialogBuilder;
import com.teambition.talk.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zeatual on 15/7/27.
 */
public class RowFactory {

    private static RowFactory instance;
    private Gson gson;

    private RowFactory() {
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    public static RowFactory getInstance() {
        if (instance == null) {
            instance = new RowFactory();
            instance.setGson(new GsonProvider.Builder().setMessageAdapter().setDateAdapter().create());
        }
        return instance;
    }

    public List<Row> makeRows(List<Message> messages, Row.OnAvatarClickListener listener,
                              MessageDialogBuilder.MessageActionCallback callback, MediaMetadataRetriever retriever) {
        List<Row> rows = new ArrayList<>();
        if (messages != null && !messages.isEmpty()) {
            for (int i = 0; i < messages.size(); i++) {
                Message msg = messages.get(i);
                rows.addAll(makeRows(msg, listener, callback, retriever));
            }
        }
        return rows;
    }

    public List<Row> makeRows(Message msg, Row.OnAvatarClickListener listener,
                              MessageDialogBuilder.MessageActionCallback callback) {
        return makeRows(msg, listener, callback, null);
    }

    public List<Row> makeRows(Message msg, Row.OnAvatarClickListener listener,
                              MessageDialogBuilder.MessageActionCallback callback, MediaMetadataRetriever retriever) {
        List<Row> rows = new ArrayList<>();
        if (msg.isSystem()) {
            rows.add(new SystemRow(msg));
        } else {
            boolean isMine = BizLogic.isMe(msg.get_creatorId());
            boolean showAvatar = !isMine;

            // do with text
            if (StringUtil.isNotBlank(msg.getBody())) {
                rows.add(new TextRow(msg, showAvatar ? msg.getCreatorAvatar() : null,
                        listener, callback));
                showAvatar = false;
            }

            // do with attachments
            JsonArray attachments = gson.fromJson(msg.getAttachments(), JsonArray.class);
            if (attachments != null && attachments.size() > 0) {
                for (JsonElement attachment : attachments) {
                    String type = attachment.getAsJsonObject().get("category").getAsString();
                    switch (AttachmentType.getEnum(type)) {
                        case QUOTE:
                            Quote quote = gson.fromJson(attachment.getAsJsonObject()
                                    .get("data"), Quote.class);
                            if (quote != null) {
                                rows.add(new QuoteRow(msg, quote,
                                        showAvatar ? msg.getCreatorAvatar() : null, callback));
                                showAvatar = false;
                            }
                            break;
                        case RTF:
                            RTF rtf = gson.fromJson(attachment.getAsJsonObject()
                                    .get("data"), RTF.class);
                            if (rtf != null) {
                                rows.add(new QuoteRow(msg, rtf, showAvatar ? msg.getCreatorAvatar()
                                        : null, listener, callback));
                                showAvatar = false;
                            }
                            break;
                        case SNIPPET:
                            Snippet snippet = gson.fromJson(attachment.getAsJsonObject()
                                    .get("data"), Snippet.class);
                            if (snippet != null) {
                                rows.add(new QuoteRow(msg, snippet, showAvatar ?
                                        msg.getCreatorAvatar() : null, listener, callback));
                                showAvatar = false;
                            }
                            break;
                        case FILE:
                        case SPEECH:
                            File file = gson.fromJson(attachment.getAsJsonObject()
                                    .get("data"), File.class);
                            if (file != null) {
                                if (AttachmentType.getEnum(type) == AttachmentType.SPEECH) {
                                    rows.add(new SpeechRow(msg, file, showAvatar ?
                                            msg.getCreatorAvatar() : null, listener, callback));
                                } else if (BizLogic.isImg(file)) {
                                    rows.add(new ImageRow(msg, file, showAvatar ?
                                            msg.getCreatorAvatar() : null, listener, callback));
                                    showAvatar = false;
                                } else {
                                    rows.add(new FileRow(msg, file, showAvatar ?
                                            msg.getCreatorAvatar() : null, listener, callback));
                                    showAvatar = false;
                                }
                            }
                            break;
                        case MESSAGE:
                            Message mentionMsg = gson.fromJson(attachment.getAsJsonObject().
                                    get("data"), Message.class);
                            if (mentionMsg != null) {
                                rows.add(new MentionRow(msg, mentionMsg, callback));
                            }
                            break;
                        case VIDEO:
                            File videoFile = gson.fromJson(attachment.getAsJsonObject()
                                    .get("data"), File.class);
                            rows.add(new VideoRow(msg, videoFile, showAvatar ?
                                    msg.getCreatorAvatar() : null, listener, callback, retriever));
                            showAvatar = false;
                            break;
                    }
                }
            }

            // do with info
            rows.add(new InfoRow(msg, msg.getCreatedAt(), msg.getCreatorName(), msg.getStatus(),
                    BizLogic.isMe(msg.get_creatorId())));
        }
        return rows;
    }

    public List<MessageRow> makeMessageRow(List<Message> messages) {
        List<MessageRow> rows = new ArrayList<>();
        for (Message msg : messages) {
            if (msg != null) {
                rows.add(makeMessageRow(msg));
            }
        }
        return rows;
    }

    public MessageRow makeMessageRow(Message message) {
        return message != null ? new MessageRow(message) : null;
    }

    public List<NotificationRow> makeNotificationRows(List<Notification> notifications,
                                                      NotificationRow.OnClickListener listener) {
        List<NotificationRow> rows = new ArrayList<>();
        for (Notification notification : notifications) {
            rows.add(makeNotificationRow(notification, listener));
        }
        return rows;
    }

    public NotificationRow makeNotificationRow(Notification notification,
                                               NotificationRow.OnClickListener listener) {
        return new NotificationRow(notification, listener);
    }
}