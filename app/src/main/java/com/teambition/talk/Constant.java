package com.teambition.talk;

import android.text.format.DateUtils;

/**
 * Created by zeatual on 14-10-10.
 */
public class Constant {

    public static final String DEFAULT_COLOR = "blue";
    public static final String ACCESS_TOKEN = "account_token";
    public static final String XIAOMI_TOKEN = "xiaomi_token";
    public static final String SOCKET_ID = "socket_id";
    public static final String TEAM = "team";
    public static final String TEAMBITION = "teambition";
    public static final String USER = "user";
    public static final String STRIKER_TOKEN = "striker_token";
    public static final String XIAOMI_APP_ID = "";
    public static final String XIAOMI_APP_KEY = "";
    public static final String HAS_POST_TOKEN = "has_post_token";
    public static final String NOTIFICATION_COUNT = "notification_count";
    public static final String NOTIFICATION_CONTENT = "notification_content";
    public static final int NOTIFICATION_ID = 901104;

    public static final String SD_CARD_ROOT_PATH = android.os.Environment
            .getExternalStorageDirectory().getAbsolutePath();
    public static final String FILE_DIR_DOWNLOAD = Constant.SD_CARD_ROOT_PATH + "/talk/download";
    public static final String FILE_DIR_CACHE = Constant.SD_CARD_ROOT_PATH + "/talk/cache";
    public static final String FILE_DIR_COMPRESSED = Constant.SD_CARD_ROOT_PATH + "/talk/compressed";
    public static final String AUDIO_DIR = Constant.SD_CARD_ROOT_PATH + "/talk/audio";

    public static final String NOTIFY_PREF = "notify_pref";
    public static final String LANGUAGE_PREF = "language_pref";

    public static final String IS_FIRST_OPEN_ITEMS = "is_first_open_items";
    public static final String IS_FIRST_CHAT = "is_first_chat";
    public static final String IS_FIRST_CLICK_NOT_VISIT = "is_first_click_not_visit";
    public static final String IS_FIRS_JOIN = "is_first_join";

    public static final String WECHAT_APP_ID = "";
    public static final String WECHAT_APP_SECRET = "";

    public static final String COPIED_INVITE_TOKEN = "copied_invite_token";

    public static final String MIXPANEL_TOKEN = "";

    public static final int MAX_MSG_VERSION = 2;

    public static final String FIRST_OPEN_3_0 = "first_open_3_0";

    public static final String SURVEY_NEW_USER = "https://jinshuju.net/f/OTOpcs";
    public static final String SURVEY_OLD_USER = "https://jinshuju.net/f/WXBTrg";

    public static final String SHOW_START_TALK_TIPS = "show_start_talk_tips";
    public static final String SHOW_EXPAND_STORY_TIPS = "show_expand_story_tips";
    public static final String SHOW_STORY_USER_MGR_TIPS = "show_story_user_mgr_tips";

    public static final String SHOW_CONTACTS_PAGE = "show_contacts_page";

    public static final int MAX_FILE_SIZE_CELLULAR = 500 * 1024;

    public static final String LAST_USE_TIMESTAMP = "last_use_timestamp";

    public static final long FULL_SYNC_INTERVAL = DateUtils.HOUR_IN_MILLIS;

    public static final String BUGLY_APP_ID = "";
}
