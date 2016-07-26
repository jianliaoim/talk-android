package com.teambition.talk;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.teambition.talk.util.StringUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by nlmartian on 4/30/15.
 */
public class FileDownloader {
    public static final int FINISH = -1;

    private static FileDownloader INSTANCE;

    private OkHttpClient client = new OkHttpClient();

    private FileDownloader() {
    }

    public static FileDownloader getInstance() {
        if (INSTANCE == null) {
            synchronized (FileDownloader.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FileDownloader();
                }
            }
        }
        return INSTANCE;
    }

    public static String getAudioPath(String fileKey) {
        return new StringBuilder()
                .append(Constant.AUDIO_DIR)
                .append("/")
                .append(fileKey)
                .append(".amr")
                .toString();
    }

    public static String getCachePath(String fileKey, String fileType) {
        return new StringBuilder()
                .append(Constant.FILE_DIR_CACHE)
                .append("/")
                .append(fileKey)
                .append(".")
                .append(fileType)
                .toString();
    }

    public static String getDownloadPath(String fileName) {
        if (StringUtil.isBlank(fileName)) {
            fileName = "unknown.file";
        }
        String path = new StringBuilder()
                .append(Constant.FILE_DIR_DOWNLOAD)
                .append("/")
                .append(fileName)
                .toString();
        java.io.File file = new File(path);
        int i = 0;
        String path2 = "";
        String fName = fileName.substring(0, fileName.lastIndexOf(".") - 1);
        String fFix = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
        while (file.exists()) {
            i++;
            path2 = new StringBuilder()
                    .append(Constant.FILE_DIR_DOWNLOAD)
                    .append("/")
                    .append(fName)
                    .append("(")
                    .append(i)
                    .append(").")
                    .append(fFix)
                    .toString();
            file = new File(path2);
        }
        return i == 0 ? path : path2;
    }

    public void startDownload(final String url, final String path, final Action1<Integer> progressAction, final Action1<Throwable> errorAction) {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    final String downloadUrl = url.replace(" ", "%20");
                    Request request = new Request.Builder().url(downloadUrl).build();
                    Response response = client.newCall(request).execute();
                    long totalSize = response.body().contentLength();
                    long currentSize = 0;
                    int lastPercent = 0;

                    BufferedInputStream bufis = new BufferedInputStream(response.body().byteStream());
                    BufferedOutputStream bufos = new BufferedOutputStream(new FileOutputStream(path));

                    int count;
                    byte[] by = new byte[4096];

                    while ((count = bufis.read(by)) != -1) {
                        currentSize += count;
                        bufos.write(by, 0, count);

                        int percent = (int) (currentSize * 100 / totalSize);

                        if (percent > lastPercent) {
                            subscriber.onNext(percent);
                            lastPercent = percent;
                        }
                    }

                    bufos.flush();
                    bufos.close();
                    bufis.close();

                    subscriber.onNext(FINISH);
                    subscriber.onCompleted();
                } catch (IOException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }

            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer progress) {
                        if (progressAction != null) {
                            progressAction.call(progress);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable err) {
                        if (errorAction != null) {
                            errorAction.call(err);
                        }
                    }
                });
    }

}
