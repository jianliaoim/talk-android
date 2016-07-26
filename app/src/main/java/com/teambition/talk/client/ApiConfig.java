package com.teambition.talk.client;

/**
 * Created by zeatual on 14-10-10.
 */
public class ApiConfig {

    public static String REDIRECT_URI;
    public static String BASE_URL;
    public static String ACCOUNT_URL;
    public static String UPLOAD_URL;
    public static String AUTHOR_URL;
    public static String POLICY_URL;
    public static String CALL_URL;

    //call phone
    public static String CALL_APP_ID;
    public static String CALL_APP_TOKEN;
    public static String CALL_ACCOUNT_SID;
    public static String CALL_ACCOUNT_TOKEN;
    public static String FORM_BASE_URL;
    public static String ABSENCE_URL;
    public static String SPIDER_URL;
    public static String SNAPPER_URL;
    public static String AUTH_SERVICE_URL;

    public static String FEEDBACK_INTEGRATION_ID;
    // tps
    public static String TPS_API;
    public static String TPS_PUSH;

    static {
        ACCOUNT_URL = "http://localhost:7001/account/v1";
        REDIRECT_URI = "http://localhost:7001/account/union/callback/teambition";
        BASE_URL = "http://localhost:7001/v2";
        UPLOAD_URL = "http://localhost";
        AUTHOR_URL = "http://localhost:7001/account/union/teambition";
        POLICY_URL = "http://localhost:7001/site/items";
        CALL_URL = "http://localhost:8883";
        FORM_BASE_URL = "http://localhost:7001/v2/services/toapp?%s&_teamId=%s&url=%s";
        ABSENCE_URL = "http://localhost/forms/leave/new";
        SPIDER_URL = "http://localhost";
        SNAPPER_URL = "";
        AUTH_SERVICE_URL = "http://localhost";
        FEEDBACK_INTEGRATION_ID = "";
        TPS_API = "http://localhost";
        TPS_PUSH = "";
        //容联云需要的id,token,多人通话需要
        CALL_APP_ID = "";
        CALL_APP_TOKEN = "";
        CALL_ACCOUNT_SID = "";
        CALL_ACCOUNT_TOKEN = "";
    }
}
