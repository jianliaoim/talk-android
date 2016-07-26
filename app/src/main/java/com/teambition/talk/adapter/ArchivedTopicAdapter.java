package com.teambition.talk.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.teambition.talk.BizLogic;
import com.teambition.talk.R;
import com.teambition.talk.entity.Room;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.ThemeUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by zeatual on 15/3/16.
 */
public class ArchivedTopicAdapter extends RecyclerView.Adapter<ArchivedTopicAdapter.ViewHolder> {

    private Context context;
    private List<Room> rooms;
    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ArchivedTopicAdapter(Context context) {
        rooms = new ArrayList<>();
        this.context = context;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onLongItemClick(int position);
    }

    public void updateData(List<Room> rooms) {
        this.rooms.clear();
        this.rooms.addAll(rooms);
        notifyDataSetChanged();
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void removeItem(Room room) {
        int position = -1;
        for (int i = 0; i < rooms.size(); i++) {
            Room r = rooms.get(i);
            if (room.get_id().equals(r.get_id())) {
                position = i;
                break;
            }
        }
        rooms.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount() - position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_topic, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Room room = rooms.get(position);
        holder.itemView.setOnClickListener(null);
        if (BizLogic.isAdmin() || BizLogic.isAdminOfRoom(room.get_id())) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(position);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onLongItemClick(position);
                    return false;
                }
            });
        }
        holder.topic.setText(room.getTopic());
        if (room.getIsPrivate() != null && room.getIsPrivate()) {
            holder.icon.setImageResource(R.drawable.ic_private);
        } else {
            holder.icon.setImageResource(R.drawable.ic_topic);
        }
        holder.image.setImageResource(ThemeUtil.getTopicRoundDrawableId(room.getColor()));
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public Room getItem(int position) {
        return rooms.get(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.image)
        ImageView image;
        @InjectView(R.id.icon)
        ImageView icon;
        @InjectView(R.id.title)
        TextView topic;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}
