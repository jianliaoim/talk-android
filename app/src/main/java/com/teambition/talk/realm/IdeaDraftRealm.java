package com.teambition.talk.realm;

import com.teambition.talk.entity.IdeaDraft;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by wlanjie on 16/3/7.
 */
public class IdeaDraftRealm {

    private static IdeaDraftRealm realm;

    public static IdeaDraftRealm getInstance() {
        if (realm == null) {
            realm = new IdeaDraftRealm();
        }
        return realm;
    }

    public Observable<Object> add(final IdeaDraft draft) {
        return Observable.create(new OnSubscribeRealm<Object>() {
            @Override
            public Object get(Realm realm) {
                IdeaDraft ideaDraft = realm.where(IdeaDraft.class).findFirst();
                if (ideaDraft != null) {
                    ideaDraft.removeFromRealm();
                }
                realm.copyToRealm(draft);
                return draft;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<IdeaDraft> getIdeaDraft() {
        return Observable.create(new OnSubscribeRealm<IdeaDraft>() {
            @Override
            public IdeaDraft get(Realm realm) {
                RealmResults<IdeaDraft> drafts = realm.where(IdeaDraft.class).findAll();
                IdeaDraft ideaDraft = new IdeaDraft();
                for (IdeaDraft draft : drafts) {
                    if (draft != null) {
                        ideaDraft.setTitle(draft.getTitle());
                        ideaDraft.setDescription(draft.getDescription());
                        return ideaDraft;
                    }
                }
                return null;
            }
        }).subscribeOn(Schedulers.io());
    }
}
