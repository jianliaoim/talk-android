package com.teambition.talk;

import com.facebook.stetho.Stetho;

/**
 * Created by nlmartian on 4/21/15.
 */
public class DebugApp extends MainApp {
    @Override
    public void onCreate() {
        super.onCreate();

        Stetho.initialize(Stetho.newInitializerBuilder(this)
                .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                .build());
    }
}
