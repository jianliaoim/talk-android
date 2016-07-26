package com.teambition.talk.ui.row;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.makeramen.roundedimageview.RoundedImageView;
import com.teambition.talk.BizLogic;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.MediaController;
import com.teambition.talk.R;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.entity.File;
import com.teambition.talk.entity.Message;
import com.teambition.talk.realm.MessageRealm;
import com.teambition.talk.ui.widget.AudioMessageView;
import com.teambition.talk.util.DensityUtil;
import com.teambition.talk.util.MessageDialogBuilder;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.ThemeUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zeatual on 15/7/29.
 */
public class SpeechRow extends Row {
    public static final int STATE_STOP = 0;
    public static final int STATE_PLAYING = 1;

    private Message message;
    private File file;
    private String avatarUrl;
    private boolean isMine;
    private MessageDialogBuilder.MessageActionCallback callback;

    public static class SpeechRowHolder {

        @InjectView(R.id.audio_view)
        AudioMessageView audioView;
        @Optional
        @InjectView(R.id.view_unread)
        View viewUnread;
        @Optional
        @InjectView(R.id.img_avatar)
        RoundedImageView imgAvatar;
        public String messageId;

        public SpeechRowHolder(View view) {
            ButterKnife.inject(this, view);
        }

        public void updateProgress(float progress, int progressSec) {
            audioView.setProgressRatio(progress);
            String timeString = String.format("%02d:%02d", progressSec / 60, progressSec % 60);
            audioView.setText(timeString);
        }

        public void updateButtonState(int state) {
            Drawable icon = null;
            String colorName = BizLogic.getTeamColor();
            Resources res = audioView.getContext().getResources();
            if (state == STATE_STOP) {
                icon = ThemeUtil.getThemeDrawable(res, R.drawable.ic_audio_play, colorName);
            } else if (state == STATE_PLAYING) {
                icon = ThemeUtil.getThemeDrawable(res, R.drawable.ic_audio_pause, colorName);
            }
            audioView.setButtonDrawable(icon);
        }

    }

    public SpeechRow(Message msg, File file, String avatarUrl, OnAvatarClickListener listener,
                     MessageDialogBuilder.MessageActionCallback callback) {
        super(msg, listener);
        this.message = msg;
        this.file = file;
        this.avatarUrl = avatarUrl;
        this.callback = callback;
        isMine = BizLogic.isMe(msg.get_creatorId());
    }

    @Override
    public View getView(View convertView, final ViewGroup parent) {
        SpeechRowHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(isMine ? R.layout.item_row_speech_self : R.layout.item_row_speech, null);
            holder = new SpeechRowHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (SpeechRowHolder) convertView.getTag();
        }

        int width = file.getDuration() < 20 ? 104 : (file.getDuration() < 40 ? 150 : 196);
        ViewGroup.LayoutParams layoutParams = holder.audioView.getLayoutParams();
        layoutParams.width = DensityUtil.dip2px(parent.getContext(), width);
        holder.audioView.setLayoutParams(layoutParams);
        if (holder.viewUnread != null) {
            holder.viewUnread.setVisibility(message.isRead() ? View.GONE : View.VISIBLE);
        }
        int state = !MediaController.getInstance().isPlayingAudio(message) || MediaController.getInstance().isAudioPaused(message)
                ? STATE_STOP : STATE_PLAYING;
        holder.updateButtonState(state);
        final SpeechRowHolder finalHolder = holder;
        holder.audioView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isPlaying = MediaController.getInstance().isPlayingAudio(message);
                if (!isPlaying) {
                    // start playing
                    playAudio(message, file, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            finalHolder.updateButtonState(STATE_STOP);
                            MainApp.showToastMsg(R.string.audio_download_fail);
                        }
                    });
                    finalHolder.updateButtonState(STATE_PLAYING);
                    if (!message.isRead()) {
                        message.setIsRead(true);
                        MessageRealm.getInstance().addOrUpdate(message)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Message>() {
                                    @Override
                                    public void call(Message message) {
                                    }
                                }, new RealmErrorAction());
                        if (finalHolder.viewUnread != null) {
                            finalHolder.viewUnread.setVisibility(View.GONE);
                        }
                    }
                } else if (isPlaying && MediaController.getInstance().isAudioPaused()) {
                    // resume playing
                    MediaController.getInstance().startPlay(message);
                    finalHolder.updateButtonState(STATE_PLAYING);
                } else if (isPlaying && !MediaController.getInstance().isAudioPaused()) {
                    // pause playing
                    MediaController.getInstance().pauseAudio(message);
                    finalHolder.updateButtonState(STATE_STOP);
                }
            }
        });
        int duration;
        if (MediaController.getInstance().isPlayingAudio(message)) {
            duration = message.getAudioProgressSec();
        } else {
            duration = file.getDuration();
        }
        String timeString = String.format("%02d:%02d", duration / 60, duration % 60);
        int unreachedColor = isMine ? parent.getContext().getResources().getColor(ThemeUtil
                .getThemeColorLightRes(BizLogic.getTeamColor())) : Color.WHITE;
        holder.audioView.setUnreachedColor(unreachedColor);
        int reachedColor = isMine ? parent.getContext().getResources().getColor(ThemeUtil
                .getThemeColorLightPressedRes(BizLogic.getTeamColor())) :
                parent.getContext().getResources().getColor(ThemeUtil
                        .getThemeColorLightRes(BizLogic.getTeamColor()));
        holder.audioView.setReachedColor(reachedColor);
        holder.audioView.setText(timeString);
        holder.audioView.setProgressRatio(message.getAudioProgress());

        if (StringUtil.isNotBlank(avatarUrl)) {
            MainApp.IMAGE_LOADER.displayImage(avatarUrl, holder.imgAvatar, ImageLoaderConfig.AVATAR_OPTIONS);
            setAvatarListener(holder.imgAvatar);
        }
        holder.messageId = message.get_id();
        holder.audioView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MessageDialogBuilder builder = new MessageDialogBuilder(parent.getContext(), getMessage(), callback);
                builder.favorite()
                        .tag()
                        .forward();
                if (BizLogic.isAdmin() || isMine) {
                    builder.delete();
                }
                builder.show();
                return true;
            }
        });
        return convertView;
    }

    @Override
    public int getViewType() {
        return isMine ? RowType.SPEECH_SELF_ROW.ordinal() : RowType.SPEECH_ROW.ordinal();
    }
}
