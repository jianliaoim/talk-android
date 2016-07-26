package com.teambition.push.util;

import android.content.Context;
import android.telephony.TelephonyManager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

/**
 * Created by nlmartian on 8/12/15.
 */
public class DeviceUtil {
    public static String getDeviceId(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        String macAddress = getMacAddress();
        return deviceId + "-" + macAddress;
    }

    private static String getMacAddress() {
        String macSerial = null;
        String str = "";
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
}
