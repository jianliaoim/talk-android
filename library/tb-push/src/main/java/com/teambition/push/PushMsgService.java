package com.teambition.push;

import android.app.AlarmManager;
import android.app.NotificationManager;
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
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.nkzawa.engineio.parser.Packet;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.teambition.push.util.DeviceUtil;
import com.teambition.push.util.DispatchQueue;
import com.teambition.push.util.Logger;
import com.teambition.push.util.Network;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by nlmartian on 5/21/15.
 */
public class PushMsgService extends Service {

    public static final String TAG = PushMsgService.class.getSimpleName();
    public static boolean LOG_FILE = false;

    public static final String EXTRA_DEBUG = "extra_debug";
    public static final String ACTION_START_SERVICE = "com.teambition.push.ACTION_START_SERVICE";
    public static final String ACTION_DISCONNECT = "com.teambition.push.ACTION_DISCONNECT";
    public static final String ACTION_CONNECT = "com.teambition.push.ACTION_CONNECT";
    public static final String ACTION_RECEIVE = "com.teambition.push.ACTION_RECEIVE";
    public static final String ACTION_RETRY = "com.teambition.push.ACTION_RETRY";
    public static final String META_APP_KEY = "WHISPER-APP-KEY";
    public static final String PREF_NAME = "whisper_pref";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_IS_SHUTDOWN = "is_shutdown";
    public static final String PREF_EXPIRED_AT = "expire_at";

    public static final int ACTION_CODE_RETRY = 33009;
    public static final long MIM_RETRY_DELAY_TIMEOUT = 10 * 1000;
    public static final long MAX_RETRY_DELAY_TIMEOUT = 160 * 1000;

//    private static final String REGISTER_URL = "http://ps.project.ci/v1/users/register";
    private static final String REGISTER_URL = "http://ps.project.ci/v1/users/register";

//private static final String WEBSOCKET_URL = "ws://ps.project.ci/engine.io/";
    private static final String WEBSOCKET_URL = "ws://ps.project.ci/engine.io/";

