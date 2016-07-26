package com.teambition.talk.presenter;

import com.teambition.talk.Constant;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.apis.AccountApi;
import com.teambition.talk.client.data.BindErrorResponseData;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.User;
import com.teambition.talk.realm.MemberRealm;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.view.AccountView;

import retrofit.RetrofitError;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zeatual on 15/9/15.
 */
public class AccountPresenter extends BasePresenter {

    AccountApi accountApi;
    AccountView callback;

    public AccountPresenter(AccountView callback) {
        this.callback = callback;
        accountApi = TalkClient.getInstance().getAccountApi();
    }

    public void bindEmail(String randomCode, String vCode) {
        accountApi.bindEmail(randomCode, vCode)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        syncUser(user);
                        callback.onBindEmail(user);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (throwable instanceof RetrofitError) {
                            try {
                                final BindErrorResponseData error = (BindErrorResponseData) ((RetrofitError) throwable)
                                        .getBodyAs(BindErrorResponseData.class);
                                if (StringUtil.isNotBlank(error.data.bindCode)) {
                                    callback.onBindEmailConflict(error.data.showname, error.data.bindCode);
                                } else {
                                    callback.onBindEmailFailed(error.message);
                                }
                            } catch (Exception e) {
                                callback.onBindEmailFailed(MainApp.CONTEXT.getString(R.string.network_failed));
                            }
                        }
                    }
                });
    }

    public void changeEmail(String randomCode, String vCode) {
        accountApi.changeEmail(randomCode, vCode)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        syncUser(user);
                        callback.onBindEmail(user);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (throwable instanceof RetrofitError) {
                            try {
                                final BindErrorResponseData error = (BindErrorResponseData) ((RetrofitError) throwable)
                                        .getBodyAs(BindErrorResponseData.class);
                                if (StringUtil.isNotBlank(error.data.bindCode)) {
                                    callback.onBindEmailConflict(error.data.showname, error.data.bindCode);
                                } else {
                                    callback.onBindEmailFailed(error.message);
                                }
                            } catch (Exception e) {
                                callback.onBindEmailFailed(MainApp.CONTEXT.getString(R.string.network_failed));
                            }
                        }
                    }
                });
    }

    public void forceBindEmail(String bindCode) {
        accountApi.forceBindEmail(bindCode)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        syncUser(user);
                        callback.onBindEmail(user);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }

    public void bindPhone(String randomCode, String vCode) {
        accountApi.bindMobile(randomCode, vCode)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        syncUser(user);
                        callback.onBindPhone(user);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (throwable instanceof RetrofitError) {
                            try {
                                final BindErrorResponseData error = (BindErrorResponseData) ((RetrofitError) throwable)
                                        .getBodyAs(BindErrorResponseData.class);
                                if (StringUtil.isNotBlank(error.data.bindCode)) {
                                    callback.onPhoneConflict(error.data.showname, error.data.bindCode);
                                } else {
                                    callback.onBindPhoneFailed(error.message);
                                }
                            } catch (Exception e) {
                                callback.onBindPhoneFailed(MainApp.CONTEXT.getString(R.string.network_failed));
                            }
                        }
                    }
                });
    }

    public void changePhone(String randomCode, String vCode) {
        accountApi.change(randomCode, vCode)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        syncUser(user);
                        callback.onBindPhone(user);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (throwable instanceof RetrofitError) {
                            try {
                                final BindErrorResponseData error = (BindErrorResponseData) ((RetrofitError) throwable)
                                        .getBodyAs(BindErrorResponseData.class);
                                if (StringUtil.isNotBlank(error.data.bindCode)) {
                                    callback.onPhoneConflict(error.data.showname, error.data.bindCode);
                                } else {
                                    callback.onBindPhoneFailed(error.message);
                                }
                            } catch (Exception e) {
                                callback.onBindPhoneFailed(MainApp.CONTEXT.getString(R.string.network_failed));
                            }
                        }
                    }
                });
    }

    public void forceBindPhone(String bindCode) {
        accountApi.forceBind(bindCode)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        syncUser(user);
                        callback.onBindPhone(user);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }

    public void bindTeambition(String code) {
        accountApi.bindTeambition(code)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        syncUser(user);
                        callback.onBindTeambition(user);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (throwable instanceof RetrofitError) {
                            try {
                                final BindErrorResponseData error = (BindErrorResponseData) ((RetrofitError) throwable)
                                        .getBodyAs(BindErrorResponseData.class);
                                if (StringUtil.isNotBlank(error.data.bindCode)) {
                                    callback.onTeambitionConflict(error.data.showname, error.data.bindCode);
                                }
                            } catch (Exception e) {
                                MainApp.showToastMsg(R.string.network_failed);
                            }
                        }
                    }
                });
    }

    public void forceBindTeambition(String bindCode) {
        accountApi.forceBindTeambition(bindCode)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        syncUser(user);
                        callback.onBindTeambition(user);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }

    public void unbindTeambition() {
        accountApi.unbindTeambition()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        syncUser(user);
                        callback.onUnbindTeambition(user);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }

    private void syncUser(final User user) {
        if (StringUtil.isNotBlank(user.getAccountToken())) {
            MainApp.PREF_UTIL.putString(Constant.ACCESS_TOKEN, user.getAccountToken());
            TalkClient.getInstance().setAccessToken(user.getAccountToken());
            talkApi.getUser()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<User>() {
                        @Override
                        public void call(User u) {
                            if (u.getPreference() == null) {
                                User user = (User) MainApp.PREF_UTIL.getObject(Constant.USER, User.class);
                                if (user != null) {
                                    u.setPreference(user.getPreference());
                                }
                            }
                            MainApp.PREF_UTIL.putObject(Constant.USER, u);
                            Member member = MainApp.globalMembers.get(u.get_id());
                            member.setPhoneForLogin(u.getPhoneForLogin());
                            MemberRealm.getInstance().updateMemberInfo(member)
                                    .subscribe(new Action1<Object>() {
                                        @Override
                                        public void call(Object o) {

                                        }
                                    }, new RealmErrorAction());
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                        }
                    });
        }
    }

}
