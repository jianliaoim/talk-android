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
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by wlanjie on 15/8/5.
 */
public class LeaveMemberAdapter extends RecyclerView.Adapter<LeaveMemberAdapter.ViewHolder> {

    private final List<MemberItem> mItems = new ArrayList<>();

    private OnItemClickListener mListener;

    public void setItems(List<Member> items) {
        if (items == null || items.isEmpty()) return;
        mItems.clear();
        for (Member member : items) {
            mItems.add(new MemberItem(member));
        }
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {

        void onClick(Member member);
    }

    public void setListener(OnItemClickListener l) {
        mListener = l;
    }


    public List<MemberItem> getItems() {
        return mItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final MemberItem item = mItems.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClick(item.member);
                }
            }
        });
        holder.headerText.setVisibility(View.INVISIBLE);
        holder.headerImg.setVisibility(View.GONE);
        if (item.member.getUnread() != null && item.member.getUnread() > 0) {
            holder.unread.setVisibility(View.VISIBLE);
        } else {
            holder.unread.setVisibility(View.GONE);
        }
        holder.name.setText(item.member.getAlias());
        MainApp.IMAGE_LOADER.displayImage(item.member.getAvatarUrl(), holder.avatar, ImageLoaderConfig.AVATAR_OPTIONS);
        if (position == 0 || item.headerText != null && !item.headerText.equals(mItems.get(position - 1).headerText)) {
            holder.headerText.setVisibility(View.VISIBLE);
            holder.headerText.setText(item.headerText);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.avatar)
        ImageView avatar;
        @InjectView(R.id.name)
        TextView name;
        @InjectView(R.id.flag_unread)
        View unread;
        @InjectView(R.id.header_text)
        TextView headerText;
        @InjectView(R.id.header_img)
        ImageView headerImg;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }

    class MemberItem {
        Member member;
        String headerText;

        MemberItem(Member member) {
            this.member = member;
            if (!BizLogic.isMe(member.get_id())) {
                if (member.getPinyin() != null && member.getPinyin().length() > 0) {
                    headerText = member.getPinyin().substring(0, 1).toUpperCase();
                } else {
                    headerText = "#";
                }
            }
        }
    }
}
