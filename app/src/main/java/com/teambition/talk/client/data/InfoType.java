package com.teambition.talk.client.data;

/**
 * Created by alfa7055 on 14_11_2.
 */
public enum InfoType {
    COMMENT("{{__info-comment}}"),
    COMMIT_COMMENT("{{__info-commit_comment}}"),
    CREATE("{{__info-create}}"),
    CREATE_INTEGRATION("{{__info-create-integration}}"),
    CREATED_FILE("{{__info-created-file}}"),
    CREATED_TASK("{{__info-created-task}}"),
    DELETE("{{__info-delete}}"),
    FIRIM_MESSAGE("{{__info-firim-message}}"),
    FORK("{{__info-fork}}"),
    GITHUB_NEW_EVENT("{{__info-github-new-event}}"),
    GITLAB_NEW_EVENT("{{__info-gitlab-new-event}}"),
    ISSUE_COMMENT("{{__info-issue_comment}}"),
    ISSUE("{{__info-issue}}"),
    JOIN_ROOM("{{__info-join-room}}"),
    JOIN_TEAM("{{__info-join-team}}"),
    LEAVE_ROOM("{{__info-leave-room}}"),
    LEAVE_TEAM("{{__info-leave-team}}"),
    MENTION("{{__info-mention}}"),
    MERGE_REQUEST("{{__info-merge_request}}"),
    MESSAGE("{{__info-message}}"),
    NEW_MAIL_MESSAGE("{{__info-new-mail-message}}"),
    PULL_REQUEST("{{__info-pull_request}}"),
    PULL_REQUEST_REVIEW_COMMENT("{{__info-pull_request_review_comment}}"),
    PUSH("{{__info-push}}"),
    REMOVE_INTEGRATION("{{__info-remove-integration}}"),
    REPOST("{{__info-repost}}"),
    RSS_NEW_ITEM("{{__info-rss-new-item}}"),
    UPDATE_INTEGRATION("{{__info-update-integration}}"),
    UPDATE_PURPOSE("{{__info-update-purpose}}"),
    UPDATE_TOPIC("{{__info-update-topic}}"),
    WEIBO_NEW_COMMENT("{{__info-weibo-new-comment}}"),
    WEIBO_NEW_MENTION("{{__info-weibo-new-mention}}"),
    WEIBO_NEW_REPOST("{{__info-weibo-new-repost}}"),
    DISABLE_GUEST("{{__info-disable-guest}}"),
    ENABLE_GUEST("{{__info-enable-guest}}"),
    PIN_NOTIFICATION("{{__info-pin-notification}}"),
    UNPIN_NOTIFICATION("{{__info-unpin-notification}}"),
    CREATE_STORY("{{__info-create-story}}"),
    INVITE_STORY_MEMBER("{{__info-invite-story-member}}"),
    REMOVE_STORY_MEMBER("{{__info-remove-story-member}}"),
    INVITE_MEMBER("{{__info-invite-members}}"),
    REMOVE_MEMBER("{{__info-remove-members}}"),
    UPDATE_STORY("{{__info-update-story}}"),
    LEAVE_STORY("{{__info-leave-story}}"),
    REMOVE_MESSAGE("{{__info-remove-message}}"),
    UPLOAD_FILES("{{__info-upload-files}}"),
    NEW_SPEECH("{{__info-new-speech}}"),
    INVITE_YOU("{{__info-invite-you}}"),
    INVITE_TEAM_MEMBER("{{__info-invite-team-member}}"),
    CREATE_ROOM("{{__info-create-room}}"),
    CREATE_FILE_STORY("{{__info-create-file-story}}"),
    CREATE_TOPIC_STORY("{{__info-create-topic-story}}"),
    CREATE_LINK_STORY("{{__info-create-link-story}}"),
    NEW_VIDEO("{{__info-new-video}}"),
    DEFAULT("T_T_type_default_^_^");

    private String value;

    InfoType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static InfoType getEnum(String value) {
        for (InfoType v : values()) {
            if (v.value.equalsIgnoreCase(value)) {
                return v;
            }
        }
        return DEFAULT;
    }

}
