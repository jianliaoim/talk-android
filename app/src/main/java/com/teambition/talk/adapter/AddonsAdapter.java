package com.teambition.talk.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teambition.talk.BizLogic;
import com.teambition.talk.R;
import com.teambition.talk.client.ApiConfig;
import com.teambition.talk.entity.AddonsItem;
import com.teambition.talk.ui.activity.AddonsWebView;
import com.teambition.talk.ui.activity.ChatActivity;
import com.teambition.talk.ui.activity.SelectImageActivity;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by zeatual on 15/6/3.
 */
public class AddonsAdapter extends RecyclerView.Adapter<AddonsAdapter.ViewHolder> {

    private List<AddonsItem> moreItems;
    private Context context;
    private final OnAddOnItemClickListener mClick;

    public interface OnAddOnItemClickListener {
        void onAddOnItemClick(int position, AddonsItem item);
    }

    public AddonsAdapter(Context context, String idName, String id, OnAddOnItemClickListener click) {
        this.context = context;
        this.mClick = click;
        moreItems = new ArrayList<>();
        moreItems.add(new AddonsItem(R.drawable.ic_chat_photo,
                context.getString(R.string.addons_picture), null));
        moreItems.add(new AddonsItem(R.drawable.ic_chat_file,
                context.getString(R.string.addons_file), null));
        moreItems.add(new AddonsItem(R.drawable.ic_chat_favorites, context.getString(R.string.favorites_items), null));
        moreItems.add(new AddonsItem(R.drawable.ic_chat_absence,
                context.getString(R.string.addons_absence),
                String.format(ApiConfig.FORM_BASE_URL, idName + id,
                        BizLogic.getTeamId(), ApiConfig.ABSENCE_URL)));
        moreItems.add(new AddonsItem(R.drawable.ic_chat_file,
                context.getString(R.string.addons_file), null));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chat_more, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.text.setCompoundDrawablesWithIntrinsicBounds(null, context.getResources().
                getDrawable(moreItems.get(position).img), null, null);
        holder.text.setText(moreItems.get(position).text);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClick.onAddOnItemClick(position, moreItems.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return moreItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.text)
        TextView text;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }

}
