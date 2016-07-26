package com.teambition.talk.util;

import android.annotation.SuppressLint;

import com.teambition.talk.BizLogic;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wlanjie on 15/9/15.
 */
public class CallUtil {

    public static String getCallSignature(String subAccountSid, String subAccountToken) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = format.format(new Date());
        String signature = subAccountSid + subAccountToken + timestamp;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return byte2HexStr(md.digest(signature.getBytes("utf-8")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    private static String byte2HexStr(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; ++i) {
            String s = Integer.toHexString(b[i] & 0xFF);
            if (s.length() == 1)
                sb.append("0");

            sb.append(s.toUpperCase());
        }
        return sb.toString();
    }

    public static String getAuthorization(String accountSid) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = format.format(new Date());
        return new BASE64Encoder().encode((accountSid + ":" + timestamp).getBytes());
    }
}
