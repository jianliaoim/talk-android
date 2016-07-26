package com.teambition.talk.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.Socket;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.otto.Bus;
import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.Constant;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.MainApp;
import com.teambition.talk.client.ApiConfig;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.apis.TalkApi;
import com.teambition.talk.client.apis.TpsApi;
import com.teambition.talk.client.data.EventType;
import com.teambition.talk.client.data.RegisterTpsResponseData;
import com.teambition.talk.client.data.SubscribeResponseData;
import com.teambition.talk.entity.Group;
import com.teambition.talk.entity.Invitation;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Notification;
import com.teambition.talk.entity.Room;
import com.teambition.talk.entity.Story;
import com.teambition.talk.entity.Team;
import com.teambition.talk.entity.TeamActivity;
import com.teambition.talk.entity.User;
import com.teambition.talk.event.DeleteMessageEvent;
import com.teambition.talk.event.LeaveRoomEvent;
import com.teambition.talk.event.LeaveTeamEvent;
import com.teambition.talk.event.NetworkEvent;
import com.teambition.talk.event.NewMessageEvent;
import com.teambition.talk.event.NewOtherMessageEvent;
import com.teambition.talk.event.NewTeamActivityEvent;
import com.teambition.talk.event.NewTeamEvent;
import com.teambition.talk.event.RemoveGroupEvent;
import com.teambition.talk.event.RemoveNotificationEvent;
import com.teambition.talk.event.RemoveStoryEvent;
import com.teambition.talk.event.RemoveTeamActivityEvent;
import com.teambition.talk.event.RoomRemoveEvent;
import com.teambition.talk.event.UpdateGroupEvent;
import com.teambition.talk.event.UpdateMemberEvent;
import com.teambition.talk.event.UpdateMessageEvent;
import com.teambition.talk.event.UpdateNotificationEvent;
import com.teambition.talk.event.UpdateRoomEvent;
import com.teambition.talk.event.UpdateStoryEvent;
import com.teambition.talk.event.UpdateTeamActivityEvent;
import com.teambition.talk.realm.GroupRealm;
import com.teambition.talk.realm.InvitationRealm;
import com.teambition.talk.realm.MemberDataProcess;
import com.teambition.talk.realm.MemberRealm;
import com.teambition.talk.realm.MessageDataProcess;
import com.teambition.talk.realm.MessageRealm;
import com.teambition.talk.realm.NotificationRealm;
import com.teambition.talk.realm.RoomRealm;
import com.teambition.talk.realm.StoryRealm;
import com.teambition.talk.realm.TeamRealm;
import com.teambition.talk.rx.EmptyAction;
import com.teambition.talk.rx.OperatorSuppressError;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.util.Connectivity;
import com.teambition.talk.util.DateUtil;
import com.teambition.talk.util.DeviceUtil;
import com.teambition.talk.util.Logger;
import com.teambition.talk.util.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by jgzhu on 10/30/14.
 */
public class MessageService extends Service {

    private static final String TAG = "MessageService";

    public static final String ACTION_MSG_RETRY = "com.teambition.talk.ACTION_MSG_RETRY";

    public static final String META_APP_KEY = "WHISPER-APP-KEY";
    public static final String ACTION_CLOSE = "com.teambition.talk.service.ACTION_CLOSE";
    public static final String ACTION_CONNECT = "com.teambition.talk.service.ACTION_CONNECT";
    public static final String ACTION_RETRY = "com.teambition.talk.service.ACTION_RETRY";
    public static final String ACTION_READ_HISTORY = "com.teambition.talk.service.ACTION_READ_HISTORY";
    public static final String ACTION_SWITCH_TEAM = "com.teambition.talk.service.ACTION_SWITCH_TEAM";
    public static final String PREF_NAME = "whisper_pref";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_CHANNEL = "user_channel";
    public static final String PREF_TEAM_CHANNEL_PREFIX = "team_channel";
    public static final String PREF_LAST_MSG_TIMESTAMP = "last_msg_timestamp";
    public static final String PREF_INCREMENTALLY_SYNC_FIRST_TIME = "pref_use_incrementally_sync";
    public static final String PREF_IS_SHUTDOWN = "is_shutdown";
    public static final String PREF_EXPIRED_AT = "expire_at";

    public static final int ACTION_CODE_RETRY = 33009;
    public static final long MIN_RETRY_DELAY_TIMEOUT = 10 * 1000;
    public static final long MAX_RETRY_DELAY_TIMEOUT = 160 * 1000;

    public static final String WEBSOCKET_URL = ApiConfig.TPS_PUSH + "/engine.io/";

    private final IBinder binder = new Binder();
    private TalkApi talkApi;
    private Handler handler;
    private boolean isShutdown;
    private Gson gson;
    private Socket socketClient;
    private long retryDelayInMilli = MIN_RETRY_DELAY_TIMEOUT;

    private WakeLock wakeLock;

    private String appKey = null;
    private String deviceID = null;
    private int xhrPollErrorRetryCount = 0;
    private SharedPreferences preferences;

