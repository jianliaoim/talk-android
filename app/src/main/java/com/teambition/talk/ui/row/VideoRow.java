package com.teambition.talk.ui.row;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.core.DefaultConfigurationFactory;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecodingInfo;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.utils.ImageSizeUtils;
import com.teambition.talk.BizLogic;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.File;
import com.teambition.talk.entity.Message;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.rx.EmptyAction;
import com.teambition.talk.ui.activity.VideoActivity;
import com.teambition.talk.util.DensityUtil;
import com.teambition.talk.util.MessageDialogBuilder;
import com.teambition.talk.util.StringUtil;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by wlanjie on 16/3/16.
 */
public class VideoRow extends Row {

    private final int maxHeight;
    private final int minHeight;
    private final int maxWidth;
    private final int minWidth;
    private File file;
    private String avatarUrl;
    private boolean isMine;
    private MessageDialogBuilder.MessageActionCallback callback;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    final android.media.MediaMetadataRetriever retriever;
    final Map<String, ReentrantLock> idLocks = new HashMap<>();
    final DiskCache diskCache = DefaultConfigurationFactory.createDiskCache(MainApp.CONTEXT, DefaultConfigurationFactory.createFileNameGenerator(), 50 * 1024 * 1024, 150);
    final ImageDecoder decoder = new BaseImageDecoder(true);
    private DisplayImageOptions options = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .build();

    public VideoRow(Message message, File file, String avatarUrl, OnAvatarClickListener listener, MessageDialogBuilder.MessageActionCallback callback, MediaMetadataRetriever retriever) {
        super(message, listener);
        this.file = file;
        this.avatarUrl = avatarUrl;
        this.callback = callback;
        this.retriever = retriever;
        this.isMine = BizLogic.isMe(message.get_creatorId());
        mFormatBuilder = new StringBuilder();
        mFormatter = new java.util.Formatter(mFormatBuilder, Locale.getDefault());
        maxHeight = DensityUtil.dip2px(MainApp.CONTEXT, 220);
        minHeight = DensityUtil.dip2px(MainApp.CONTEXT, 120);
        maxWidth = DensityUtil.dip2px(MainApp.CONTEXT, 140);
        minWidth = DensityUtil.dip2px(MainApp.CONTEXT, 90);
    }

    static class VideoRowHolder {
        View convertView;
        @InjectView(R.id.img_pic)
        RoundedImageView imgPic;
        @InjectView(R.id.duration)
        TextView durationText;
        @Optional
        @InjectView(R.id.card_avatar)
        CardView cardAvatar;
        @Optional
        @InjectView(R.id.img_avatar)
        RoundedImageView imgAvatar;
        @InjectView(R.id.tv_file_size)
        TextView fileSizeText;

        public VideoRowHolder(View view) {
            this.convertView = view;
            ButterKnife.inject(this, view);
        }
    }

