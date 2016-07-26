package com.teambition.talk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.Member;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by zeatual on 14/11/24.
 */
public class NewTopicAddMemberAdapter extends BaseAdapter {

    private Context context;
    private List<Item> items;
    private List<Member> membersAlreadyIn;

    public NewTopicAddMemberAdapter(Context context, List<Member> membersAlreadyIn) {
        this.context = context;
        this.membersAlreadyIn = membersAlreadyIn;
        items = new ArrayList<>();
    }

    public void updateData(List<Member> members) {
        this.items.clear();
        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);
            Item item = new Item();
            for (int j = 0; j < membersAlreadyIn.size(); j++) {
                if (member.get_id().equals(membersAlreadyIn.get(j).get_id())) {
                    item.isSelected = true;
                    break;
                }
            }
            item.member = member;
            items.add(item);
        }
        notifyDataSetChanged();
    }

    public LinkedList<Member> getSelectedMembers() {
        LinkedList<Member> members = new LinkedList<>();
        for (Item item : items) {
            if (item.isSelected) {
                members.add(item.member);
            }
        }
        return members;
    }

    public void isSelected(int position) {
        items.get(position).isSelected = !items.get(position).isSelected;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Member getItem(int position) {
        return items.get(position).member;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_add_member, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final Item item = items.get(position);
        if (item.isSelected) {
            holder.isSelected.setChecked(true);
        } else {
            holder.isSelected.setChecked(false);
        }
        holder.name.setText(item.member.getAlias());
        MainApp.IMAGE_LOADER.displayImage(item.member.getAvatarUrl(), holder.avatar,
                ImageLoaderConfig.AVATAR_OPTIONS);
        return convertView;
    }

    static class ViewHolder {

        @InjectView(R.id.image)
        ImageView avatar;
        @InjectView(R.id.name)
        TextView name;
        @InjectView(R.id.checkbox)
        CheckBox isSelected;

        public ViewHolder(View v) {
            ButterKnife.inject(this, v);
        }
    }

    class Item {
        Member member;
        boolean isSelected = false;
    }
}
