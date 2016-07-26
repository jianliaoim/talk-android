package com.teambition.talk.realm;

import org.parceler.converter.CollectionParcelConverter;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by wlanjie on 15/10/14.
 */
public abstract class RealmListParcelConverter<T extends RealmObject> extends CollectionParcelConverter<T, RealmList<T>> {

    @Override
    public RealmList<T> createCollection() {
        return new RealmList<>();
    }
}
