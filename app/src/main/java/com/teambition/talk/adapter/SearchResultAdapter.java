package com.teambition.talk.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.style.ImageSpan;
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
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Room;
import com.teambition.talk.realm.MemberRealm;
import com.teambition.talk.realm.RoomRealm;
import com.teambition.talk.ui.RowFactory;
import com.teambition.talk.ui.activity.MessageSearchActivity;
import com.teambition.talk.ui.row.MessageRow;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.ThemeUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by ZZQ on 3/27/15.
 */
public class SearchResultAdapter extends RecyclerView.Adapter {

    enum ItemType {
        TYPE_MEMBER,
        TYPE_MEMBER_TAG,
        TYPE_MEMBER_MORE,
        TYPE_ROOM,
        TYPE_ROOM_TAG,
        TYPE_ROOM_MORE,
        TYPE_SEARCH,
        TYPE_MESSAGE,
        TYPE_MESSAGE_TAG,
        TYPE_MESSAGE_MORE,
        TYPE_NULL
    }

    private final int VIEW_TYPE_TAG = 0;
    private final int VIEW_TYPE_ITEM = 1;
    private final int VIEW_TYPE_MORE = 2;
    private final int VIEW_TYPE_SEARCH = 3;
    private final int VIEW_TYPE_MESSAGE = 4;

    private Context context;
    private SearchListener listener;

    private String keyword;

    private List<Member> originMembers;
    private List<Room> originRooms;

    private List<Member> members;
    private List<Room> rooms;
    private List<Message> messages;
    private List<MessageRow> rows;

    private ImageSpan arrow;

    private int memberItemCount = 0;
    private int roomItemCount = 0;
    private int messageItemCount = 0;
    private boolean isMemberExpanded = true;
    private boolean isRoomExpanded = true;
    private boolean isMessageExpanded = true;
    private boolean isMessageSearched = false;
    private boolean isSearching = false;
    private boolean isEmpty = false;

    public interface SearchListener {
        void search(String keyword);

        void onMemberClick(Member member);

        void onRoomClick(Room room);

        void onMessageClick(Message message);

        void onMoreClick();
    }

    public SearchResultAdapter(Context context, SearchListener listener) {
        this.context = context;
        this.listener = listener;
        originMembers = MemberRealm.getInstance().getNotRobotMemberWithCurrentThread();
        if (originMembers == null) {
            originMembers = new ArrayList<>();
        }
        int pMe = -1;
        for (int i = 0; i < originMembers.size(); i++) {
            if (BizLogic.isMe(originMembers.get(i).get_id())) {
                pMe = i;
                break;
            }
        }
        if (pMe != -1) {
            originMembers.remove(pMe);
        }
        originRooms = RoomRealm.getInstance().getRoomOnNotArchivedWithCurrentThread();
        if (originRooms == null) {
            originRooms = new ArrayList<>();
        }
        members = new ArrayList<>();
        rooms = new ArrayList<>();
        messages = new ArrayList<>();
        rows = new ArrayList<>();
        arrow = new ImageSpan(context, R.drawable.ic_right_triangle);
    }

    public void filter(String keyword) {
        this.keyword = keyword.toLowerCase();
        members.clear();
        for (Member member : originMembers) {
            if (isMatched(member.getAlias())) {
                members.add(member);
            }
        }
        rooms.clear();
        for (Room room : originRooms) {
            if (isMatched(room.getTopic())) {
                rooms.add(room);
            }
        }
        messages.clear();
        rows.clear();

        isMemberExpanded = members.size() <= 3;
        isRoomExpanded = rooms.size() <= 3;
        isMessageExpanded = false;
        isMessageSearched = false;
        isSearching = false;
        isEmpty = false;
        memberItemCount = calcItemCount(members.size(), isMemberExpanded);
        roomItemCount = calcItemCount(rooms.size(), isRoomExpanded);
        messageItemCount = 2;
        notifyDataSetChanged();
    }

