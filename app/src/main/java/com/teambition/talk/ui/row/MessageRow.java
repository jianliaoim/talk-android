package com.teambition.talk.ui.row;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.teambition.talk.BizLogic;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.File;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Quote;
import com.teambition.talk.entity.RTF;
import com.teambition.talk.entity.Snippet;
import com.teambition.talk.realm.MessageDataProcess;
import com.teambition.talk.ui.MessageFormatter;
import com.teambition.talk.util.DensityUtil;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.ThemeUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A row for display whole message in list.
 * Created by zeatual on 15/8/18.
 */
public class MessageRow {

    private Message message;
    private boolean enableEditMode;
    private final GradientDrawable drawable;

    public Message getMessage() {
        return message;
    }

    public static class MessageHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.tv_creator)
        TextView tvCreator;
        @InjectView(R.id.tv_target)
        TextView tvTarget;
        @InjectView(R.id.tv_time)
        TextView tvTime;
        @InjectView(R.id.tv_title)
        TextView tvTitle;
        @InjectView(R.id.tv_content)
        TextView tvContent;
        @InjectView(R.id.img)
        ImageView img;
        @InjectView(R.id.img_arrow)
        ImageView imgArrow;
        @InjectView(R.id.checkbox)
        public CheckBox checkBox;
        @InjectView(R.id.file_scheme)
        TextView fileScheme;

        public MessageHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }

    public MessageRow(Message message) {
        this.message = message;
        drawable = new GradientDrawable();
        final float radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, MainApp.CONTEXT.getResources().getDisplayMetrics());
        drawable.setCornerRadii(new float[]{radius, radius, radius, radius, radius, radius, radius, radius});
    }

    public void enableEditMode(boolean enableEditMode) {
        this.enableEditMode = enableEditMode;
    }

    /****************
     * for ListView *
     ****************/

    public View getViewBySpecifiedType(View convertView, ViewGroup parent,
                                       MessageDataProcess.DisplayMode displayMode) {
        MessageHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, null);
            holder = new MessageHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (MessageHolder) convertView.getTag();
        }
        renderView(holder, parent.getContext(), displayMode);
        return convertView;
    }

    public View getView(View convertView, ViewGroup parent) {
        return getViewBySpecifiedType(convertView, parent, null);
    }

    public MessageHolder getViewHolder(View convertView, ViewGroup parent) {
        MessageHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, null);
            holder = new MessageHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (MessageHolder) convertView.getTag();
        }
        return holder;
    }

    /********************
     * for RecyclerView *
     ********************/

    public static RecyclerView.ViewHolder createViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, null);
        return new MessageHolder(view);
    }

    public void renderView(RecyclerView.ViewHolder viewHolder, Context context) {
        renderView(viewHolder, context, null);
    }

    public void renderView(RecyclerView.ViewHolder viewHolder, Context context,
                           MessageDataProcess.DisplayMode displayMode) {
        if (viewHolder instanceof MessageHolder) {
            MessageHolder holder = (MessageHolder) viewHolder;
            holder.imgArrow.setImageDrawable(ThemeUtil.getThemeDrawable(context.getResources(),
                    R.drawable.ic_right_triangle, BizLogic.getTeamColor()));
            holder.tvCreator.setText(message.getCreatorName());
            String target;
            if (message.get_toId() != null) {
                if (BizLogic.isMe(message.get_toId())) {
                    target = context.getString(R.string.me);
                } else if (MainApp.globalMembers.containsKey(message.get_toId())) {
                    target = MainApp.globalMembers.get(message.get_toId()).getAlias();
                } else if (message.getTo() != null) {
                    target = message.getTo().getAlias();
                } else {
                    target = context.getString(R.string.anonymous_user);
                }
            } else {
                target = message.getChatTitle();
            }
            holder.tvTarget.setText(target);
            holder.tvTime.setText(MessageFormatter.formatCreateTimeForShort(message.getCreatedAt()));
            if (enableEditMode) {
                holder.checkBox.setVisibility(View.VISIBLE);
            } else {
                holder.checkBox.setVisibility(View.GONE);
            }
            holder.tvContent.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            holder.tvContent.setCompoundDrawablePadding(0);
            switch (displayMode == null ? MessageDataProcess.DisplayMode.getEnum(message.getDisplayMode()) :
                    displayMode) {
                case MESSAGE:
                    holder.tvTitle.setSingleLine(false);
                    holder.tvTitle.setMaxLines(3);
                    holder.tvTitle.setVisibility(View.VISIBLE);
                    holder.tvContent.setVisibility(View.GONE);
                    holder.tvTitle.setEllipsize(TextUtils.TruncateAt.END);
                    holder.img.setImageResource(R.drawable.ic_type_message);
                    if (message.getHighlight() != null &&
                            StringUtil.isNotBlank(message.getHighlight().getBody())) {
                        holder.tvTitle.setText(MessageFormatter.
                                formatHighlightSpan(message.getHighlight().getBody(),
                                        context.getResources()));
                    } else {
                        holder.tvTitle.setText(MessageFormatter.formatToPureText(message.getBody()));
                    }
                    break;
                case IMAGE:
                case FILE:
                    File file = MessageDataProcess.getInstance().getFile(message);
                    if (file != null) {
                        holder.tvTitle.setSingleLine();
                        holder.tvContent.setMaxLines(1);
                        holder.tvTitle.setVisibility(View.VISIBLE);
                        holder.tvContent.setVisibility(View.VISIBLE);
                        holder.tvTitle.setEllipsize(TextUtils.TruncateAt.MIDDLE);
                        if (message.getHighlight() != null &&
                                StringUtil.isNotBlank(message.getHighlight().getFileName())) {
                            holder.tvTitle.setText(MessageFormatter
                                    .formatHighlightSpan(message.getHighlight().getFileName(),
                                            context.getResources()));
                        } else {
                            holder.tvTitle.setText(file.getFileName());
                        }
                        holder.img.setVisibility(View.GONE);
                        holder.fileScheme.setVisibility(View.VISIBLE);
                        holder.tvContent.setText(Formatter.formatFileSize(context, file.getFileSize()));
                        final String fileScheme = file.getFileName().contains(".") ? file.getFileName().substring(file.getFileName().lastIndexOf(".") + 1) : "bin";
                        drawable.setColor(Color.parseColor(file.getSchemeColor(fileScheme)));
                        holder.fileScheme.setBackgroundDrawable(drawable);
                        holder.fileScheme.setText(fileScheme);
//                        MainApp.IMAGE_LOADER.displayImage(file.getIcon(context), holder.img,
//                                ImageLoaderConfig.DEFAULT_OPTIONS);
                    }
                    break;
                case SPEECH:
                    File audio = MessageDataProcess.getInstance().getFile(message);
                    if (audio != null) {
                        holder.tvTitle.setSingleLine();
                        holder.tvContent.setMaxLines(1);
                        holder.tvTitle.setVisibility(View.VISIBLE);
                        holder.tvContent.setVisibility(View.VISIBLE);
                        holder.tvTitle.setEllipsize(TextUtils.TruncateAt.END);
                        holder.img.setImageResource(R.drawable.ic_type_speech);
                        if (message.getHighlight() != null &&
                                StringUtil.isNotBlank(message.getHighlight().getFileName())) {
                            holder.tvTitle.setText(String.format(context
                                            .getString(R.string.audio_message_name),
                                    MessageFormatter.formatHighlightSpan(message.getHighlight()
                                            .getFileName(), context.getResources())));
                        } else {
                            holder.tvTitle.setText(context.getString(R.string.audio_message_name));
                        }
                        holder.tvContent.setText(String.format("%02d:%02d", audio.getDuration() / 60,
                                audio.getDuration() % 60));
                    }
                    break;
                case RTF:
                    RTF rtf = MessageDataProcess.getInstance().getRTF(message);
                    if (rtf != null) {
                        holder.tvTitle.setSingleLine(false);
                        holder.tvTitle.setMaxLines(2);
                        holder.tvContent.setMaxLines(3);
                        holder.tvTitle.setEllipsize(TextUtils.TruncateAt.END);
                        holder.tvContent.setVisibility(View.VISIBLE);
                        MainApp.IMAGE_LOADER.displayImage(rtf.getThumbnailUrl(), holder.img,
                                ImageLoaderConfig.RTF_OPTIONS);
                        if (StringUtil.isBlank(rtf.getTitle())) {
                            holder.tvTitle.setVisibility(View.GONE);
                        } else {
                            if (message.getHighlight() != null &&
                                    StringUtil.isNotBlank(message.getHighlight().getTitle())) {
                                holder.tvTitle.setText(MessageFormatter
                                        .formatHighlightSpan(message.getHighlight().getTitle(),
                                                context.getResources()));
                            } else {
                                holder.tvTitle.setText(rtf.getTitle());
                            }
                            holder.tvTitle.setVisibility(View.VISIBLE);
                        }
                        if (message.getHighlight() != null &&
                                StringUtil.isNotBlank(message.getHighlight().getText())) {
                            holder.tvContent.setText(MessageFormatter
                                    .formatHighlightSpan(message.getHighlight().getText(),
                                            context.getResources()));
                        } else {
                            holder.tvContent.setText(MessageFormatter.filterHtml(rtf.getText()));
                        }
                    }
                    break;
                case SNIPPET:
                    Snippet snippet = MessageDataProcess.getInstance().getSnippet(message);
                    if (snippet != null) {
                        holder.tvTitle.setSingleLine(false);
                        holder.tvTitle.setMaxLines(2);
                        holder.tvContent.setMaxLines(3);
                        holder.tvTitle.setEllipsize(TextUtils.TruncateAt.END);
                        holder.tvContent.setVisibility(View.VISIBLE);
                        holder.img.setImageResource(R.drawable.ic_type_snippet);
                        if (StringUtil.isBlank(snippet.getTitle())) {
                            holder.tvTitle.setVisibility(View.GONE);
                        } else {
                            if (message.getHighlight() != null &&
                                    StringUtil.isNotBlank(message.getHighlight().getTitle())) {
                                holder.tvTitle.setText(MessageFormatter
                                        .formatHighlightSpan(message.getHighlight().getTitle(),
                                                context.getResources()));
                            } else {
                                holder.tvTitle.setText(snippet.getTitle());
                            }
                            holder.tvTitle.setVisibility(View.VISIBLE);
                        }
                        if (message.getHighlight() != null &&
                                StringUtil.isNotBlank(message.getHighlight().getText())) {
                            holder.tvContent.setText(MessageFormatter
                                    .formatHighlightSpan(message.getHighlight().getText(),
                                            context.getResources()));
                        } else {
                            holder.tvContent.setText(MessageFormatter.filterHtml(snippet.getText()));
                        }
                    }
                    break;
                case INTEGRATION:
                    Quote quote = MessageDataProcess.getInstance().getQuote(message);
                    holder.tvTitle.setEllipsize(TextUtils.TruncateAt.END);
                    if ("url".equals(quote.getCategory())) {
                        holder.tvTitle.setSingleLine(false);
                        holder.tvTitle.setMaxLines(3);
                        holder.tvContent.setMaxLines(1);
                        holder.tvTitle.setText(MessageFormatter.formatToPureText(message.getBody()));
                        holder.tvContent.setCompoundDrawablesWithIntrinsicBounds(context.
                                getResources().getDrawable(R.drawable.ic_link), null, null, null);
                        holder.tvContent.setCompoundDrawablePadding(DensityUtil.dip2px(context, 4));
                        if (StringUtil.isBlank(quote.getTitle())) {
                            holder.tvContent.setVisibility(View.GONE);
                        } else {
                            if (message.getHighlight() != null &&
                                    StringUtil.isNotBlank(message.getHighlight().getTitle())) {
                                holder.tvContent.setText(MessageFormatter
                                        .formatHighlightSpan(message.getHighlight().getTitle(),
                                                context.getResources()));
                            } else {
                                holder.tvContent.setText(quote.getTitle());
                            }
                            holder.tvContent.setVisibility(View.VISIBLE);
                        }
                        MainApp.IMAGE_LOADER.displayImage(quote.getThumbnailUrl(),
                                holder.img, ImageLoaderConfig.LINK_OPTIONS);
                    } else {
                        holder.tvTitle.setSingleLine(false);
                        holder.tvTitle.setMaxLines(2);
                        holder.tvContent.setMaxLines(3);
                        holder.tvContent.setVisibility(View.VISIBLE);
                        if (StringUtil.isBlank(quote.getTitle())) {
                            holder.tvTitle.setVisibility(View.GONE);
                        } else {
                            if (message.getHighlight() != null &&
                                    StringUtil.isNotBlank(message.getHighlight().getTitle())) {
                                holder.tvTitle.setText(MessageFormatter
                                        .formatHighlightSpan(message.getHighlight().getTitle(),
                                                context.getResources()));
                            } else {
                                holder.tvTitle.setText(quote.getTitle());
                            }
                            holder.tvTitle.setVisibility(View.VISIBLE);
                        }
                        if (message.getHighlight() != null &&
                                StringUtil.isNotBlank(message.getHighlight().getText())) {
                            holder.tvContent.setText(MessageFormatter
                                    .formatHighlightSpan(message.getHighlight().getText(),
                                            context.getResources()));
                        } else {
                            holder.tvContent.setText(MessageFormatter.filterHtml(quote.getText()));
                        }
                        MainApp.IMAGE_LOADER.displayImage(message.getCreatorAvatar(),
                                holder.img, ImageLoaderConfig.AVATAR_OPTIONS);
                    }
                    break;
            }
        }
    }
}
