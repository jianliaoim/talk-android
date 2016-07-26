package com.teambition.talk.ui.row;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.R;
import com.teambition.talk.entity.Message;
import com.teambition.talk.event.RePostEvent;
import com.teambition.talk.realm.MessageDataProcess;
import com.teambition.talk.ui.MessageFormatter;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

/**
 * Created by zeatual on 15/7/27.
 */
public class InfoRow extends Row {

    private Date date;
    private String name;
    private int status;
    private boolean isMine;

    static class InfoHolder {

        @InjectView(R.id.tv_name)
        TextView tvName;
        @InjectView(R.id.tv_time)
        TextView tvTime;
        @Optional
        @InjectView(R.id.tv_status)
        TextView tvStatus;
        @InjectView(R.id.img_tag)
        ImageView imgTag;

        public InfoHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    public InfoRow(Message message, Date date, String name, int status, boolean isMine) {
        super(message);
        this.date = date;
        this.name = name;
        this.status = status;
        this.isMine = isMine;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public View getView(View convertView, ViewGroup parent) {
        InfoHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).
                    inflate(isMine ? R.layout.item_row_info_self : R.layout.item_row_info, null);
            holder = new InfoHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (InfoHolder) convertView.getTag();
        }
        if (status == MessageDataProcess.Status.NONE.ordinal()) {
            if (holder.tvStatus != null) {
                holder.tvStatus.setVisibility(View.GONE);
                holder.tvStatus.setOnClickListener(null);
            }
            holder.tvName.setText(name);
            holder.tvTime.setText(MessageFormatter.formatCreateTime(date));
            if (getMessage().getTags() != null && !getMessage().getTags().isEmpty()) {
                holder.imgTag.setVisibility(View.VISIBLE);
            } else {
                holder.imgTag.setVisibility(View.GONE);
            }
        } else if (holder.tvStatus != null) {
            holder.tvStatus.setVisibility(View.VISIBLE);
            switch (MessageDataProcess.Status.getEnum(status)) {
                case SENDING:
                    holder.tvStatus.setTextColor(parent.getContext().getResources()
                            .getColor(R.color.material_grey_400));
                    holder.tvStatus.setText(parent.getContext().getString(R.string.sending));
                    holder.tvStatus.setOnClickListener(null);
                    break;
                case SEND_FAILED:
                    holder.tvStatus.setTextColor(parent.getContext().getResources()
                            .getColor(R.color.talk_red));
                    holder.tvStatus.setText(parent.getContext().getString(R.string.send_failed));
                    holder.tvStatus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            BusProvider.getInstance().post(new RePostEvent(RePostEvent.RE_SEND,
                                    getMessage()));
                        }
                    });
                    break;
                case UPLOADING:
                    holder.tvStatus.setTextColor(parent.getContext().getResources()
                            .getColor(R.color.material_grey_400));
                    holder.tvStatus.setText(parent.getContext().getString(R.string.uploading));
                    holder.tvStatus.setOnClickListener(null);
                    break;
                case UPLOAD_FAILED:
                    holder.tvStatus.setTextColor(parent.getContext().getResources()
                            .getColor(R.color.talk_red));
                    holder.tvStatus.setText(parent.getContext().getString(R.string.upload_failed));
                    holder.tvStatus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            BusProvider.getInstance().post(new RePostEvent(RePostEvent.RE_UPLOAD,
                                    getMessage()));
                        }
                    });
                    break;
            }
        }
        return convertView;
    }

    @Override
    public int getViewType() {
        return isMine ? RowType.INFO_SELF_ROW.ordinal() : RowType.INFO_ROW.ordinal();
    }
}
