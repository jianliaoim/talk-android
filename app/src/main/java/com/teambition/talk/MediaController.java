package com.teambition.talk;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Vibrator;
import android.provider.MediaStore;

import com.rockerhieu.emojicon.emoji.Objects;
import com.teambition.talk.entity.Message;
import com.teambition.talk.event.AudioProgressChangeEvent;
import com.teambition.talk.event.AudioRecordProgressEvent;
import com.teambition.talk.event.AudioRecordStartEvent;
import com.teambition.talk.event.AudioRecordStopEvent;
import com.teambition.talk.event.AudioResetEvent;
import com.teambition.talk.event.AudioRouteChangeEvent;
import com.teambition.talk.util.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by nlmartian on 4/23/15.
 */
public class MediaController implements SensorEventListener {
    public static final String TAG = MediaController.class.getSimpleName();

    private MediaRecorder audioRecorder = null;
    private boolean isRecording = false;
    private long recordStartTime;
    private long recordTimeCount;
    private String recordFilePath;

    private MediaPlayer audioPlayer;
    private Message playingMessage;
    private final Object recordTimerSync = new Objects();
    private Timer recordTimer = null;
    private final Object progressTimerSync = new Objects();
    private Timer progressTimer = null;
    private int lastProgress;
    private boolean isPaused;
    private boolean useFrontSpeaker;

    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private boolean ignoreProximity;

    private SoundPool soundPool;
    private int soundEffectId;

    private static MediaController INSTANCE = null;

