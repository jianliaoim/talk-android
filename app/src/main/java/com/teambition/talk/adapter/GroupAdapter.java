package com.teambition.talk.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.teambition.common.PinyinUtil;
import com.teambition.talk.R;
import com.teambition.talk.entity.Group;
import com.teambition.talk.ui.activity.GroupMemberActivity;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by wlanjie on 15/10/27.
 */
public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    final List<Group> mGroups = new ArrayList<>();

    final List<Group> mOriginGroups = new ArrayList<>();

    private OnItemLongClickListener itemLongClick;

    public interface OnItemLongClickListener {
        void onLongClick(Group group);
    }

    public void setOnItemLongClick(OnItemLongClickListener l) {
        this.itemLongClick = l;
    }

    public void clear() {
        mOriginGroups.clear();
        mGroups.clear();
    }

    public void filter(String keyword) {
        mGroups.clear();
        if (TextUtils.isEmpty(keyword)) {
            for (Group group : mOriginGroups) {
                mGroups.add(group);
            }
            notifyDataSetChanged();
        } else {
            for (Group group : mOriginGroups) {
                if (group.getName().contains(keyword) || PinyinUtil.converterToFirstSpell(group.getName()).toLowerCase().contains(keyword)) {
                    mGroups.add(group);
                }
            }
        }
        Collections.sort(mGroups, new Comparator<Group>() {
            @Override
            public int compare(Group lhs, Group rhs) {
                return PinyinUtil.converterToFirstSpell(lhs.getName()).toLowerCase().compareTo(PinyinUtil.converterToFirstSpell(rhs.getName()).toLowerCase());
            }
        });
        notifyDataSetChanged();
    }

    public void addItems(final List<Group> groups) {
        if (groups == null || groups.isEmpty()) return;
        for (Group group : groups) {
            if (group != null) {
                mGroups.add(group);
                mOriginGroups.add(group);
            }
        }
        Collections.sort(mGroups, new Comparator<Group>() {
            @Override
            public int compare(Group lhs, Group rhs) {
                return PinyinUtil.converterToFirstSpell(lhs.getName()).toLowerCase().compareTo(PinyinUtil.converterToFirstSpell(rhs.getName()).toLowerCase());
            }
        });
        notifyDataSetChanged();
    }

    public void updateOne(Group group) {
        int position = findOne(group);
        if (position != -1) {
            mGroups.remove(position);
            mOriginGroups.remove(position);
            mGroups.add(position, group);
            mOriginGroups.add(position, group);
            notifyItemChanged(position);
        } else {
            mGroups.add(0, group);
            mOriginGroups.add(0, group);
            notifyItemInserted(0);
        }
    }

    public void removeOne(Group group) {
        int position = findOne(group);
        if (position != -1) {
            mGroups.remove(position);
            mOriginGroups.remove(position);
            notifyItemRemoved(position);
        }
    }

    private int findOne(Group group) {
        int position = -1;
        for (int i = 0; i < mGroups.size(); i++) {
            if (mGroups.get(i).get_id().equals(group.get_id())) {
                position = i;
                break;
            }
        }
        return position;
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GroupViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_choose_group, parent, false));
    }

    @Override
    public void onBindViewHolder(GroupViewHolder holder, int position) {
        final Group group = mGroups.get(position);
        holder.avatarImage.setBackgroundResource(R.drawable.bg_round_blue);
        holder.nameText.setText(group.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), GroupMemberActivity.class);
                intent.putExtra(GroupMemberActivity.MEMBER_IDS, Parcels.wrap(group.get_memberIds()));
                intent.putExtra(GroupMemberActivity.GROUP, Parcels.wrap(group));
                v.getContext().startActivity(intent);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (itemLongClick != null) {
                    itemLongClick.onLongClick(group);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mGroups.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {

        final ImageView avatarImage;
        final TextView nameText;

        GroupViewHolder(View view) {
            super(view);
            avatarImage = (ImageView) view.findViewById(R.id.image);
            nameText = (TextView) view.findViewById(R.id.name);
        }
    }
}
