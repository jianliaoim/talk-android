package com.teambition.talk.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.teambition.talk.R;
import com.teambition.talk.entity.Room;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by zeatual on 14/10/31.
 */
public class TopicAdapter extends RecyclerView.Adapter {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TOPIC_TO_JOIN = 1;
    private static final int TYPE_TOPIC_JOINED = 2;
    private static final int TYPE_ARCHIVE = 3;

    private OnItemClickListener listener;

    private List<Room> roomsJoined;
    private List<Room> roomsToJoin;

    public interface OnItemClickListener {
        void onTopicToJoinClick(Room room);

        void onTopicJoinedClick(Room room);

        void onArchiveClick();
    }

    public TopicAdapter(OnItemClickListener listener) {
        this.listener = listener;
        roomsJoined = new ArrayList<>();
        roomsToJoin = new ArrayList<>();
    }

    public void updateData(List<Room> roomsJoined, List<Room> roomsToJoin) {
        this.roomsJoined.clear();
        this.roomsToJoin.clear();
        this.roomsJoined.addAll(roomsJoined);
        this.roomsToJoin.addAll(roomsToJoin);
        notifyDataSetChanged();
    }

    public void updateOne(Room room) {
        int positionToJoin = findOne(roomsToJoin, room.get_id());
        if (positionToJoin != -1) {
            roomsToJoin.remove(positionToJoin);
        }
        int positionJoined = findOne(roomsJoined, room.get_id());
        if (positionJoined != -1) {
            roomsJoined.remove(positionJoined);
        }
        if (room.getIsQuit()) {
            roomsToJoin.add(0, room);
        } else {
            roomsJoined.add(0, room);
        }
        notifyDataSetChanged();
    }

    public void removeOne(String id) {
        int positionJoined = findOne(roomsJoined, id);
        if (positionJoined != -1) {
            roomsJoined.remove(positionJoined);
            notifyItemRemoved(positionJoined + 1);
            return;
        }
        int positionToJoin = findOne(roomsToJoin, id);
        if (positionToJoin != -1) {
            roomsToJoin.remove(positionToJoin);
            notifyItemRemoved(positionJoined + roomsJoined.size() + 2);
        }
    }

    private int findOne(List<Room> data, String id) {
        int position = -1;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).get_id().equals(id)) {
                position = i;
                break;
            }
        }
        return position;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER:
                return new HeaderViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_group, null));
            case TYPE_ARCHIVE:
                return new ArchivedViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_enter_archived, null));
            default:
                return new TopicViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_topic, null));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            if (position == 0) {
                headerHolder.text.setText(R.string.group_already_joined);
            } else {
                headerHolder.text.setText(R.string.group_to_join);
            }
        }
        if (holder instanceof ArchivedViewHolder) {
            ArchivedViewHolder archivedHolder = (ArchivedViewHolder) holder;
            archivedHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onArchiveClick();
                }
            });
        }
        if (holder instanceof TopicViewHolder) {
            TopicViewHolder topicHolder = (TopicViewHolder) holder;
            final Room room;
            if (getItemViewType(position) == TYPE_TOPIC_JOINED) {
                room = roomsJoined.get(position - 1);
                topicHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onTopicJoinedClick(room);
                    }
                });
            } else {
                room = roomsToJoin.get(position - roomsJoined.size() - 2);
                topicHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onTopicToJoinClick(room);
                    }
                });
            }
            if (room.getUnread() != null && room.getUnread() > 0) {
                topicHolder.unread.setVisibility(View.VISIBLE);
            }
            topicHolder.title.setText(room.getTopic());
            if (room.getIsPrivate() != null && room.getIsPrivate()) {
                topicHolder.icon.setImageResource(R.drawable.ic_private);
            } else {
                topicHolder.icon.setImageResource(R.drawable.ic_topic);
            }
            topicHolder.image.setImageResource(R.drawable.bg_round_blue);
        }
    }

    @Override
    public int getItemCount() {
        return roomsJoined.size() + roomsToJoin.size() == 0 ? 1 : (roomsJoined.size() + roomsToJoin.size() + 1) + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == roomsJoined.size() + 1) {
            return TYPE_HEADER;
        } else if (position > 0 && position <= roomsJoined.size()) {
            return TYPE_TOPIC_JOINED;
        } else if (position == getItemCount() - 1) {
            return TYPE_ARCHIVE;
        } else {
            return TYPE_TOPIC_TO_JOIN;
        }
    }

    static class TopicViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.image)
        ImageView image;
        @InjectView(R.id.icon)
        ImageView icon;
        @InjectView(R.id.title)
        TextView title;
        @InjectView(R.id.flag_unread)
        View unread;

        public TopicViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.text)
        TextView text;

        public HeaderViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }

    static class ArchivedViewHolder extends RecyclerView.ViewHolder {
        public ArchivedViewHolder(View itemView) {
            super(itemView);
        }
    }
}