    public static MediaController getInstance() {
        if (INSTANCE == null) {
            synchronized (MediaController.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MediaController();
                }
            }
        }
        return INSTANCE;
    }

    private MediaController() {
        try {
            sensorManager = (SensorManager) MainApp.CONTEXT.getSystemService(Context.SENSOR_SERVICE);
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
            soundEffectId = soundPool.load(MainApp.CONTEXT, R.raw.add, 1);
        } catch (Exception e) {
            Logger.e(TAG, "Sensor init error", e);
        }
    }

    public void playSoundEffect() {
        AudioManager am = (AudioManager) MainApp.CONTEXT.getSystemService(MainApp.CONTEXT.AUDIO_SERVICE);
        float audioMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        float volumeRatio = audioCurrentVolume / audioMaxVolume;
        soundPool.play(soundEffectId, 0.5f, 0.5f, 0, 0, 1);
    }

    public void startRecording() {
        try {
            Vibrator vibrator = (Vibrator) MainApp.CONTEXT.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(20);
        } catch (Exception e) {
        }
        try {
            recordStartTime = System.currentTimeMillis();
            recordTimeCount = 0;
            recordFilePath = Constant.AUDIO_DIR + "/" + recordStartTime + ".amr";
            audioRecorder = new MediaRecorder();
            audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            audioRecorder.setAudioSamplingRate(8000);
            audioRecorder.setOutputFile(recordFilePath);
            audioRecorder.prepare();
            audioRecorder.start();
            startRecordTimer();
            BusProvider.getInstance().post(new AudioRecordStartEvent());
        } catch (Exception e) {
        }
    }

    public Message stopRecording(boolean send) {
        if (audioRecorder == null) {
            return null;
        }
        try {
            Vibrator vibrator = (Vibrator) MainApp.CONTEXT.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(20);
        } catch (Exception e) {
        }
        try {
            audioRecorder.stop();
            audioRecorder.release();
            audioRecorder = null;
            stopRecordTimer();
        } catch (Exception e) {
            Logger.e(TAG, "stop recording err", e);
            return null;
        }

        if (send) {

            recordTimeCount = System.currentTimeMillis() - recordStartTime;
            if (recordTimeCount < 1000) {
                BusProvider.getInstance().post(new AudioRecordStopEvent(true));
                return null;
            }

            Message tempMessage = Message.newPreSendSpeechInstance(recordFilePath,
                    (int) (recordTimeCount / 1000));
            BusProvider.getInstance().post(new AudioRecordStopEvent());
            return tempMessage;
        } else {
            try {
                File file = new File(recordFilePath);
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {

            }
        }
        BusProvider.getInstance().post(new AudioRecordStopEvent());
        return null;
    }

    public void startPlay(Message message) {
        if (message == null) {
            return;
        }
        if (audioPlayer != null && playingMessage != null && playingMessage.get_id().equals(message.get_id())) {
            resumeAudio(message);
            return;
        }
        resetPlayer(true);
        playingMessage = message;
        try {
            audioPlayer = new MediaPlayer();
            audioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    resetPlayer(true);
                }
            });
            audioPlayer.setAudioStreamType(useFrontSpeaker ? AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC);
            audioPlayer.setDataSource(message.getAudioLocalPath());
            audioPlayer.prepare();
            audioPlayer.start();
            startProgressTimer();
            startProximitySensor();
        } catch (Exception e) {
            if (audioPlayer != null) {
                audioPlayer.release();
                audioPlayer = null;
            }
        }

        if (audioPlayer != null) {
            try {
                if (playingMessage.getAudioProgress() != 0) {
                    int seekTo = (int) (audioPlayer.getDuration() * playingMessage.getAudioProgress());
                    audioPlayer.seekTo(seekTo);
                }
            } catch (Exception e) {
                playingMessage.setAudioProgress(0);
                playingMessage.setAudioProgressSec(0);
            }
        }
    }

    public boolean isPlayingAudio(Message message) {
        return !(audioPlayer == null || message == null || playingMessage == null || (playingMessage != null && !playingMessage.get_id().equals(message.get_id())));
    }

    public boolean isAudioPaused(Message message) {
        return isPlayingAudio(message) && isAudioPaused();
    }

    public boolean isAudioPaused() {
        return isPaused;
    }

    public void pauseAudio(Message message) {
        stopProximitySensor();
        if (audioPlayer == null || message == null || (playingMessage != null && !playingMessage.get_id().equals(message.get_id()))) {
            return;
        }
        try {
            audioPlayer.pause();
            isPaused = true;
        } catch (Exception e) {
            Logger.e(TAG, "pause audio err", e);
            isPaused = false;
        }
    }

    public void resumeAudio(Message message) {
        if (audioPlayer == null || message == null || (playingMessage != null && !playingMessage.get_id().equals(message.get_id()))) {
            return;
        }
        try {
            audioPlayer.start();
            isPaused = false;
            startProximitySensor();
        } catch (Exception e) {
            Logger.e(TAG, "resume audio err", e);
        }
    }

    public void stopPlaying() {
        resetPlayer(true);
    }

    private void resetPlayer(boolean notify) {
        stopProximitySensor();
        if (audioPlayer != null) {
            try {
                audioPlayer.stop();
            } catch (Exception e) {

            }
            try {
                audioPlayer.release();
                audioPlayer = null;
            } catch (Exception e) {

            }
            stopProgressTimer();
            playingMessage.setAudioProgress(0);
            playingMessage.setAudioProgressSec(0);
            lastProgress = 0;
            isPaused = false;

            if (notify) {
                BusProvider.getInstance().post(new AudioResetEvent(playingMessage));
            }
        }
    }

    private void startProximitySensor() {
        if (ignoreProximity) {
            return;
        }
        try {
            if (sensorManager != null && proximitySensor != null) {
                sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        } catch (Exception e) {
        }
    }

    private void stopProximitySensor() {
        if (ignoreProximity) {
            return;
        }
        try {
            useFrontSpeaker = false;
            BusProvider.getInstance().post(new AudioRouteChangeEvent(useFrontSpeaker));

            if (sensorManager != null && proximitySensor != null) {
                sensorManager.unregisterListener(this);
            }
        } catch (Exception e) {
        }
    }

    private void startProgressTimer() {
        synchronized (progressTimerSync) {
            if (progressTimer != null) {
                try {
                    progressTimer.cancel();
                } catch (Exception e) {
                    Logger.e(TAG, "", e);
                }
            }
            final String finalMsgId = playingMessage.get_id();
            progressTimer = new Timer();
            progressTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (audioPlayer != null) {
                        Observable.create(new Observable.OnSubscribe<Object>() {
                            @Override
                            public void call(Subscriber<? super Object> subscriber) {
                                if (playingMessage != null && !isPaused && audioPlayer != null) {
                                    try {
                                        int progress = 0;
                                        float value = 0;
                                        progress = audioPlayer.getCurrentPosition();
                                        value = (float) progress / (float) audioPlayer.getDuration();
                                        if (progress <= lastProgress) {
                                            return;
                                        }
                                        lastProgress = progress;
                                        playingMessage.setAudioProgress(value);
                                        playingMessage.setAudioProgressSec(lastProgress / 1000);
                                        BusProvider.getInstance().post(new AudioProgressChangeEvent(playingMessage));
                                        subscriber.onNext(null);
                                    } catch (Exception e) {
                                        Logger.e(TAG, "progressTimer", e);
                                    }
                                }
                            }
                        }).subscribeOn(AndroidSchedulers.mainThread())
                                .subscribe();
                    }
                }
            }, 0, 17);
        }
    }

    private void stopProgressTimer() {
        synchronized (progressTimerSync) {
            if (progressTimer != null) {
                try {
                    progressTimer.cancel();
                    progressTimer = null;
                } catch (Exception e) {
                    Logger.e(TAG, "stop progressTimer err", e);
                }
            }
        }
    }

    private void startRecordTimer() {
        synchronized (recordTimerSync) {
            if (recordTimer != null) {
                try {
                    recordTimer.cancel();
                } catch (Exception e) {
                    Logger.e(TAG, "", e);
                }
            }
            recordTimer = new Timer();
            recordTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Observable.create(new Observable.OnSubscribe<Object>() {
                        @Override
                        public void call(Subscriber<? super Object> subscriber) {
                            try {
                                int recordTimeInSec = (int) ((System.currentTimeMillis() - recordStartTime) / 1000);
                                BusProvider.getInstance().post(new AudioRecordProgressEvent(recordTimeInSec));
                                if (recordTimeInSec >= 60) {
                                    stopRecording(true);
                                }
                            } catch (Exception e) {

                            }
                        }
                    }).subscribeOn(AndroidSchedulers.mainThread())
                            .subscribe();
                }
            }, 0, 17);
        }
    }

    private void stopRecordTimer() {
        synchronized (recordTimerSync) {
            if (recordTimerSync != null) {
                try {
                    recordTimer.cancel();
                    recordTimer = null;
                } catch (Exception e) {
                    Logger.e(TAG, "stop recordTimer err", e);
                }
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (proximitySensor != null && audioPlayer == null || isPaused || (useFrontSpeaker == (event.values[0] < proximitySensor.getMaximumRange() / 10))) {
            return;
        }
        ignoreProximity = true;
        useFrontSpeaker = event.values[0] < proximitySensor.getMaximumRange() / 10;
        BusProvider.getInstance().post(new AudioRouteChangeEvent(useFrontSpeaker));

        Message currentMessage = playingMessage;
        float audioProgress = playingMessage.getAudioProgress();
        int audioProgressSec = playingMessage.getAudioProgressSec();
        resetPlayer(false);
        currentMessage.setAudioProgress(audioProgress);
        currentMessage.setAudioProgressSec(audioProgressSec);
        startPlay(currentMessage);
        ignoreProximity = false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public static void updateSystemGallery(String path) {
        File file = new File(path);
        try {
            //把文件插入到系统图库
            MediaStore.Images.Media.insertImage(MainApp.CONTEXT.getContentResolver(),
                    file.getAbsolutePath(), file.getName(), null);
            // 最后通知图库更新
            MainApp.CONTEXT.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.parse("file://" + path)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
