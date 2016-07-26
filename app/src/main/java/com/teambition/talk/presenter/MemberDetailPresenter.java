package com.teambition.talk.presenter;

import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.client.data.MemberRequestData;
import com.teambition.talk.entity.Member;
import com.teambition.talk.event.UpdateMemberEvent;
import com.teambition.talk.realm.MemberRealm;
import com.teambition.talk.util.Logger;
import com.teambition.talk.view.MemberDetailView;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by nlmartian on 3/17/15.
 */
public class MemberDetailPresenter extends BasePresenter {
    public static final String TAG = MemberDetailPresenter.class.getSimpleName();

    private MemberDetailView callback;

    public MemberDetailPresenter(MemberDetailView callback) {
        this.callback = callback;
    }

    public void setRole(final Member member, String role) {
        final String teamId = BizLogic.getTeamId();
        MemberRequestData requestData = new MemberRequestData(member);
        requestData.role = role;
        talkApi.setMemberRole(teamId, requestData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Member>() {
                    @Override
                    public void call(Member res) {
                        member.setRole(res.getRole());
                        MemberRealm.getInstance().addOrUpdate(member)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Member>() {
                                    @Override
                                    public void call(Member member) {
                                        MainApp.IS_MEMBER_CHANGED = true;
                                        BusProvider.getInstance().post(new UpdateMemberEvent());
                                    }
                                }, new RealmErrorAction());
                        callback.onSetAdminSuccess(res.getRole());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.e(TAG, "set as admin fail", throwable);
                        callback.dismissProgressDialog();
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }

    public void removeTeamMember(final Member member) {
        final String teamId = BizLogic.getTeamId();
        talkApi.removeMemberFromTeam(teamId, new MemberRequestData(member))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        member.setIsQuit(true);
                        MemberRealm.getInstance().addOrUpdate(member)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Member>() {
                                    @Override
                                    public void call(Member member) {
                                        MainApp.IS_MEMBER_CHANGED = true;
                                        BusProvider.getInstance().post(new UpdateMemberEvent());
                                    }
                                }, new RealmErrorAction());
                        callback.onRemoveMemberSuccess();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.e(TAG, "remove member fail", throwable);
                        callback.dismissProgressDialog();
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }

}
