package com.teambition.talk.realm;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.exceptions.RealmMigrationNeededException;

/**
 * Created by wlanjie on 15/10/12.
 */
public class RealmProvider {

    public static Realm getInstance() {
        return Realm.getInstance(RealmConfig.getTalkRealm());
    }
}
