package com.teambition.talk.client;

import android.content.Intent;
import android.webkit.CookieManager;

import com.google.gson.Gson;
import com.teambition.talk.BizLogic;
import com.teambition.talk.BuildConfig;
import com.teambition.talk.Constant;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.MainApp;
import com.teambition.talk.client.apis.AccountApi;
import com.teambition.talk.client.apis.CallApi;
import com.teambition.talk.client.apis.SpiderApi;
import com.teambition.talk.client.apis.TalkApi;
import com.teambition.talk.client.apis.TbAuthApi;
import com.teambition.talk.client.apis.TpsApi;
import com.teambition.talk.client.apis.UploadApi;
import com.teambition.talk.client.data.ErrorResponseData;
import com.teambition.talk.realm.RealmConfig;
import com.teambition.talk.service.MessageService;
import com.teambition.talk.ui.activity.Oauth2Activity;
import com.teambition.talk.util.DeviceUtil;
import com.teambition.talk.util.NotificationUtil;
import com.teambition.talk.util.StringUtil;

import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import retrofit.converter.SimpleXMLConverter;

/**
 * Created by ZZQ on 14-8-5.
 */
public class TalkClient {

    private static final String AUTH_HEADER = "Authorization";

    public static final RestAdapter.LogLevel REST_LOG_LEVEL = MainApp.BUILD_TYPE == MainApp.RELEASE ?
            RestAdapter.LogLevel.NONE : RestAdapter.LogLevel.FULL;

    private static TalkClient talkClient;

    private Gson gson = new GsonProvider.Builder()
            .setDateAdapter()
            .setRoomAdapter()
            .setMessageAdapter()
            .setStoryAdapter()
            .setNotificationAdapter()
            .setTeamActivitiesAdapter()
            .create();

    private String accessToken;
    private String socketId;
    private String clientType = NotificationUtil.USE_XIAOMI ?
            (NotificationUtil.isMIUI() ? "miui": "xiaomi") : "tbpush";
    private String strikerToken;
    private String language = "zh";
    private String deviceId = "";

    private RestAdapter restAdapter;
    private RestAdapter uploadAdapter;
    private RestAdapter accountAdapter;
    private RestAdapter callAdapter;
    private RestAdapter spiderAdapter;
    private RestAdapter tbAuthAdapter;
    private RestAdapter tpsAdapter;

    private TalkApi talkApi;
    private UploadApi uploadApi;
    private AccountApi accountApi;
    private CallApi callApi;
    private SpiderApi spiderApi;
    private TbAuthApi tbAuthApi;
    private TpsApi tpsApi;

    public static TalkClient getInstance() {
        if (talkClient == null) {
            synchronized (TalkClient.class) {
                if (talkClient == null)
                    talkClient = new TalkClient();
            }
        }
        return talkClient;
    }

    private TalkClient() {
        restAdapter = new RestAdapter.Builder()
                .setEndpoint(ApiConfig.BASE_URL)
                .setClient(new OkClient())
                .setRequestInterceptor(requestInterceptor)
                .setConverter(new GsonConverter(gson))
                .setErrorHandler(authFailedErrorHandler)
                .setLogLevel(REST_LOG_LEVEL)
                .build();

        uploadAdapter = new RestAdapter.Builder()
                .setEndpoint(ApiConfig.UPLOAD_URL)
                .setClient(new OkClient())
                .setRequestInterceptor(strikerInterceptor)
                .setLogLevel(REST_LOG_LEVEL)
                .build();

        accountAdapter = new RestAdapter.Builder()
                .setEndpoint(ApiConfig.ACCOUNT_URL)
                .setClient(new OkClient())
                .setRequestInterceptor(requestInterceptor)
                .setLogLevel(REST_LOG_LEVEL)
                .build();

        callAdapter = new RestAdapter.Builder()
                .setClient(new OkClient())
                .setEndpoint(ApiConfig.CALL_URL)
                .setConverter(new SimpleXMLConverter(new Persister(new AnnotationStrategy())))
                .setRequestInterceptor(callInterceptor)
                .setLogLevel(REST_LOG_LEVEL)
                .build();

        spiderAdapter = new RestAdapter.Builder()
                .setEndpoint(ApiConfig.SPIDER_URL)
                .setClient(new OkClient())
                .setRequestInterceptor(requestInterceptor)
                .setConverter(new GsonConverter(gson))
                .build();

        tpsAdapter = new RestAdapter.Builder()
                .setClient(new OkClient())
                .setEndpoint(ApiConfig.TPS_API)
                .setRequestInterceptor(requestInterceptor)
                .setLogLevel(REST_LOG_LEVEL)
                .build();

        tbAuthAdapter = new RestAdapter.Builder()
                .setEndpoint(ApiConfig.AUTH_SERVICE_URL)
                .setClient(new OkClient())
                .setRequestInterceptor(requestInterceptor)
                .setConverter(new GsonConverter(gson))
                .setLogLevel(REST_LOG_LEVEL)
                .build();

        deviceId = DeviceUtil.getDeviceId(MainApp.CONTEXT);
    }

