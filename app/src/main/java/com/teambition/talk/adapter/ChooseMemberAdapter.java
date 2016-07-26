package com.teambition.talk.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.teambition.common.PinyinUtil;
import com.teambition.talk.BizLogic;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.Group;
import com.teambition.talk.entity.Member;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.ThemeUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by zeatual on 15/10/20.
 */
public class ChooseMemberAdapter extends RecyclerView.Adapter {

    private static final int TYPE_MEMBER = 0;
    private static final int TYPE_GROUP = 1;
    private static final int TYPE_GROUP_SWITCHER = 2;

    private OnItemClickListener listener;
    private boolean isGroup = false;
    private String keyword;

    private List<Member> originMembers;
    private List<Member> members;
    private List<Group> originGroups;
    private List<Group> groups;
    private List<Member> selectedMembers;
    private final List<String> selectedMemberIds;
    private List<String> originalSelectMemberIds;
    private final List<String> removeMemberIds;
    private boolean isOrdinary;

    public interface OnItemClickListener {
        void onGroupClick(Group group);

        void onMemberClick(Member member);
    }

    public ChooseMemberAdapter(OnItemClickListener listener) {
        this.listener = listener;
        this.isOrdinary = isOrdinary;
        selectedMemberIds = new ArrayList<>();
        originMembers = new ArrayList<>();
        members = new ArrayList<>();
        originGroups = new ArrayList<>();
        groups = new ArrayList<>();
        selectedMembers = new ArrayList<>();
        originalSelectMemberIds = new ArrayList<>();
        removeMemberIds = new ArrayList<>();
    }

    public void setIsOrdinary(boolean isOrdinary) {
        this.isOrdinary = isOrdinary;
    }

    public void addMemberIds(List<Member> members) {
        if (members == null || members.isEmpty()) return;
        for (Member member : members) {
            if (member != null) {
                selectedMemberIds.add(member.get_id());
                originalSelectMemberIds.add(member.get_id());
            }
        }
    }

    public List<String> getRemoveMemberIds() {
        return removeMemberIds;
    }

    public List<Member> getSelectedMembers() {
        return selectedMembers;
    }

    public void setData(List<Member> members, List<Group> groups) {
        originMembers.addAll(members);
        Iterator<Member> iterator = originMembers.iterator();
        Member me = null;
        while (iterator.hasNext()) {
            Member member = iterator.next();
            if (member != null && member.get_id().equals(BizLogic.getUserInfo().get_id())) {
                me = member;
                iterator.remove();
            }
        }
        if (me != null) {
            this.originMembers.add(0, me);
        }
        this.members.addAll(this.originMembers);
        if (groups != null) {
            originGroups.addAll(groups);
            this.groups.addAll(groups);
        }
        notifyDataSetChanged();
    }

    public void setIsGroup(boolean isGroup) {
        this.isGroup = isGroup;
        reset();
    }

    public boolean getIsGroup() {
        return isGroup;
    }

    public void reset() {
        keyword = "";
        members.clear();
        members.addAll(originMembers);
        groups.clear();
        groups.addAll(originGroups);
        notifyDataSetChanged();
    }

