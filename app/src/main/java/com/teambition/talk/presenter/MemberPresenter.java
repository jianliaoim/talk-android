package com.teambition.talk.presenter;

import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.rx.ApiErrorAction;
import com.teambition.talk.entity.Invitation;
import com.teambition.talk.entity.Member;
import com.teambition.talk.event.UpdateMemberEvent;
import com.teambition.talk.realm.InvitationRealm;
import com.teambition.talk.realm.MemberRealm;
import com.teambition.talk.view.MemberView;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by zeatual on 14/10/31.
 */
public class MemberPresenter extends BasePresenter {

    private MemberView callback;

    public MemberPresenter(MemberView callback) {
        this.callback = callback;
    }

    public void getMembers() {
        callback.showProgressBar();
        Observable.create(new Observable.OnSubscribe<List<Member>>() {
            @Override
            public void call(Subscriber<? super List<Member>> subscriber) {
                List<Member> result = new ArrayList<>();
                List<Member> admins = MemberRealm.getInstance().getAdminsMemberWithCurrentThread();
                List<Member> members = MemberRealm.getInstance().getMemberNotAdminWithCurrentThread();
                Member me = MemberRealm.getInstance().getMemberWithCurrentThread(BizLogic.getUserInfo().get_id());
                List<Invitation> invitations = InvitationRealm.getInstance().getInvitationWithCurrentThread();
                for (Invitation invitation : invitations) {
                    members.add(InvitationRealm.getInstance().convertToMember(invitation));
                }
                for (Member m : admins) {
                    if ((!m.getIsRobot()) && !BizLogic.isMe(m.get_id())) {
                        result.add(m);
                    }
                }
                for (Member m : members) {
                    if ((!m.getIsRobot()) && !BizLogic.isMe(m.get_id())) {
                        result.add(m);
                    }
                }
                if (me != null && result.contains(me)) {
                    result.remove(me);
                }
                if (me != null) {
                    result.add(0, me);
                }
                subscriber.onNext(result);
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<List<Member>>() {
                    @Override
                    public void call(List<Member> members) {
                        if (!members.isEmpty()) {
                            callback.onLoadMembersFinish(members);
                        }
                    }
                });
    }

    public void getLeaveMembers() {
        Observable.create(new Observable.OnSubscribe<List<Member>>() {
            @Override
            public void call(Subscriber<? super List<Member>> subscriber) {
                subscriber.onNext(MemberRealm.getInstance().getQuitMemberWithCurrentThread());

            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<List<Member>>() {
                    @Override
                    public void call(List<Member> members) {
                        callback.onLoadLeaveMembersFinish(members);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    public void deleteInvitation(String id) {
        talkApi.removeInvitation(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Invitation>() {
                    @Override
                    public void call(Invitation invitation) {
                        InvitationRealm.getInstance().remove(invitation);
                        BusProvider.getInstance().post(new UpdateMemberEvent());
                    }
                }, new ApiErrorAction());
    }
}
