package com.teambition.talk.util;

import android.content.res.Resources;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.CharacterStyle;

import com.teambition.common.PinyinUtil;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.client.data.InfoType;
import com.teambition.talk.ui.span.HighlightSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jgzhu on 5/7/14.
 */
public class StringUtil {

    public static boolean isNotBlank(String str) {
        return !TextUtils.isEmpty(str) && !TextUtils.isEmpty(str.trim());
    }

    public static boolean isBlank(String str) {
        return !StringUtil.isNotBlank(str);
    }

    public static boolean isEmail(String strEmail) {
        String EMAIL_PATTERN = "^[a-zA-Z0-9_-][\\w\\.-]*[a-zA-Z0-9_-]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";
        Pattern p = Pattern.compile(EMAIL_PATTERN);
        Matcher m = p.matcher(strEmail);
        return m.matches();
    }

    public static boolean isNumber(String str) {
        String NUMBER_PATTERN = "^\\d+$";
        Pattern p = Pattern.compile(NUMBER_PATTERN);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    public static boolean isLetter(String str) {
        String LETTER_PATTERN = "^\\w+$";
        Pattern p = Pattern.compile(LETTER_PATTERN);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    public static boolean isChinaPhoneNumber(String phoneNum) {
        String PHONE_NUMBER_PATTERN = "^1[3578]\\d{9}";
        Pattern p = Pattern.compile(PHONE_NUMBER_PATTERN);
        Matcher m = p.matcher(phoneNum);
        return m.matches();
    }

    public static boolean isInternationalPhoneNumber(String phoneNum) {
        String PHONE_NUMBER_PATTERN = "^\\d{1,4}-\\d{3,11}$";
        Pattern p = Pattern.compile(PHONE_NUMBER_PATTERN);
        Matcher m = p.matcher(phoneNum);
        return m.matches();
    }

    public static boolean isChinese(char c) {

        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);

        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS

                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS

                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A;

//                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION

//                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION

//                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;

    }

    public static CharSequence getHighlightSpan(String str, String keyword, Resources res) {
        if (str != null && str.length() > 0) {
            SpannableStringBuilder ssb = new SpannableStringBuilder(str);
            if (StringUtil.isBlank(keyword)) {
                return ssb;
            }
            int start;
            if (str.toLowerCase().contains(keyword)) {
                Matcher matcher = Pattern.compile("\\Q" + keyword + "\\E").matcher(str.toLowerCase());
                while (matcher.find()) {
                    start = matcher.start();
                    int end = matcher.end();
                    ssb.setSpan(CharacterStyle.wrap(new HighlightSpan(res)), start, end,
                            Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
                return ssb;
            } else if (StringUtil.isChinese(str.charAt(0)) &&
                    PinyinUtil.converterToSpell(str).startsWith(keyword)) {
                ssb.setSpan(CharacterStyle.wrap(new HighlightSpan(res)), 0, 1,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                return ssb;
            } else {
                for (int i = 0; i < str.length(); i++) {
                    if (PinyinUtil.converterToSpell(str.substring(i, i + 1)).contains(keyword)) {
                        ssb.setSpan(CharacterStyle.wrap(new HighlightSpan(res)), i, i + 1,
                                Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                }
                return ssb;
            }
        } else {
            return "";
        }
    }

    public static String getIdInUrl(String type, String url) {
        Pattern p = Pattern.compile("/" + type + "/(\\w+)(\\?|/)*");
        Matcher matcher = p.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    public static String getFormatText(InfoType infoType, String name) {
        switch (infoType) {
            case COMMENT:
                return String.format(MainApp.CONTEXT.getString(R.string.info_comment), name);
            case COMMIT_COMMENT:
                return MainApp.CONTEXT.getString(R.string.info_commit_comment);
            case CREATE:
                return MainApp.CONTEXT.getString(R.string.info_create);
            case CREATE_INTEGRATION:
                return String.format(MainApp.CONTEXT.getString(R.string.info_create_integration),
                        name);
            case CREATED_FILE:
                return MainApp.CONTEXT.getString(R.string.info_created_file);
            case CREATED_TASK:
                return String.format(MainApp.CONTEXT.getString(R.string.info_created_task), name);
            case DELETE:
                return MainApp.CONTEXT.getString(R.string.info_delete);
            case FIRIM_MESSAGE:
                return MainApp.CONTEXT.getString(R.string.info_firim_message);
            case FORK:
                return MainApp.CONTEXT.getString(R.string.info_fork);
            case GITHUB_NEW_EVENT:
                return MainApp.CONTEXT.getString(R.string.info_github_new_event);
            case GITLAB_NEW_EVENT:
                return MainApp.CONTEXT.getString(R.string.info_gitlab_new_event);
            case ISSUE_COMMENT:
                return MainApp.CONTEXT.getString(R.string.info_issue_comment);
            case ISSUE:
                return MainApp.CONTEXT.getString(R.string.info_issue);
            case JOIN_ROOM:
                return String.format(MainApp.CONTEXT.getString(R.string.info_join_room), name);
            case JOIN_TEAM:
                return String.format(MainApp.CONTEXT.getString(R.string.info_join_team), name);
            case LEAVE_ROOM:
                return String.format(MainApp.CONTEXT.getString(R.string.info_leave_room), name);
            case LEAVE_TEAM:
                return String.format(MainApp.CONTEXT.getString(R.string.info_leave_team), name);
            case MENTION:
                return String.format(MainApp.CONTEXT.getString(R.string.info_mention), name);
            case MERGE_REQUEST:
                return MainApp.CONTEXT.getString(R.string.info_merge_request);
            case MESSAGE:
                return MainApp.CONTEXT.getString(R.string.info_message);
            case NEW_MAIL_MESSAGE:
                return MainApp.CONTEXT.getString(R.string.info_new_mail_message);
            case PULL_REQUEST:
                return MainApp.CONTEXT.getString(R.string.info_pull_request);
            case PULL_REQUEST_REVIEW_COMMENT:
                return MainApp.CONTEXT.getString(R.string.info_pull_request_review_comment);
            case PUSH:
                return MainApp.CONTEXT.getString(R.string.info_push);
            case REMOVE_INTEGRATION:
                return String.format(MainApp.CONTEXT.getString(R.string.info_remove_integration),
                        name);
            case REPOST:
                return String.format(MainApp.CONTEXT.getString(R.string.info_repost), name);
            case RSS_NEW_ITEM:
                return MainApp.CONTEXT.getString(R.string.info_rss_new_item);
            case UPDATE_INTEGRATION:
                return String.format(MainApp.CONTEXT.getString(R.string.info_update_integration), name);
            case UPDATE_PURPOSE:
                return String.format(MainApp.CONTEXT.getString(R.string.info_update_purpose), name);
            case UPDATE_TOPIC:
                return String.format(MainApp.CONTEXT.getString(R.string.info_update_topic), name);
            case WEIBO_NEW_COMMENT:
                return MainApp.CONTEXT.getString(R.string.info_weibo_new_comment);
            case WEIBO_NEW_MENTION:
                return MainApp.CONTEXT.getString(R.string.info_weibo_new_mention);
            case WEIBO_NEW_REPOST:
                return MainApp.CONTEXT.getString(R.string.info_weibo_new_repost);
            case ENABLE_GUEST:
                return String.format(MainApp.CONTEXT.getString(R.string.info_enable_guest), name);
            case DISABLE_GUEST:
                return String.format(MainApp.CONTEXT.getString(R.string.info_disable_guest), name);
            case PIN_NOTIFICATION:
                return MainApp.CONTEXT.getString(R.string.info_pin_notification, name);
            case UNPIN_NOTIFICATION:
                return MainApp.CONTEXT.getString(R.string.info_unpin_notification, name);
            case CREATE_STORY:
                return MainApp.CONTEXT.getString(R.string.info_create_story, name);
            case INVITE_STORY_MEMBER:
                return MainApp.CONTEXT.getString(R.string.info_invite_story_member, name);
            case REMOVE_STORY_MEMBER:
                return MainApp.CONTEXT.getString(R.string.info_remove_story_member, name);
            case INVITE_MEMBER:
                return MainApp.CONTEXT.getString(R.string.info_invite_members, name);
            case REMOVE_MEMBER:
                return MainApp.CONTEXT.getString(R.string.info_remove_members, name);
            case UPDATE_STORY:
                return MainApp.CONTEXT.getString(R.string.info_update_story, name);
            case LEAVE_STORY:
                return MainApp.CONTEXT.getString(R.string.info_leave_story, name);
            case REMOVE_MESSAGE:
                return MainApp.CONTEXT.getString(R.string.info_remove_message, name);
            case UPLOAD_FILES:
                return MainApp.CONTEXT.getString(R.string.info_upload_files, name);
            case NEW_SPEECH:
                return MainApp.CONTEXT.getString(R.string.info_new_speech, name);
            case INVITE_YOU:
                return MainApp.CONTEXT.getString(R.string.info_invite_you, name);
            case INVITE_TEAM_MEMBER:
                return MainApp.CONTEXT.getString(R.string.info_invite_team_member, name);
            case CREATE_ROOM:
                return MainApp.CONTEXT.getString(R.string.info_create_room, name);
            case CREATE_LINK_STORY:
                return MainApp.CONTEXT.getString(R.string.info_create_link_story, name);
            case CREATE_FILE_STORY:
                return MainApp.CONTEXT.getString(R.string.info_create_file_story, name);
            case CREATE_TOPIC_STORY:
                return MainApp.CONTEXT.getString(R.string.info_create_topic_story, name);
            case NEW_VIDEO:
                return MainApp.CONTEXT.getString(R.string.info_new_video, name);
            default:
                return "";
        }
    }
}
