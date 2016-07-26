package com.teambition.talk.ui.row;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.teambition.talk.BizLogic;
import com.teambition.talk.R;
import com.teambition.talk.ui.widget.AudioMessageView;
import com.teambition.talk.util.ThemeUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by nlmartian on 8/3/15.
 */
public class SpeechRecordRow extends Row {

    public static class SpeechRecordRowHolder {

        @InjectView(R.id.audio_view)
        AudioMessageView audioView;

        public SpeechRecordRowHolder(View view) {
            ButterKnife.inject(this, view);
        }

        public void updateRecordingTime(int sec) {
            String timeString = String.format("%02d:%02d", sec / 60, sec % 60);
            audioView.setText(timeString);
        }
    }

    public SpeechRecordRow() {
        super(null);
    }

    @Override
    public View getView(View convertView, ViewGroup parent) {
        SpeechRecordRowHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_recording, parent, false);
            holder = new SpeechRecordRowHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (SpeechRecordRowHolder) convertView.getTag();
        }

        holder.audioView.setUnreachedColor(parent.getContext().getResources()
                .getColor(ThemeUtil.getThemeColorLightRes(BizLogic.getTeamColor())));
        holder.audioView.setButtonDrawable(null);
        holder.audioView.setTextColor(Color.parseColor("#DE000000"));
        return convertView;
    }

    @Override
    public int getViewType() {
        return RowType.SPEECH_RECORD_ROW.ordinal();
    }
}
