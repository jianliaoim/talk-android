package com.teambition.talk.entity;

import com.teambition.talk.adapter.ChatSelectAdapter;

/**
 * Created by zeatual on 15/3/10.
 */
public class ChatItem {
    public String id;
    public String name;
    public boolean isTopic;
    public boolean isPrivate;
    public String color;
    public String avatarUrl;
    public int type;
    public boolean isQuit;
    public Room room;
    public Member member;

    public ChatItem(Room room) {
        this.room = room;
        isTopic = true;
        type = ChatSelectAdapter.TOPIC;
        id = room.get_id();
        name = room.getTopic();
        color = room.getColor();
        isPrivate = room.getIsPrivate() == null ? false : room.getIsPrivate();
    }

    public ChatItem(Member member) {
        this.member = member;
        isTopic = false;
        type = ChatSelectAdapter.MEMBER;
        id = member.get_id();
        name = member.getAlias();
        avatarUrl = member.getAvatarUrl();
        isQuit = member.getIsQuit() == null ? false : member.getIsQuit();
    }

    public FilterItem convertToFilterItem() {
        switch (type) {
            case ChatSelectAdapter.MEMBER:
                return new FilterItem(FilterItem.TYPE_MEMBER, id, name);
            case ChatSelectAdapter.TOPIC:
                return new FilterItem(FilterItem.TYPE_ROOM, id, name);
            default:
                return null;
        }
    }
}
