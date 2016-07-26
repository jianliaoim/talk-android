package com.teambition.talk.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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

import java.util.Iterator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by zeatual on 15/2/3.
 */
public class MemberAddAdapter extends RecyclerView.Adapter<MemberAddAdapter.ViewHolder> {

    private List<Member> members;
    private Context context;
    private OnItemClickListener listener;
    private OnRemoveClickListener removeListener;

    public MemberAddAdapter(Context context, List<Member> members, OnItemClickListener listener) {
        this.context = context;
        this.members = members;
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnRemoveClickListener {
        void onRemove(Member member);
    }

    public void setOnRemoveListener(OnRemoveClickListener l) {
        removeListener = l;
    }

    public void addMembers(List<Member> items, List<String> removeMemberIds) {
        if (items == null) return;
        for (Member item : items) {
            boolean isFind = true;
            for (Member member : members) {
                if (member == null || item.get_id().equals(member.get_id())) {
                    isFind = false;
                    break;
                }
            }
            if (isFind) {
                members.add(item);
            }
        }
        final Iterator iterator = members.iterator();
        for (String removeMemberId : removeMemberIds) {
            while (iterator.hasNext()) {
                final Member member = (Member) iterator.next();
                if (removeMemberId.equals(member.get_id())) {
                    iterator.remove();
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_add_member, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.name.setTextColor(context.getResources().getColor(R.color.material_grey_900));
        holder.checkbox.setVisibility(View.GONE);
        holder.removeView.setVisibility(View.GONE);
        if (position == members.size()) {
            holder.name.setTextColor(context.getResources().getColor(R.color.material_grey_700));
            holder.name.setText(context.getString(R.string.add_from_team));
            holder.avatar.setImageResource(R.drawable.ic_add_grey);
        } else {
            final Member member = members.get(position);
            if (member != null) {
                if (!member.get_id().equals(BizLogic.getUserInfo().get_id())) {
                    holder.removeView.setVisibility(View.VISIBLE);
                    holder.removeView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            members.remove(position);
                            notifyItemRangeRemoved(position, getItemCount());
                            if (removeListener == null) return;
                            removeListener.onRemove(member);
                        }
                    });
                }
                if (!TextUtils.isEmpty(member.getAvatarUrl())) {
                    MainApp.IMAGE_LOADER.displayImage(member.getAvatarUrl(), holder.avatar,
                            ImageLoaderConfig.AVATAR_OPTIONS);
                }
                holder.name.setText(member.getAlias());
            }
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return members.size() + 1;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.image)
        ImageView avatar;
        @InjectView(R.id.name)
        TextView name;
        @InjectView(R.id.checkbox)
        View checkbox;
        @InjectView(R.id.remove)
        View removeView;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}
