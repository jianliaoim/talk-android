package com.teambition.talk.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.Message;
import com.teambition.talk.ui.RowFactory;
import com.teambition.talk.ui.row.MessageRow;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wlanjie on 16/2/15.
 */
public class MessageSearchAdapter extends RecyclerView.Adapter {

    private final static int VIEW_TYPE_ITEM = 0;
    private final static int VIEW_TYPE_LOADING = 1;
    final List<Message> items = new ArrayList<>();
    final List<MessageRow> rows = new ArrayList<>();
    private OnItemClick itemClick;
    private boolean isLoading = false;

    public void setOnItemClick(OnItemClick itemClick) {
        this.itemClick = itemClick;
    }

    public void clear() {
        this.items.clear();
        this.rows.clear();
    }

    public void addItems(List<Message> messages) {
        if (messages == null) return;
        for (Message message : messages) {
            if (message != null) {
                items.add(message);
            }
        }
        rows.addAll(RowFactory.getInstance().makeMessageRow(messages));
        notifyDataSetChanged();
    }

    public void setIsLoading(boolean isLoading) {
        this.isLoading = isLoading;
        if (isLoading) {
            notifyItemInserted(rows.size());
        } else {
            notifyItemRemoved(rows.size());
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_LOADING:
                return new LoadingHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading_white, parent, false));
            case VIEW_TYPE_ITEM:
                return MessageRow.createViewHolder(parent);
        }
        return MessageRow.createViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_ITEM) {
            final MessageRow row = rows.get(position);
            row.renderView(holder, MainApp.CONTEXT);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClick == null) return;
                    itemClick.onItemClick(row.getMessage());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return rows.size() + (isLoading ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoading && position == rows.size() || isLoading && position == rows.size() + 1) {
            return VIEW_TYPE_LOADING;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    static class LoadingHolder extends RecyclerView.ViewHolder {
        public LoadingHolder(View itemView) {
            super(itemView);
        }
    }

    public interface OnItemClick {
        void onItemClick(Message message);
    }
}
