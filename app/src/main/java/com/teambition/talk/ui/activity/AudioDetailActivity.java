package com.teambition.talk.ui.activity;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.otto.Subscribe;
import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.FileDownloader;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.MediaController;
import com.teambition.talk.R;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.entity.Message;
import com.teambition.talk.event.AudioProgressChangeEvent;
import com.teambition.talk.event.AudioResetEvent;
import com.teambition.talk.event.NewMessageEvent;
import com.teambition.talk.realm.MessageDataProcess;
import com.teambition.talk.realm.MessageRealm;
import com.teambition.talk.ui.MessageFormatter;
import com.teambition.talk.ui.widget.AudioMessageView;
import com.teambition.talk.util.DensityUtil;
import com.teambition.talk.util.ThemeUtil;

import org.parceler.Parcels;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by nlmartian on 6/1/15.
 */
public class AudioDetailActivity extends BaseActivity {
    public static final String EXTRA_MESSAGE = "message";
    public static final int STATE_STOP = 0;
    public static final int STATE_PLAYING = 1;

    @InjectView(R.id.avatar)
    RoundedImageView avatar;
    @InjectView(R.id.audio_view)
    AudioMessageView audioView;
    @InjectView(R.id.name)
    TextView tvName;
    @InjectView(R.id.time)
    TextView tvTime;
    @InjectView(R.id.unread_dot)
    View unreadDot;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    private Message message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_detail);
        ButterKnife.inject(this);

        unreadDot.setVisibility(View.GONE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.details);

        message = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_MESSAGE));
        if (message != null) {
            initWidgets();
        }

        BusProvider.getInstance().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MediaController.getInstance().stopPlaying();
    }

    @Override
    protected void onDestroy() {
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onAudioProgressChangeEvent(AudioProgressChangeEvent event) {
        if (MediaController.getInstance().isPlayingAudio(event.message)) {
            if (event.message.get_id().equals(message.get_id())) {
                updateProgress(event.message.getAudioProgress(),
                        event.message.getAudioProgressSec());
            }
        }
    }

    @Subscribe
    public void onAudioRestEvent(AudioResetEvent event) {
        if (event.message.get_id().equals(message.get_id())) {
            updateButtonState(STATE_STOP);
            updateProgress(0, MessageDataProcess.getInstance().getFile(event.message).getDuration());
        }
    }

    private void initWidgets() {
        MainApp.IMAGE_LOADER.displayImage(message.getCreator().getAvatarUrl(),
                avatar, ImageLoaderConfig.AVATAR_OPTIONS);
        tvName.setText(message.getCreator().getAlias());
        tvTime.setText(MessageFormatter.formatCreateTime(message.getCreatedAt()));
        updateButtonState(STATE_STOP);
        int duration = 0;
        if (MediaController.getInstance().isPlayingAudio(message)) {
            duration = message.getAudioProgressSec();
        } else {
            duration = MessageDataProcess.getInstance().getFile(message).getDuration();
        }
        String timeString = String.format("%02d:%02d", duration / 60, duration % 60);
        audioView.setText(timeString);
        int width = MessageDataProcess.getInstance().getFile(message).getDuration() < 20 ? 104
                : (MessageDataProcess.getInstance().getFile(message).getDuration() < 40 ? 150 : 196);
        ViewGroup.LayoutParams layoutParams = audioView.getLayoutParams();
        layoutParams.width = DensityUtil.dip2px(this, width);
        audioView.setLayoutParams(layoutParams);
        int unreachedColor = getResources().getColor(ThemeUtil.getThemeColorLightRes(BizLogic.getTeamColor()));
        audioView.setUnreachedColor(unreachedColor);
        int reachedColor = getResources().getColor(ThemeUtil.getThemeColorLightPressedRes(BizLogic.getTeamColor()));
        audioView.setReachedColor(reachedColor);
        audioView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isPlaying = MediaController.getInstance().isPlayingAudio(message);
                if (!isPlaying) {
                    // start playing
                    playAudio(message, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            updateButtonState(STATE_STOP);
                            MainApp.showToastMsg(R.string.audio_download_fail);
                        }
                    });
                    updateButtonState(STATE_PLAYING);
                } else if (isPlaying && MediaController.getInstance().isAudioPaused()) {
                    // resume playing
                    MediaController.getInstance().startPlay(message);
                    updateButtonState(STATE_PLAYING);
                } else if (isPlaying && !MediaController.getInstance().isAudioPaused()) {
                    // pause playing
                    MediaController.getInstance().pauseAudio(message);
                    updateButtonState(STATE_STOP);
                }
            }
        });
    }

    private void playAudio(final Message message, Action1<Throwable> errorAction) {
        final String savingPath = FileDownloader.getAudioPath(MessageDataProcess.getInstance().getFile(message).getFileKey());
        if (message.getAudioLocalPath() == null || !new File(message.getAudioLocalPath()).exists()) {
            FileDownloader.getInstance().startDownload(
                    MessageDataProcess.getInstance().getFile(message).getDownloadUrl(), savingPath, new Action1<Integer>() {
                        @Override
                        public void call(Integer process) {
                            if (process == FileDownloader.FINISH) {
                                message.setAudioLocalPath(savingPath);
                                MessageRealm.getInstance().addOrUpdate(message)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Action1<Message>() {
                                            @Override
                                            public void call(Message message) {
                                                BusProvider.getInstance().post(new NewMessageEvent(message));
                                            }
                                        }, new RealmErrorAction());
                                MediaController.getInstance().startPlay(AudioDetailActivity.this.message);
                            }
                        }
                    }, errorAction);
        } else {
            MediaController.getInstance().startPlay(this.message);
        }
    }

    private void updateButtonState(int state) {
        String colorName = BizLogic.getTeamColor();
        Resources res = audioView.getContext().getResources();
        if (state == STATE_STOP) {
            audioView.setButtonDrawable(ThemeUtil.getThemeDrawable(res, R.drawable.ic_audio_play, colorName));
        } else if (state == STATE_PLAYING) {
            audioView.setButtonDrawable(ThemeUtil.getThemeDrawable(res, R.drawable.ic_audio_pause, colorName));
        }
    }

    private void updateProgress(float progress, int progressSec) {
        audioView.setProgressRatio(progress);
        String timeString = String.format("%02d:%02d", progressSec / 60, progressSec % 60);
        audioView.setText(timeString);
    }
}
