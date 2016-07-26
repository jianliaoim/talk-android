package com.teambition.talk.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.teambition.talk.BizLogic;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Room;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.util.StringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by wlanjie on 16/1/14.
 */
public class TopicSettingAdapter extends RecyclerView.Adapter {

    final static int HEADER = 0;

    final static int CONTENT = 1;

    Room room;

    final List<Member> items = new ArrayList<>();

    private EditText nameEditText;

    private EditText goalEditText;

    TopicSettingListener listener;

    public interface TopicSettingListener {
        void onRemove(Member member);
        void onCheckedChanged(CompoundButton buttonView, boolean isChecked);
        void onSave();
        void onDiscard();
        void onItemClick(int position);
    }

    public TopicSettingAdapter(Room room) {
        this.room = room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public void setListener(TopicSettingListener l) {
        listener = l;
    }

    public List<Member> getItems() {
        return items;
    }

    public void addItems(List<Member> items) {
        if (items == null || items.isEmpty()) return;
        for (Member item : items) {
            if (item != null) {
                this.items.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public void remove(String memberId) {
        if (StringUtil.isBlank(memberId)) return;
        Iterator<Member> iterator = items.iterator();
        while (iterator.hasNext()) {
            Member member = iterator.next();
            if (member != null && memberId.equals(member.get_id())) {
                final int index = items.indexOf(member);
                iterator.remove();
                notifyItemRemoved(index + 1);
                break;
            }
        }
    }

    public void update(Member member) {
        int index = -1;
        if (member != null) {
            Iterator<Member> iterator = items.iterator();
            while (iterator.hasNext()) {
                Member m = iterator.next();
                if (m.get_id().equals(member.get_id())) {
                    index = items.indexOf(m);
                    iterator.remove();
                }
            }
        }
        if (index != -1) {
            items.add(index, member);
            notifyDataSetChanged();
        }
    }

    public EditText getNameEditText() {
        return nameEditText;
    }

    public EditText getGoalEditText() {
        return goalEditText;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == HEADER) {
            final View view = inflater.inflate(R.layout.item_topic_setting_header, parent, false);
            return new TopicSettingHeaderViewHolder(view);
        } else {
            final View view = inflater.inflate(R.layout.item_topic_member, parent, false);
            return new TopicSettingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof TopicSettingHeaderViewHolder) {
            final TopicSettingHeaderViewHolder viewHolder = (TopicSettingHeaderViewHolder) holder;
            nameEditText = viewHolder.topicName;
            goalEditText = viewHolder.topicGoal;
            TextWatcher textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    viewHolder.btnSave.setEnabled(!viewHolder.topicName.getText().toString().equals(room.getTopic()) ||
                            !viewHolder.topicGoal.getText().toString().equals(room.getPurpose()));
                    viewHolder.btnDiscard.setEnabled(!viewHolder.topicName.getText().toString().equals(room.getTopic()) ||
                            !viewHolder.topicGoal.getText().toString().equals(room.getPurpose()));
                }
            };
            if (room.getIsGeneral() != null && room.getIsGeneral()) {
                viewHolder.topicName.setEnabled(false);
                viewHolder.settingView.setVisibility(View.GONE);
                viewHolder.visibilityView.setVisibility(View.GONE);
            }
            if (BizLogic.isAdmin() || BizLogic.isAdminOfRoom(room.get_id())) {
                viewHolder.topicName.addTextChangedListener(textWatcher);
                viewHolder.topicGoal.addTextChangedListener(textWatcher);
                viewHolder.topicName.setEnabled(true);
                viewHolder.topicGoal.setEnabled(true);
                viewHolder.topicVisibility.setEnabled(true);
            } else {
                viewHolder.topicName.setEnabled(false);
                viewHolder.topicGoal.setEnabled(false);
                viewHolder.topicVisibility.setEnabled(false);
            }
            viewHolder.topicName.setText(room.getTopic());
            viewHolder.topicGoal.setText(room.getPurpose());
            viewHolder.topicVisibility.setChecked(room.getIsPrivate() != null && room.getIsPrivate());
            viewHolder.topicVisibility.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (listener != null) {
                        listener.onCheckedChanged(buttonView, isChecked);
                    }
                }
            });
            viewHolder.btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onSave();
                    }
                }
            });
            viewHolder.btnDiscard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onDiscard();
                    }
                }
            });
        } else {
            TopicSettingViewHolder viewHolder = (TopicSettingViewHolder) holder;
            Member member = items.get(position - 1);
            if (MainApp.globalMembers.get(member.get_id()) != null) {
                member = MainApp.globalMembers.get(member.get_id());
            }
            viewHolder.text.setText(member.getAlias());
            MainApp.IMAGE_LOADER.displayImage(member.getAvatarUrl(), viewHolder.image, ImageLoaderConfig.AVATAR_OPTIONS);
            if (!room.getIsGeneral() && !BizLogic.isMe(member.get_id()) && !room.get_creatorId().equals(member.get_id()) && !member.getIsRobot()) {
                viewHolder.remove.setVisibility(View.VISIBLE);
            } else {
                viewHolder.remove.setVisibility(View.GONE);
            }
            final Member m = member;
            viewHolder.remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onRemove(m);
                    }
                }
            });
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? HEADER : CONTENT;
    }

    @Override
    public int getItemCount() {
        return items.size() + 1;
    }

    static class TopicSettingHeaderViewHolder extends RecyclerView.ViewHolder {

        EditText topicName;
        EditText topicGoal;
        SwitchCompat topicVisibility;
        Button btnDiscard;
        Button btnSave;
        View settingView;
        View visibilityView;
        public TopicSettingHeaderViewHolder(View view) {
            super(view);
            topicName = (EditText) view.findViewById(R.id.topic_name);
            topicGoal = (EditText) view.findViewById(R.id.topic_goal);
            topicVisibility = (SwitchCompat) view.findViewById(R.id.topic_visibility);
            btnDiscard = (Button) view.findViewById(R.id.btn_discard);
            btnSave = (Button) view.findViewById(R.id.btn_save);
            settingView = view.findViewById(R.id.setting);
            visibilityView = view.findViewById(R.id.layout_visibility);
        }
    }

    static class TopicSettingViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView text;
        View remove;
        public TopicSettingViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.image);
            text = (TextView) view.findViewById(R.id.text);
            remove = view.findViewById(R.id.remove);
        }
    }
}
