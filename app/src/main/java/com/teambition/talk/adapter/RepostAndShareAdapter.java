package com.teambition.talk.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.teambition.common.PinyinUtil;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.ChatItem;
import com.teambition.talk.entity.FilterItem;
import com.teambition.talk.ui.fragment.FilterFragment;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.ThemeUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by wlanjie on 15/7/31.
 */
public class RepostAndShareAdapter extends RecyclerView.Adapter {

    public static final int TOPIC = 0;
    public static final int MEMBER = 1;

    private String keyword = "";

    private FilterFragment.FilterListener filterListener;

    final List<ChatItem> items;
    final List<ChatItem> originItems;
    private OnItemClickListener listener;

    public RepostAndShareAdapter() {
        items = new ArrayList<>();
        originItems = new ArrayList<>();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setFilterListener(FilterFragment.FilterListener filterListener) {
        this.filterListener = filterListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new HeaderViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_filter_header, parent, false));
        } else {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chatroom, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ViewHolder) {
            ViewHolder itemHolder = (ViewHolder) holder;
            itemHolder.header.setVisibility(View.GONE);
            itemHolder.icon.setVisibility(View.GONE);
            itemHolder.divider.setVisibility(View.GONE);

            ChatItem item = getItem(position);

            if (position == 1) {
                itemHolder.group.setText(getGroupNameRes(item.type));
                itemHolder.header.setVisibility(View.VISIBLE);
            } else if (item.type != items.get(position - 2).type) {
                itemHolder.group.setText(getGroupNameRes(item.type));
                itemHolder.divider.setVisibility(View.VISIBLE);
                itemHolder.header.setVisibility(View.VISIBLE);
            }

            if (item.isTopic) {
                itemHolder.icon.setVisibility(View.VISIBLE);
                itemHolder.image.setImageResource(ThemeUtil.getTopicRoundDrawableId(item.color));
                if (item.isPrivate) {
                    itemHolder.icon.setImageResource(R.drawable.ic_private);
                } else {
                    itemHolder.icon.setImageResource(R.drawable.ic_topic);
                }
            } else {
                MainApp.IMAGE_LOADER.displayImage(item.avatarUrl, itemHolder.image,
                        ImageLoaderConfig.AVATAR_OPTIONS);
            }
            itemHolder.name.setText(StringUtil.getHighlightSpan(item.name, keyword, itemHolder.name.getResources()));

            itemHolder.content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(position);
                }
            });
        } else if (holder instanceof HeaderViewHolder) {
            final HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.tvAll.setVisibility(View.GONE);
            headerHolder.tvAllLayout.setVisibility(View.GONE);
            headerHolder.etKeyword.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    filterData(s.toString());
                }
            });

            final FilterFragment.FilterListener finalFilterListener = filterListener;
            headerHolder.etKeyword.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_ENTER) &&
                            StringUtil.isNotBlank(headerHolder.etKeyword.getText().toString())) {
                        FilterItem item = new FilterItem(FilterItem.TYPE_KEYWORD,
                                headerHolder.etKeyword.getText().toString(), headerHolder.etKeyword.getText().toString());
                        if (finalFilterListener != null) {
                            finalFilterListener.onFilter(item);
                        }
                        return true;
                    }
                    return false;
                }
            });
            headerHolder.tvAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FilterItem item = new FilterItem(FilterItem.TYPE_ALL, null, v.getContext().getString(R.string.all));
                    if (finalFilterListener != null) {
                        finalFilterListener.onFilter(item);
                    }
                }
            });
            headerHolder.etKeyword.requestFocus();

        }
    }

    public void updateData(List<ChatItem> items) {
        originItems.clear();
        originItems.addAll(items);
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : 1;
    }

    @Override
    public int getItemCount() {
        return items.size() + 1;
    }

    public ChatItem getItem(int position) {
        if (position < 0 && position - 1 > items.size())
            return null;
        return items.get(position - 1);
    }

    public void filterData(String keyword) {
        this.keyword = keyword;
        List<ChatItem> newItems = new ArrayList<>();
        for (ChatItem item : originItems) {
            if (item.name.contains(keyword) ||
                    PinyinUtil.converterToSpell(item.name).toLowerCase().contains(keyword)) {
                newItems.add(item);
            }
        }
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }


    private int getGroupNameRes(int type) {
        switch (type) {
            case TOPIC:
                return R.string.group_name_topic;
            case MEMBER:
                return R.string.group_name_member;
            default:
                return 0;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.image)
        ImageView image;
        @InjectView(R.id.topic_icon)
        ImageView icon;
        @InjectView(R.id.name)
        TextView name;
        @InjectView(R.id.header)
        View header;
        @InjectView(R.id.group)
        TextView group;
        @InjectView(R.id.divider)
        View divider;
        @InjectView(R.id.content)
        View content;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.tv_all)
        View tvAll;
        @InjectView(R.id.et_keyword)
        EditText etKeyword;
        @InjectView(R.id.tv_all_layout)
        View tvAllLayout;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}
