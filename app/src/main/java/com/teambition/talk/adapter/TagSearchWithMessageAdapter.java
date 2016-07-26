package com.teambition.talk.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.teambition.talk.BizLogic;
import com.teambition.talk.entity.Message;
import com.teambition.talk.realm.MessageDataProcess;
import com.teambition.talk.ui.RowFactory;
import com.teambition.talk.ui.row.MessageRow;
import com.teambition.talk.util.MessageDialogBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by wlanjie on 15/7/16.
 */
public class TagSearchWithMessageAdapter extends RecyclerView.Adapter {

    final List<Message> mItems = new ArrayList<>();
    final List<MessageRow> rows = new ArrayList<>();

    final Context context;
    final String mTagName;
    OnClickListener mListener;
    final MessageDialogBuilder.MessageActionCallback mMessageActionCallback;

    public interface OnClickListener {
        void onItemClick(Message message);
    }

    public TagSearchWithMessageAdapter(String tagName, OnClickListener listener, MessageDialogBuilder.MessageActionCallback callback) {
        mListener = listener;
        mTagName = tagName;
        mMessageActionCallback = callback;
        this.context = (Context) listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return MessageRow.createViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Message message = mItems.get(position);
        final MessageDialogBuilder builder = new MessageDialogBuilder(context, message, mMessageActionCallback);
        builder.favorite().addTag();
        if (MessageDataProcess.DisplayMode.getEnum(message.getDisplayMode()) == MessageDataProcess.DisplayMode.FILE ||
                MessageDataProcess.DisplayMode.getEnum(message.getDisplayMode()) == MessageDataProcess.DisplayMode.IMAGE) {
            builder.saveFile();
        }
        if (BizLogic.isAdmin() || BizLogic.isMe(message.get_creatorId())) {
            builder.delete();
        }
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                builder.copyMaterialDialog().show();
                return true;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(message);
                }
            }
        });

        MessageRow row = rows.get(position);
        row.renderView(holder, context);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public Message getItem(int position) {
        return mItems.get(position);
    }

    public void setItems(List<Message> items) {
        if (items == null || items.isEmpty()) return;
        for (Message item : items) {
            if (item != null) {
                mItems.add(item);
                rows.add(RowFactory.getInstance().makeMessageRow(item));
            }
        }
        notifyDataSetChanged();
    }

    public void deleteItem(String messageId) {
        Iterator<Message> iterator = mItems.iterator();
        int index = -1;
        while (iterator.hasNext()) {
            Message message = iterator.next();
            if (message.get_id().equals(messageId)) {
                index = mItems.indexOf(message);
                iterator.remove();
                break;
            }
        }
        if (index != -1) {
            rows.remove(index);
            notifyItemRemoved(index);
        }
    }
}
