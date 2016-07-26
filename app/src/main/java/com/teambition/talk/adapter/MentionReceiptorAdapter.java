package com.teambition.talk.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.Member;
import com.teambition.talk.imageloader.ImageLoaderConfig;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by nlmartian on 2/18/16.
 */
public class MentionReceiptorAdapter extends RecyclerView.Adapter {

    private List<String> mentionedIds = new ArrayList<>();
    private List<String> receivedIds = new ArrayList<>();

    public void updateData(List<String> mentionedIds, List<String> receivedIds) {
        this.mentionedIds.clear();
        this.receivedIds.clear();
        this.mentionedIds.addAll(mentionedIds);
        this.receivedIds.addAll(receivedIds);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_receiptor, parent, false);
        return new ReceiptorHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ReceiptorHolder receiptorHolder = (ReceiptorHolder) holder;
        Member member = MainApp.globalMembers.get(mentionedIds.get(position));
        if (member != null) {
            MainApp.IMAGE_LOADER.displayImage(member.getAvatarUrl(), receiptorHolder.avatar,
                    ImageLoaderConfig.AVATAR_OPTIONS);
            receiptorHolder.checkLayout.setVisibility(receivedIds.contains(mentionedIds.get(position))
                    ? View.VISIBLE : View.GONE);
            receiptorHolder.name.setText(member.getAlias());
        }
    }

    @Override
    public int getItemCount() {
        return mentionedIds.size();
    }

    static class ReceiptorHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.image)
        RoundedImageView avatar;
        @InjectView(R.id.name)
        TextView name;
        @InjectView(R.id.img_check_layout)
        View checkLayout;

        public ReceiptorHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}
