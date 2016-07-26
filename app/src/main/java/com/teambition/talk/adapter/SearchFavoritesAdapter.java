package com.teambition.talk.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.teambition.talk.entity.Message;
import com.teambition.talk.ui.RowFactory;
import com.teambition.talk.ui.row.MessageRow;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nlmartian on 6/1/15.
 */
public class SearchFavoritesAdapter extends RecyclerView.Adapter {

    public interface SearchFavoritesListener {
        void onFavoriteClick(Message message);
    }

    private SearchFavoritesListener listener;
    private Context context;
    private List<Message> favoritesList = new ArrayList<>();
    private List<MessageRow> rows = new ArrayList<>();

    public SearchFavoritesAdapter(Context context, SearchFavoritesListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void updateSearchResult(List<Message> messages) {
        this.favoritesList.clear();
        this.favoritesList.addAll(messages);
        rows.clear();
        rows.addAll(RowFactory.getInstance().makeMessageRow(messages));
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return MessageRow.createViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Message message = favoritesList.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onFavoriteClick(message);
                }
            }
        });
        MessageRow row = rows.get(position);
        row.renderView(holder, context);
    }

    @Override
    public int getItemCount() {
        return favoritesList.size();
    }
}