    public void filter(String keyword) {
        this.keyword = keyword;
        if (isGroup) {
            groups.clear();
            for (Group group : originGroups) {
                if (isMatched(group.getName())) {
                    groups.add(group);
                }
            }
        } else {
            members.clear();
            for (Member member : originMembers) {
                if (isMatched(member.getAlias())) {
                    members.add(member);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateSelectedMember(Member member) {
        if (selectedMemberIds.contains(member.get_id())) {
            selectedMemberIds.remove(member.get_id());
            if (!isGroup) {
                notifyDataSetChanged();
            }
        }
    }

    public void rangeSelect(List<Member> members) {
        for (Member member : members) {
            if (!selectedMemberIds.contains(member.get_id())) {
                selectedMemberIds.add(member.get_id());
                selectedMembers.add(member);
                notifyDataSetChanged();
            }
        }
    }

    private boolean isMatched(String str) {
        return str.contains(keyword) ||
                PinyinUtil.converterToSpell(str).toLowerCase().contains(keyword);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_GROUP_SWITCHER:
                return new GroupSwitcherHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_contacts_group, parent, false));
            case TYPE_GROUP:
                return new GroupHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_choose_group, parent, false));
            case TYPE_MEMBER:
            default:
                return new MemberHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_choose_member, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        switch (getItemViewType(position)) {
            case TYPE_MEMBER:
                final Member member = members.get(members.size() == originMembers.size()
                        ? position - 1 : position);
                MemberHolder memberHolder = (MemberHolder) holder;
                memberHolder.headerText.setVisibility(View.INVISIBLE);
                memberHolder.headerImage.setVisibility(View.INVISIBLE);
                MainApp.IMAGE_LOADER.displayImage(member.getAvatarUrl(), memberHolder.image,
                        ImageLoaderConfig.AVATAR_OPTIONS);
                memberHolder.tvName.setText(StringUtil.getHighlightSpan(member.getAlias(), keyword,
                        holder.itemView.getContext().getResources()));
                memberHolder.checkLayout.setVisibility(selectedMemberIds.contains(member.get_id())
                        ? View.VISIBLE : View.INVISIBLE);
                if (BizLogic.isMe(member.get_id())) {
                    memberHolder.checkLayout.setVisibility(View.VISIBLE);
                    memberHolder.itemView.setEnabled(false);
                } else {
                    memberHolder.itemView.setEnabled(true);
                }
                if (position == (members.size() == originMembers.size() ? 1 : 0)) {
                    memberHolder.headerImage.setVisibility(View.VISIBLE);
                    memberHolder.headerText.setVisibility(View.GONE);
                    memberHolder.headerImage.setImageDrawable(ThemeUtil.getDrawableWithColor(memberHolder.headerImage.getResources(),
                            R.drawable.ic_me, R.color.colorPrimary));
                } else if (position == (members.size() == originMembers.size() ? position - 2 : position - 1) && BizLogic.isAdmin(member)) {
                    memberHolder.headerImage.setVisibility(View.VISIBLE);
                    memberHolder.headerText.setVisibility(View.GONE);
                    memberHolder.headerImage.setImageDrawable(ThemeUtil.getDrawableWithColor(memberHolder.headerImage.getResources(),
                            R.drawable.ic_admin, R.color.colorPrimary));
                } else if (member.getAliasPinyin()!= null && !member.getAliasPinyin().
                        equals(members.get((members.size() == originMembers.size() ? position - 2 : position - 1)).getAliasPinyin())) {
                    memberHolder.headerImage.setVisibility(View.GONE);
                    memberHolder.headerText.setVisibility(View.VISIBLE);
                    memberHolder.headerText.setText(member.getAliasPinyin());
                }
                memberHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isOrdinary && originalSelectMemberIds.contains(member.get_id())) {
                            return;
                        }
                        if (listener != null) {
                            listener.onMemberClick(member);
                        }

                        if (selectedMemberIds.contains(member.get_id())) {
                            selectedMemberIds.remove(member.get_id());
                            removeMemberIds.add(member.get_id());
                            int memberPos = 0;
                            for (Member selectedMember : selectedMembers) {
                                if (selectedMember.get_id().equals(member.get_id())) {
                                    selectedMembers.remove(memberPos);
                                    break;
                                }
                                memberPos++;
                            }
                        } else {
                            if (removeMemberIds.contains(member.get_id())) {
                                removeMemberIds.remove(member.get_id());
                            }
                            selectedMemberIds.add(member.get_id());
                            selectedMembers.add(member);
                        }
                        notifyDataSetChanged();
                    }
                });
                break;
            case TYPE_GROUP:
                final Group group = groups.get(position);
                GroupHolder groupHolder = (GroupHolder) holder;
                groupHolder.image.setBackgroundResource(R.drawable.bg_round_blue);
                groupHolder.tvName.setText(StringUtil.getHighlightSpan(group.getName(), keyword,
                        holder.itemView.getContext().getResources()));
                groupHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onGroupClick(group);
                    }
                });
                break;
            case TYPE_GROUP_SWITCHER:
                GroupSwitcherHolder groupSwitcherHolder = (GroupSwitcherHolder) holder;
                groupSwitcherHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setIsGroup(true);
                    }
                });
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (isGroup) {
            return groups.size();
        } else {
            return members.size() == originMembers.size() ? members.size() + 1 : members.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isGroup) {
            return TYPE_GROUP;
        } else if (members.size() == originMembers.size() && position == 0) {
            return TYPE_GROUP_SWITCHER;
        } else {
            return TYPE_MEMBER;
        }
    }

    static class MemberHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.image)
        RoundedImageView image;
        @InjectView(R.id.name)
        TextView tvName;
        @InjectView(R.id.img_check_layout)
        View checkLayout;
        @InjectView(R.id.header_img)
        ImageView headerImage;
        @InjectView(R.id.header_text)
        TextView headerText;

        public MemberHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }

    static class GroupHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.image)
        RoundedImageView image;
        @InjectView(R.id.name)
        TextView tvName;

        public GroupHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }

    static class GroupSwitcherHolder extends RecyclerView.ViewHolder {
        public GroupSwitcherHolder(View itemView) {
            super(itemView);
        }
    }
}