    private BroadcastReceiver retryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "service");
            wakeLock.acquire();

            String userId = intent.getStringExtra("userId");
            String deviceToken = intent.getStringExtra("deviceToken");
            openSocket(userId, deviceToken);

            wakeLock.release();
        }
    };

    public class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (msg.obj == null) {
                    PushMsgService.this.sendRegistered(1, null);
                } else {
                    String userId = (String) msg.obj;
                    PushMsgService.this.sendRegistered(0, userId);
                }
            } else if (msg.what == 2) {
                PushMsgService.this.sendMessage((String) msg.obj);
            }
            super.handleMessage(msg);
        }
    }

    public class Binder extends android.os.Binder {
        public PushMsgService getService() {
            return PushMsgService.this;
        }
    }

    private final IBinder binder = new Binder();

    NotificationManager mNM;

    private Socket mSocket;

    private long retryDelayInMilli = MIM_RETRY_DELAY_TIMEOUT;

    private boolean isDisconnect = false;

    private MessageHandler messageHandler;

    public static Intent startIntent(Context context) {
        Intent intent = new Intent(context, PushMsgService.class);
        return intent;
    }

    public static Intent startIntent(Context context, String action) {
        Intent intent = new Intent(context, PushMsgService.class);
        intent.setAction(action);
        return intent;
    }

    FileOutputStream mOutputStream;
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");

    private String appKey = null;
    private String deviceID = null;

    private Random random = null;
    private HashSet<Long> idSet;
    private volatile DispatchQueue rpcQueue = new DispatchQueue("RpcQueue");

    @Override
    public void onCreate() {
        super.onCreate();
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        if (LOG_FILE) {
            File logFile = new File(Environment.getExternalStorageDirectory() + "/" + "push_log_" + dateFormat.format(new Date()) + ".txt");
            try {
                mOutputStream = new FileOutputStream(logFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        appKey = readMeta(META_APP_KEY);
        if (TextUtils.isEmpty(appKey)) {
            Logger.e(TAG, "read app key failed", new IllegalStateException());
        }

        deviceID = DeviceUtil.getDeviceId(this);

        random = new Random(System.currentTimeMillis());

        idSet = new HashSet<>();

        IntentFilter intentFilter = new IntentFilter(ACTION_RETRY);
        registerReceiver(retryReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(retryReceiver);
        } catch (Exception e) {
        }
        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "service");
        wakeLock.acquire();

        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        if (intent != null && ACTION_START_SERVICE.equals(intent.getAction())) {
            pref.edit().putBoolean(PREF_IS_SHUTDOWN, false).commit();
        }
        if (intent != null && ACTION_DISCONNECT.equals(intent.getAction())) {
            try {
                unregisterReceiver(retryReceiver);
            } catch (Exception e) {
                Logger.e(TAG, "receiver not registered", e);
            }
            disconnectSocket();
            pref.edit().putBoolean(PREF_IS_SHUTDOWN, true).commit();
            stopSelf();
            return START_NOT_STICKY;
        } else {
            isDisconnect = false;
            if (messageHandler == null) {
                synchronized (PushMsgService.class) {
                    if (messageHandler == null) {
                        messageHandler = new MessageHandler();
                    }
                }
            }

            String userId = pref.getString(PREF_USER_ID, "");
            boolean isShutdown = pref.getBoolean(PREF_IS_SHUTDOWN, false);
            if (intent != null) {
                LOG_FILE = intent.getBooleanExtra(EXTRA_DEBUG, false);
            }
            if (!isShutdown) {
                register(appKey, deviceID, userId);
            }
        }

        wakeLock.release();
        return START_STICKY;
    }

    private void disconnectSocket() {
        if (mSocket != null) {
            try {
                mSocket.close();
                mSocket = null;
                isDisconnect = true;
            } catch (Exception e) {
            }
        }
    }

    private void register(String appKey, String deviceId, String userId) {
        if (mSocket != null) {
            return;
        }

        MediaType FORM_URLENCODED = MediaType.parse("application/x-www-form-urlencoded");
        String requestBody = String.format("appKey=%s&deviceToken=%s", appKey, deviceId);
        if (!TextUtils.isEmpty(userId)) {
            requestBody += ("&userId=" + userId);
        }
        OkHttpClient okClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(REGISTER_URL)
                .post(RequestBody.create(FORM_URLENCODED, requestBody))
                .build();
        okClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                messageHandler.sendMessage(messageHandler.obtainMessage(1, null));
                Logger.e(TAG, "register failed", e);
                if (LOG_FILE) {
                    writeFileLog("" + e.toString());
                }
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String resStr = response.body().string();
                if (LOG_FILE) {
                    writeFileLog("register: " + resStr);
                }

                try {
                    JSONObject json = new JSONObject(resStr);
                    String userId = json.optString("userId");
                    String expiredAt = json.optString("expiredAt");

                    SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(PREF_USER_ID, userId);
                    editor.putString(PREF_EXPIRED_AT, expiredAt);
                    editor.commit();

                    messageHandler.sendMessage(messageHandler.obtainMessage(1, userId));
                    if (!TextUtils.isEmpty(userId)) {
                        openSocket(userId, deviceID);
                        Logger.d(TAG, "userId: " + userId);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void openSocket(String userId, String deviceToken) {
        if (isDisconnect) return;
        if (mSocket == null) {
            synchronized (PushMsgService.class) {
                if (mSocket == null) {
                    try {
                        String query = "userId=" + userId + "&deviceToken=" + deviceToken;
                        mSocket = new Socket(WEBSOCKET_URL + "?" + query);
                        setSocketOperation(userId, deviceToken);
                        mSocket.setContext(this);
                        mSocket.open();
                    } catch (Exception e) {
                        e.printStackTrace();
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

    private void setSocketOperation(final String userId, final String deviceToken) {
        try {
            mSocket.on(Socket.EVENT_OPEN, new Emitter.Listener() {
                @Override
                public void call(Object... objects) {
                    retryDelayInMilli = MIM_RETRY_DELAY_TIMEOUT;

                    // TODO: print log
                    Logger.d(TAG, "userId: " + userId + " connected " + dateFormat.format(new Date()));
                    if (LOG_FILE) {
                        writeFileLog("userId: " + userId + " connected " + dateFormat.format(new Date()));
                    }
                }
            }).on(Socket.EVENT_MESSAGE, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    String data = (String) args[0];

                    try {
                        JSONObject jsonData = new JSONObject(data);
                        final String id = jsonData.optString("id");

                        // send message receipt to server
                        rpcQueue.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                final JSONObject jsonReceipt = new JSONObject();
                                try {
                                    JSONArray arrayArgs = new JSONArray();
                                    arrayArgs.put(id);
                                    jsonReceipt.put("args", arrayArgs);
                                    jsonReceipt.put("cid", id);
                                    mSocket.send(jsonReceipt.toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // TODO: receive message
                    Date date = new Date();
                    Logger.d(TAG, data + " " + dateFormat.format(date));
                    if (LOG_FILE) {
                        writeFileLog(data + " " + dateFormat.format(date));
                    }
                    messageHandler.sendMessage(messageHandler.obtainMessage(2, data));
                }
            }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... objects) {
                    // TODO: print log
                    Exception err = (Exception) objects[0];
                    Logger.d(TAG, "socketId: " + mSocket.id() + " " + err.getCause().toString() + " " + dateFormat.format(new Date()));
                    if (LOG_FILE) {
                        writeFileLog("socketId: " + mSocket.id() + " " + err.getCause().toString() + " " + dateFormat.format(new Date()));
                    }
                }
            }).on(Socket.EVENT_CLOSE, new Emitter.Listener() {
                @Override
                public void call(Object... args) {

                    // TODO: print log
                    Logger.d(TAG, "close");
                    if (LOG_FILE) {
                        writeFileLog("close " + dateFormat.format(new Date()));
                    }

                    mSocket = null;
                    if (!Network.isNetworkConnected(getApplicationContext())) {
                        return;
                    }

                    retryConnect();
                }
            }).on(Socket.EVENT_PACKET_CREATE, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    // TODO:
                    if (LOG_FILE) {
                        Packet packet = (Packet) args[0];
                        if (packet.type.equals(Packet.PING)) {
                            writeFileLog("========PING========" + dateFormat.format(new Date()));
                        }
                    }
                }
            }).on(Socket.EVENT_HEARTBEAT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    if (LOG_FILE) {
                        writeFileLog("========PONG========" + dateFormat.format(new Date()));
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void retryConnect() {
        Intent retryIntent = new Intent(ACTION_RETRY);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(PushMsgService.this,
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

    private void sendRegistered(int code, String userId) {
        Intent intent = new Intent(ACTION_CONNECT);
        intent.putExtra("error", code);
        intent.putExtra("userId", userId);
        String permission = getPackageName() + ".permission.WHISPER";
        sendBroadcast(intent, permission);
    }

    private void sendMessage(String content) {
        Intent intent = new Intent(ACTION_RECEIVE);
        intent.putExtra("message", content);
        String permission = getPackageName() + ".permission.WHISPER";
        sendBroadcast(intent, permission);
    }

    private void writeFileLog(String content) {
        try {
            mOutputStream.write((content + "\n").getBytes());
            mOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
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
}
