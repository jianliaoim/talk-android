package com.teambition.talk.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.teambition.common.PinyinUtil;
import com.teambition.talk.BizLogic;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.ChatItem;
import com.teambition.talk.entity.FilterItem;
import com.teambition.talk.ui.activity.ItemsActivity;
import com.teambition.talk.ui.fragment.FilterFragment;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.ThemeUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by zeatual on 15/3/10.
 */
public class ChatSelectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TOPIC = 0;
    public static final int MEMBER = 1;
    public static final int HEADER = 2;

    private Context context;
    private List<ChatItem> items;
    private List<ChatItem> originItems;
    private OnItemClickListener listener;
    private boolean showHeader = false;
    private boolean isShowAllDoneIcon = true;
    private String showItemIconId = "";

    private String keyword = "";

    private FilterFragment.FilterListener filterListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setShowHeader(boolean showHeader) {
        this.showHeader = showHeader;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
        notifyDataSetChanged();
    }

    public void setFilterListener(FilterFragment.FilterListener filterListener) {
        this.filterListener = filterListener;
        notifyDataSetChanged();
    }

    public ChatSelectAdapter(Context context, boolean isShowAllDoneIcon, String showItemIconId) {
        this.context = context;
        this.isShowAllDoneIcon = isShowAllDoneIcon;
        this.showItemIconId = showItemIconId;
        items = new ArrayList<>();
        originItems = new ArrayList<>();
    }

    public void updateData(List<ChatItem> items) {
        originItems.clear();
        originItems.addAll(items);
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new HeaderViewHolder(LayoutInflater.from(context)
                    .inflate(R.layout.item_filter_header, parent, false));
        } else {
            return new ViewHolder(LayoutInflater.from(context)
                    .inflate(R.layout.item_chatroom, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ViewHolder) {
            final ViewHolder itemHolder = (ViewHolder) holder;
            itemHolder.header.setVisibility(View.GONE);
            itemHolder.icon.setVisibility(View.GONE);
            itemHolder.divider.setVisibility(View.GONE);
            itemHolder.doneImage.setImageDrawable(ThemeUtil.getThemeDrawable(context.getResources(), R.drawable.ic_done_color, BizLogic.getTeamColor()));
            final ChatItem item = getItem(position);
            itemHolder.doneImage.setVisibility(showItemIconId.equals(item.id) ? View.VISIBLE : View.GONE);


            if (position == 0) {
                itemHolder.group.setText(getGroupNameRes(item.type));
                itemHolder.header.setVisibility(View.VISIBLE);
            } else if (item.type != items.get(position - 1).type) {
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
            itemHolder.name.setText(StringUtil.getHighlightSpan(item.name, keyword, context.getResources()));
            itemHolder.leaveMemberText.setVisibility(item.isQuit ? View.VISIBLE : View.GONE);
            itemHolder.content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(position);
                    if (context instanceof ItemsActivity) {
                        ((ItemsActivity) context).showItemIconId = item.id;
                        ((ItemsActivity) context).isShowAllDoneIcon = false;
                    }
                    itemHolder.doneImage.setVisibility(View.VISIBLE);
                    notifyDataSetChanged();
                }
            });
        } else if (holder instanceof HeaderViewHolder) {
            final HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.etKeyword.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 0) {
                        headerHolder.layoutAll.setVisibility(View.VISIBLE);
                    } else {
                        headerHolder.layoutAll.setVisibility(View.GONE);
                    }
                    filterData(s.toString());
                }
            });

            final FilterFragment.FilterListener finalFilterListener = filterListener;
            headerHolder.etKeyword.setImeOptions(EditorInfo.IME_ACTION_DONE);
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
                    if (context instanceof ItemsActivity) {
                        ((ItemsActivity) context).isShowAllDoneIcon = true;
                        ((ItemsActivity) context).showItemIconId = "";
                    }
                    FilterItem item = new FilterItem(FilterItem.TYPE_ALL, null, context.getString(R.string.all));
                    if (finalFilterListener != null) {
                        finalFilterListener.onFilter(item);
                    }
                }
            });
            headerHolder.doneImage.setImageDrawable(ThemeUtil.getThemeDrawable(context.getResources(), R.drawable.ic_done_color, BizLogic.getTeamColor()));
            headerHolder.etKeyword.requestFocus();
            headerHolder.doneImage.setVisibility(isShowAllDoneIcon ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if (showHeader) {
            return items.size() + 1;
        } else {
            return items.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (showHeader) {
            if (position == 0) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return 1;
        }
    }

    public ChatItem getItem(int position) {
        if (showHeader) {
            return items.get(position - 1);
        } else {
            return items.get(position);
        }
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
        @InjectView(R.id.leave_member_text)
        TextView leaveMemberText;
        @InjectView(R.id.done_image)
        ImageView doneImage;

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
        @InjectView(R.id.done_image)
        ImageView doneImage;
        @InjectView(R.id.tv_all_layout)
        View layoutAll;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}
