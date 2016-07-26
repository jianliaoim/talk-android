package com.teambition.talk.util;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by nlmartian on 8/12/15.
 */
public class DeviceUtil {
    public static final String TAG = DeviceUtil.class.getSimpleName();

    public static String getDeviceId(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = "";
        try {
            deviceId = tm.getDeviceId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String macAddress = getMacAddress();
        return deviceId + "-" + macAddress;
    }

    private static String getMacAddress() {
        String macSerial = null;
        String str = "";
        String wifiInterface = SystemInfo.getInstance().getProperty("wifi.interface");
        if (wifiInterface == null) wifiInterface = "wlan0";
        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str;) {
                str = input.readLine();
                if (str != null) {
                    str = str.trim();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < str.length(); i++) {
                        if (str.charAt(i) != ':') {
                            sb.append(str.charAt(i));
                        }
                    }
                    macSerial = sb.toString();
                    break;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return macSerial;
    }

    public static class SystemInfo{
        private Method methodGetProperty;
        private static SystemInfo instance = new SystemInfo();

        public static SystemInfo getInstance() {
            return instance;
        }

        private SystemInfo(){
            Class classSystemProperties = null;
            try {
                classSystemProperties = Class.forName("android.os.SystemProperties");
                methodGetProperty = classSystemProperties.getMethod("get", String.class);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        public String getProperty(String property) {
            if(methodGetProperty == null) return null;
            try {
                return (String)methodGetProperty.invoke(null, property);
            } catch(IllegalAccessException e) {
                Log.w(TAG, "Failed to get property");
            } catch(InvocationTargetException e) {
                Log.w(TAG, "Exception thrown while getting property");
            }
            return null;
        }

    }
}