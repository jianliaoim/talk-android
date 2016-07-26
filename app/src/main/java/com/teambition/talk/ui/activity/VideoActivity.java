package com.teambition.talk.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;

import com.teambition.talk.Constant;
import com.teambition.talk.FileDownloader;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.yuv.video.IjkVideoView;
import com.teambition.talk.yuv.video.MediaController;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.functions.Action1;

/**
 * Created by wlanjie on 16/3/16.
 */
public class VideoActivity extends BaseActivity {

    public static final String VIDEO_PATH = "video_path";
    public static final String DURATION = "duration";
    public static final String FILE_NAME = "file_name";
    public static final String VIDEO_WIDTH = "video_width";
    public static final String VIDEO_HEIGHT = "video_height";

    @InjectView(R.id.video_view)
    IjkVideoView videoView;

    @InjectView(R.id.video_controller)
    MediaController mediaController;

    @InjectView(R.id.video_progress)
    View progressView;

    final PhoneReceiver receiver = new PhoneReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video);
        ButterKnife.inject(this);
        final String videoPath = getIntent().getStringExtra(VIDEO_PATH);
        final int duration = getIntent().getIntExtra(DURATION, -1);
        final String fileName = getIntent().getStringExtra(FILE_NAME);
        final int width = getIntent().getIntExtra(VIDEO_WIDTH, -1);
        final int height = getIntent().getIntExtra(VIDEO_HEIGHT, -1);

        final IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(receiver, filter);

        videoView.setMediaController(mediaController);
        videoView.setProgressView(progressView);
        videoView.setDuration(duration);
        videoView.setVideoWidth(width);
        videoView.setVideoHeight(height);

        final String path = new StringBuilder()
                .append(Constant.FILE_DIR_DOWNLOAD)
                .append("/")
                .append(fileName)
                .toString();
        final File file = new File(path);
        if (file.exists()) {
            videoView.setVideoURI(file.getAbsolutePath());
        } else {
            if (StringUtil.isNotBlank(videoPath)) {
                FileDownloader.getInstance().startDownload(videoPath,  FileDownloader.getDownloadPath(fileName), new Action1<Integer>() {
                    @Override
                    public void call(Integer progress) {
                        if (progress == FileDownloader.FINISH) {
                            videoView.setVideoPath(new File(path).getAbsolutePath());
                            videoView.start();
                        }
                    }
                }, null);
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView.getUri() != null) {
            videoView.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        videoView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        videoView.release(true);
        ButterKnife.reset(this);
    }

    class PhoneReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) {
                TelephonyManager phonyManager = (TelephonyManager) MainApp.CONTEXT.getSystemService(Context.TELEPHONY_SERVICE);
                phonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
            }
        }
    }

    PhoneStateListener listener = new PhoneStateListener(){

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    videoView.pause();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    videoView.pause();
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    videoView.resume();
                    break;
            }
        }
    };
}
