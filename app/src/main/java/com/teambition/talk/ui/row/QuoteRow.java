package com.teambition.talk.ui.row;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.teambition.talk.BizLogic;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.AttachmentType;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Quote;
import com.teambition.talk.entity.RTF;
import com.teambition.talk.entity.Snippet;
import com.teambition.talk.ui.MessageFormatter;
import com.teambition.talk.ui.activity.CodePreviewActivity;
import com.teambition.talk.ui.activity.RichContentActivity;
import com.teambition.talk.ui.activity.WebContainerActivity;
import com.teambition.talk.ui.span.ClickableTextViewOnTouchListener;
import com.teambition.talk.util.MessageDialogBuilder;
import com.teambition.talk.util.StringUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

/**
 * Created by zeatual on 15/7/29.
 */
public class QuoteRow extends Row {

    private AttachmentType type;
    private String title;
    private String content;
    private String codeType;
    private String thumbnail;
    private String redirectUrl;
    private String avatarUrl;
    private boolean isMine;
    private MessageDialogBuilder.MessageActionCallback callback;

    static class QuoteRowHolder {

        @InjectView(R.id.img_thumbnail)
        RoundedImageView imgThumbnail;
        @InjectView(R.id.tv_title)
        TextView tvTitle;
        @InjectView(R.id.tv_content)
        TextView tvContent;
        @InjectView(R.id.layout_quote)
        LinearLayout layoutQuote;
        @Optional
        @InjectView(R.id.card_avatar)
        CardView cardAvatar;
        @Optional
        @InjectView(R.id.img_avatar)
        RoundedImageView imgAvatar;

        public QuoteRowHolder(View view) {
            ButterKnife.inject(this, view);
            tvContent.setOnTouchListener(new ClickableTextViewOnTouchListener(tvContent));
        }
    }

    public QuoteRow(Message message, Quote quote, String avatarUrl,
                    MessageDialogBuilder.MessageActionCallback callback) {
        super(message);
        this.title = quote.getTitle();
        this.content = quote.getText();
        this.thumbnail = quote.getThumbnailUrl();
        this.redirectUrl = quote.getRedirectUrl();
        this.avatarUrl = avatarUrl;
        this.callback = callback;
        isMine = BizLogic.isMe(message.get_creatorId());
        type = AttachmentType.QUOTE;
    }

    public QuoteRow(Message message, RTF rtf, String avatarUrl, OnAvatarClickListener listener,
                    MessageDialogBuilder.MessageActionCallback callback) {
        super(message, listener);
        this.title = rtf.getTitle();
        this.content = rtf.getText();
        this.thumbnail = rtf.getThumbnailUrl();
        this.avatarUrl = avatarUrl;
        this.callback = callback;
        isMine = BizLogic.isMe(message.get_creatorId());
        type = AttachmentType.RTF;
    }

    public QuoteRow(Message message, Snippet snippet, String avatarUrl, OnAvatarClickListener listener,
                    MessageDialogBuilder.MessageActionCallback callback) {
        super(message, listener);
        this.title = snippet.getTitle();
        this.content = snippet.getText();
        this.codeType = snippet.getCodeType();
        this.avatarUrl = avatarUrl;
        this.callback = callback;
        isMine = BizLogic.isMe(message.get_creatorId());
        type = AttachmentType.SNIPPET;
    }

    @Override
    public View getView(View convertView, final ViewGroup parent) {
        QuoteRowHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).
                    inflate(isMine ? R.layout.item_row_quote_self : R.layout.item_row_quote, null);
            holder = new QuoteRowHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (QuoteRowHolder) convertView.getTag();
        }
        holder.tvTitle.setVisibility(View.GONE);
        holder.tvContent.setVisibility(View.GONE);
        holder.imgThumbnail.setVisibility(View.GONE);
        if (StringUtil.isNotBlank(title)) {
            holder.tvTitle.setText(title);
            holder.tvTitle.setVisibility(View.VISIBLE);
        }
        if (StringUtil.isNotBlank(content)) {
            switch (type) {
                case QUOTE:
                case RTF:
                    holder.tvContent.setText(MessageFormatter.formatFromHtml(Html.fromHtml(content)));
                    break;
                case SNIPPET:
                    holder.tvContent.setText(content);
                    break;
            }
            holder.tvContent.setVisibility(View.VISIBLE);
        }
        if (StringUtil.isNotBlank(thumbnail)) {
            MainApp.IMAGE_LOADER.displayImage(thumbnail, holder.imgThumbnail,
                    ImageLoaderConfig.DEFAULT_OPTIONS);
            holder.imgThumbnail.setVisibility(View.VISIBLE);
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
        holder.layoutQuote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                switch (type) {
                    case QUOTE:
                        if (StringUtil.isNotBlank(redirectUrl) && redirectUrl.startsWith("http")) {
                            intent = WebContainerActivity.newIntent(parent.getContext(), redirectUrl, title);
                            parent.getContext().startActivity(intent);
                        } else {
                            intent = new Intent(parent.getContext(), RichContentActivity.class);
                            if (StringUtil.isNotBlank(title)) {
                                intent.putExtra("title", title);
                            }
                            intent.putExtra("text", content);
                            parent.getContext().startActivity(intent);
                        }
                        break;
                    case RTF:
                        intent = new Intent(parent.getContext(), RichContentActivity.class);
                        if (StringUtil.isNotBlank(title)) {
                            intent.putExtra("title", title);
                        }
                        intent.putExtra("text", content);
                        parent.getContext().startActivity(intent);
                        break;
                    case SNIPPET:
                        intent = CodePreviewActivity.startIntent(parent.getContext(), title, content, codeType);
                        parent.getContext().startActivity(intent);
                        break;
                }
            }
        });
        holder.layoutQuote.setOnLongClickListener(new View.OnLongClickListener() {
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
        return isMine ? RowType.QUOTE_SELF_ROW.ordinal() : RowType.QUOTE_ROW.ordinal();
    }
}
