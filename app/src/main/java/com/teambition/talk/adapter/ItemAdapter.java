package com.teambition.talk.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.teambition.talk.entity.Message;
import com.teambition.talk.realm.MessageDataProcess;
import com.teambition.talk.ui.RowFactory;
import com.teambition.talk.ui.row.MessageRow;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zeatual on 15/5/6.
 */
public class ItemAdapter extends BaseAdapter {

    private List<Message> items;
    private List<MessageRow> rows;
    private MessageDataProcess.DisplayMode displayMode;

    public ItemAdapter() {
        items = new ArrayList<>();
        rows = new ArrayList<>();
        displayMode = MessageDataProcess.DisplayMode.FILE;
    }

    public void setDisplayMode(MessageDataProcess.DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    public MessageDataProcess.DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void updateData(List<Message> messages) {
        items.clear();
        items.addAll(messages);
        rows.clear();
        rows.addAll(RowFactory.getInstance().makeMessageRow(messages));
        notifyDataSetChanged();
    }

    public void addToEnd(List<Message> messages) {
        items.addAll(messages);
        rows.addAll(RowFactory.getInstance().makeMessageRow(messages));
        notifyDataSetChanged();
    }

    public void removeMessage(String messageId) {
        int position = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).get_id().equals(messageId)) {
                position = i;
                break;
            }
        }
        if (position != -1) {
            items.remove(position);
            rows.remove(position);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Message getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageRow row = rows.get(position);
        return row.getViewBySpecifiedType(convertView, parent, displayMode);
    }
}
