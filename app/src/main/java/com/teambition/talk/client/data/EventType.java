package com.teambition.talk.client.data;

/**
 * Created by alfa7055 on 14-11-2.
 */
public enum EventType {
    TEAM_JOIN("team:join"),
    TEAM_LEAVE("team:leave"),
    TEAM_UPDATE("team:update"),
    TEAM_PREFS_UPDATE("team.prefs:update"),
    TEAM_MEMBERS_PREFS_UPDATE("team.members.prefs:update"),
    ROOM_CREATE("room:create"),
    ROOM_JOIN("room:join"),
    ROOM_LEAVE("room:leave"),
    ROOM_UPDATE("room:update"),
    ROOM_ARCHIVE("room:archive"),
    ROOM_REMOVE("room:remove"),
    USER_UPDATE("user:update"),
    MESSAGE_CREATE("message:create"),
    MESSAGE_UNREAD("message:unread"),
    MESSAGE_UPDATE("message:update"),
    MESSAGE_REMOVE("message:remove"),
    FILE_CREATE("file:create"),
    FILE_UPDATE("file:update"),
    MEMBER_UPDATE("member:update"),
    INTEGRATION_CREATE("integration:create"),
    INTEGRATION_UPDATE("integration:update"),
    INTEGRATION_REMOVE("integration:remove"),
    INTEGRATION_GETTOKEN("integration:gettoken"),
    TEAM_PIN("team:pin"),
    TEAM_UNPIN("team:unpin"),
    ROOM_PREFS_UPDATE("room.prefs:update"),
    ROOM_MEMBERS_PREFS_UPDATE("room.members.prefs:update"),
    INVITATION_CREATE("invitation:create"),
    INVITATION_REMOVE("invitation:remove"),
    NOTIFICATION_UPDATE("notification:update"),
    NOTIFICATION_CREATE("notification:create"),
    NOTIFICATION_REMOVE("notification:remove"),
    STORY_CREATE("story:create"),
    STORY_UPDATE("story:update"),
    STORY_REMOVE("story:remove"),
    GROUP_CREATE("group:create"),
    GROUP_UPDATE("group:update"),
    GROUP_REMOVE("group:remove"),
    ACTIVITY_CREATE("activity:create"),
    ACTIVITY_UPDATE("activity:update"),
    ACTIVITY_REMOVE("activity:remove"),
    EVENT_TYPE_DEFAULT("~~~(>.<)~~~");

    private String value;

    EventType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static EventType getEnum(String value) {
        for (EventType v : values()) {
            if (v.value.equalsIgnoreCase(value)) {
                return v;
            }
        }
        return EVENT_TYPE_DEFAULT;
    }
}
