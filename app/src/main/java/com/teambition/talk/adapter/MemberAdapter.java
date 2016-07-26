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
import com.teambition.talk.BizLogic;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.Member;
import com.teambition.talk.ui.activity.GroupActivity;
import com.teambition.talk.ui.activity.LeaveMemberActivity;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.ThemeUtil;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by zeatual on 14/10/31.
 */
public class MemberAdapter extends RecyclerView.Adapter {

    private final static int GROUP_ITEM = 0;
    private final static int CONTENT = 1;
    private final static int LEAVE_MEMBER = 2;

    private List<Member> items;

    private final List<Member> originMembers = new ArrayList<>();

    private final ArrayList<Member> leaveMemberItems = new ArrayList<>();

    private OnItemClickListener listener;

    private boolean isShowGroup = true;

    private boolean isShowMe = true;

    private boolean isShowAdmin = true;

    private boolean isShowUnread = true;

    public interface OnItemClickListener {
        void onClick(Member member);
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public MemberAdapter() {
        items = new ArrayList<>();
    }

    public void setShowMe(boolean isShowMe) {
        this.isShowMe = isShowMe;
    }

    public void setShowUnread(boolean isShowUnread) {
        this.isShowUnread = isShowUnread;
    }

    public void setShowAdmin(boolean isShowAdmin) {
        this.isShowAdmin = isShowAdmin;
    }

    public void setShowGroup(boolean isShowGroup) {
        this.isShowGroup = isShowGroup;
    }

    public boolean getShowGroup() {
        return isShowGroup;
    }

    public boolean getShowMe() {
        return isShowMe;
    }

    public boolean getShowAdmin() {
        return isShowAdmin;
    }

    public void filter(String keyword) {
        this.items.clear();
        if (TextUtils.isEmpty(keyword)) {
            for (Member originMember : originMembers) {
                this.items.add(originMember);
            }
            notifyDataSetChanged();
            return;
        }
        for (Member item : originMembers) {
            if (isMatched(item.getAlias(), keyword)) {
                this.items.add(item);
            }
        }
        notifyDataSetChanged();
    }

    private boolean isMatched(String str, String keyword) {
        return str.contains(keyword) ||
                PinyinUtil.converterToSpell(str).toLowerCase().contains(keyword);
    }

    public void addLeaveMembers(List<Member> members) {
        this.leaveMemberItems.clear();
        for (Member member : members) {
            leaveMemberItems.add(member);
        }
        notifyDataSetChanged();
    }

    public List<Member> getLeaveMembers() {
        return leaveMemberItems;
    }

    public void addItems(List<Member> members) {
        this.items.clear();
        this.originMembers.clear();
        for (Member member : members) {
            if (member != null) {
                member.setAliasPinyin(null);
                if (!BizLogic.isAdmin(member) && !BizLogic.isMe(member.get_id())) {
                    String aliasPinyin = "";
                    try {
                        if (StringUtil.isNotBlank(member.getAlias())) {
                            aliasPinyin = PinyinUtil.converterToFirstSpell(member.getAlias()).toUpperCase();
                        }
                    } catch (Exception e) {
                        if (StringUtil.isNotBlank(member.getPinyin())) {
                            aliasPinyin = member.getPinyin().substring(0, 1).toUpperCase();
                        }
                    }
                    if (!StringUtil.isNumber(aliasPinyin) && !StringUtil.isLetter(aliasPinyin)) {
                        aliasPinyin = "#";
                    }
                    member.setAliasPinyin(aliasPinyin);
                }
                items.add(member);
                originMembers.add(member);
            }
        }
        notifyDataSetChanged();
    }

