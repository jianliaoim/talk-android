package com.teambition.talk.adapter;

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
import com.teambition.talk.util.ThemeUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by zeatual on 14/10/31.
 */
public class MemberCheckAdapter extends RecyclerView.Adapter {

    private List<Member> items;

    private List<Member> originMembers = new ArrayList<>();

    private OnItemClickListener listener;

    private final List<Member> selectedMembers = new ArrayList<>();

    public interface OnItemClickListener {
        void onClick(Member member);
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public MemberCheckAdapter() {
        items = new ArrayList<>();
    }

    public List<Member> getItems() {
        return items;
    }

    public List<Member> getSelectedMembers() {
        return selectedMembers;
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

    public void addItems(List<Member> members) {
        this.items.clear();
        this.originMembers.clear();
        for (Member member : members) {
            if (member != null) {
                items.add(member);
                originMembers.add(member);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View itemView = inflater.inflate(R.layout.item_choose_member, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder vh, final int position) {
        if (vh instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder) vh;
            final Member item = items.get(position);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener == null) return;
                    listener.onClick(item);
                }
            });
            holder.headerText.setVisibility(View.INVISIBLE);
            holder.headerImg.setVisibility(View.INVISIBLE);
            holder.imageCheck.setVisibility(selectedMembers.contains(item)
                    ? View.VISIBLE : View.INVISIBLE);
            holder.name.setText(item.getAlias());
            MainApp.IMAGE_LOADER.displayImage(item.getAvatarUrl(), holder.avatar,
                    ImageLoaderConfig.AVATAR_OPTIONS);
            if (position == 0) {
                if (item.get_id().equals(BizLogic.getUserInfo().get_id())) {
                    holder.headerImg.setVisibility(View.VISIBLE);
                    holder.headerImg.setImageDrawable(ThemeUtil.getDrawableWithColor(holder.headerImg.getResources(),
                            R.drawable.ic_me, R.color.colorPrimary));
                    selectedMembers.add(item);
                    holder.imageCheck.setVisibility(View.VISIBLE);
                } else {
                    holder.headerText.setVisibility(View.VISIBLE);
                    holder.headerText.setText(item.getAliasPinyin());
                }
            } else if (item.getAliasPinyin() != null && !item.getAliasPinyin().
                    equals(items.get(position - 1).getAliasPinyin())) {
                holder.headerText.setVisibility(View.VISIBLE);
                holder.headerText.setText(item.getAliasPinyin());
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) listener.onClick(item);
                    if (selectedMembers.contains(item)) {
                        selectedMembers.remove(item);
                    } else {
                        selectedMembers.add(item);
                    }
                    notifyItemChanged(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.image)
        ImageView avatar;
        @InjectView(R.id.name)
        TextView name;
        @InjectView(R.id.header_text)
        TextView headerText;
        @InjectView(R.id.header_img)
        ImageView headerImg;
        @InjectView(R.id.img_check_layout)
        View imageCheck;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }
}
