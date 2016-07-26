package com.teambition.talk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.squareup.okhttp.OkHttpClient;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Room;
import com.teambition.talk.imageloader.OkHttpImageDownloader;
import com.teambition.talk.receiver.NetworkStateReceiver;
import com.teambition.talk.receiver.ScreenLockReceiver;
import com.teambition.talk.util.AnalyticsHelper;
import com.teambition.talk.util.FileUtil;
import com.teambition.talk.util.PrefUtil;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.imageloader.ThumbnailDecoder;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zeatual on 14-10-10.
 */
public class MainApp extends MultiDexApplication {

    public static int RELEASE = 0;
    public static int BETA = 1;
    public static int DEBUG = 2;

    public static int BUILD_TYPE = BuildConfig.BUILD_TYPE_INT;

    public static Context CONTEXT;
    public static PrefUtil PREF_UTIL;
    public static ImageLoader IMAGE_LOADER;
    public static IWXAPI WX_API;

    // global data
    public static Map<String, Member> globalMembers = new HashMap<>();
    public static Map<String, Room> globalRooms = new HashMap<>();

    public static boolean IS_SCREEN_LOCK = false;
    public static boolean IS_SCREEN_ON = true;
    public static boolean IS_SYNCING = false;
    public static boolean IS_LEAVE_MEMBER_SYNCING = false;
    public static boolean DATA_IS_READY = false;
    public static boolean IS_MEMBER_CHANGED = true;
    public static boolean IS_ROOM_CHANGED = true;
    public static MemoryCache memoryCache = new LruMemoryCache(2 * 1024 * 1024);
    private static NetworkStateReceiver networkStateReceiver = new NetworkStateReceiver();
    private static ScreenLockReceiver screenLockReceiver = new ScreenLockReceiver();

    public static String TALK_BUSINESS_CALL = "";

    private int activityCount;

    @Override
    public void onCreate() {
        MultiDex.install(getApplicationContext());
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
                activityCount++;
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
                activityCount--;
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });


        CONTEXT = this;

        // init SharedPreference
        PREF_UTIL = PrefUtil.make(this);

        // listen network state change
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkStateReceiver, mFilter);

        // listen screen state
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(screenLockReceiver, filter);

        // init client
        String accessToken = PREF_UTIL.getString(Constant.ACCESS_TOKEN);
        if (StringUtil.isNotBlank(accessToken)) {
            TalkClient.getInstance().setAccessToken(accessToken);
        }
        if ("CN".equals(getResources().getConfiguration().locale.getCountry())
                || "TW".equals(getResources().getConfiguration().locale.getCountry())) {
            TalkClient.getInstance().setLanguage("zh");
        } else {
            TalkClient.getInstance().setLanguage("en");
        }

        // load sound effect
        MediaController.getInstance();

        // init ImageLoader
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .imageDecoder(new ThumbnailDecoder(getContentResolver(), new BaseImageDecoder(false)))
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .diskCacheSize(20 * 1024 * 1024)
                .diskCacheFileCount(50)
                .imageDownloader(new OkHttpImageDownloader(this, new OkHttpClient()))
                .build();
        IMAGE_LOADER = ImageLoader.getInstance();
        IMAGE_LOADER.init(config);

        // init wechat api
        WX_API = WXAPIFactory.createWXAPI(this, Constant.WECHAT_APP_ID);
        WX_API.registerApp(Constant.WECHAT_APP_ID);

        initDir();

        AnalyticsHelper.getInstance().init(this);
    }

    private void initDir() {
        FileUtil.createDirIfNotExisted(Constant.FILE_DIR_CACHE, true);
        FileUtil.createDirIfNotExisted(Constant.FILE_DIR_COMPRESSED, true);
        FileUtil.createDirIfNotExisted(Constant.AUDIO_DIR, true);
        FileUtil.createDirIfNotExisted(Constant.FILE_DIR_DOWNLOAD, false);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(networkStateReceiver);
        unregisterReceiver(screenLockReceiver);
    }

    public int getActivityCount() {
        return activityCount;
    }

    public static void showToastMsg(int resId) {
        Observable.just(resId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer result) {
                        Toast.makeText(CONTEXT, result, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static void showToastMsg(String msg) {
        Observable.just(msg)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String result) {
                        Toast.makeText(CONTEXT, result, Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
