package com.teambition.talk.ui.row;

import android.app.Activity;
import android.content.Intent;
import android.text.Spannable;
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
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Room;
import com.teambition.talk.ui.MessageFormatter;
import com.teambition.talk.ui.activity.ChatActivity;
import com.teambition.talk.ui.span.ActionSpan;
import com.teambition.talk.util.MessageDialogBuilder;

import org.parceler.Parcels;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by zeatual on 15/7/29.
 */
public class MentionRow extends Row {

    private Message mentionMsg;
    private MessageDialogBuilder.MessageActionCallback callback;
    private boolean isMine;
    private String roomId;

    static class MentionHolder {

        @InjectView(R.id.layout_mention)
        RelativeLayout layoutMention;
        @InjectView(R.id.img_creator)
        RoundedImageView imgCreator;
        @InjectView(R.id.tv_creator)
        TextView tvCreator;
        @InjectView(R.id.tv_body)
        TextView tvBody;

        public MentionHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    public MentionRow(Message message, Message mentionMsg, MessageDialogBuilder.MessageActionCallback callback) {
        super(message);
        this.mentionMsg = mentionMsg;
        this.callback = callback;
        isMine = BizLogic.isMe(message.get_creatorId());
        Spannable s = MessageFormatter.formatActionSpan(message.getBody());
        ActionSpan[] spans = s.getSpans(0, s.length(), ActionSpan.class);
        if (spans.length > 0) {
            ActionSpan span = spans[0];
            if ("link".equals(span.getAction())) {
                String link = span.getData();
                if (link.contains("/") && link.lastIndexOf("/") + 1 < link.length()) {
                    roomId = link.substring(link.lastIndexOf("/") + 1, link.length());
                }
            }
        }
    }

    @Override
    public View getView(View convertView, final ViewGroup parent) {
        MentionHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(isMine ?
                    R.layout.item_row_mention_self : R.layout.item_row_mention, null);
            holder = new MentionHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (MentionHolder) convertView.getTag();
        }

        MainApp.IMAGE_LOADER.displayImage(mentionMsg.getCreatorAvatar(),
                holder.imgCreator, ImageLoaderConfig.AVATAR_OPTIONS);
        holder.tvCreator.setText(mentionMsg.getCreatorName());
        holder.tvBody.setText(MessageFormatter.formatToPureText(mentionMsg.getBody()));

        holder.layoutMention.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Room room = MainApp.globalRooms.get(roomId);
                if (room != null) {
                    Intent intent = new Intent(view.getContext(), ChatActivity.class);
                    intent.putExtra(ChatActivity.EXTRA_ROOM, Parcels.wrap(room));
                    intent.putExtra(ChatActivity.EXTRA_MESSAGE, Parcels.wrap(mentionMsg));
                    intent.putExtra(ChatActivity.IS_PREVIEW, room.getIsQuit());
                    if (view.getContext() instanceof Activity) {
                        ((Activity) view.getContext()).finish();
                    }
                    view.getContext().startActivity(intent);
                }
            }
        });
        holder.layoutMention.setOnLongClickListener(new View.OnLongClickListener() {
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
        return isMine ? RowType.MENTION_SELF_ROW.ordinal() : RowType.MENTION_ROW.ordinal();
    }

}
