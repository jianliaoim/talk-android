package com.teambition.talk.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.joooonho.SelectableRoundedImageView;
import com.teambition.common.PinyinUtil;
import com.teambition.talk.BizLogic;
import com.teambition.talk.entity.Group;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.Member;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by wlanjie on 15/12/28.
 */
public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ViewHolder> {

    final List<Member> mItems = new ArrayList<>();
    final List<Member> mOriginItems = new ArrayList<>();
    private OnRemoveClickListener removeListener;
    private OnItemClickListener listener;
    private final Group group;

    public GroupMemberAdapter(Group group) {
        this.group = group;
    }

    public void setMembers(List<Member> members) {
        if (members == null || members.isEmpty()) return;
        for (Member member : members) {
            if (member != null) {
                mItems.add(member);
            }
        }
        mOriginItems.addAll(mItems);
        notifyDataSetChanged();
    }

    public void addMembers(List<Member> members) {
        if (members == null || members.isEmpty()) return;
        mItems.addAll(members);
        mOriginItems.addAll(members);
        notifyDataSetChanged();
    }

    public void removeMemberById(String memberId) {
        if (TextUtils.isEmpty(memberId)) return;
        Iterator<Member> iterator = mItems.iterator();
        while (iterator.hasNext()) {
            Member member = iterator.next();
            if (member != null && memberId.equals(member.get_id())) {
                final int index = mItems.indexOf(member);
                notifyItemRangeRemoved(index, getItemCount());
                iterator.remove();
            }
        }
        mOriginItems.clear();
        mOriginItems.addAll(mItems);
    }

    public void filter(String keyword) {
        mItems.clear();
        if (TextUtils.isEmpty(keyword)) {
            for (Member member: mOriginItems) {
                mItems.add(member);
            }
            notifyDataSetChanged();
            return;
        }
        for (Member member: mOriginItems) {
            if (member.getAlias().contains(keyword) || PinyinUtil.converterToFirstSpell(member.getAlias()).toLowerCase().contains(keyword)) {
                mItems.add(member);
            }
        }
        notifyDataSetChanged();
    }

    public List<Member> getItems() {
        return mItems;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnRemoveClickListener {
        void onRemove(Member member);
    }

    public void setOnRemoveListener(OnRemoveClickListener l) {
        removeListener = l;
    }

    @Override
    public GroupMemberAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_member, parent, false));
    }

    @Override
    public void onBindViewHolder(GroupMemberAdapter.ViewHolder holder, final int position) {
        if (position == mItems.size()) {
            holder.text.setTextColor(holder.text.getContext().getResources().getColor(R.color.material_grey_700));
            holder.text.setText(holder.text.getContext().getString(R.string.add_from_team));
            holder.imageView.setImageResource(R.drawable.ic_add_grey);
            holder.removeView.setVisibility(View.GONE);
        } else {
            final Member member = mItems.get(position);
            holder.removeView.setVisibility(BizLogic.isAdmin() ? View.VISIBLE : View.GONE);
            holder.removeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (removeListener == null) return;
                    removeListener.onRemove(member);
                }
            });
            if (!TextUtils.isEmpty(member.getAvatarUrl())) {
                MainApp.IMAGE_LOADER.displayImage(member.getAvatarUrl(), holder.imageView,
                        ImageLoaderConfig.AVATAR_OPTIONS);
            }
            holder.text.setText(member.getAlias());
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return group.get_creatorId().equals(BizLogic.getUserInfo().get_id()) && BizLogic.isAdmin() ? mItems.size() + 1 : mItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        SelectableRoundedImageView imageView;
        View removeView;
        TextView text;
        public ViewHolder(View view) {
            super(view);
            imageView = (SelectableRoundedImageView) view.findViewById(R.id.image);
            removeView = view.findViewById(R.id.remove);
            text = (TextView) view.findViewById(R.id.text);
        }
    }
}
