package com.teambition.talk.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teambition.talk.R;
import com.teambition.talk.entity.Team;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by nlmartian on 11/27/15.
 */
public class ThirdPartTeamAdapter extends RecyclerView.Adapter {

    private List<Team> teamList;

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        public void onSyncClickListener(TextView syncView, String sourceId);
    }

    public ThirdPartTeamAdapter() {
        teamList = new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row_thirdpart_team, parent, false);
        return new TeamViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Team team = teamList.get(position);
        final TeamViewHolder teamHolder = (TeamViewHolder) holder;
        teamHolder.name.setText(team.getName());
        if (onItemClickListener != null) {
            teamHolder.sync.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onSyncClickListener(teamHolder.sync, team.getSourceId());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return teamList.size();
    }

    public void setTeamList(List<Team> teams) {
        teamList.addAll(teams);
        notifyDataSetChanged();
    }

    static class TeamViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.name)
        TextView name;
        @InjectView(R.id.sync)
        TextView sync;

        public TeamViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
        
        
    }
}
