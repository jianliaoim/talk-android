package com.teambition.talk.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.teambition.talk.BizLogic;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.Member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by zeatual on 15/11/4.
 */
public class StorySettingMemberAdapter extends RecyclerView.Adapter<StorySettingMemberAdapter.ViewHolder> {

    private List<Member> members;

    private OnRemoveListener mListener;

    private boolean canRemoveMember;

    public StorySettingMemberAdapter(boolean canRemoveMember) {
        this.canRemoveMember = canRemoveMember;
        members = new ArrayList<>();
    }

    public void updateData(List<Member> members) {
        this.members.clear();
        this.members.addAll(members);
        notifyDataSetChanged();
    }

    public List<Member> getMembers() {
        return members;
    }

    public void addMembers(List<Member> members) {
        if (members == null || members.isEmpty()) return;
        this.members.addAll(members);
        Iterator<Member> iterator = this.members.iterator();
        while (iterator.hasNext()) {
            Member member = iterator.next();
            if (member != null) {
                if (member.get_id().equals(BizLogic.getUserInfo().get_id())) {
                    iterator.remove();
                }
            }
        }
        Collections.sort(this.members, new Comparator<Member>() {
            @Override
            public int compare(Member lhs, Member rhs) {
                return lhs.getAliasPinyin().compareTo(rhs.getAliasPinyin());
            }
        });
        this.members.add(0, MainApp.globalMembers.get(BizLogic.getUserInfo().get_id()));
        notifyDataSetChanged();
    }

    public void removeMembers(List<Member> members) {
        if (members == null || members.isEmpty()) return;
        Iterator<Member> iterator = this.members.iterator();
        while (iterator.hasNext()) {
            Member member = iterator.next();
            if (member != null) {
                for (Member m : members) {
                    if (m != null && m.get_id().equals(member.get_id())) {
                        final int index = this.members.indexOf(member);
                        iterator.remove();
                        notifyItemRemoved(index);
                        break;
                    }
                }
            }
        }
    }

    public void removeMember(final Member member) {
        final int index = members.indexOf(member);
        members.remove(member);
        notifyItemRemoved(index);
    }

    public interface OnRemoveListener {
        void onRemove(Member member);
    }

    public void setOnRemoveListener(OnRemoveListener l) {
        mListener = l;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_topic_member, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Member member = members.get(position);
        if (member == null) return;
        holder.name.setText(member.getAlias());
        if (!BizLogic.isMe(member.get_id()) && canRemoveMember) {
            holder.btnRemove.setVisibility(View.VISIBLE);
        } else {
            holder.btnRemove.setVisibility(View.GONE);
        }
        MainApp.IMAGE_LOADER.displayImage(member.getAvatarUrl(), holder.avatar,
                ImageLoaderConfig.AVATAR_OPTIONS);
        holder.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener == null) return;
                mListener.onRemove(member);
            }
        });
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.image)
        ImageView avatar;
        @InjectView(R.id.remove)
        View btnRemove;
        @InjectView(R.id.text)
        TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}
