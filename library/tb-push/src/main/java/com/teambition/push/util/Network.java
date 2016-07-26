package com.teambition.push.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by wlanjie on 15/7/8.
 */
public class Network {

    /**
     * 获取网络类型
     * @param context
     * @return 网络类型  -1: 没有网络, 0:net网络, 1: wifi网络
     */
    public static int getAPNType(Context context) {
        int netType = -1;
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if(networkInfo == null) {
            return netType;
        }
        int type = networkInfo.getType();
        if(type == ConnectivityManager.TYPE_MOBILE) {
            netType = ConnectivityManager.TYPE_MOBILE ;
        }
        if(type == ConnectivityManager.TYPE_WIFI) {
            netType = ConnectivityManager.TYPE_WIFI ;
        }
        return netType;
    }

    /**
     * 判断是否有网络连接
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        return mNetworkInfo != null && mNetworkInfo.isAvailable();
    }

    /**
     * 判断mobile网络是否可用
     * @param context
     * @return
     */
    public static boolean isMobileConnected(Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mMobileNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return mMobileNetworkInfo != null && mMobileNetworkInfo.isAvailable();
    }

    /**
     * 判断wifi网络是否可用
     * @param context
     * @return
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWiFiNetworkInfo != null && mWiFiNetworkInfo.isAvailable();
    }
}
