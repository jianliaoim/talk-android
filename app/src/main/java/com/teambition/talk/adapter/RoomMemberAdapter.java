package com.teambition.talk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.Member;
import com.teambition.talk.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by zeatual on 14/11/27.
 */
public class RoomMemberAdapter extends BaseAdapter {

    private Context context;
    private List<Member> members;

    public RoomMemberAdapter(Context context) {
        this.context = context;
        members = new ArrayList<>();
    }

    public void updateData(List<Member> members) {
        this.members.clear();
        this.members.addAll(members);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return members.size();
    }

    @Override
    public Member getItem(int position) {
        return members.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_member_simple, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Member member = members.get(position);
        if ("all".equals(member.get_id())) {
            holder.title.setText(MainApp.CONTEXT.getString(R.string.at_all));
            holder.image.setImageResource(R.drawable.ic_all_member_avatar);
        } else {
            if (StringUtil.isNotBlank(member.getAlias())) {
                holder.title.setText(member.getAlias());
            }
            if (StringUtil.isNotBlank(member.getAvatarUrl())) {
                MainApp.IMAGE_LOADER.displayImage(member.getAvatarUrl(), holder.image,
                        ImageLoaderConfig.AVATAR_OPTIONS);
            }
        }
        return convertView;
    }

    static class ViewHolder {
        @InjectView(R.id.image)
        ImageView image;
        @InjectView(R.id.title)
        TextView title;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
