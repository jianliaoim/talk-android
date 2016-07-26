package com.teambition.talk.ui.row;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.makeramen.roundedimageview.RoundedImageView;
import com.teambition.talk.BizLogic;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.File;
import com.teambition.talk.entity.Message;
import com.teambition.talk.realm.MessageDataProcess;
import com.teambition.talk.ui.activity.ChatPhotoViewActivity;
import com.teambition.talk.ui.activity.ImageReviewActivity;
import com.teambition.talk.util.DensityUtil;
import com.teambition.talk.util.MessageDialogBuilder;
import com.teambition.talk.util.StringUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

/**
 * Created by zeatual on 15/7/29.
 */
public class ImageRow extends Row {

    private final int maxHeight;
    private final int minHeight;
    private final int maxWidth;
    private final int minWidth;
    private File file;
    private String avatarUrl;
    private boolean isMine;
    private Message message;
    private MessageDialogBuilder.MessageActionCallback callback;
    private final Gson gson;

    static class ImageRowHolder {

        @InjectView(R.id.img_pic)
        RoundedImageView imgPic;
        @Optional
        @InjectView(R.id.card_avatar)
        CardView cardAvatar;
        @Optional
        @InjectView(R.id.img_avatar)
        RoundedImageView imgAvatar;

        public ImageRowHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    public ImageRow(Message message, File file, String avatarUrl, OnAvatarClickListener listener,
                    MessageDialogBuilder.MessageActionCallback callback) {
        super(message, listener);
        this.message = message;
        this.file = file;
        this.avatarUrl = avatarUrl;
        this.callback = callback;
        this.isMine = BizLogic.isMe(message.get_creatorId());
        this.gson = GsonProvider.getGson();
        maxHeight = DensityUtil.dip2px(MainApp.CONTEXT, 220);
        minHeight = DensityUtil.dip2px(MainApp.CONTEXT, 120);
        maxWidth = DensityUtil.dip2px(MainApp.CONTEXT, 140);
        minWidth = DensityUtil.dip2px(MainApp.CONTEXT, 90);
    }

    public void setMessage(Message message) {
        super.setMessage(message);
        this.message = message;
        JsonArray attachments = gson.fromJson(message.getAttachments(), JsonArray.class);
        this.file = gson.fromJson(attachments.get(0).getAsJsonObject().get("data"), File.class);
    }

    @Override
    public View getView(View convertView, final ViewGroup parent) {
        ImageRowHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).
                    inflate(isMine ? R.layout.item_row_image_self : R.layout.item_row_image, null);
            holder = new ImageRowHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ImageRowHolder) convertView.getTag();
        }
        if (file.getImageWidth() == 0 || file.getImageHeight() == 0) {
            String path;
            if (file.getThumbnailUrl().startsWith("file://")) {
                path = file.getThumbnailUrl().substring(file.getThumbnailUrl().indexOf("/") + 2, file.getThumbnailUrl().length());
            } else {
                path = file.getThumbnailUrl();
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            final int width = options.outWidth;
            final int height = options.outHeight;
            resize(holder.imgPic, width, height);
            MainApp.IMAGE_LOADER.displayImage(file.getThumbnailUrl(), holder.imgPic, ImageLoaderConfig.DEFAULT_OPTIONS);
        } else {
            resize(holder.imgPic, file.getImageWidth(), file.getImageHeight());
            MainApp.IMAGE_LOADER.displayImage(file.getThumbnailUrl(), holder.imgPic, ImageLoaderConfig.DEFAULT_OPTIONS);
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

        holder.imgPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // isLocal is dependent on message status
                boolean isLocal = message.getStatus() != MessageDataProcess.Status.NONE.ordinal();
                Intent intent;
                if (isLocal) {
                    intent = ImageReviewActivity.getIntent(view.getContext(), file.getThumbnailUrl());
                } else {
                    intent = ChatPhotoViewActivity.getIntent(view.getContext(), message.get_id(),
                            message.get_roomId(), message.get_toId(), message.get_storyId(), message.get_creatorId());
                }
                view.getContext().startActivity(intent);
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
        return convertView;
    }

    @Override
    public int getViewType() {
        return isMine ? RowType.IMAGE_SELF_ROW.ordinal() : RowType.IMAGE_ROW.ordinal();
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

    private RoundedImageView resizeImageView(RoundedImageView view, int width, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        int maxLength = DensityUtil.dip2px(MainApp.CONTEXT, 264);
        int minLength = DensityUtil.dip2px(MainApp.CONTEXT, 40);
        int shortOne = width >= height ? height : width;
        int longOne = width >= height ? width : height;
        if (width == 0 || height == 0) {
            params.width = DensityUtil.dip2px(MainApp.CONTEXT, 112);
            params.height = DensityUtil.dip2px(MainApp.CONTEXT, 112);
            view.setLayoutParams(params);
            return view;
        }
        if (width <= maxLength && height <= maxLength && width >= minLength && height >= minLength) {
            params.width = width;
            params.height = height;
            view.setLayoutParams(params);
            return view;
        }
        if (longOne / shortOne > maxLength / minLength) {
            shortOne = minLength;
            longOne = maxLength;
        } else {
            if (shortOne <= minLength) {
                longOne = longOne * minLength / shortOne;
                shortOne = minLength;
            }
            if (longOne >= maxLength) {
                shortOne = shortOne * maxLength / longOne;
                longOne = maxLength;
            }
        }
        int viewWidth, viewHeight;
        if (width >= height) {
            viewWidth = longOne;
            viewHeight = shortOne;
        } else {
            viewWidth = shortOne;
            viewHeight = longOne;
        }
        params.width = viewWidth;
        params.height = viewHeight;
        view.setLayoutParams(params);
        return view;
    }
}
