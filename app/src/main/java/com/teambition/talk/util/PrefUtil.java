package com.teambition.talk.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.teambition.talk.GsonProvider;

/**
 * Created by jgzhu on 5/7/14.
 */
public class PrefUtil {
    public static final String PREFERENCE_NAME = "preference";

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    private Gson mGson;
    private Context mContext;

    private PrefUtil(Context context) {
        mPreferences = context.getSharedPreferences(PrefUtil.PREFERENCE_NAME, Context.MODE_PRIVATE);
        mEditor = mPreferences.edit();
        mGson = GsonProvider.getGson();
        mContext = context;
    }

    public static PrefUtil make(Context context) {
        return new PrefUtil(context);
    }

    public String getString(String key, String defaultValue) {
        return mPreferences.getString(key, defaultValue);
    }

    public String getString(String key) {
        return mPreferences.getString(key, "");
    }

    public PrefUtil putString(String key, String value) {
        mEditor.putString(key, value).apply();
        return this;
    }

    public PrefUtil putBoolean(String key, Boolean value) {
        mEditor.putBoolean(key, value).apply();
        return this;
    }

    public int getInt(String key, int defaultValue) {
        return mPreferences.getInt(key, defaultValue);
    }

    public PrefUtil putInt(String key, int i) {
        mEditor.putInt(key, i).apply();
        return this;
    }

    public Boolean getBoolean(String key, Boolean defaultValue) {
        return mPreferences.getBoolean(key, defaultValue);
    }

    public Boolean getBoolean(String key) {
        return mPreferences.getBoolean(key, false);
    }

    public PrefUtil putObject(String key, Object object) {
        String json = mGson.toJson(object);
        return putString(key, json);
    }

    public PrefUtil removeObject(String key) {
        mPreferences.edit().remove(key).apply();
        return this;
    }

    public Object getObject(String key, Class<?> clazz) {
        Object object = null;
        if (StringUtil.isNotBlank(getString(key))) {
            object = mGson.fromJson(getString(key), clazz);
        }
        return object;
    }

    public void clear() {
        mEditor.clear();
        mEditor.commit();
    }

}
