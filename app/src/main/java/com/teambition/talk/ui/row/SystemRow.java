package com.teambition.talk.ui.row;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teambition.talk.R;
import com.teambition.talk.entity.Message;
import com.teambition.talk.ui.MessageFormatter;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by zeatual on 15/7/27.
 */
public class SystemRow extends Row {

    private String text;

    static class SystemHolder {

        @InjectView(R.id.tv_text)
        TextView tvText;

        public SystemHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    public SystemRow(Message message) {
        super(message);
        this.text = MessageFormatter.formatToPureText(message.getBody(), message.getCreatorName());
    }

    @Override
    public View getView(View convertView, ViewGroup parent) {
        SystemHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.item_row_system, null);
            holder = new SystemHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (SystemHolder) convertView.getTag();
        }
        holder.tvText.setText(text);
        return convertView;
    }

    @Override
    public int getViewType() {
        return RowType.SYSTEM_ROW.ordinal();
    }
}
