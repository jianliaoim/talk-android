package com.teambition.talk.realm;

import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.MainApp;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.entity.Invitation;
import com.teambition.talk.entity.Member;
import com.teambition.talk.event.UpdateMemberEvent;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by wlanjie on 15/10/13.
 */
public class InvitationRealm extends AbstractRealm {

    private static InvitationRealm realm;

    public static InvitationRealm getInstance() {
        if (realm == null) {
            realm = new InvitationRealm();
        }
        return realm;
    }

    public List<Invitation> getInvitationWithCurrentThread() {
        final List<Invitation> invitations = new ArrayList<>();
        final Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            RealmResults<Invitation> realmResults = realm.where(Invitation.class)
                    .equalTo(Invitation.TEAM_ID, BizLogic.getTeamId())
                    .findAll();
            for (Invitation realmResult : realmResults) {
                Invitation invitation = new Invitation();
                copy(invitation, realmResult);
                invitations.add(invitation);
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
        return invitations;
    }

    public void addOrUpdateWithCurrentThread(final Invitation invitation) {
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            Invitation realmInvitation = realm.where(Invitation.class)
                    .equalTo(Invitation.TEAM_ID, BizLogic.getTeamId())
                    .equalTo(Invitation.KEY, invitation.getKey())
                    .findFirst();
            if (realmInvitation == null) {
                realmInvitation = realm.createObject(Invitation.class);
            }
            copy(realmInvitation, invitation);
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
    }

    public Observable<Invitation> addOrUpdate(final Invitation invitation) {
        return Observable.create(new OnSubscribeRealm<Invitation>() {
            @Override
            public Invitation get(Realm realm) {
                copy(invitation, invitation);
                realm.copyToRealmOrUpdate(invitation);
                return invitation;
            }
        }).subscribeOn(Schedulers.io());
    }

    public void removeWithCurrentThread() {
        Realm realm = RealmProvider.getInstance();
        realm.beginTransaction();
        RealmResults<Invitation> realmResults = realm.where(Invitation.class).findAll();
        for (int i = 0; i < realmResults.size(); i++) {
            realmResults.get(i).removeFromRealm();
        }
        realm.commitTransaction();
        realm.close();
    }

    public void remove(final Invitation invitation) {
        Observable.create(new OnSubscribeRealm<Invitation>() {
            @Override
            public Invitation get(Realm realm) {
                Invitation realmInvitation = realm.where(Invitation.class)
                        .equalTo(Invitation.KEY, invitation.getKey())
                        .findFirst();
                if (realmInvitation == null) {
                    return null;
                }
                realmInvitation.removeFromRealm();
                return invitation;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Invitation>() {
                    @Override
                    public void call(Invitation invitation) {
                        MainApp.IS_MEMBER_CHANGED = true;
                        BusProvider.getInstance().post(new UpdateMemberEvent());
                    }
                }, new RealmErrorAction());
    }

    private void copy(Invitation realmInvitation, Invitation invitation) {
        if (realmInvitation == null || invitation == null) return;
        if (invitation.getKey() != null) {
            realmInvitation.setKey(invitation.getKey());
        }
        if (invitation.get_teamId() != null) {
            realmInvitation.set_teamId(invitation.get_teamId());
        }
        if (invitation.getName() != null) {
            realmInvitation.setName(invitation.getName());
        }
        if (invitation.getMobile() != null) {
            realmInvitation.setMobile(invitation.getMobile());
        }
        if (invitation.get_id() != null) {
            realmInvitation.set_id(invitation.get_id());
        }
    }

    public Member convertToMember(Invitation invitation) {
        Member member = new Member();
        member.setName(invitation.getName());
        member.setMobile(invitation.getMobile());
        member.setPhoneForLogin(invitation.getMobile());
        member.setIsInvite(true);
        member.set_id(invitation.get_id());
        return member;
    }
}
