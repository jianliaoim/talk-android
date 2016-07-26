package com.teambition.talk.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.apis.UploadApi;
import com.teambition.talk.client.data.FileUploadResponseData;
import com.teambition.talk.entity.CountingTypedFile;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by jgzhu on 10/10/14.
 */
public class ProgressImageView extends FrameLayout {
    private static final int MAX_UPLOAD_NUM = 2;
    private static BlockingQueue QUEUE;
    private static ThreadPoolExecutor EXECUTOR;

    static {
        QUEUE = new LinkedBlockingQueue();
        EXECUTOR = new ThreadPoolExecutor(2, MAX_UPLOAD_NUM, 3, TimeUnit.SECONDS, QUEUE);
    }

    private ColorDrawable emptyDrawable = new ColorDrawable(Color.rgb(170, 170, 168));
    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .showImageOnFail(emptyDrawable)
            .build();
    private LayoutInflater layoutInflater;
    private ImageView imageView;
    private ImageView localImageView;
    private View maskView;
    private TextView tvPercentage;
    private String filePath;
    private long totalSize;

    private UploadApi uploadApi;

    private OnUploadFinishListener onUploadFinishListener;

    private CountingTypedFile.ProgressListener progressListener =
            new CountingTypedFile.ProgressListener() {
                @Override
                public void transferred(final long num) {
                    Observable.just(null)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Object>() {
                                @Override
                                public void call(Object o) {
                                    int percent = (int) ((num / (float) totalSize) * 100);
                                    tvPercentage.setText(percent + "%");
                                }
                            });
                }
            };

    public interface OnUploadFinishListener {
        void onUploadFinish(FileUploadResponseData file);
    }

    public ProgressImageView(Context context) {
        this(context, null);
    }

    public ProgressImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        layoutInflater = LayoutInflater.from(context);
        layoutInflater.inflate(R.layout.view_progress_image, this);

        imageView = (ImageView) findViewById(R.id.image);
        localImageView = (ImageView) findViewById(R.id.image_local);
        maskView = findViewById(R.id.mask);
        tvPercentage = (TextView) findViewById(R.id.percentage);

        TalkClient client = TalkClient.getInstance();
        uploadApi = client.getUploadApi();
    }

    public void setImageUrl(String url) {
        localImageView.setVisibility(VISIBLE);
        MainApp.IMAGE_LOADER.displayImage(url, localImageView, displayImageOptions);
    }

    public void setLocalImageUrl(String path) {
        localImageView.setVisibility(INVISIBLE);
        filePath = path;
        String url = "file://" + path;
        MainApp.IMAGE_LOADER.displayImage(url, imageView, displayImageOptions);

        uploadFile();
    }

    public void setLocalImageView(int resourceId) {
        localImageView.setVisibility(VISIBLE);
        String url = "drawable://" + resourceId;
        MainApp.IMAGE_LOADER.displayImage(url, localImageView, displayImageOptions);
    }

    public void setOnUploadFinishListener(OnUploadFinishListener onUploadFinishListener) {
        this.onUploadFinishListener = onUploadFinishListener;
    }

    private void uploadFile() {
        tvPercentage.setOnClickListener(null);
        File file = new File(filePath);
        totalSize = file.length();
        uploadApi.uploadFile(file.getName(), "image/*", file.length(), new CountingTypedFile("image/*", file, progressListener))
                .subscribeOn(Schedulers.from(EXECUTOR))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<FileUploadResponseData>() {
                    @Override
                    public void call(FileUploadResponseData data) {
                        tvPercentage.setVisibility(View.GONE);
                        maskView.setVisibility(View.GONE);
                        if (onUploadFinishListener != null) {
                            onUploadFinishListener.onUploadFinish(data);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        tvPercentage.setText("upload failed.");
                        tvPercentage.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                uploadFile();
                            }
                        });
                    }
                });
    }
}
