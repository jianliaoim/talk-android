package com.teambition.push.util;

import android.util.Log;

/**
 * Created by jgzhu on 10/31/14.
 */
public class Logger {
    private static final boolean IS_DEBUG = false;

    public static void v(String tag, Object obj) {
        if (IS_DEBUG) {
            String content = obj == null ? "null" : obj.toString();
            Log.v(tag, content);
        }
    }

    public static void i(String tag, Object obj) {
        if (IS_DEBUG) {
            String content = obj == null ? "null" : obj.toString();
            Log.i(tag, content);
        }
    }

    public static void d(String tag, Object obj) {
        if (IS_DEBUG) {
            String content = obj == null ? "null" : obj.toString();
            Log.d(tag, content);
        }
    }

    public static void w(String tag, Object obj) {
        if (IS_DEBUG) {
            String content = obj == null ? "null" : obj.toString();
            Log.w(tag, content);
        }
    }

    public static void e(String tag, Object obj, Throwable e) {
        if (IS_DEBUG) {
            String content = obj == null ? "null" : obj.toString();
            Log.e(tag, content, e);
        }
    }
}