    private boolean isReadingHistory = false;

    private BroadcastReceiver retryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Connectivity.isConnected(getApplicationContext())) {
                Logger.d(TAG, "retry connect tps");
                String userId = preferences.getString(PREF_USER_ID, "");
                if (StringUtil.isBlank(userId)) {
                    register(appKey, deviceID, userId);
                } else {
                    openSocket(userId, deviceID);
                }
            }
        }
    };

    public class Binder extends android.os.Binder {
        public MessageService getService() {
            return MessageService.this;
        }
    }

    public static Intent startIntent(Context context) {
        Intent intent = new Intent(context, MessageService.class);
        intent.setAction(ACTION_CONNECT);
        return intent;
    }

    public static Intent switchTeamIntent(Context context) {
        Intent intent = new Intent(context, MessageService.class);
        intent.setAction(ACTION_SWITCH_TEAM);
        return intent;
    }

    public static Intent readHistoryIntent(Context context) {
        Intent intent = new Intent(context, MessageService.class);
        intent.setAction(ACTION_READ_HISTORY);
        return intent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        gson = new GsonProvider.Builder()
                .setDateFormat(DateUtil.DATE_FORMAT_JSON)
                .setDateAdapter()
                .setRoomAdapter()
                .setMessageAdapter()
                .setNotificationAdapter()
                .setTeamActivitiesAdapter()
                .setStoryAdapter()
                .create();

        appKey = readMeta(META_APP_KEY);
        if (TextUtils.isEmpty(appKey)) {
            Logger.e(TAG, "please set tps app key in AndroidManifest.xml", new IllegalStateException());
        }
        deviceID = DeviceUtil.getDeviceId(this);
        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        registerReceiver(retryReceiver, new IntentFilter(ACTION_RETRY));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d(TAG, "onStartCommand!");
        if (talkApi == null) {
            talkApi = TalkClient.getInstance().getTalkApi();
        }

        isShutdown = false;
        String userId = preferences.getString(PREF_USER_ID, "");

        if (intent != null && ACTION_READ_HISTORY.equals(intent.getAction())) {
            readHistory(userId, preferences.getString(PREF_USER_CHANNEL, ""), PREF_LAST_MSG_TIMESTAMP);
            readHistory(userId, preferences.getString(PREF_TEAM_CHANNEL_PREFIX + BizLogic.getTeamId(), ""),
                    PREF_LAST_MSG_TIMESTAMP + BizLogic.getTeamId());
            return START_STICKY;
        }

        if (intent != null && ACTION_SWITCH_TEAM.equals(intent.getAction())) {
            subscribeTeamChannel(userId);
            return START_STICKY;
        }

        if (intent != null && ACTION_CLOSE.equals(intent.getAction())) {
            isShutdown = true;
            disconnectSocket();
            return START_STICKY;
        }

        Logger.d(TAG, "init socketClient!");
        if (StringUtil.isBlank(userId)) {
            register(appKey, deviceID, userId);
        } else {
            openSocket(userId, deviceID);
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void register(String appKey, String deviceId, String userId) {
        if (socketClient != null) {
            return;
        }

        String uid = null;
        if (userId == null) {
            uid = "";
        }
        TalkClient.getInstance().getTpsApi()
                .register(appKey, uid, deviceId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RegisterTpsResponseData>() {
                    @Override
                    public void call(RegisterTpsResponseData res) {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(PREF_USER_ID, res.userId);
                        editor.commit();
                        if (!TextUtils.isEmpty(res.userId)) {
                            openSocket(res.userId, deviceID);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        Logger.e(TAG, "tps register failed", e);
                        retryConnect();
                    }
                });
    }

    private void openSocket(String userId, String deviceToken) {
        if (socketClient == null) {
            synchronized (MessageService.class) {
                if (socketClient == null) {
                    try {
                        String query = "userId=" + userId + "&deviceToken=" + deviceToken;
                        socketClient = new Socket(WEBSOCKET_URL + "?" + query);
                        setSocketOperation(userId);
                        socketClient.setContext(this);
                        socketClient.open();
                    } catch (Exception e) {
                        Logger.e(TAG, "socket open error", e);
                        retryConnect();
                    }
                } else {
                    disconnectSocket();
                    openSocket(userId, deviceToken);
                }
            }
        } else {
            disconnectSocket();
            openSocket(userId, deviceToken);
        }
    }

    private void setSocketOperation(final String userId) {
        socketClient.on(Socket.EVENT_OPEN, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                acquireWakeLock();
                Logger.d(TAG, "socket open");
                cancelRetry();
                subscribeUserChannel(userId);
                subscribeTeamChannel(userId);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        BusProvider.getInstance().post(new NetworkEvent(NetworkEvent.STATE_CONNECTED));
                    }
                });
                releaseWakeLock();
            }
        }).on(Socket.EVENT_MESSAGE, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String data = (String) args[0];
                JSONObject jsonData = null;

                try {
                    jsonData = new JSONObject(data);
                    final String id = jsonData.optString("id");
                    // send message receipt to server
                    final JSONObject jsonReceipt = new JSONObject();
                    jsonReceipt.put("cid", id);
                    if (socketClient != null) {
                        socketClient.send(jsonReceipt.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONArray jsonArgs = jsonData.optJSONArray("args");
                if (!isReadingHistory) {
                    String timestamp = jsonArgs.optJSONObject(0).optString("createdAt");
                    preferences.edit().putString(PREF_LAST_MSG_TIMESTAMP, timestamp).commit();
                    preferences.edit().putString(PREF_LAST_MSG_TIMESTAMP + BizLogic.getTeamId(), timestamp).commit();
                }
                JSONObject jsonObject = jsonArgs == null ? null : jsonArgs.optJSONObject(0).optJSONObject("payload");
                Logger.d(TAG, jsonObject.toString());
                final JSONObject finalJson = jsonObject;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (finalJson != null && finalJson.has("a")) {
                                dispatchEvent(finalJson);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                releaseWakeLock();
            }
        }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Exception ex = (Exception) args[0];
                if (socketClient != null) {
                    Logger.d(TAG, "socket error! id = " + socketClient.id() + ex.getCause().toString());
                }
                if (xhrPollErrorRetryCount < 1 && "xhr poll error".equals(ex.getMessage())) {
                    xhrPollErrorRetryCount++;
                    socketClient = null;
                    register(appKey, deviceID, null);
                }
            }
        }).on(Socket.EVENT_CLOSE, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Logger.d(TAG, "socket close");
                socketClient = null;
                retryConnect();
            }
        });
    }

    private void disconnectSocket() {
        if (socketClient != null) {
            try {
                socketClient.close();
                socketClient = null;
                // TODO
                // isDisconnect = true;
            } catch (Exception e) {
            }
        }
    }

    private void retryConnect() {
        Intent retryIntent = new Intent(ACTION_RETRY);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MessageService.this,
                ACTION_CODE_RETRY, retryIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + retryDelayInMilli, pendingIntent);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + retryDelayInMilli, pendingIntent);
        }
        if (retryDelayInMilli <= MAX_RETRY_DELAY_TIMEOUT / 2) {
            retryDelayInMilli *= 2;
        }
    }

    private void cancelRetry() {
        Intent retryIntent = new Intent(ACTION_RETRY);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MessageService.this,
                ACTION_CODE_RETRY, retryIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
        retryDelayInMilli = MIN_RETRY_DELAY_TIMEOUT;
    }

    /**
     * Subscribe to user channel and team channel
     * @param userId
     */
    private void subscribeUserChannel(final String userId) {
        if (StringUtil.isNotBlank(userId)) {
            MainApp.PREF_UTIL.putString(Constant.SOCKET_ID, userId);
            TalkClient.getInstance().setSocketId(userId);
            if (BizLogic.isLogin()) {
                Observable<SubscribeResponseData> userStream = talkApi.subscribeUser(userId)
                        .map(new Func1<SubscribeResponseData, SubscribeResponseData>() {
                            @Override
                            public SubscribeResponseData call(SubscribeResponseData subscribeResponseData) {
                                subscribeResponseData.type = SubscribeResponseData.TYPE_USER;
                                return subscribeResponseData;
                            }
                        });
                userStream.subscribe(new Action1<SubscribeResponseData>() {
                    @Override
                    public void call(SubscribeResponseData subscribeResponseData) {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(PREF_USER_CHANNEL, subscribeResponseData.channelId);
                        editor.commit();
                        readHistory(userId, subscribeResponseData.channelId, PREF_LAST_MSG_TIMESTAMP);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
            }
        }
    }

    private void subscribeTeamChannel(final String userId) {
        String teamId = BizLogic.getTeamId();
        if (StringUtil.isNotBlank(userId)) {
            MainApp.PREF_UTIL.putString(Constant.SOCKET_ID, userId);
            TalkClient.getInstance().setSocketId(userId);
            if (BizLogic.isLogin()) {
                Observable<SubscribeResponseData> teamStream = talkApi.subscribeTeam(teamId, userId)
                        .map(new Func1<SubscribeResponseData, SubscribeResponseData>() {
                            @Override
                            public SubscribeResponseData call(SubscribeResponseData subscribeResponseData) {
                                subscribeResponseData.type = SubscribeResponseData.TYPE_TEAM;
                                return subscribeResponseData;
                            }
                        });
                teamStream.subscribe(new Action1<SubscribeResponseData>() {
                    @Override
                    public void call(SubscribeResponseData subscribeResponseData) {
                        SharedPreferences.Editor editor = preferences.edit();
                        String prefKey = PREF_TEAM_CHANNEL_PREFIX + BizLogic.getTeamId();
                        editor.putString(prefKey, subscribeResponseData.channelId);
                        editor.commit();
                        readHistory(userId, subscribeResponseData.channelId,
                                PREF_LAST_MSG_TIMESTAMP + BizLogic.getTeamId());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
            }
        }
    }

    /**
     * 同步channel离线消息
     */
    private void readHistory(String userId, String channelId, String pref) {
        if (StringUtil.isBlank(userId) || StringUtil.isBlank(channelId) || StringUtil.isBlank(pref)) {
            return;
        }
        TpsApi tpsApi = TalkClient.getInstance().getTpsApi();
        String lastMsgTimestamp = preferences.getString(pref, "");
        Date lastMsgTime = DateUtil.parseISO8601(lastMsgTimestamp, DateUtil.DATE_FORMAT_JSON);
        boolean useIncrementallySyncFirstTime = preferences.getBoolean(PREF_INCREMENTALLY_SYNC_FIRST_TIME, true);
        boolean isWithIncrementInterval = lastMsgTime != null
                && System.currentTimeMillis() - lastMsgTime.getTime() < Constant.FULL_SYNC_INTERVAL;
        if (!useIncrementallySyncFirstTime && isWithIncrementInterval) {
            isReadingHistory = true;
            Observable<JsonArray> historyStream = tpsApi.getHistory(userId, channelId, lastMsgTimestamp);
            historyStream.observeOn(AndroidSchedulers.mainThread())
                    .flatMap(new Func1<JsonArray, Observable<JsonElement>>() {
                        @Override
                        public Observable<JsonElement> call(final JsonArray jsonArray) {
                            return Observable.from(jsonArray);
                        }
                    })
                    .filter(new Func1<JsonElement, Boolean>() {
                        @Override
                        public Boolean call(JsonElement element) {
                            return element.isJsonObject();
                        }
                    })
                    .lift(new OperatorSuppressError(new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Logger.e(TAG, "read history error", throwable);
                        }
                    }))
                    .subscribe(new Action1<JsonElement>() {
                        @Override
                        public void call(JsonElement element) {
                            JsonObject jsonObject = (JsonObject) element;
                            String payloadJsonStr = jsonObject.get("payload").toString();
                            try {
                                dispatchEvent(new JSONObject(payloadJsonStr));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Logger.e(TAG, "read history error", throwable);
                            isReadingHistory = false;
                        }
                    }, new Action0() {
                        @Override
                        public void call() {
                            String timestamp = DateUtil.formatISO8601(new Date(), DateUtil.DATE_FORMAT_JSON);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(PREF_LAST_MSG_TIMESTAMP, timestamp);
                            editor.putString(PREF_LAST_MSG_TIMESTAMP + BizLogic.getTeamId(), timestamp);
                            editor.commit();
                            isReadingHistory = false;
                        }
                    });
        } else {
            BizLogic.syncTeamData();
        }
        if (useIncrementallySyncFirstTime) {
            preferences.edit().putBoolean(PREF_INCREMENTALLY_SYNC_FIRST_TIME, false).commit();
        }
    }

    private String readMeta(String key) {
        String value = "";
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            value = bundle.getString(key);
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e(TAG, "Failed to load meta-data, NameNotFound: ", e);
        } catch (NullPointerException e) {
            Logger.e(TAG, "Failed to load meta-data, NullPointer: ", e);
        }
        return value;
    }

    private void acquireWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "service");
        wakeLock.acquire();
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    // deal with different types of messages
    private void dispatchEvent(JSONObject data) throws JSONException {
        if (data.optInt("v") > Constant.MAX_MSG_VERSION) {
            return;
        }
        Bus eventBus = BusProvider.getInstance();
        String action = data.getString("a");
        Team team;
        Member member;
        Invitation invitation;
        Room room;
        final Message message;
        Notification notification;
        String userId;
        String roomId;
        String targetId;
        String teamId;
        Boolean isMute;
        String alias;
        Boolean hideMobile;
        switch (EventType.getEnum(action)) {
            case TEAM_JOIN:
                member = deserializeJson(data.getString("d"), Member.class);
                if (member.get_teamId().equals(BizLogic.getTeamId())) {
                    member.setIsQuit(false);
                    MainApp.globalMembers.put(member.get_id(), member);
                    MemberDataProcess.getInstance().processNewMember(member);
                    MemberDataProcess.getInstance().processPrefers(member);
                    MemberRealm.getInstance().addOrUpdate(member)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Member>() {
                                @Override
                                public void call(Member member) {
                                    MainApp.IS_MEMBER_CHANGED = true;
                                    BusProvider.getInstance().post(new UpdateMemberEvent());
                                }
                            }, new RealmErrorAction());
                    RoomRealm.getInstance().updateGeneralRoom(member)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Room>() {
                                @Override
                                public void call(Room room) {

                                }
                            }, new RealmErrorAction());
                } else {
                    BusProvider.getInstance().post(new NewTeamEvent(member.get_teamId()));
                }
                break;
            case TEAM_LEAVE:
                member = MainApp.globalMembers.get(data.getJSONObject("d").getString("_userId"));
                teamId = data.getJSONObject("d").getString("_teamId");
                if (member != null) {
                    MainApp.globalMembers.remove(member.get_id());
                    if (BizLogic.isMe(member.get_id())) {
                        TeamRealm.getInstance().deleteTeam(teamId)
                                .subscribe(new EmptyAction<Team>(), new RealmErrorAction());
                        BusProvider.getInstance().post(new LeaveTeamEvent(teamId));
                        if (teamId.equals(BizLogic.getTeamId())) {
                            MainApp.PREF_UTIL.removeObject(Constant.TEAM);
                        }
                    }
                    member.setIsQuit(true);
                    MemberRealm.getInstance().addOrUpdate(member)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Member>() {
                                @Override
                                public void call(Member member) {
                                    MainApp.IS_MEMBER_CHANGED = true;
                                    BusProvider.getInstance().post(new UpdateMemberEvent());
                                }
                            }, new RealmErrorAction());
                }
                break;
            case TEAM_UPDATE:
                team = deserializeJson(data.getString("d"), Team.class);
                Team.update(team, eventBus);
                TeamRealm.getInstance().addOrUpdate(team)
                        .subscribe(new EmptyAction<Team>(), new RealmErrorAction());
                break;
            case TEAM_PREFS_UPDATE:
            case TEAM_MEMBERS_PREFS_UPDATE:
                teamId = data.getJSONObject("d").getString("_teamId");
                userId = data.getJSONObject("d").getString("_userId");
                alias = data.getJSONObject("d").has("alias") ? data.getJSONObject("d")
                        .optString("alias") : null;
                hideMobile = data.getJSONObject("d").has("hideMobile") ? data.getJSONObject("d")
                        .optBoolean("hideMobile") : null;
                member = MainApp.globalMembers.get(userId);
                if (BizLogic.isCurrentTeam(teamId) && member != null && alias != null) {
                    member.setAlias(alias);
                    member.setHideMobile(hideMobile);
                    MemberRealm.getInstance().addOrUpdate(member)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Member>() {
                                @Override
                                public void call(Member member) {
                                    MainApp.IS_MEMBER_CHANGED = true;
                                    BusProvider.getInstance().post(new UpdateMemberEvent());
                                }
                            }, new RealmErrorAction());
                }
                break;
            case TEAM_PIN:
                targetId = data.getJSONObject("d").getString("_targetId");
                teamId = data.getJSONObject("d").getString("_teamId");
                if (BizLogic.isCurrentTeam(teamId)) {
                    if (MainApp.globalMembers.containsKey(targetId)) {
                        member = MainApp.globalMembers.get(targetId);
                        member.setPinnedAt(new Date());
                        MemberRealm.getInstance().addOrUpdate(member)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Member>() {
                                    @Override
                                    public void call(Member member) {
                                        MainApp.IS_MEMBER_CHANGED = true;
                                        BusProvider.getInstance().post(new UpdateMemberEvent());
                                    }
                                }, new RealmErrorAction());
                    } else if (MainApp.globalRooms.containsKey(targetId)) {
                        room = MainApp.globalRooms.get(targetId);
                        room.setPinnedAt(new Date());
                        RoomRealm.getInstance().addOrUpdate(room)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Room>() {
                                    @Override
                                    public void call(Room room) {
                                        MainApp.IS_ROOM_CHANGED = true;
                                        BusProvider.getInstance().post(new UpdateRoomEvent());
                                    }
                                }, new RealmErrorAction());
                    } else if (MainApp.globalRooms.containsKey(targetId)) {
                        room = MainApp.globalRooms.get(targetId);
                        room.setPinnedAt(new Date());
                    }
                }
                break;
            case TEAM_UNPIN: {
                targetId = data.getJSONObject("d").getString("_targetId");
                teamId = data.getJSONObject("d").getString("_teamId");
                if (BizLogic.isCurrentTeam(teamId)) {
                    if (MainApp.globalMembers.containsKey(targetId)) {
                        member = MainApp.globalMembers.get(targetId);
                        member.setPinnedAt(null);
                        MemberRealm.getInstance().addOrUpdate(member)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Member>() {
                                    @Override
                                    public void call(Member member) {
                                        MainApp.IS_MEMBER_CHANGED = true;
                                        BusProvider.getInstance().post(new UpdateMemberEvent());
                                    }
                                }, new RealmErrorAction());
                    } else if (MainApp.globalRooms.containsKey(targetId)) {
                        room = MainApp.globalRooms.get(targetId);
                        room.setPinnedAt(null);
                        RoomRealm.getInstance().addOrUpdate(room)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Room>() {
                                    @Override
                                    public void call(Room room) {
                                        MainApp.IS_ROOM_CHANGED = true;
                                        BusProvider.getInstance().post(new UpdateRoomEvent());
                                    }
                                }, new RealmErrorAction());
                    } else if (MainApp.globalRooms.containsKey(targetId)) {
                        room = MainApp.globalRooms.get(targetId);
                        room.setPinnedAt(null);
                    }
                }
                break;
            }
            case ROOM_CREATE:
                room = deserializeJson(data.getString("d"), Room.class);
                room.setIsQuit(true);
                RoomRealm.getInstance().addOrUpdate(room)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Room>() {
                            @Override
                            public void call(Room room) {
                                MainApp.IS_ROOM_CHANGED = true;
                                BusProvider.getInstance().post(new UpdateRoomEvent());
                            }
                        }, new RealmErrorAction());
                break;
            case ROOM_JOIN: {
                member = deserializeJson(data.getString("d"), Member.class);
                teamId = member.get_teamId();
                // 判断是否为新加入团队的加入话题事件
                boolean isNewTeam = true;
                if (teamId == null) {
                    isNewTeam = false;
                } else {
                    List<Team> teams = TeamRealm.getInstance().getTeamWithCurrentThread();
                    for (Team teamInfo : teams) {
                        if (teamInfo.get_id().equals(teamId)) {
                            isNewTeam = false;
                        }
                    }
                    if (teamId.equals(BizLogic.getTeamId())) {
                        isNewTeam = false;
                    }
                }
                if (isNewTeam) {
                    BusProvider.getInstance().post(new NewTeamEvent(teamId));
                    break;
                }

                roomId = data.getJSONObject("d").getString("_roomId");
                if (MainApp.globalRooms.containsKey(roomId)) {
                    room = MainApp.globalRooms.get(roomId);
                    if (BizLogic.isMe(member.get_id())) {
                        room.setIsQuit(false);
                        RoomRealm.getInstance().addOrUpdate(room)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Room>() {
                                    @Override
                                    public void call(Room room) {
                                        MainApp.IS_ROOM_CHANGED = true;
                                        BusProvider.getInstance().post(new UpdateRoomEvent());
                                    }
                                }, new RealmErrorAction());
                    }
                } else {
                    // 被邀请进私有话题
                    if (BizLogic.isMe(member.get_id())) {
                        room = deserializeJson(data.getJSONObject("d").getJSONObject("room").toString(), Room.class);
                        room.setIsQuit(false);
                        RoomRealm.getInstance().addOrUpdate(room)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Room>() {
                                    @Override
                                    public void call(Room room) {
                                        MainApp.IS_ROOM_CHANGED = true;
                                        BusProvider.getInstance().post(new UpdateRoomEvent());
                                    }
                                }, new RealmErrorAction());
                    }
                }
                break;
            }
            case ROOM_LEAVE:
                roomId = data.getJSONObject("d").getString("_roomId");
                String memberId = data.getJSONObject("d").getString("_userId");
                String roomMemberId = memberId + roomId;
                room = MainApp.globalRooms.get(roomId);
                if (room != null) {
                    RoomRealm.getInstance().leave(room, memberId)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Room>() {
                                @Override
                                public void call(Room room) {
                                    MainApp.IS_ROOM_CHANGED = true;
                                }
                            }, new RealmErrorAction());
                }
                if (BizLogic.isMe(memberId)) {
                    BusProvider.getInstance().post(new LeaveRoomEvent(roomId));
                }
                break;
            case ROOM_UPDATE:
                room = deserializeJson(data.getString("d"), Room.class);
                RoomRealm.getInstance().addOrUpdate(room)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Room>() {
                            @Override
                            public void call(Room room) {
                                MainApp.IS_ROOM_CHANGED = true;
                                BusProvider.getInstance().post(new UpdateRoomEvent());
                            }
                        }, new RealmErrorAction());
                break;
            case ROOM_ARCHIVE:
                room = deserializeJson(data.getString("d"), Room.class);
                RoomRealm.getInstance().archive(room)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Room>() {
                            @Override
                            public void call(Room room) {
                                MainApp.IS_ROOM_CHANGED = true;
                            }
                        }, new RealmErrorAction());
                break;
            case ROOM_REMOVE:
                room = deserializeJson(data.getString("d"), Room.class);
                final Room r = room;
                RoomRealm.getInstance().remove(room)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Room>() {
                            @Override
                            public void call(Room room) {
                                BusProvider.getInstance().post(new RoomRemoveEvent(r.get_id()));
                            }
                        }, new RealmErrorAction());
                break;
            case USER_UPDATE:
                member = deserializeJson(data.getString("d"), Member.class);
                if (BizLogic.isMe(member.get_id())) {
                    User user = deserializeJson(data.getString("d"), User.class);
                    user.update(eventBus);
                }
                final Member localMember = MainApp.globalMembers.get(member.get_id());
                if (localMember != null) {
                    member.setAlias(localMember.getAlias());
                    member.setHideMobile(localMember.getHideMobile());
                }
                MemberRealm.getInstance().addOrUpdate(member)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Member>() {
                            @Override
                            public void call(Member member) {
                                MainApp.IS_MEMBER_CHANGED = true;
                                BusProvider.getInstance().post(new UpdateMemberEvent());
                            }
                        }, new RealmErrorAction());
                break;
            case MESSAGE_CREATE:
                message = deserializeJson(data.getString("d"), Message.class);
                if (message != null) {
                    if (message.getDisplayMode() != null && message.getDisplayMode().equals(MessageDataProcess.DisplayMode.SPEECH.toString())) {
                        message.setIsRead(false);
                    }
                    MessageRealm.getInstance().addOrUpdate(message)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Message>() {
                                @Override
                                public void call(Message message) {
                                    BusProvider.getInstance().post(new NewMessageEvent(message));
                                }
                            }, new RealmErrorAction());
                    if (BizLogic.isCurrentTeam(message.get_teamId())) {
                        if (!message.isSystem()) {
                            MessageDataProcess.getInstance().updateUnreadNum(message);
                        }
                    } else {
                        if (!message.isSystem() && !BizLogic.isMe(message.get_creatorId())) {
                            JSONObject jObj = data.getJSONObject("d").getJSONObject("notification");
                            if (jObj != null) {
                                eventBus.post(new NewOtherMessageEvent(message.get_teamId(),
                                        jObj.getBoolean("isMute")));
                            }
                        }
                    }
                }
                break;
            case MESSAGE_UNREAD:
                JSONObject unreadItemsObj = data.getJSONObject("d").getJSONObject("unread");
                teamId = data.getJSONObject("d").getString("_teamId");
                Iterator<String> ids = unreadItemsObj.keys();
                while (ids.hasNext()) {
                    String id = ids.next();
                    member = MainApp.globalMembers.get(id);
                    room = MainApp.globalRooms.get(id);
                    if (member != null) {
                        member.setUnread(unreadItemsObj.getInt(id));
                        MemberRealm.getInstance().addOrUpdate(member)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Member>() {
                                    @Override
                                    public void call(Member member) {

                                    }
                                }, new RealmErrorAction());
                        if (BizLogic.isCurrentTeam(teamId)) {
                            MainApp.IS_MEMBER_CHANGED = true;
                            eventBus.post(new UpdateMemberEvent());
                        }
                    }
                    if (room != null) {
                        room.setUnread(unreadItemsObj.getInt(id));
                        room.setState(Room.UPDATE);
                        RoomRealm.getInstance().addOrUpdate(room)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Room>() {
                                    @Override
                                    public void call(Room room) {
                                        MainApp.IS_ROOM_CHANGED = true;
                                        BusProvider.getInstance().post(new UpdateRoomEvent());
                                    }
                                }, new RealmErrorAction());
                        if (BizLogic.isCurrentTeam(teamId)) {
                            MainApp.IS_ROOM_CHANGED = true;
                            eventBus.post(new UpdateRoomEvent());
                        }
                    }
                }
                break;
            case MESSAGE_UPDATE:
                message = deserializeJson(data.getString("d"), Message.class);
                MessageRealm.getInstance().addOrUpdate(message)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Message>() {
                            @Override
                            public void call(Message message) {
                                if (message != null) {
                                    BusProvider.getInstance().post(new UpdateMessageEvent(message));
                                }
                            }
                        }, new RealmErrorAction());
                break;
            case MESSAGE_REMOVE:
                message = deserializeJson(data.getString("d"), Message.class);
                MessageRealm.getInstance().remove(message)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Message>() {
                            @Override
                            public void call(Message msg) {
                                BusProvider.getInstance().post(new DeleteMessageEvent(msg.get_id()));
                            }
                        }, new RealmErrorAction());
                break;
            case MEMBER_UPDATE:
                userId = data.getJSONObject("d").getString("_userId");
                String role = data.getJSONObject("d").getString("role");
                if (BizLogic.isCurrentTeam(data.getJSONObject("d").getString("_teamId"))) {
                    member = MainApp.globalMembers.get(userId);
                    if (member != null) {
                        member.setRole(role);
                        MemberRealm.getInstance().addOrUpdate(member)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Member>() {
                                    @Override
                                    public void call(Member member) {
                                        MainApp.IS_MEMBER_CHANGED = true;
                                        BusProvider.getInstance().post(new UpdateMemberEvent(member));
                                    }
                                }, new RealmErrorAction());
                    }
                }
                break;
            case ROOM_PREFS_UPDATE:
            case ROOM_MEMBERS_PREFS_UPDATE:
                roomId = data.getJSONObject("d").getString("_roomId");
                userId = data.getJSONObject("d").getString("_userId");
                isMute = data.getJSONObject("d").has("isMute") ? data.getJSONObject("d")
                        .optBoolean("isMute") : null;
                if (MainApp.globalRooms.containsKey(roomId) && BizLogic.isMe(userId) && isMute != null) {
                    room = MainApp.globalRooms.get(roomId);
                    room.setIsMute(isMute);
                    RoomRealm.getInstance().addOrUpdate(room)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Room>() {
                                @Override
                                public void call(Room room) {
                                    MainApp.IS_ROOM_CHANGED = true;
                                    BusProvider.getInstance().post(new UpdateRoomEvent());
                                }
                            }, new RealmErrorAction());
                }
                break;
            case INVITATION_CREATE:
                invitation = deserializeJson(data.getString("d"), Invitation.class);
                if (invitation != null && BizLogic.isCurrentTeam(invitation.get_teamId())) {
                    InvitationRealm.getInstance().addOrUpdate(invitation)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Invitation>() {
                                @Override
                                public void call(Invitation invitation) {
                                    MainApp.IS_MEMBER_CHANGED = true;
                                    BusProvider.getInstance().post(new UpdateMemberEvent());
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {

                                }
                            });
                }
                break;
            case INVITATION_REMOVE:
                invitation = deserializeJson(data.getString("d"), Invitation.class);
                if (invitation != null && BizLogic.isCurrentTeam(invitation.get_teamId())) {
                    InvitationRealm.getInstance().remove(invitation);
                }
                break;
            case NOTIFICATION_CREATE:
            case NOTIFICATION_UPDATE:
                notification = deserializeJson(data.getString("d"), Notification.class);
                if (notification != null) {
                    if (notification.getIsHidden() != null && notification.getIsHidden()) {
                        NotificationRealm.getInstance()
                                .remove(notification.get_id())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Notification>() {
                                    @Override
                                    public void call(Notification notification) {
                                        if (notification != null) {
                                            BusProvider.getInstance().post(new RemoveNotificationEvent(notification));
                                        }
                                    }
                                }, new RealmErrorAction());
                    } else {
                        NotificationRealm.getInstance().addOrUpdate(notification)
                                .subscribe(new EmptyAction<Notification>(), new RealmErrorAction());
                        if (BizLogic.isCurrentTeam(notification.get_teamId())) {
                            BusProvider.getInstance().post(new UpdateNotificationEvent(notification));
                        }
                    }
                }
                break;
            case NOTIFICATION_REMOVE:
                NotificationRealm.getInstance()
                        .remove(data.getJSONObject("d").getString("_id"))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Notification>() {
                            @Override
                            public void call(Notification notification) {
                                if (notification != null) {
                                    BusProvider.getInstance().post(new RemoveNotificationEvent(notification));
                                }
                            }
                        }, new RealmErrorAction());
                break;
            case STORY_UPDATE:
            case STORY_CREATE:
                final Story story = deserializeJson(data.getString("d"), Story.class);
                if (story != null) {
                    StoryRealm.getInstance().addOrUpdate(story)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Story>() {
                                @Override
                                public void call(Story story) {
                                    BusProvider.getInstance().post(new UpdateStoryEvent(story));
                                }
                            }, new RealmErrorAction());
                }
                break;
            case STORY_REMOVE:
                final Story removeStory = deserializeJson(data.getString("d"), Story.class);
                if (removeStory != null) {
                    StoryRealm.getInstance().remove(removeStory.get_id())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Story>() {
                                @Override
                                public void call(Story story) {
                                    BusProvider.getInstance().post(new RemoveStoryEvent(removeStory));
                                }
                            }, new RealmErrorAction());
                }
                break;
            case GROUP_CREATE:
            case GROUP_UPDATE:
                final Group group = deserializeJson(data.getString("d"), Group.class);
                if (group != null) {
                    GroupRealm.getInstance().addOrUpdate(group)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Group>() {
                                @Override
                                public void call(Group g) {
                                    BusProvider.getInstance().post(new UpdateGroupEvent(group));
                                }
                            }, new RealmErrorAction());
                }
                break;
            case GROUP_REMOVE:
                final Group removeGroup = deserializeJson(data.getString("d"), Group.class);
                if (removeGroup != null) {
                    GroupRealm.getInstance().remove(removeGroup)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Object>() {
                                @Override
                                public void call(Object o) {
                                    BusProvider.getInstance().post(new RemoveGroupEvent(removeGroup));
                                }
                            }, new RealmErrorAction());
                }

                break;
            case ACTIVITY_CREATE: {
                final TeamActivity activity = deserializeJson(data.getString("d"), TeamActivity.class);
                BusProvider.getInstance().post(new NewTeamActivityEvent(activity));
                break;
            }
            case ACTIVITY_REMOVE: {
                final TeamActivity activity = deserializeJson(data.getString("d"), TeamActivity.class);
                BusProvider.getInstance().post(new RemoveTeamActivityEvent(activity));
                break;
            }
            case ACTIVITY_UPDATE: {
                final TeamActivity activity = deserializeJson(data.getString("d"), TeamActivity.class);
                BusProvider.getInstance().post(new UpdateTeamActivityEvent(activity));
                break;
            }
            default:
                break;
        }
    }

    private <T> T deserializeJson(String json, Class<T> clazz) {
        T result = null;
        try {
            result = gson.fromJson(json, clazz);
        } catch (Exception e) {
            Logger.e(TAG, "deserialize fail", e);
        }
        return result;
    }

}
