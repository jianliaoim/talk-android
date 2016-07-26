package com.teambition.talk.realm;

import com.google.gson.Gson;
import com.teambition.common.PinyinUtil;
import com.teambition.talk.BizLogic;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.entity.Member;
import com.teambition.talk.util.StringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by wlanjie on 15/10/10.
 */
public class MemberRealm extends AbstractRealm {

    private static MemberRealm realm;

    final Gson gson;

    private MemberRealm() {
        gson = GsonProvider.getGson();
    }

    public static MemberRealm getInstance() {
        if (realm == null) {
            realm = new MemberRealm();
        }
        return realm;
    }

    public List<Member> getAdminsMemberWithCurrentThread() {
        final List<Member> members = new ArrayList<>();
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            final RealmResults<Member> realmResults = realm.where(Member.class)
                    .equalTo(Member.TEAM_ID, BizLogic.getTeamId())
                    .beginGroup()
                    .equalTo(Member.ROLE, Member.ADMIN)
                    .or()
                    .equalTo(Member.ROLE, Member.OWNER)
                    .endGroup()
                    .findAll();
            realmResults.sort(Member.ALIAS_PINYIN, Sort.ASCENDING);
            for (Member realmMember : realmResults) {
                Member member = new Member();
                copy(member, realmMember);
                members.add(member);
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
        return members;
    }

    public List<Member> getMemberWithCurrentThread() {
        final List<Member> members = new ArrayList<>();
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            RealmResults<Member> realmResults = realm.where(Member.class)
                    .equalTo(Member.TEAM_ID, BizLogic.getTeamId())
                    .equalTo(Member.IS_QUIT, false)
                    .findAll();
            realmResults.sort(Member.ALIAS_PINYIN, Sort.ASCENDING);
            for (Member realmResult : realmResults) {
                Member member = new Member();
                copy(member, realmResult);
                members.add(member);
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
        return members;
    }

    public Observable<List<Member>> getMemberNotAdnim() {
        return Observable.create(new OnSubscribeRealm<List<Member>>() {
            @Override
            public List<Member> get(Realm realm) {
                final RealmResults<Member> realmResults = realm.where(Member.class)
                        .equalTo(Member.TEAM_ID, BizLogic.getTeamId())
                        .equalTo(Member.IS_QUIT, false)
                        .notEqualTo(Member.ROLE, Member.ADMIN)
                        .findAllSorted(Member.ALIAS_PINYIN, Sort.ASCENDING);
                final List<Member> members = new ArrayList<>(realmResults.size());
                for (Member realmResult : realmResults) {
                    Member member = new Member();
                    copy(member, realmResult);
                    members.add(member);
                }
                return members;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Member> getMemberById(final String memberId){
        return Observable.create(new OnSubscribeRealm<Member>() {
            @Override
            public Member get(Realm realm) {
                Member realmMember = realm.where(Member.class)
                        .equalTo(Member.ID, memberId)
                        .equalTo(Member.TEAM_ID, BizLogic.getTeamId())
                        .findFirst();
                if (realmMember == null) return null;
                Member member = new Member();
                copy(member, realmMember);
                return member;
            }
        }).subscribeOn(Schedulers.io());
    }

    public List<Member> getMemberNotAdminWithCurrentThread() {
        final List<Member> members = new ArrayList<>();
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            final RealmResults<Member> realmResults = realm.where(Member.class)
                    .equalTo(Member.TEAM_ID, BizLogic.getTeamId())
                    .equalTo(Member.IS_QUIT, false)
                    .equalTo(Member.ROLE, Member.MEMBER)
                    .findAllSorted(Member.ALIAS_PINYIN, Sort.ASCENDING);
            for (Member realmResult : realmResults) {
                Member member = new Member();
                copy(member, realmResult);
                members.add(member);
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
        return members;
    }

    public List<Member> getQuitMemberWithCurrentThread() {
        final List<Member> members = new ArrayList<>();
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            RealmResults<Member> realmResults = realm.where(Member.class)
                    .equalTo(Member.TEAM_ID, BizLogic.getTeamId())
                    .equalTo(Member.IS_QUIT, true)
                    .findAll();
            realmResults.sort(Member.ALIAS_PINYIN, Sort.ASCENDING);
            for (Member realmResult : realmResults) {
                Member member = new Member();
                copy(member, realmResult);
                members.add(member);
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
        return members;
    }

    public List<Member> getAllMemberExceptMeWithCurrentThread() {
        final List<Member> members = new ArrayList<>();
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            RealmResults<Member> realmResults = realm.where(Member.class)
                    .equalTo(Member.TEAM_ID, BizLogic.getTeamId())
                    .equalTo(Member.IS_QUIT, false)
                    .notEqualTo(Member.ID, BizLogic.getUserInfo().get_id())
                    .findAll();
            realmResults.sort(Member.ALIAS_PINYIN, Sort.ASCENDING);
            for (Member realmResult : realmResults) {
                Member member = new Member();
                copy(member, realmResult);
                members.add(member);
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
        return members;
    }

    public List<Member> getNotQuitAndNotRobotMemberWithCurrentThread() {
        final List<Member> members = new ArrayList<>();
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            RealmResults<Member> realmResults = realm.where(Member.class)
                    .equalTo(Member.TEAM_ID, BizLogic.getTeamId())
                    .equalTo(Member.IS_QUIT, false)
                    .equalTo(Member.IS_ROBOT, false)
                    .findAll();
            realmResults.sort(Member.ALIAS_PINYIN, Sort.ASCENDING);
            for (Member realmResult : realmResults) {
                Member member = new Member();
                copy(member, realmResult);
                members.add(member);
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
        return members;
    }

    public List<Member> getNotRobotMemberWithCurrentThread() {
        final List<Member> members = new ArrayList<>();
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            RealmResults<Member> realmResults = realm.where(Member.class)
                    .equalTo(Member.TEAM_ID, BizLogic.getTeamId())
                    .equalTo(Member.IS_ROBOT, false)
                    .findAll();
            realmResults.sort(Member.ALIAS_PINYIN, Sort.ASCENDING);
            for (Member realmResult : realmResults) {
                Member member = new Member();
                copy(member, realmResult);
                members.add(member);
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
        return members;
    }

    public List<Member> getMembersByIdsWithCurrentThread(final List<String> ids) {
        List<Member> members = new ArrayList<>(ids.size());
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            for (String id : ids) {
                Member member = new Member();
                Member realmMember = realm.where(Member.class)
                        .equalTo(Member.TEAM_ID, BizLogic.getTeamId())
                        .equalTo(Member.ID, id).findFirst();
                if (realmMember == null) continue;
                copy(member, realmMember);
                members.add(member);
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
        return members;
    }

    public Observable<Member> getMember(final String memberId) {
        return Observable.create(new OnSubscribeRealm<Member>() {
            @Override
            public Member get(Realm realm) {
                Member realmMember = realm.where(Member.class)
                        .equalTo(Member.TEAM_MEMBER_ID, BizLogic.getTeamId() + memberId)
                        .equalTo(Member.ID, memberId)
                        .findFirst();
                if (realmMember == null) return null;
                Member member = new Member();
                copy(member, realmMember);
                return member;
            }
        }).subscribeOn(Schedulers.io());
    }
    public Member getMemberWithCurrentThread(final String memberId) {
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            Member realmMember = realm.where(Member.class)
                    .equalTo(Member.TEAM_MEMBER_ID, BizLogic.getTeamId() + memberId)
                    .equalTo(Member.ID, memberId)
                    .findFirst();
            if (realmMember == null) return null;
            Member member = new Member();
            copy(member, realmMember);
            realm.commitTransaction();
            return member;
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
        return null;
    }

    public Observable<List<Member>> getMembersByIds(final List<String> ids) {
        return Observable.create(new OnSubscribeRealm<List<Member>>() {
            @Override
            public List<Member> get(Realm realm) {
                List<Member> members = new ArrayList<>(ids.size());
                for (String id : ids) {
                    Member member = new Member();
                    copy(member, realm.where(Member.class)
                            .equalTo(Member.TEAM_ID, BizLogic.getTeamId())
                            .equalTo(Member.ID, id)
                            .findFirst());
                    members.add(member);
                }
                return members;
            }
        }).subscribeOn(Schedulers.io());
    }

    public void addOrUpdateWithCurrentThread(final Member member) {
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            copy(member, member);
            realm.copyToRealmOrUpdate(member);
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
    }

    public void batchAddWithCurrentThread(final List<Member> members) {
        final List<Member> realmMembers = new ArrayList<>(members.size());
        Realm memberRealm = RealmProvider.getInstance();
        for (Member member : members) {
            Member realmMember = new Member();
            copy(realmMember, member);
            realmMembers.add(realmMember);
        }
        try {
            memberRealm.beginTransaction();
            memberRealm.copyToRealmOrUpdate(realmMembers);
            memberRealm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            memberRealm.cancelTransaction();
        } finally {
            memberRealm.close();
        }
    }

    public Observable<Object> updateMemberInfo(final Member member) {
        return Observable.create(new OnSubscribeRealm<Object>() {
            @Override
            public Object get(Realm realm) {
                RealmResults<Member> realmResults = realm.where(Member.class)
                        .equalTo(Member.ID, member.get_id())
                        .findAll();
                for (int i = 0; i < realmResults.size(); i++) {
                    copy(realmResults.get(i), member);
                }
                return member;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Member> addOrUpdate(final Member member) {
        return Observable.create(new OnSubscribeRealm<Member>() {
            @Override
            public Member get(Realm realm) {
                copy(member, member);
                realm.copyToRealmOrUpdate(member);
                return member;
            }
        }).subscribeOn(Schedulers.io());
    }

    public void removeQuitMemberOnMainThread() {
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            RealmResults<Member> realmResults = realm.where(Member.class)
                    .equalTo(Member.TEAM_ID, BizLogic.getTeamId())
                    .equalTo(Member.IS_QUIT, true)
                    .findAll();
            for (Member realmResult : realmResults) {
                if (realmResult != null) {
                    realmResult.removeFromRealm();
                }
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
    }

    public void copy(Member dest, Member source) {
        if (dest == null || source == null) return;
        if (source.get_id() != null) {
            dest.set_id(source.get_id());
            dest.set_teamMemberId(BizLogic.getTeamId() + source.get_id());
        }
        dest.set_teamId(BizLogic.getTeamId());
        if (source.getName() != null) {
            dest.setName(source.getName());
        }
        if (source.getAvatarUrl() != null) {
            dest.setAvatarUrl(source.getAvatarUrl());
        }
        if (source.getEmail() != null) {
            dest.setEmail(source.getEmail());
        }
        if (source.getMobile() != null) {
            dest.setMobile(source.getMobile());
        }
        if (source.getPhoneForLogin() != null) {
            dest.setPhoneForLogin(source.getPhoneForLogin());
        }
        if (source.getPinyin() != null) {
            dest.setPinyin(source.getPinyin());
        }
        dest.setIsRobot(source.getIsRobot() == null ? false : source.getIsRobot());
        if (source.getPinyin() != null) {
            dest.setPinyin(source.getPinyin());
        }
        if (source.getAlias() != null) {
            dest.setAlias(source.getAlias());
        }
        if (source.getRole() != null) {
            dest.setRole(source.getRole());
        }
        dest.setIsQuit(source.getIsQuit() == null ? false : source.getIsQuit());
        if (source.getPinnedAt() != null) {
            dest.setPinnedAtTime(source.getPinnedAt().getTime());
        }
        if (source.getPinnedAtTime() != 0) {
            dest.setPinnedAt(new Date(source.getPinnedAtTime()));
        }
        if (source.getCreatedAt() != null) {
            dest.setCreatedAtTime(source.getCreatedAt().getTime());
        }
        if (source.getCreatedAtTime() != 0) {
            dest.setCreatedAt(new Date(source.getCreatedAtTime()));
        }
        if (source.getUnread() != null) {
            dest.setUnread(source.getUnread());
        }
        if (source.getPrefs() != null) {
            dest.setAlias(source.getPrefs().getAlias() == null ? "" : source.getPrefs().getAlias());
            if (StringUtil.isNotBlank(source.getPrefs().getAlias())) {
                dest.setAliasPinyin(PinyinUtil.converterToFirstSpell(source.getPrefs().getAlias()));
            } else if (StringUtil.isNotBlank(source.getName())) {
                dest.setAliasPinyin(PinyinUtil.converterToFirstSpell(source.getName()));
            }
        } else {
            final String alias = StringUtil.isNotBlank(source.getAlias()) ? source.getAlias() : source.getName();
            if (StringUtil.isNotBlank(alias)) {
                dest.setAliasPinyin(PinyinUtil.converterToFirstSpell(alias));
            }
        }
        if (StringUtil.isNotBlank(source.getAliasPinyin())) {
            dest.setAliasPinyin(source.getAliasPinyin());
        }
        dest.setHideMobile(source.getHideMobile() == null ? false : source.getHideMobile());
    }
}
