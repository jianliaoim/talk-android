package com.teambition.talk.ui.row;

import android.text.Spannable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.makeramen.roundedimageview.RoundedImageView;
import com.rockerhieu.emojicon.EmojiconTextView;
import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.Message;
import com.teambition.talk.event.MentionMessageClickEvent;
import com.teambition.talk.event.MentionReadEvent;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.ui.MessageFormatter;
import com.teambition.talk.ui.span.ClickableTextViewOnTouchListener;
import com.teambition.talk.util.MessageDialogBuilder;
import com.teambition.talk.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

/**
 * Created by zeatual on 15/7/27.
 */
public class TextRow extends Row {

    private Spannable text;
    private String avatarUrl;
    private boolean isMine;
    private List<String> mentions;
    private MessageDialogBuilder.MessageActionCallback callback;

    static class TextRowHolder {

        @InjectView(R.id.tv_text)
        EmojiconTextView tvText;
        @Optional
        @InjectView(R.id.img_avatar)
        RoundedImageView imgAvatar;
        @InjectView(R.id.layout_text)
        RelativeLayout layoutText;

        public TextRowHolder(View view) {
            ButterKnife.inject(this, view);
            tvText.setOnTouchListener(new ClickableTextViewOnTouchListener(tvText));
        }
    }

    public TextRow(Message message, String avatarUrl, OnAvatarClickListener listener,
                   MessageDialogBuilder.MessageActionCallback callback) {
        super(message, listener);
        this.avatarUrl = avatarUrl;
        this.callback = callback;
        isMine = BizLogic.isMe(message.get_creatorId());
        text = MessageFormatter.formatToSpannable(message.getBody(), message.getCreatorName());
        this.mentions = MessageFormatter.getMentions(message.getBody());
    }

    @Override
    public View getView(View convertView, final ViewGroup parent) {
        TextRowHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).
                    inflate(isMine ? R.layout.item_row_text_self : R.layout.item_row_text, null);
            holder = new TextRowHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (TextRowHolder) convertView.getTag();
        }
        if (!mentions.isEmpty()) {
            String userId = BizLogic.getUserInfo().get_id();
            for (String mention : mentions) {
                if (("all".equals(mention) || TextUtils.equals(userId, mention))
                        && getMessage().getReceiptors() != null
                        && !getMessage().getReceiptors().contains(userId)) {
                    if (getMessage().getReceiptors() == null) {
                        getMessage().setReceiptors(new ArrayList<String>());
                    }
                    getMessage().getReceiptors().add(userId);
                    BusProvider.getInstance().post(new MentionReadEvent(getMessage().get_id()));
                    break;
                }
            }
            holder.layoutText.setBackgroundResource(R.drawable.selector_message_white_with_border);
            holder.layoutText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BusProvider.getInstance().post(new MentionMessageClickEvent(mentions, getMessage().getReceiptors()));
                }
            });
        } else {
            holder.layoutText.setBackgroundResource(R.drawable.selector_message_white);
            holder.layoutText.setOnClickListener(null);
        }
        holder.tvText.setText(text);
        if (holder.imgAvatar != null && StringUtil.isNotBlank(avatarUrl)) {
            MainApp.IMAGE_LOADER.displayImage(avatarUrl, holder.imgAvatar,
                    ImageLoaderConfig.AVATAR_OPTIONS);
            setAvatarListener(holder.imgAvatar);
        }
        holder.layoutText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MessageDialogBuilder builder = new MessageDialogBuilder(parent.getContext(), getMessage(), callback);
                builder.favorite()
                        .tag();
                builder.copyText()
                        .forward();
                if (isMine) {
                    builder.edit();
                }
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
        return isMine ? RowType.TEXT_SELF_ROW.ordinal() : RowType.TEXT_ROW.ordinal();
    }
}
