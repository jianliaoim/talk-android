package com.teambition.talk.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.teambition.talk.entity.Member;
import com.teambition.talk.ui.activity.MultiCallActivity;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.ThemeUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by wlanjie on 15/9/11.
 */
public class MultiCallAdapter extends RecyclerView.Adapter<MultiCallAdapter.ViewHolder> {

    private final List<MemberItem> mItems = new ArrayList<>();

    final MultiCallActivity mActivity;

    public MultiCallAdapter(MultiCallActivity activity) {
        mActivity = activity;
    }

    public List<MemberItem> getItems() {
        return mItems;
    }

    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onClick(View view, Member member);
    }

    public void setListener(OnItemClickListener l) {
        mListener = l;
    }

    public void setItems(List<Member> items) {
        if (items == null || items.isEmpty()) return;
        mItems.clear();
        for (Member member : items) {
            if (member != null && !member.isInvite()) {
                mItems.add(new MemberItem(member));
                if (BizLogic.getUserInfo() != null && BizLogic.getUserInfo().get_id().equals(member.get_id())) {
                    mActivity.mMembers.add(member);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public MultiCallAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_choose_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MultiCallAdapter.ViewHolder holder, final int position) {
        final MemberItem item = mItems.get(position);
        if (!TextUtils.isEmpty(item.member.getAvatarUrl())) {
            MainApp.IMAGE_LOADER.displayImage(item.member.getAvatarUrl(), holder.imageView, ImageLoaderConfig.AVATAR_OPTIONS);
        }
        holder.nameText.setText(item.member.getAlias());
        holder.headerText.setVisibility(View.INVISIBLE);
        holder.headerImg.setVisibility(View.INVISIBLE);
        holder.checkLayout.setVisibility(mActivity.mMembers.contains(item.member) ? View.VISIBLE : View.GONE);
        if (position == 0) {
            holder.headerImg.setVisibility(View.VISIBLE);
            holder.headerImg.setImageDrawable(ThemeUtil.getThemeDrawable(holder.headerImg.getResources(),
                    R.drawable.ic_me, BizLogic.getTeamColor()));
        } else if (position == 1 && BizLogic.isAdmin(item.member)) {
            holder.headerImg.setVisibility(View.VISIBLE);
            holder.headerImg.setImageDrawable(ThemeUtil.getThemeDrawable(holder.headerImg.getResources(),
                    R.drawable.ic_admin, BizLogic.getTeamColor()));
        } else if (item.headerText != null && !item.headerText.
                equals(mItems.get(position - 1).headerText)) {
            holder.headerText.setVisibility(View.VISIBLE);
            holder.headerText.setText(item.headerText);
        }
        if (TextUtils.isEmpty(item.member.getPhoneForLogin())) {
            holder.itemView.setEnabled(false);
            holder.maskImage.setVisibility(View.VISIBLE);
        } else {
            holder.itemView.setEnabled(true);
            holder.maskImage.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null && BizLogic.getUserInfo() != null && !item.member.get_id().equals(BizLogic.getUserInfo().get_id())) {
                    if (mActivity.mMembers.size() >= MultiCallActivity.MAX_CALL) return;
                    if (mActivity.mMembers.contains(item.member)) {
                        mActivity.mMembers.remove(item.member);
                    } else {
                        mActivity.mMembers.add(item.member);
                    }
                    notifyItemChanged(position);
                    mListener.onClick(v, item.member);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.header_text)
        TextView headerText;
        @InjectView(R.id.header_img)
        ImageView headerImg;
        @InjectView(R.id.image)
        RoundedImageView imageView;
        @InjectView(R.id.name)
        TextView nameText;
        @InjectView(R.id.img_check_layout)
        View checkLayout;
        @InjectView(R.id.mask)
        ImageView maskImage;

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
            if (!BizLogic.isAdmin(member) && !BizLogic.isMe(member.get_id())) {
                headerText = PinyinUtil.converterToFirstSpell(member.getAlias()).toUpperCase();
                if (!StringUtil.isNumber(headerText) && !StringUtil.isLetter(headerText)) {
                    headerText = "#";
                }
            }
        }
    }
}
