package com.teambition.talk.ui.row;

import android.view.View;
import android.view.ViewGroup;

import com.teambition.talk.BusProvider;
import com.teambition.talk.FileDownloader;
import com.teambition.talk.MediaController;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.entity.File;
import com.teambition.talk.entity.Message;
import com.teambition.talk.event.NewMessageEvent;
import com.teambition.talk.realm.MessageRealm;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zeatual on 15/7/27.
 */
public abstract class Row {

    public interface OnAvatarClickListener {
        void onAvatarClick(String memberId);

        void onAvatarLongClick(String memberId);
    }

    private Message message;

    private OnAvatarClickListener listener;

    public abstract View getView(View convertView, ViewGroup parent);

    public abstract int getViewType();

    public Message getMessage() {
        return message;
    }

    public OnAvatarClickListener getListener() {
        return listener;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Row(Message message) {
        this.message = message;
    }

    public Row(Message message, OnAvatarClickListener listener) {
        this.message = message;
        this.listener = listener;
    }

    protected void playAudio(final Message message, final File file, Action1<Throwable> errorAction) {
        if (message.getAudioLocalPath() == null || !new java.io.File(message.getAudioLocalPath()).exists()) {
            final String savingPath = FileDownloader.getAudioPath(file.getFileKey());
            FileDownloader.getInstance().startDownload(
                    file.getDownloadUrl(), savingPath, new Action1<Integer>() {
                        @Override
                        public void call(Integer process) {
                            if (process == FileDownloader.FINISH) {
                                message.setAudioLocalPath(savingPath);
                                MessageRealm.getInstance().addOrUpdate(message)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Action1<Message>() {
                                            @Override
                                            public void call(Message message) {
                                            }
                                        }, new RealmErrorAction());
                                MediaController.getInstance().startPlay(message);
                            }
                        }
                    }, errorAction);
        } else {
            MediaController.getInstance().startPlay(message);
        }
    }

    protected void setAvatarListener(View avatar) {
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onAvatarClick(message.get_creatorId());
                }
            }
        });
        avatar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.onAvatarLongClick(message.get_creatorId());
                }
                return true;
            }
        });
    }
}
