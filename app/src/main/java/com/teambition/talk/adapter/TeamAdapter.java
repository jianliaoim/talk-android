package com.teambition.talk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.teambition.talk.Constant;
import com.teambition.talk.R;
import com.teambition.talk.entity.Team;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.ThemeUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by zeatual on 14/10/30.
 */
public class TeamAdapter extends BaseAdapter {

    private Context context;
    private List<Team> teams;

    public TeamAdapter(Context context) {
        this.context = context;
        teams = new ArrayList<>();
    }

    public void updateData(List<Team> teams) {
        this.teams.clear();
        this.teams.addAll(teams);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return teams.size();
    }

    @Override
    public Team getItem(int position) {
        return teams.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_team, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            holder.source.setVisibility(View.INVISIBLE);
            holder.unread.setVisibility(View.INVISIBLE);
        }
        Team team = teams.get(position);
        if (Constant.TEAMBITION.equals(team.getSource())) {
            holder.source.setVisibility(View.VISIBLE);
        }
        holder.text.setText(team.getName());
        holder.image.setImageResource(R.drawable.shape_team_icon);
        return convertView;
    }

    static class ViewHolder {
        @InjectView(R.id.image)
        ImageView image;
        @InjectView(R.id.text)
        TextView text;
        @InjectView(R.id.imageView_source)
        FrameLayout source;
        @InjectView(R.id.unread_num)
        TextView unread;

        public ViewHolder(View v) {
            ButterKnife.inject(this, v);
        }
    }

}
