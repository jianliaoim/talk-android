package com.teambition.talk.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.makeramen.roundedimageview.RoundedImageView;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.Member;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by zeatual on 15/10/21.
 */
public class ChooseMemberResultAdapter extends RecyclerView.Adapter<ChooseMemberResultAdapter.ViewHolder> {

    private OnItemClickListener listener;
    private List<Member> members;

    public interface OnItemClickListener {
        void onItemClick(Member member);
    }

    public ChooseMemberResultAdapter(OnItemClickListener listener) {
        this.listener = listener;
        members = new ArrayList<>();
    }

    public void updateMember(Member member) {
        int position = 0;
        boolean contains = false;
        for (int i = 0; i < members.size(); i++) {
            Member m = members.get(i);
            if (member.get_id().equals(m.get_id())) {
                contains = true;
                position = i;
                break;
            }
        }
        if (contains) {
            members.remove(position);
            notifyItemRemoved(position);
        } else {
            members.add(member);
            notifyItemInserted(members.size());
        }
    }

    public void addMe(Member me) {
        if (me != null) {
            this.members.add(me);
            notifyItemInserted(0);
        }
    }

    public void addMembers(List<Member> members) {
        if (members == null || members.isEmpty()) return;
        int size = 0;
        for (Member member : members) {
            boolean contains = false;
            for (Member member1 : this.members) {
                if (member1.get_id().equals(member.get_id())) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                this.members.add(member);
                size++;
            }
        }
        if (size != 0) {
            notifyItemRangeInserted(this.members.size(), size);
        }
    }

    public List<Member> getMembers() {
        return members;
    }

    public ArrayList<String> getMemberIds() {
        ArrayList<String> ids = new ArrayList<>();
        for (Member member : members) {
            ids.add(member.get_id());
        }
        return ids;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_choose_member_result, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Member member = members.get(position);
        MainApp.IMAGE_LOADER.displayImage(member.getAvatarUrl(), holder.avatar,
                ImageLoaderConfig.AVATAR_OPTIONS);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.avatar)
        RoundedImageView avatar;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}