    public void updateSearchResult(List<Message> messages) {
        this.messages.clear();
        this.messages.addAll(messages);
        rows.clear();
        rows.addAll(RowFactory.getInstance().makeMessageRow(messages));
        isSearching = false;
        isMessageSearched = true;
        isEmpty = messages.size() == 0;
        if (isEmpty) {
            messageItemCount = 2;
            notifyItemChanged(memberItemCount + roomItemCount + 1);
        } else {
            isMessageExpanded = messages.size() <= 3;
            messageItemCount = calcItemCount(messages.size(), isMessageExpanded);
            notifyItemRemoved(memberItemCount + roomItemCount + 1); // remove search item
            notifyItemRangeInserted(memberItemCount + roomItemCount + 1, messageItemCount - 1);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_TAG:
                return new TagViewHolder(inflateView(parent.getContext(),
                        R.layout.item_search_tag));
            case VIEW_TYPE_ITEM:
                return new ItemViewHolder(inflateView(parent.getContext(),
                        R.layout.item_search_item));
            case VIEW_TYPE_MORE:
                return new MoreViewHolder(inflateView(parent.getContext(),
                        R.layout.item_search_more));
            case VIEW_TYPE_MESSAGE:
                return MessageRow.createViewHolder(parent);
            case VIEW_TYPE_SEARCH:
                return new SearchViewHolder(inflateView(parent.getContext(),
                        R.layout.item_search_search));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(v, position);
            }
        });
        TagViewHolder tagHolder;
        ItemViewHolder itemHolder;
        MoreViewHolder moreHolder;
        SearchViewHolder searchHolder;
        switch (getItemType(position)) {
            case TYPE_MEMBER:
                itemHolder = (ItemViewHolder) holder;
                itemHolder.icon.setVisibility(View.GONE);
                Member member = members.get(getDataListPosition(position));
                itemHolder.text.setText(StringUtil.getHighlightSpan(member.getAlias(), keyword,
                        context.getResources()));
                MainApp.IMAGE_LOADER.displayImage(member.getAvatarUrl(), itemHolder.image,
                        ImageLoaderConfig.AVATAR_OPTIONS);
                itemHolder.leaveMemberText.setVisibility(member.getIsQuit() ? View.VISIBLE : View.GONE);
                break;
            case TYPE_ROOM:
                itemHolder = (ItemViewHolder) holder;
                itemHolder.icon.setVisibility(View.VISIBLE);
                itemHolder.leaveMemberText.setVisibility(View.GONE);
                Room room = rooms.get(getDataListPosition(position));
                itemHolder.text.setText(StringUtil.getHighlightSpan(room.getTopic(), keyword,
                        context.getResources()));
                itemHolder.image.setImageResource(ThemeUtil.getTopicRoundDrawableId(room.getColor()));
                if (room.getIsPrivate() != null && room.getIsPrivate()) {
                    itemHolder.icon.setImageResource(R.drawable.ic_private);
                } else {
                    itemHolder.icon.setImageResource(R.drawable.ic_topic);
                }
                break;
            case TYPE_MEMBER_TAG:
                tagHolder = (TagViewHolder) holder;
                tagHolder.text.setText(context.getString(R.string.search_result_tag_member));
                break;
            case TYPE_ROOM_TAG:
                tagHolder = (TagViewHolder) holder;
                tagHolder.text.setText(context.getString(R.string.search_result_tag_room));
                break;
            case TYPE_MESSAGE_TAG:
                tagHolder = (TagViewHolder) holder;
                tagHolder.text.setText(context.getString(R.string.search_result_tag_message));
                break;
            case TYPE_MEMBER_MORE:
                moreHolder = (MoreViewHolder) holder;
                moreHolder.text.setText(String.format(context.
                        getString(R.string.search_result_more_member), keyword));
                moreHolder.icon.setBackgroundDrawable(ThemeUtil.getThemeDrawable(context.getResources(),
                        R.drawable.ic_more, BizLogic.getTeamColor()));
                break;
            case TYPE_ROOM_MORE:
                moreHolder = (MoreViewHolder) holder;
                moreHolder.text.setText(String.format(context.
                        getString(R.string.search_result_more_room), keyword));
                moreHolder.icon.setBackgroundDrawable(ThemeUtil.getThemeDrawable(context.getResources(),
                        R.drawable.ic_more, BizLogic.getTeamColor()));
                break;
            case TYPE_MESSAGE_MORE:
                moreHolder = (MoreViewHolder) holder;
                moreHolder.text.setText(String.format(context.
                        getString(R.string.search_result_more_message), keyword));
                moreHolder.icon.setBackgroundDrawable(ThemeUtil.getThemeDrawable(context.getResources(),
                        R.drawable.ic_more, BizLogic.getTeamColor()));
                break;
            case TYPE_MESSAGE:
                MessageRow row = rows.get(getDataListPosition(position));
                row.renderView(holder, context);
                break;
            case TYPE_SEARCH:
                searchHolder = (SearchViewHolder) holder;
                if (isEmpty) {
                    holder.itemView.setOnClickListener(null);
                    searchHolder.tvEmpty.setText(String.format(context.
                            getString(R.string.search_result_empty), keyword));
                    searchHolder.emptyLayout.setVisibility(View.VISIBLE);
                    searchHolder.loadingLayout.setVisibility(View.GONE);
                } else if (isSearching) {
                    holder.itemView.setOnClickListener(null);
                    searchHolder.loadingLayout.setVisibility(View.VISIBLE);
                    searchHolder.emptyLayout.setVisibility(View.GONE);
                } else {
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onItemClick(v, position);
                        }
                    });
                    searchHolder.text.setText(String.format(context.
                            getString(R.string.search_result_search_message), keyword));
                    searchHolder.loadingLayout.setVisibility(View.GONE);
                    searchHolder.emptyLayout.setVisibility(View.GONE);
                }
                break;
            default:
                break;
        }

    }

    @Override
    public int getItemCount() {
        return memberItemCount + roomItemCount + messageItemCount;
    }

    @Override
    public int getItemViewType(int position) {
        if ((memberItemCount > 0 && position == 0) ||
                (roomItemCount > 0 && position == memberItemCount) ||
                position == memberItemCount + roomItemCount) {
            return VIEW_TYPE_TAG;
        } else if ((!isMessageSearched || isEmpty) && position > memberItemCount + roomItemCount) {
            return VIEW_TYPE_SEARCH;
        } else if ((!isMemberExpanded && memberItemCount > 0 && position == memberItemCount - 1) ||
                (!isRoomExpanded && roomItemCount > 0 && position == memberItemCount + roomItemCount - 1) ||
                (isMessageSearched && !isMessageExpanded && position == memberItemCount + roomItemCount + messageItemCount - 1)) {
            return VIEW_TYPE_MORE;
        } else if (isMessageSearched && position > memberItemCount + roomItemCount) {
            return VIEW_TYPE_MESSAGE;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    private ItemType getItemType(int position) {
        if (memberItemCount > 0 && position == 0) {
            return ItemType.TYPE_MEMBER_TAG;
        } else if (roomItemCount > 0 && position == memberItemCount) {
            return ItemType.TYPE_ROOM_TAG;
        } else if (position == memberItemCount + roomItemCount) {
            return ItemType.TYPE_MESSAGE_TAG;
        } else if ((!isMessageSearched || isEmpty) && position == memberItemCount +
                roomItemCount + 1) {
            return ItemType.TYPE_SEARCH;
        } else if (!isMemberExpanded && memberItemCount > 0 &&
                position == memberItemCount - 1) {
            return ItemType.TYPE_MEMBER_MORE;
        } else if (!isRoomExpanded && roomItemCount > 0 &&
                position == memberItemCount + roomItemCount - 1) {
            return ItemType.TYPE_ROOM_MORE;
        } else if (isMessageSearched && !isMessageExpanded &&
                position == memberItemCount + roomItemCount + messageItemCount - 1) {
            return ItemType.TYPE_MESSAGE_MORE;
        } else if (position < memberItemCount) {
            return ItemType.TYPE_MEMBER;
        } else if (position > memberItemCount && position < memberItemCount + roomItemCount) {
            return ItemType.TYPE_ROOM;
        } else if (isMessageSearched && position > memberItemCount + roomItemCount &&
                position < memberItemCount + roomItemCount + messageItemCount) {
            return ItemType.TYPE_MESSAGE;
        } else {
            return ItemType.TYPE_NULL;
        }
    }

    private int getDataListPosition(int position) {
        switch (getItemType(position)) {
            case TYPE_MEMBER:
                return position - 1;
            case TYPE_ROOM:
                return position - memberItemCount - 1;
            case TYPE_MESSAGE:
                return position - memberItemCount - roomItemCount - 1;
            default:
                return -1;
        }
    }

    private int calcItemCount(int size, boolean isExpanded) {
        if (size > 3 && !isExpanded) {
            return 5;
        } else if (size > 0) {
            return size + 1;
        } else {
            return 0;
        }
    }

    private void onItemClick(View v, int position) {
        switch (getItemType(position)) {
            case TYPE_MEMBER:
                Member member = members.get(getDataListPosition(position));
                listener.onMemberClick(member);
                break;
            case TYPE_ROOM:
                Room room = rooms.get(getDataListPosition(position));
                listener.onRoomClick(room);
                break;
            case TYPE_MESSAGE:
                Message message = messages.get(getDataListPosition(position));
                listener.onMessageClick(message);
                break;
            case TYPE_MEMBER_MORE:
                showMoreMembers();
                break;
            case TYPE_ROOM_MORE:
                showMoreRooms();
                break;
            case TYPE_MESSAGE_MORE:
//                showMoreMessages();
                listener.onMoreClick();
                break;
            case TYPE_SEARCH:
                search();
                break;
            default:
                break;
        }
    }

    public void search() {
        if (!isSearching) {
            isSearching = true;
            notifyItemChanged(memberItemCount + roomItemCount + 1);
            listener.search(keyword);
        }
    }

    private void showMoreMembers() {
        isMemberExpanded = true;
        memberItemCount = calcItemCount(members.size(), isMemberExpanded);
        notifyItemRemoved(4);
        notifyItemRangeInserted(4, members.size() - 3);
        notifyItemRangeChanged(4, members.size() - 3);
    }

    private void showMoreRooms() {
        isRoomExpanded = true;
        roomItemCount = calcItemCount(rooms.size(), isRoomExpanded);
        notifyItemRemoved(memberItemCount + 4);
        notifyItemRangeInserted(memberItemCount + 4, rooms.size() - 3);
        notifyItemRangeChanged(memberItemCount + 4, rooms.size() - 3);
    }

    private void showMoreMessages() {
        isMessageExpanded = true;
        messageItemCount = calcItemCount(messages.size(), isMessageExpanded);
        notifyItemRemoved(memberItemCount + roomItemCount + 4);
        notifyItemRangeInserted(memberItemCount + roomItemCount + 4, messages.size() - 3);
        notifyItemRangeChanged(memberItemCount + roomItemCount + 4, messages.size() - 3);
    }

    private View inflateView(Context ctx, int resId) {
        return LayoutInflater.from(ctx).inflate(resId, null);
    }

    private boolean isMatched(String str) {
        return str.contains(keyword) ||
                PinyinUtil.converterToSpell(str).toLowerCase().contains(keyword);
    }

    static class TagViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.text)
        TextView text;

        TagViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.text)
        TextView text;
        @InjectView(R.id.icon)
        ImageView icon;
        @InjectView(R.id.image)
        ImageView image;
        @InjectView(R.id.leave_member_text)
        TextView leaveMemberText;

        ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }

    static class MoreViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.icon)
        ImageView icon;
        @InjectView(R.id.text)
        TextView text;

        MoreViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }

    static class SearchViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.layout_loading)
        View loadingLayout;
        @InjectView(R.id.layout_empty)
        View emptyLayout;
        @InjectView(R.id.text)
        TextView text;
        @InjectView(R.id.tv_empty)
        TextView tvEmpty;

        SearchViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}