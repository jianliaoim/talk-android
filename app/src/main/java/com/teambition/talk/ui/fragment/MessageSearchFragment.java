package com.teambition.talk.ui.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.ui.activity.MessageSearchActivity;
import com.teambition.talk.util.ThemeUtil;

import java.util.ArrayList;

/**
 * Created by wlanjie on 16/2/16.
 */
public class MessageSearchFragment extends BaseFragment {

    final MessageSearchAdapter adapter = new MessageSearchAdapter();

    private OnItemClick itemClick;

    private OnRemoveFragmentListener listener;

    private String tag;

    private RecyclerView recyclerView;

    private int recyclerViewState;

    private int memberPosition;
    private int roomPosition;
    private int tagPosition;
    private int typePosition;
    private int timePosition;

    public static MessageSearchFragment getInstance() {
        return new MessageSearchFragment();
    }

    public void setItems(String tag, ArrayList<String> items) {
        this.tag = tag;
        adapter.setItems(items);
    }

    public void setMemberPosition(int position) {
        this.memberPosition = position;
    }

    public void setRoomPosition(int position) {
        this.roomPosition = position;
    }

    public void setTagPosition(int position) {
        this.tagPosition = position;
    }

    public void setTypePosition(int position) {
        this.typePosition = position;
    }

    public void setTimePosition(int position) {
        this.timePosition = position;
    }

    public void setOnItemClick(OnItemClick itemClick) {
        this.itemClick = itemClick;
    }

    public void setOnRemoveFragment(OnRemoveFragmentListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.message_search_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP && recyclerViewState == RecyclerView.SCROLL_STATE_IDLE) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out)
                            .remove(MessageSearchFragment.this)
                            .commit();
                    if (listener != null) {
                        listener.onRemoveFragment();
                    }
                }
                return false;
            }
        });
        recyclerView.addOnScrollListener(scrollListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView.removeOnScrollListener(scrollListener);
    }

    RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            recyclerViewState = newState;
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }
    };

    class MessageSearchAdapter extends RecyclerView.Adapter<MessageSearchViewHolder> {

        final ArrayList<String> items = new ArrayList<>();

        final Drawable selectImage;

        public MessageSearchAdapter() {
            selectImage = ThemeUtil.getDrawableWithColor(MainApp.CONTEXT.getResources(), R.drawable.ic_save_blue, R.color.colorPrimary);
        }

        public void setItems(ArrayList<String> items) {
            if (items == null) return;
            this.items.clear();
            for (String item : items) {
                this.items.add(item);
            }
            notifyDataSetChanged();
        }

        @Override
        public MessageSearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            final View view = inflater.inflate(R.layout.message_search_item, parent, false);
            return new MessageSearchViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final MessageSearchViewHolder holder, final int position) {
            final String text = items.get(position);
            if (MessageSearchActivity.MEMBER.equals(tag) && memberPosition == position
                    || MessageSearchActivity.ROOM.equals(tag) && roomPosition == position
                    || MessageSearchActivity.TAG.equals(tag) && tagPosition == position
                    || MessageSearchActivity.TYPE.equals(tag) && typePosition == position
                    || MessageSearchActivity.TIME.equals(tag) && timePosition == position) {
                holder.imageView.setVisibility(View.VISIBLE);
                holder.imageView.setImageDrawable(selectImage);
                holder.nameText.setTextColor(MainApp.CONTEXT.getResources().getColor(R.color.colorPrimary));
            } else {
                holder.imageView.setVisibility(View.GONE);
                holder.nameText.setTextColor(MainApp.CONTEXT.getResources().getColor(R.color.material_grey_700));
            }
            holder.nameText.setText(text);
            holder.nameText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClick == null) return;
                    notifyItemChanged(holder.getAdapterPosition());
                    itemClick.onClick(tag, holder.getAdapterPosition(), text);
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    class MessageSearchViewHolder extends RecyclerView.ViewHolder {

        public TextView nameText;
        public ImageView imageView;

        public MessageSearchViewHolder(View item) {
            super(item);
            nameText = (TextView) item.findViewById(R.id.name);
            imageView = (ImageView) item.findViewById(R.id.image);
        }
    }

    public interface OnRemoveFragmentListener {
        void onRemoveFragment();
    }

    public interface OnItemClick {
        void onClick(String tag, int position, String text);
    }
}
