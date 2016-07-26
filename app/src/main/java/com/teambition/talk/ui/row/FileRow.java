package com.teambition.talk.ui.row;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.CardView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.teambition.talk.BizLogic;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.File;
import com.teambition.talk.entity.Message;
import com.teambition.talk.util.MessageDialogBuilder;
import com.teambition.talk.util.StringUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

/**
 * Created by zeatual on 15/7/29.
 */
public class FileRow extends Row {

    private File file;
    private String avatarUrl;
    private boolean isMine;
    final GradientDrawable drawable;

    MessageDialogBuilder.MessageActionCallback callback;

    static class FileRowHolder {
        @InjectView(R.id.tv_file_name)
        TextView tvFileName;
        @InjectView(R.id.tv_file_size)
        TextView tvFileSize;
        @Optional
        @InjectView(R.id.card_avatar)
        CardView cardAvatar;
        @Optional
        @InjectView(R.id.img_avatar)
        RoundedImageView imgAvatar;
        @InjectView(R.id.layout_file)
        RelativeLayout layoutFile;
        @InjectView(R.id.file_scheme)
        TextView fileScheme;

        public FileRowHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    public FileRow(Message message, File file, String avatarUrl, OnAvatarClickListener listener,
                   MessageDialogBuilder.MessageActionCallback callback) {
        super(message, listener);
        this.file = file;
        this.avatarUrl = avatarUrl;
        this.callback = callback;
        this.isMine = BizLogic.isMe(message.get_creatorId());
        drawable = new GradientDrawable();
        final float radius = MainApp.CONTEXT.getResources().getDimension(R.dimen.file_format_radius);
        drawable.setCornerRadii(new float[]{radius, radius, radius, radius, radius, radius, radius, radius});
    }

    @Override
    public View getView(View convertView, final ViewGroup parent) {
        FileRowHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).
                    inflate(isMine ? R.layout.item_row_file_self : R.layout.item_row_file, null);
            holder = new FileRowHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (FileRowHolder) convertView.getTag();
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
            if (StringUtil.isNotBlank(file.getFileName())) {
                final String fileScheme = file.getFileName().contains(".") ? file.getFileName().substring(file.getFileName().lastIndexOf(".") + 1) : "bin";
                holder.tvFileName.setText(file.getFileName());
                drawable.setColor(Color.parseColor(file.getSchemeColor(fileScheme)));
                holder.fileScheme.setBackgroundDrawable(drawable);
                holder.fileScheme.setText(fileScheme);
            }
            holder.tvFileSize.setText(Formatter.formatFileSize(MainApp.CONTEXT, file.getFileSize()));
        }
        holder.layoutFile.setOnLongClickListener(new View.OnLongClickListener() {
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
        return isMine ? RowType.FILE_SELF_ROW.ordinal() : RowType.FILE_ROW.ordinal();
    }
}
