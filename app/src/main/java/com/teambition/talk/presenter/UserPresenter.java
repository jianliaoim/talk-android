package com.teambition.talk.presenter;

import com.teambition.talk.rx.ApiErrorAction;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.entity.User;
import com.teambition.talk.util.AnalyticsHelper;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.view.UserView;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Created by zeatual on 15/9/17.
 */
public class UserPresenter {

    UserView callback;

    public UserPresenter(UserView callback) {
        this.callback = callback;
    }

    public void getUser() {
        Observable.zip(TalkClient.getInstance().getAccountApi().getAccounts(),
                TalkClient.getInstance().getTalkApi().getUser(),
                new Func2<List<User>, User, User>() {
                    @Override
                    public User call(List<User> users, User user) {
                        List<User.Account> accounts = new ArrayList<>();
                        for (User u : users) {
                            if (StringUtil.isNotBlank(u.getLogin())) {
                                switch (User.Refer.getEnum(u.getLogin())) {
                                    case MOBILE:
                                        accounts.add(new User.Account(u.getPhoneNumber(), User.Refer.MOBILE));
                                        break;
                                    case TEAMBITION:
                                        accounts.add(new User.Account(u.getShowname(), User.Refer.TEAMBITION));
                                        break;
                                    case EMAIL:
                                        accounts.add(new User.Account(u.getEmailAddress(), User.Refer.EMAIL));
                                        break;
                                }
                            }
                        }
                        user.setAccounts(accounts);
                        return user;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        callback.onLoadUserFinish(user);
                        AnalyticsHelper.getInstance().setUid(user.get_id());
                    }
                }, new ApiErrorAction());
    }
}
