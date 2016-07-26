package com.teambition.talk.realm;

import com.teambition.talk.MainApp;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.rx.RealmObservableFactory;

/**
 * Created by zeatual on 15/10/8.
 * <p/>
 * 需要更新Realm数据库时,首先需要先升级SCHEMA_VERSION,在SCHEMA_VERSION原有的值上+1
 * 然后需要给RealmConfiguration设置migration(new Migration()),类Migration是需要
 * 实现RealmMigration,这个类控制Realm版本.
 */
public class RealmConfig {

    private static final String TALK_REALM = "Talk.realm";
    private static final int SCHEMA_VERSION = 2;

    private static RealmConfiguration realmConfiguration;

    public static RealmConfiguration getTalkRealm() {
        if (realmConfiguration == null) {
            realmConfiguration = new RealmConfiguration.Builder(MainApp.CONTEXT)
                    .name(TALK_REALM)
                    .schemaVersion(SCHEMA_VERSION)
                    .migration(new Migration())
                    .rxFactory(new RealmObservableFactory())
                    .build();
        }
        return realmConfiguration;
    }

    public static void deleteRealm() {
        try {
            Realm.deleteRealm(realmConfiguration);
        } catch (Exception e) {

        }
    }
}