    @Override
    public View getView(View convertView, final ViewGroup parent) {
        VideoRowHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).
                    inflate(isMine ? R.layout.item_row_video_self : R.layout.item_row_video, null);
            holder = new VideoRowHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (VideoRowHolder) convertView.getTag();
        }
        if (holder.imgAvatar != null) {
            holder.imgAvatar.setVisibility(View.GONE);
            if (holder.cardAvatar != null) {
                holder.cardAvatar.setVisibility(View.GONE);
            }
            if (StringUtil.isNotBlank(avatarUrl)) {
                if (holder.cardAvatar != null) {
                    holder.cardAvatar.setVisibility(View.VISIBLE);
                }
                holder.imgAvatar.setVisibility(View.VISIBLE);
                MainApp.IMAGE_LOADER.displayImage(avatarUrl, holder.imgAvatar,
                        ImageLoaderConfig.AVATAR_OPTIONS);
                setAvatarListener(holder.imgAvatar);
            }
        }

        if (file != null) {
            if (file.getWidth() == 0 || file.getHeight() == 0) {
                resize(holder.imgPic, DensityUtil.dip2px(MainApp.CONTEXT, 480), DensityUtil.dip2px(MainApp.CONTEXT, 854));
            } else {
                resize(holder.imgPic, file.getWidth(), file.getHeight());
            }
            if (file.getDownloadUrl().startsWith("http")) {
                holder.fileSizeText.setText(android.text.format.Formatter.formatFileSize(convertView.getContext(), file.getFileSize()));
            } else {
                holder.fileSizeText.setText(MainApp.CONTEXT.getString(R.string.is_compressed));
            }
            holder.durationText.setText(stringForTime(file.getDuration()));
            holder.imgPic.setImageBitmap(null);
            getVideoThumbnail(file, new ImageViewAware(holder.imgPic));
            holder.imgPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (StringUtil.isNotBlank(file.getDownloadUrl())) {
                        Intent intent = new Intent(v.getContext(), VideoActivity.class);
                        intent.putExtra(VideoActivity.VIDEO_PATH, file.getDownloadUrl());
                        intent.putExtra(VideoActivity.DURATION, file.getDuration());
                        intent.putExtra(VideoActivity.FILE_NAME, file.getFileName());
                        intent.putExtra(VideoActivity.VIDEO_WIDTH, file.getWidth());
                        intent.putExtra(VideoActivity.VIDEO_HEIGHT, file.getHeight());
                        v.getContext().startActivity(intent);
                    }
                }
            });
            holder.imgPic.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    MessageDialogBuilder builder = new MessageDialogBuilder(parent.getContext(), getMessage(), callback);
                    builder.favorite()
                            .tag()
                            .saveFile()
                            .forward();
                    if (BizLogic.isAdmin() || isMine) {
                        builder.delete();
                    }
                    builder.show();
                    return true;
                }
            });
        }
        return convertView;
    }

    private boolean isViewCollected(ImageViewAware imageAware) {
        return imageAware.isCollected();
    }

    ReentrantLock getLockForId(String id) {
        ReentrantLock lock = idLocks.get(id);
        if (lock == null) {
            lock = new ReentrantLock();
            idLocks.put(id, lock);
        }
        return lock;
    }

    Bitmap tryLoadBitmap(ImageViewAware imageAware) {
        Bitmap bitmap = null;
        try {
            java.io.File imageFile = diskCache.get(getMessage().get_id());
            if (imageFile != null && imageFile.exists() && imageFile.length() > 0) {
                ViewScaleType viewScaleType = imageAware.getScaleType();
                ImageSize imageSize = ImageSizeUtils.defineTargetSizeForView(imageAware, new ImageSize(MainApp.CONTEXT.getResources().getDisplayMetrics().widthPixels, MainApp.CONTEXT.getResources().getDisplayMetrics().heightPixels));
                ImageDecodingInfo decodingInfo = new ImageDecodingInfo(getMessage().get_id(),
                        ImageDownloader.Scheme.FILE.wrap(imageFile.getAbsolutePath()), getMessage().get_id(), imageSize, viewScaleType,
                        new BaseImageDownloader(MainApp.CONTEXT), options);
                bitmap = decoder.decode(decodingInfo);
                MainApp.memoryCache.put(getMessage().get_id(), bitmap);
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return bitmap;
    }

    private void getVideoThumbnail(final File file, final ImageViewAware imageView) {
        if (retriever == null) return;
        Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                if (isViewCollected(imageView)) subscriber.onNext(null);
                ReentrantLock lock = getLockForId(getMessage().get_id());
                lock.lock();
                Bitmap bitmap = null;
                try {
                    bitmap = MainApp.memoryCache.get(getMessage().get_id());
                    if (bitmap == null || bitmap.isRecycled()) {
                        bitmap = tryLoadBitmap(imageView);
                        if (bitmap != null) {
                            MainApp.memoryCache.put(getMessage().get_id(), bitmap);
                        }
                    }
                    if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
                        if (file.getDownloadUrl().startsWith("http")) {
                            retriever.setDataSource(file.getDownloadUrl(), new HashMap<String, String>());
                        } else {
                            retriever.setDataSource(file.getDownloadUrl());
                        }
                        bitmap = retriever.getFrameAtTime(500000);
                        if (bitmap != null) {
                            MainApp.memoryCache.put(getMessage().get_id(), bitmap);
                            diskCache.save(getMessage().get_id(), bitmap);
                        }
                    }
                } catch (Exception ignored) {
                } finally {
                    lock.unlock();
                }
                subscriber.onNext(bitmap);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap bitmap) {
                        imageView.setImageBitmap(bitmap);
                    }
                }, new EmptyAction<Throwable>());
    }

    private RoundedImageView resize(RoundedImageView view, int width, int height) {
        float scale = 1.0f;
        final Matrix matrix = new Matrix();
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (width > maxWidth && height > maxHeight) {
            scale = Math.max(maxWidth * 1.0f / width, maxHeight * 1.0f / height);
            params.width = maxWidth;
            params.height = maxHeight;
        }
        if (width < minWidth && height < minHeight) {
            scale = Math.max(minWidth * 1.0f / width, minHeight * 1.0f / height);
            params.width = minWidth;
            params.height = minHeight;
        }
        if (width > maxWidth && height < maxHeight) {
            scale = maxWidth * 1.0f / width;
            params.width = maxWidth;
            params.height = minHeight;
        }
        if (height > maxHeight && width < maxWidth) {
            scale = maxHeight * 1.0f / height;
            params.width = minWidth;
            params.height = maxHeight;
        }
        view.setLayoutParams(params);
        matrix.postScale(scale, scale, params.width / 2, params.height / 2);
        view.setImageMatrix(matrix);
        return view;
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    @Override
    public int getViewType() {
        return isMine ? RowType.FILE_SELF_ROW.ordinal() : RowType.FILE_ROW.ordinal();
    }
}
