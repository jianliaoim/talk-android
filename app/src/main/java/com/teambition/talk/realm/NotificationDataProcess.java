package com.teambition.talk.realm;

import com.teambition.talk.BizLogic;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Notification;

import java.util.Date;

/**
 * Created by wlanjie on 15/10/23.
 */
public class NotificationDataProcess {

    public enum Type {

        DMS("dms"),
        ROOM("room"),
        STORY("story"),
        DEFAULT("default");

        private String value;

        Type(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        public static Type getEnum(String value) {
            for (Type v : values()) {
                if (v.value.equalsIgnoreCase(value)) {
                    return v;
                }
            }
            return DEFAULT;
        }
    }

    public static Notification getNotificationFromMessage(Message message) {
        Notification notification = new Notification();
        if (message == null) return notification;
        notification.set_id(message.get_id());
        notification.set_teamId(BizLogic.getTeamId());
        notification.set_targetId(message.getForeignId());
        if (message.getTo() != null) {
            notification.setMember(message.getTo());
            notification.setType(Type.DMS.value);
        } else if (message.getRoom() != null) {
            notification.setRoom(message.getRoom());
            notification.setType(Type.ROOM.value);
        } else if (message.getStory() != null) {
            notification.setStory(message.getStory());
            notification.setType(Type.STORY.value);
        }
        notification.setCreator(MainApp.globalMembers.get(BizLogic.getUserInfo().get_id()));
        notification.set_creatorId(BizLogic.getUserInfo().get_id());
        notification.setCreatedAt(new Date(message.getCreateAtTime()));
        notification.setCreatedAtTime(message.getCreateAtTime());
        notification.setUpdatedAt(new Date(message.getCreateAtTime()));
        notification.setUpdateAtTime(message.getCreateAtTime());
        notification.setStatus(message.getStatus());
        if (MessageDataProcess.DisplayMode.getEnum(message.getDisplayMode())
                == MessageDataProcess.DisplayMode.FILE) {
            notification.setText(MainApp.CONTEXT.getString(R.string.picture));
        } else {
            notification.setText(message.getBody());
        }
        notification.setUnreadNum(0);
        return notification;
    }
}
