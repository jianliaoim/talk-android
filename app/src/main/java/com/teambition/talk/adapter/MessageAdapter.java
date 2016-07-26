package com.teambition.talk.adapter;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.teambition.talk.ui.row.InfoRow;
import com.teambition.talk.ui.row.Row;
import com.teambition.talk.ui.row.RowType;
import com.teambition.talk.ui.row.SpeechRecordRow;
import com.teambition.talk.util.StringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zeatual on 15/7/27.
 */
public class MessageAdapter extends BaseAdapter {

    private List<Row> rows;

    private boolean isRecording;

    public MessageAdapter() {
        rows = new ArrayList<>();
    }

    public void replace(final List<Row> rows) {
        this.rows.clear();
        this.rows.addAll(rows);
        notifyDataSetChanged();
    }

    public void addToStart(List<Row> rows) {
        this.rows.addAll(0, rows);
        notifyDataSetChanged();
    }

    public void addToEnd(List<Row> rows) {
        for (Row _row : rows) {
            for (Row row : this.rows) {
                if (row.getMessage() != null
                        && _row.getMessage() != null
                        && TextUtils.equals(row.getMessage().get_id(), _row.getMessage().get_id())) {
                    return;
                }
            }
        }
        this.rows.addAll(rows);
        notifyDataSetChanged();
    }

    public List<Row> getRows() {
        return rows;
    }

    public void clear() {
        rows.clear();
    }

    public void removeLast() {
        this.rows.remove(rows.size() - 1);
        notifyDataSetChanged();
    }

    public void updateOne(String msgId, List<Row> rows) {
        int position = removeRowsById(msgId);
        if (position != -1) {
            this.rows.addAll(position, rows);
            notifyDataSetChanged();
        }
    }

    public void deleteOne(String msgId) {
        removeRowsById(msgId);
        notifyDataSetChanged();
    }

    /**
     * remove rows by id & return the position
     *
     * @param msgId msgId or Row
     * @return position of the first Row with msgId. Default: -1
     */
    private int removeRowsById(String msgId) {
        int position = -1;
        if (StringUtil.isNotBlank(msgId)) {
            boolean find = false;
            Iterator<Row> iterator = rows.iterator();
            while (iterator.hasNext()) {
                Row row = iterator.next();
                if (msgId.equals(row.getMessage().get_id())) {
                    find = true;
                    position = rows.indexOf(row);
                    iterator.remove();
                }
                if (find && !msgId.equals(row.getMessage().get_id())) {
                    break;
                }
            }
        }
        return position;
    }

    /**
     * 更新消息状态
     *
     * @param msgId  本地生成的msgId
     * @param status 消息状态
     */
    public void updateStatus(String msgId, int status) {
        if (msgId != null) {
            for (int i = 0; i < rows.size(); i++) {
                Row row = rows.get(i);
                if (row.getMessage() != null) {
                    if (msgId.equals(row.getMessage().get_id())) {
                        if (row instanceof InfoRow) {
                            ((InfoRow) row).setStatus(status);
                        }
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return rows.size();
    }

    @Override
    public Row getItem(int position) {
        return rows.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Row row = rows.get(position);

        View rowView = row.getView(convertView, parent);
        if (isRecording) {
            if (position == rows.size() - 1) {
                rowView.setAlpha(1f);
            } else {
                rowView.setAlpha(0.26f);
            }
        } else {
            rowView.setAlpha(1f);
        }
        return rowView;
    }

    @Override
    public int getViewTypeCount() {
        return RowType.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        Row row = rows.get(position);
        return row.getViewType();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    public void setIsRecording(boolean isRecording) {
        this.isRecording = isRecording;
        if (isRecording) {
            rows.add(new SpeechRecordRow());
        } else {
            rows.remove(rows.size() - 1);
        }
        notifyDataSetChanged();
    }
}
