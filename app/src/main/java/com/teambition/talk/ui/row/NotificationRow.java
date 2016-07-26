package com.teambition.talk.ui.row;

import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.teambition.talk.BizLogic;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Notification;
import com.teambition.talk.entity.Room;
import com.teambition.talk.entity.Story;
import com.teambition.talk.realm.MemberDataProcess;
import com.teambition.talk.realm.MessageDataProcess;
import com.teambition.talk.realm.NotificationDataProcess;
import com.teambition.talk.realm.RoomRealm;
import com.teambition.talk.realm.StoryDataProcess;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.ui.MessageFormatter;
import com.teambition.talk.util.StringUtil;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zeatual on 15/10/10.
 */
public class NotificationRow {

    public interface OnClickListener {
        void onClick(Notification notification);

        void onLongClick(Notification notification);
    }

    public static class NotificationHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.image)
        RoundedImageView image;
        @InjectView(R.id.tv_unread_num)
        TextView tvUnreadNum;
        @InjectView(R.id.ic_pin)
        View icPin;
        @InjectView(R.id.ic_mute)
        View icMute;
        @InjectView(R.id.tv_time)
        TextView tvTime;
        @InjectView(R.id.tv_title)
        TextView tvTitle;
        @InjectView(R.id.tv_content)
        TextView tvContent;
        @InjectView(R.id.background)
        RelativeLayout background;
        @InjectView(R.id.img_failed)
        ImageView imgFailed;

        public NotificationHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }

    private OnClickListener listener;
    private Notification notification;

    public NotificationRow(Notification notification, OnClickListener listener) {
        this.notification = notification;
        this.listener = listener;
    }

    public static NotificationHolder createViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_notification, null);
        return new NotificationHolder(view);
    }

    public void bindViewHolder(NotificationRow.NotificationHolder holder) {
        holder.icMute.setVisibility(View.GONE);
        holder.icPin.setVisibility(View.GONE);
        holder.tvUnreadNum.setVisibility(View.INVISIBLE);
        holder.image.setImageDrawable(null);
        holder.image.setBackgroundDrawable(null);
        holder.tvTime.setVisibility(View.VISIBLE);
        holder.tvTitle.getPaint().setFakeBoldText(false);
        holder.imgFailed.setVisibility(View.GONE);

        Member creator;
        if (MainApp.globalMembers.containsKey(notification.get_creatorId())) {
            creator = MainApp.globalMembers.get(notification.get_creatorId());
        } else if (notification.getCreator() != null) {
            creator = notification.getCreator();
        } else {
            creator = MemberDataProcess.getAnonymousInstance();
        }

        switch (MessageDataProcess.Status.getEnum(notification.getStatus())) {
            case SENDING:
            case SEND_FAILED:
            case UPLOADING:
            case UPLOAD_FAILED:
                holder.imgFailed.setVisibility(View.VISIBLE);
                break;
            default:
                holder.imgFailed.setVisibility(View.GONE);
        }

        if (StringUtil.isNotBlank(notification.getOutlineText())) {
            final String outlineRes = MainApp.CONTEXT.getString(R.string.outline);
            SpannableString spannableString = new SpannableString(outlineRes + " " + notification.getOutlineText());
            spannableString.setSpan(new ForegroundColorSpan(holder.tvContent.getResources().getColor(R.color.colorPrimary)), 0, outlineRes.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.tvContent.setText(spannableString);
        } else {
            String authorName;
            if (creator != null && BizLogic.isMe(creator.get_id())) {
                authorName = MainApp.CONTEXT.getString(R.string.me);
            } else if (creator != null && MainApp.globalMembers.get(creator.get_id()) != null) {
                authorName = BizLogic.isXiaoai(creator) ? MainApp.CONTEXT.getString(R.string.talk_ai) : creator.getAlias();
            } else if (StringUtil.isNotBlank(notification.getAuthorName())) {
                authorName = notification.getAuthorName();
            } else {
                authorName = MainApp.CONTEXT.getString(R.string.anonymous_user);
            }
            holder.tvContent.setText(MessageFormatter.formatNotification(notification.getText(), authorName));
        }

        if (notification.getIsMute() != null && notification.getIsMute()) {
            holder.icMute.setVisibility(View.VISIBLE);
        }
        if (notification.getUnreadNum() > 0) {
            holder.tvUnreadNum.setVisibility(View.VISIBLE);
            if (notification.getIsMute() != null && notification.getIsMute()) {
                holder.tvUnreadNum.setText("");
            } else {
                holder.tvUnreadNum.setText(notification.getUnreadNum() > 99 ? "99+"
                        : notification.getUnreadNum() + "");
            }
            holder.tvTitle.getPaint().setFakeBoldText(true);
        } else {
            holder.tvUnreadNum.setVisibility(View.INVISIBLE);
            holder.tvTitle.getPaint().setFakeBoldText(false);
        }
        Date time = notification.getDraftTempUpdateAt() == null
                ? notification.getUpdatedAt() : notification.getDraftTempUpdateAt();
        holder.tvTime.setText(MessageFormatter.formatCreateTime(time));
        if (notification.getIsPinned() != null && notification.getIsPinned()) {
            holder.icPin.setVisibility(View.VISIBLE);
        }

        try {
            switch (NotificationDataProcess.Type.getEnum(notification.getType())) {
                case DMS:
                    holder.image.setOval(true);
                    holder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    final Member dms = notification.getMember();
                    MainApp.IMAGE_LOADER.displayImage(dms.getAvatarUrl(), holder.image,
                            ImageLoaderConfig.AVATAR_OPTIONS);
                    if (MainApp.globalMembers.containsKey(dms.get_id())) {
                        holder.tvTitle.setText(MainApp.globalMembers.get(dms.get_id()).getAlias());
                    } else {
                        holder.tvTitle.setText(dms.getName());
                    }
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.onClick(notification);
                        }
                    });
                    holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            listener.onLongClick(notification);
                            return true;
                        }
                    });
                    break;
                case ROOM:
                    holder.image.setOval(true);
                    holder.image.setScaleType(ImageView.ScaleType.CENTER);
                    final Room room = notification.getRoom();
                    holder.image.setBackgroundResource(R.drawable.ic_story_type_topic);
                    holder.tvTitle.setText(room.getTopic());
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            RoomRealm.getInstance().getRoom(room.get_id())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Action1<Room>() {
                                        @Override
                                        public void call(Room room) {
                                            listener.onClick(notification);
                                        }
                                    }, new RealmErrorAction());
                        }
                    });
                    holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            listener.onLongClick(notification);
                            return true;
                        }
                    });
                    break;
                case STORY:
                    holder.image.setOval(false);
                    holder.image.setScaleType(ImageView.ScaleType.CENTER);
                    holder.image.setBackgroundResource(R.drawable.bg_circle_grey);
                    holder.image.setImageDrawable(null);
                    final Story story = notification.getStory();
                    if (story != null) {
                        switch (StoryDataProcess.Category.getEnum(story.getCategory())) {
                            case TOPIC:
                                holder.image.setBackgroundResource(R.drawable.ic_story_type_idea);
                                break;
                            case FILE:
                                holder.image.setBackgroundResource(R.drawable.ic_story_type_file);
                                break;
                            case LINK:
                                holder.image.setBackgroundResource(R.drawable.ic_story_type_link);
                                break;
                        }
                        holder.tvTitle.setText(story.getTitle());
                    }
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.onClick(notification);
                        }
                    });
                    holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            listener.onLongClick(notification);
                            return true;
                        }
                    });
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Notification getNotification() {
        return notification;
    }
}
