package com.teambition.talk.realm;

import com.teambition.talk.BizLogic;
import com.teambition.talk.entity.Draft;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by wlanjie on 16/1/5.
 */
public class DraftRealm extends AbstractRealm<Draft> {

    private static DraftRealm realm;

    private DraftRealm() {

    }

    public static DraftRealm getInstance() {
        if (realm == null) {
            realm = new DraftRealm();
        }
        return realm;
    }

    public Observable<Object> addOrUpdate(final Draft draft) {
         return Observable.create(new OnSubscribeRealm<Object>() {
            @Override
            public Object get(Realm realm) {
                draft.setUpdatedAtTime(draft.getUpdatedAt().getTime());
                Draft realmDraft = realm.where(Draft.class).equalTo(Draft.ID, draft.get_id()).findFirst();
                if (realmDraft == null) {
                    realm.copyToRealmOrUpdate(draft);
                    return draft;
                }
                if (draft.getContent().equals(realmDraft.getContent())) {
                    realm.copyToRealmOrUpdate(draft);
                    return null;
                }
                realm.copyToRealmOrUpdate(draft);
                return draft;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<List<Draft>> getDrafts() {
        return Observable.create(new OnSubscribeRealm<List<Draft>>() {
            @Override
            public List<Draft> get(Realm realm) {
                RealmResults<Draft> realmResults = realm.where(Draft.class).equalTo(Draft.TEAM_ID, BizLogic.getTeamId()).findAll();
                List<Draft> drafts = new ArrayList<>(realmResults.size());
                for (Draft realmResult : realmResults) {
                    Draft outline = new Draft();
                    outline.set_id(realmResult.get_id());
                    outline.setContent(realmResult.getContent());
                    outline.setUpdatedAt(new Date(realmResult.getUpdatedAtTime()));
                    drafts.add(outline);
                }
                return drafts;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Draft> getDraft(final String id) {
        return Observable.create(new OnSubscribeRealm<Draft>() {
            @Override
            public Draft get(Realm realm) {
                Draft realmDraft = realm.where(Draft.class)
                        .equalTo(Draft.TEAM_ID, BizLogic.getTeamId())
                        .equalTo(Draft.ID, id).findFirst();
                if (realmDraft != null) {
                    Draft outline = new Draft();
                    outline.set_id(id);
                    outline.setContent(realmDraft.getContent());
                    outline.setUpdatedAt(new Date(realmDraft.getUpdatedAtTime()));
                    return outline;
                }
                return null;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Object> remove(final String id) {
        return Observable.create(new OnSubscribeRealm<Object>() {
            @Override
            public Object get(Realm realm) {
                Draft draft = realm.where(Draft.class).equalTo(Draft.ID, id).findFirst();
                if (draft != null) {
                    draft.removeFromRealm();
                    return id;
                }
                return null;
            }
        }).subscribeOn(Schedulers.io());
    }
}