    public List<Member> getItems() {
        return items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == GROUP_ITEM) {
            final View groupView = inflater.inflate(R.layout.item_contacts_group, parent, false);
            return new GroupViewHolder(groupView);
        } else if (viewType == CONTENT) {
            final View itemView = inflater.inflate(R.layout.item_member, parent, false);
            return new ViewHolder(itemView);
        } else {
            final View leaveView = inflater.inflate(R.layout.item_leave_member, parent, false);
            return new LeaveMemberViewHolder(leaveView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder vh, final int position) {
        if (vh instanceof GroupViewHolder) {
            vh.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Intent intent = new Intent(v.getContext(), GroupActivity.class);
                    v.getContext().startActivity(intent);
                }
            });
        } else if (vh instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder) vh;
            final Member item = items.get(isShowGroup ? position - 1 : position);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener == null) return;
                    listener.onClick(item);
                }
            });
            holder.headerText.setVisibility(View.INVISIBLE);
            holder.headerImg.setVisibility(View.INVISIBLE);
            holder.vNotVisit.setVisibility(View.GONE);
            MainApp.IMAGE_LOADER.displayImage(item.getAvatarUrl(), holder.avatar,
                    ImageLoaderConfig.AVATAR_OPTIONS);
            holder.name.setText(item.getAlias());
            if (item.isInvite()) {
                holder.vNotVisit.setVisibility(View.VISIBLE);
            }
            if (item.getUnread() != null && item.getUnread() > 0 && isShowUnread) {
                holder.unread.setVisibility(View.VISIBLE);
            } else {
                holder.unread.setVisibility(View.GONE);
            }
            if (position == (isShowGroup ? 1 : 0)) {
                if (isShowMe) {
                    holder.headerImg.setVisibility(View.VISIBLE);
                    holder.headerImg.setImageDrawable(ThemeUtil.getDrawableWithColor(holder.headerImg.getResources(),
                            R.drawable.ic_me, R.color.colorPrimary));
                } else {
                    holder.headerText.setVisibility(View.VISIBLE);
                    holder.headerText.setText(item.getAliasPinyin());
                }
            } else if (isShowAdmin && position == (isShowGroup ? 2 : 1) && BizLogic.isAdmin(item)) {
                holder.headerImg.setVisibility(View.VISIBLE);
                holder.headerImg.setImageDrawable(ThemeUtil.getDrawableWithColor(holder.headerImg.getResources(),
                        R.drawable.ic_admin, R.color.colorPrimary));
            } else if (item.getAliasPinyin() != null
                    && !item.getAliasPinyin().equals(items.get(isShowMe ? position - (isShowGroup ? 2 : 1) : position - 1).getAliasPinyin())
                    && !item.isInvite()) {
                holder.headerText.setVisibility(View.VISIBLE);
                holder.headerText.setText(item.getAliasPinyin());
            }
        } else {
            LeaveMemberViewHolder holder = (LeaveMemberViewHolder) vh;
            holder.itemView.setVisibility(View.VISIBLE);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), LeaveMemberActivity.class);
                    intent.putExtra("leave_members", Parcels.wrap(leaveMemberItems));
                    v.getContext().startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (!isShowGroup) return items.size();
        return leaveMemberItems == null || leaveMemberItems.isEmpty() ? items.size() + 1 : items.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (isShowGroup && position == 0) {
            return GROUP_ITEM;
        } else if (!leaveMemberItems.isEmpty() && getItemCount() - 1 == position) {
            return LEAVE_MEMBER;
        } else {
            return CONTENT;
        }
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {

        GroupViewHolder(View view) {
            super(view);
        }
    }

    static class LeaveMemberViewHolder extends RecyclerView.ViewHolder {

        public LeaveMemberViewHolder(View v) {
            super(v);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.avatar)
        ImageView avatar;
        @InjectView(R.id.name)
        TextView name;
        @InjectView(R.id.flag_unread)
        View unread;
        @InjectView(R.id.header_text)
        public TextView headerText;
        @InjectView(R.id.header_img)
        public ImageView headerImg;
        @InjectView(R.id.tv_not_visit)
        View vNotVisit;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }
}
