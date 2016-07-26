package com.teambition.talk.adapter;

import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.teambition.talk.R;
import com.teambition.talk.entity.Tag;
import com.teambition.talk.util.DensityUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by wlanjie on 15/7/16.
 */
public class TeamTagAdapter extends RecyclerView.Adapter<TeamTagAdapter.TeamTagViewHolder> {

    final List<Tag> mTags = new ArrayList<>();

    final OnItemListener mListener;

    public TeamTagAdapter(OnItemListener listener) {
        mListener = listener;
    }

    @Override
    public TeamTagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        TextView textView = new TextView(parent.getContext());
//        final int height = DensityUtil.dip2px(parent.getContext(), 56);
//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, height);
//        params.gravity = Gravity.CENTER_VERTICAL;
//        textView.setLayoutParams(params);
//        final int padding = DensityUtil.dip2px(parent.getContext(), 16);
//        textView.setPadding(padding, 0, 0, 0);
//        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
//        textView.setTextColor(0xff212121);
//        textView.setGravity(Gravity.CENTER_VERTICAL);
//        textView.setBackgroundResource(R.drawable.bg_item);
//        return new TeamTagViewHolder(textView);
        return new TeamTagViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag, parent, false));
    }

    @Override
    public void onBindViewHolder(TeamTagViewHolder holder, int position) {
        final Tag tag = mTags.get(position);
        holder.tagText.setText(tag.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClickListener(tag);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mListener.onItemLongClickListener(tag);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTags.size();
    }

    public void setItems(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) return;
        for (Tag tag : tags) {
            if (tag != null) {
                mTags.add(tag);
            }
        }
        notifyDataSetChanged();
    }

    public void removeItem(Tag tag) {
        if (tag == null) return;
        Iterator<Tag> iterator = mTags.iterator();
        while (iterator.hasNext()) {
            Tag t = iterator.next();
            if (t.get_id().equals(tag.get_id())) {
                int index = mTags.indexOf(t);
                iterator.remove();
                notifyItemRemoved(index);
            }
        }
    }

    public void updateItem(String _id, String id, String name) {
        for (int i = 0; i < mTags.size(); i++) {
            Tag t = mTags.get(i);
            if (_id.equals(t.get_id())) {
                t.setName(name);
                t.setId(id);
                t.set_id(_id);
                notifyItemChanged(i);
            }
        }
    }

    static class TeamTagViewHolder extends RecyclerView.ViewHolder {

        final TextView tagText;

        public TeamTagViewHolder(View view) {
            super(view);
            view.setBackgroundResource(R.drawable.bg_item);
            tagText = (TextView) view.findViewById(R.id.tag_text);
            ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
            final int size = DensityUtil.dip2px(itemView.getContext(), 8.0f);
            drawable.setIntrinsicHeight(size);
            drawable.setIntrinsicWidth(size);
            drawable.setBounds(0, 0, size, size);
            drawable.getPaint().setColor(itemView.getResources().getColor(R.color.colorPrimary));
            tagText.setCompoundDrawables(drawable, null, null, null);
            itemView.findViewById(R.id.tag_check).setVisibility(View.GONE);
        }
    }

    public interface OnItemListener {

        void onItemClickListener(Tag tag);

        void onItemLongClickListener(Tag tag);
    }
}
