package com.teambition.talk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.teambition.talk.R;
import com.teambition.talk.entity.Message;
import com.teambition.talk.ui.RowFactory;
import com.teambition.talk.ui.row.MessageRow;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by nlmartian on 5/27/15.
 */
public class FavoritesAdapter extends BaseAdapter {

    public interface OnSelectedChangedListener {
        void onSelectedChanged(int selectedItems);
    }

    private List<Message> items;
    private List<MessageRow> rows;
    private List<Message> selectedFavorite = new ArrayList<>();
    private boolean editMode = false;
    private OnSelectedChangedListener onSelectedChangedListener;

    public FavoritesAdapter(Context context) {
        items = new ArrayList<>();
        rows = new ArrayList<>();
    }

    public List<Message> getItems() {
        return items;
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

    public void removeSelected() {
        for (Message msg : selectedFavorite) {
            for (int i = 0; i < items.size(); i++) {
                Message message = items.get(i);
                if (message.get_id().equals(msg.get_id())) {
                    items.remove(i);
                    rows.remove(i);
                    break;
                }
            }
        }
        editMode = false;
        notifyDataSetChanged();
    }

    public void setEditMode(boolean isEditMode) {
        if (!isEditMode) {
            selectedFavorite.clear();
        }
        this.editMode = isEditMode;
        notifyDataSetChanged();
    }

    public void setItemSelection(int pos, boolean select) {
        if (select) {
            if (!selectedFavorite.contains(getItem(pos))) {
                selectedFavorite.add(getItem(pos));
            }
        } else {
            selectedFavorite.remove(getItem(pos));
        }
        notifyDataSetChanged();
    }

    public boolean isEditMode() {
        return editMode;
    }

    public List<Message> getSelectedFavorite() {
        return selectedFavorite;
    }

    public void setOnSelectedChangedListener(OnSelectedChangedListener onSelectedChangedListener) {
        this.onSelectedChangedListener = onSelectedChangedListener;
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
        final Message message = items.get(position);
        MessageRow row = rows.get(position);
        row.enableEditMode(editMode);
        MessageRow.MessageHolder holder = row.getViewHolder(convertView, parent);
        holder.checkBox.setOnCheckedChangeListener(null);
        if (editMode) {
            holder.checkBox.setChecked(selectedFavorite.contains(message));
        }
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    selectedFavorite.add(message);
                } else {
                    selectedFavorite.remove(message);
                }
                if (onSelectedChangedListener != null) {
                    onSelectedChangedListener.onSelectedChanged(selectedFavorite.size());
                }
            }
        });
        return row.getView(convertView, parent);
    }
}
