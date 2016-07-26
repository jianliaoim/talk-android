package com.teambition.talk.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;

import com.rockerhieu.emojicon.EmojiconEditText;
import com.talk.dialog.TalkDialog;
import com.teambition.talk.BizLogic;
import com.teambition.talk.Constant;
import com.teambition.talk.R;
import com.teambition.talk.entity.File;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Tag;
import com.teambition.talk.realm.MessageDataProcess;
import com.teambition.talk.ui.MessageFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zeatual on 15/8/11.
 */
public class MessageDialogBuilder {

    public interface MessageActionCallback {
        void deleteMessage(Message msg);

        void editMessage(Message msg, String text);

        void saveFile(String fileName, String fileType, String downloadUrl);

        void copyText(CharSequence text);

        void favorite(String msgId);

        void tag(String msgId, List<Tag> tags);

        void addTag(Message msg);

        void forward(String msgId);

    }

    private Context context;
    private Message message;
    private MessageActionCallback callback;
    private TalkDialog.Builder builder;
    private List<CharSequence> actions;

    public MessageDialogBuilder(Context context, Message message, MessageActionCallback callback) {
        this.context = context;
        this.message = message;
        this.callback = callback;
        builder = new TalkDialog.Builder(context);
        actions = new ArrayList<>();
    }

    public MessageDialogBuilder delete() {
        actions.add(context.getString(R.string.delete));
        return this;
    }

    public MessageDialogBuilder edit() {
        actions.add(context.getString(R.string.edit));
        return this;
    }

    public MessageDialogBuilder saveFile() {
        actions.add(context.getString(R.string.save_to_local));
        return this;
    }

    public MessageDialogBuilder copyText() {
        actions.add(context.getString(R.string.copy));
        return this;
    }

    public MessageDialogBuilder favorite() {
        actions.add(context.getString(R.string.favorite));
        return this;
    }

    public MessageDialogBuilder tag() {
        actions.add(context.getString(R.string.tag));
        return this;
    }

    public MessageDialogBuilder addTag() {
        actions.add(context.getString(R.string.add_tag));
        return this;
    }

    public MessageDialogBuilder forward() {
        actions.add(context.getString(R.string.transmit));
        return this;
    }

    public MessageDialogBuilder copyMaterialDialog() {
        builder = null;
        builder = new TalkDialog.Builder(context);
        return this;
    }

    public void show() {
        builder.items(actions.toArray(new CharSequence[actions.size()]))
                .itemsCallback(new TalkDialog.ListCallback() {
                    @Override
                    public void onSelection(TalkDialog materialDialog, View view, int i,
                                            CharSequence charSequence) {
                        String action = charSequence.toString();
                        if (context.getString(R.string.delete).equals(action)) {
                            new TalkDialog.Builder(context)
                                    .title(R.string.title_delete_message)
                                    .titleColorRes(R.color.white)
                                    .titleBackgroundColorRes(R.color.talk_warning)
                                    .content(R.string.message_delete_message)
                                    .positiveText(R.string.confirm)
                                    .positiveColorRes(R.color.talk_warning)
                                    .negativeText(R.string.cancel)
                                    .negativeColorRes(R.color.material_grey_700)
                                    .callback(new TalkDialog.ButtonCallback() {
                                        @Override
                                        public void onPositive(TalkDialog materialDialog, View v) {
                                            callback.deleteMessage(message);
                                        }
                                    })
                                    .show();
                        } else if (context.getString(R.string.edit).equals(action)) {
                            Spannable text = MessageFormatter.formatToSpannable(message.getBody());
                            View updateMsgView = LayoutInflater.from(context)
                                    .inflate(R.layout.dialog_message_update, null);
                            final EmojiconEditText updateMsgET = (EmojiconEditText) updateMsgView
                                    .findViewById(R.id.update_msg_et);
                            updateMsgET.setText(text);
                            updateMsgET.setSelection(updateMsgET.getText().length());
                            new TalkDialog.Builder(context)
                                    .title(R.string.action_edit)
                                    .titleColorRes(R.color.white)
                                    .titleBackgroundColorRes(R.color.colorPrimary)
                                    .customView(updateMsgView, false)
                                    .positiveText(R.string.save)
                                    .negativeColorRes(R.color.material_grey_700)
                                    .negativeText(R.string.cancel)
                                    .callback(new TalkDialog.ButtonCallback() {
                                        @Override
                                        public void onPositive(TalkDialog materialDialog, View v) {
                                            callback.editMessage(message, MessageFormatter
                                                    .formatToPost(updateMsgET.getText()));
                                        }
                                    }).show();

                        } else if (context.getString(R.string.save_to_local).equals(action)) {
                            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(
                                    Context.CONNECTIVITY_SERVICE);
                            final File file = MessageDataProcess.getInstance().getFile(message);
                            if (file != null) {
                                if (file.getFileSize() > Constant.MAX_FILE_SIZE_CELLULAR && Connectivity.isConnectedMobile(context)) {
                                    new TalkDialog.Builder(context)
                                            .title(R.string.confirm_download)
                                            .titleColorRes(R.color.white)
                                            .titleBackgroundColorRes(R.color.talk_red)
                                            .content(R.string.file_too_large)
                                            .positiveText(R.string.confirm)
                                            .negativeText(R.string.cancel)
                                            .callback(new TalkDialog.ButtonCallback() {
                                                @Override
                                                public void onPositive(TalkDialog dialog, View v) {
                                                    callback.saveFile(file.getFileName(), file.getFileType(), file.getDownloadUrl());
                                                }
                                            })
                                            .build()
                                            .show();
                                } else {
                                    callback.saveFile(file.getFileName(), file.getFileType(), file.getDownloadUrl());
                                }
                            }
                        } else if (context.getString(R.string.copy).equals(action)) {
                            String text = MessageFormatter.formatToPureText(message.getBody());
                            callback.copyText(text);
                        } else if (context.getString(R.string.favorite).equals(action)) {
                            callback.favorite(message.get_id());
                        } else if (context.getString(R.string.tag).equals(action)) {
                            callback.tag(message.get_id(), message.getTags());
                        } else if (context.getString(R.string.add_tag).equals(action)) {
                            callback.addTag(message);
                        } else if (context.getString(R.string.transmit).equals(action)) {
                            callback.forward(message.get_id());
                        }
                    }
                }).show();
    }
}
