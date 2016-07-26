package com.teambition.talk.util;

import android.content.Context;
import android.os.Build;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.teambition.talk.BuildConfig;
import com.teambition.talk.Constant;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nlmartian on 12/3/15.
 */
public class AnalyticsHelper {

    private Map<String, Long> timingStartTimeMap = new HashMap<>();

    public enum Category {
        login("login "), rookie("rookie"), switch_team("switch team"),
        start_talk("start talk"), page_elements("page elements"), team("team"),
        retention("retention");

        private String name;

        private Category(String name) {
            this.name = name;
        }
    }

    private Tracker googleTracker;
    private MixpanelAPI mixpanel;

    private final static AnalyticsHelper instance = new AnalyticsHelper();

    private AnalyticsHelper() {

    }

    public static AnalyticsHelper getInstance() {
        return instance;
    }

    public void init(Context context) {
        GoogleAnalytics googleAnalytics = GoogleAnalytics.getInstance(context);
        googleTracker = googleAnalytics.newTracker(R.xml.google_analytics);

        mixpanel = MixpanelAPI.getInstance(context, Constant.MIXPANEL_TOKEN);
        JSONObject props = new JSONObject();
        try {
            props.put("platform", "android");
        } catch (JSONException e) {
        }
        mixpanel.registerSuperProperties(props);
    }

    public void flushEvent() {
        mixpanel.flush();
    }

    public void setUid(String uid) {
        mixpanel.getPeople().identify(uid);
    }

    public void startTiming(Category category, String action) {
        if (shouldSendEvent()) return;

        timingStartTimeMap.put(category.name + action, System.currentTimeMillis());

        mixpanel.timeEvent(action);
    }

    public void endTiming(Category category, String action) {
        if (shouldSendEvent()) return;

        long startTime = timingStartTimeMap.containsKey(category + action)
                ? timingStartTimeMap.get(category + action) : System.currentTimeMillis();
        long interval = System.currentTimeMillis() - startTime;

        googleTracker.send(new HitBuilders.TimingBuilder()
                .setCategory(category.name)
                .setValue(interval)
                .setVariable(action)
                .build());

        sendMixpanelEvent(category.name(), action, null);
    }

    public void sendEvent(Category category, String action, String label) {
        if (shouldSendEvent()) return;

        // google analytics
        sendGAEvent(category.name, action, label);

        // mixpanel
        sendMixpanelEvent(category.name, action, label);
    }

    private boolean shouldSendEvent() {
        return MainApp.BUILD_TYPE == MainApp.DEBUG;
    }

    private void sendGAEvent(String category, String action, String label) {
        try {
            HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder();
            builder.setCategory(category)
                    .setAction(action);
            if (StringUtil.isNotBlank(label)) {
                builder.setLabel(label);
            }
            googleTracker.send(builder.build());
        } catch (Exception e) {
            Logger.e("AnalyticsHelper", "parse categoryId failed", e.getCause());
        }
    }

    private void sendMixpanelEvent(String category, String action, String label) {
        try {
            JSONObject props = new JSONObject();
            props.put("category", category);
            if (StringUtil.isNotBlank(label)) {
                props.put("label", label);
            }
            mixpanel.track(action, props);
        } catch (JSONException e) {
        }
    }

}