    private RequestInterceptor callInterceptor = new RequestInterceptor() {
        @Override
        public void intercept(RequestFacade request) {
            request.addHeader("accept", "application/xml");
            request.addHeader("connection", "close");
            request.addHeader("content-type", "application/xml;charset=utf-8");
        }
    };

    private RequestInterceptor requestInterceptor = new RequestInterceptor() {
        @Override
        public void intercept(RequestFacade request) {
            if (StringUtil.isNotBlank(accessToken)) {
                request.addHeader(AUTH_HEADER, "aid " + accessToken);
            }
            if (StringUtil.isNotBlank(socketId)) {
                request.addHeader("X-Socket-Id", socketId);
            }
            if (MainApp.BUILD_TYPE != MainApp.RELEASE || BuildConfig.BUILD_FOR_FIR) {
                request.addHeader("X-Release-Version", "ga");
            }
            request.addHeader("X-Client-Id", deviceId);
            request.addHeader("X-Client-Type", clientType);
            request.addHeader("user-agent", "retrofit, android");
            request.addHeader("X-Language", language);
        }
    };

    private RequestInterceptor strikerInterceptor = new RequestInterceptor() {

        @Override
        public void intercept(RequestFacade request) {
            if (StringUtil.isBlank(strikerToken)) {
                strikerToken = MainApp.PREF_UTIL.getString(Constant.STRIKER_TOKEN);
            }
            request.addHeader(AUTH_HEADER, strikerToken);
        }
    };

    private ErrorHandler authFailedErrorHandler = new ErrorHandler() {

        @Override
        public Throwable handleError(RetrofitError cause) {
            if (cause.getResponse() == null) {
                return new NetworkConnectedException("no network connected, please check network connected");
            }
            int code = cause.getResponse().getStatus();
            if (code == 403) {
                ErrorResponseData errorBoyd = (ErrorResponseData) cause.getBodyAs(ErrorResponseData.class);
                if (errorBoyd != null) {
                    if (errorBoyd.code == 201 || errorBoyd.code == 220) {
                        RealmConfig.deleteRealm();
                        MainApp.PREF_UTIL.clear();
                        CookieManager cookieManager = CookieManager.getInstance();
                        cookieManager.removeAllCookie();
                        NotificationUtil.stopPush(MainApp.CONTEXT);
                        Intent intent = new Intent(MainApp.CONTEXT, Oauth2Activity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        BizLogic.cancelSync();
                        MainApp.CONTEXT.startActivity(intent);

                        // disconnect message service
                        Intent closeMsgIntent = new Intent(MainApp.CONTEXT, MessageService.class);
                        closeMsgIntent.setAction(MessageService.ACTION_CLOSE);
                        MainApp.CONTEXT.startService(closeMsgIntent);
                    }
                }
            }
            return cause;
        }
    };

    private ErrorHandler errorHandler = new ErrorHandler() {
        @Override
        public Throwable handleError(RetrofitError cause) {
            if (cause.getResponse() == null) {
                return new NetworkConnectedException("no network connected, please check network connected");
            }
            int code = cause.getResponse().getStatus();
            if (code >= 400) {
                return new TalkException(code, cause);
            }
            return cause;
        }
    };

    public void setStrikerToken(String strikerToken) {
        this.strikerToken = strikerToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setSocketId(String socketId) {
        this.socketId = socketId;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public TalkApi getTalkApi() {
        if (talkApi == null) {
            talkApi = restAdapter.create(TalkApi.class);
        }
        return talkApi;
    }

    public UploadApi getUploadApi() {
        if (uploadApi == null) {
            uploadApi = uploadAdapter.create(UploadApi.class);
        }
        return uploadApi;
    }

    public AccountApi getAccountApi() {
        if (accountApi == null) {
            accountApi = accountAdapter.create(AccountApi.class);
        }
        return accountApi;
    }

    public CallApi getCallApi() {
        if (callApi == null) {
            callApi = callAdapter.create(CallApi.class);
        }
        return callApi;
    }

    public SpiderApi getSpiderApi() {
        if (spiderApi == null) {
            spiderApi = spiderAdapter.create(SpiderApi.class);
        }
        return spiderApi;
    }

    public TbAuthApi getTbAuthApi() {
        if (tbAuthApi == null) {
            tbAuthApi = tbAuthAdapter.create(TbAuthApi.class);
        }
        return tbAuthApi;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language == null ? "zh" : language;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public TpsApi getTpsApi() {
        if (tpsApi == null) {
            tpsApi = tpsAdapter.create(TpsApi.class);
        }
        return tpsApi;
    }
}
