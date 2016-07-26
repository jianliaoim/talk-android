package com.teambition.talk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.teambition.talk.BizLogic;
import com.teambition.talk.R;
import com.teambition.talk.entity.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by zeatual on 15/01/26.
 */
public class DrawerTeamAdapter extends BaseAdapter {

    private Context context;
    private List<Team> teams;
    private String newTeamId;

    public DrawerTeamAdapter(Context context) {
        this.context = context;
        teams = new ArrayList<>();
    }

    public void updateData(List<Team> teams) {
        this.teams.clear();
        Iterator<Team> iterator = teams.iterator();
        Team currentTeam = null;
        while (iterator.hasNext()) {
            Team team = iterator.next();
            if (team!= null && BizLogic.isCurrentTeam(team.get_id())) {
                currentTeam = team;
                iterator.remove();
            }
        }
        Collections.sort(teams, new Comparator<Team>() {
            @Override
            public int compare(Team lhs, Team rhs) {
                return rhs.getUnread() - lhs.getUnread();
            }
        });
        if (currentTeam != null) {
            teams.add(0, currentTeam);
        }
        this.teams.addAll(teams);
        notifyDataSetChanged();
    }

    public boolean checkUnread() {
        boolean result = false;
        for (Team team : teams) {
            if (BizLogic.isCurrentTeam(team.get_id())) {
                continue;
            }
            if (team.getUnread() > 0) {
                result = true;
                break;
            }
        }
        return result;
    }

    public void updateUnread(String teamId, boolean isMute, int unReadNum, int oldUnreadNum) {
        Iterator<Team> iterator = teams.iterator();
        Team currentTeam = null;
        int totalUnreadNum = 0;
        while (iterator.hasNext()) {
            Team team = iterator.next();
            if (team.get_id().equals(teamId)) {
                if (!isMute) {
                    team.setUnread(Math.max(0, team.getUnread() - (oldUnreadNum - unReadNum)));
                }
                team.setHasUnread(team.getUnread() > 0);
            }
            if (BizLogic.hasChosenTeam() && BizLogic.isCurrentTeam(teamId)) {
                currentTeam = team;
                iterator.remove();
            }
            totalUnreadNum += team.getUnread();
        }
        if (totalUnreadNum == 0) {
            iterator = teams.iterator();
            while (iterator.hasNext()) {
                Team team = iterator.next();
                if (BizLogic.isCurrentTeam(team.get_id())) {
                    currentTeam = team;
                    iterator.remove();
                }
            }
        }

        Collections.sort(teams, new Comparator<Team>() {
            @Override
            public int compare(Team lhs, Team rhs) {
                return rhs.getUnread() - lhs.getUnread();
            }
        });
        if (currentTeam != null) {
            teams.add(0, currentTeam);
        }
        notifyDataSetChanged();
    }

    public void setNewTeamId(String newTeamId) {
        this.newTeamId = newTeamId;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_drawer_team, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            holder.unread.setVisibility(View.INVISIBLE);
            holder.unreadMini.setVisibility(View.INVISIBLE);
            holder.text.setTextColor(context.getResources().getColor(R.color.material_grey_900));
        }

        Team team = teams.get(position);
        if (team.getUnread() > 0) {
            holder.unread.setText("" + team.getUnread());
            holder.unread.setVisibility(View.VISIBLE);
        } else {
            holder.unread.setText("0");
            holder.unread.setVisibility(View.GONE);
        }
        if (team.isHasUnread()) {
            holder.unreadMini.setVisibility(View.VISIBLE);
        }
        if (team.get_id() != null && team.get_id().equals(newTeamId)) {
            holder.unread.setVisibility(View.VISIBLE);
        }
        if (BizLogic.getTeamId().equals(team.get_id())) {
            holder.unread.setVisibility(View.INVISIBLE);
            holder.unreadMini.setVisibility(View.INVISIBLE);
            holder.image.setImageResource(R.drawable.shape_team_icon_selected);
            holder.background.setBackgroundColor(0xfff5f5f5);
            holder.text.setTextColor(0xfffa6855);
        } else {
            holder.image.setImageResource(R.drawable.shape_team_icon);
            holder.background.setBackgroundResource(R.drawable.bg_item);
            holder.text.setTextColor(0xff757575);
        }
        holder.text.setText(team.getName());
        return convertView;
    }

    static class ViewHolder {
        @InjectView(R.id.bg)
        View background;
        @InjectView(R.id.image)
        ImageView image;
        @InjectView(R.id.text)
        TextView text;
        @InjectView(R.id.unread_num)
        TextView unread;
        @InjectView(R.id.unread)
        View unreadMini;

        public ViewHolder(View v) {
            ButterKnife.inject(this, v);
        }
    }

}
