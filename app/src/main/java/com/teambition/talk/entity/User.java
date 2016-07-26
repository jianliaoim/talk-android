package com.teambition.talk.entity;

import com.squareup.otto.Bus;
import com.teambition.talk.Constant;
import com.teambition.talk.MainApp;
import com.teambition.talk.event.UpdateUserEvent;

import org.parceler.Parcel;

import java.util.List;

/**
 * Created by zeatual on 14/10/28.
 */
@Parcel(Parcel.Serialization.BEAN)
public class User {

    public enum Refer {
        MOBILE("mobile"),
        TEAMBITION("teambition"),
        EMAIL("email"),
        NONE("none");

        private String value;

        Refer(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        public static Refer getEnum(String value) {
            for (Refer v : values()) {
                if (v.value.equals(value)) {
                    return v;
                }
            }
            return NONE;
        }
    }

    @Parcel(Parcel.Serialization.BEAN)
    public static class Account {
        String showName;
        Refer refer;

        public Account(){}

        public Account(String showName, Refer refer) {
            this.showName = showName;
            this.refer = refer;
        }

        public String getShowName() {
            return showName;
        }

        public void setShowName(String showName) {
            this.showName = showName;
        }

        public Refer getRefer() {
            return refer;
        }

        public void setRefer(Refer refer) {
            this.refer = refer;
        }
    }

    public Account getTeambitionAccount() {
        if (accounts != null && !accounts.isEmpty()) {
            for (Account account : accounts) {
                if (account.getRefer() == Refer.TEAMBITION) {
                    return account;
                }
            }
        }
        return null;
    }

    public Account getMobileAccount() {
        if (accounts != null && !accounts.isEmpty()) {
            for (Account account : accounts) {
                if (account.getRefer() == Refer.MOBILE) {
                    return account;
                }
            }
        }
        return null;
    }

    public Account getEmailAccount() {
        if (accounts != null && !accounts.isEmpty()) {
            for (Account account : accounts) {
                if (account.getRefer() == Refer.EMAIL) {
                    return account;
                }
            }
        }
        return null;
    }

    String _id;
    String name;
    String avatarUrl;
    String email;
    String phoneForLogin;
    Preference preference;
    String subAccountSid;
    UserVoip voip;

    // for account
    String accountToken;
    String login;   // 登录方式
    String phoneNumber;
    String emailAddress;
    String showname;
    boolean wasNew;

    // for local logic
    List<Account> accounts;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccountToken() {
        return accountToken;
    }

    public void setAccountToken(String accountToken) {
        this.accountToken = accountToken;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneForLogin() {
        return phoneForLogin;
    }

    public void setPhoneForLogin(String phoneForLogin) {
        this.phoneForLogin = phoneForLogin;
    }

    public Preference getPreference() {
        return preference;
    }

    public void setPreference(Preference preference) {
        this.preference = preference;
    }

    public String getSubAccountSid() {
        return subAccountSid;
    }

    public void setSubAccountSid(String subAccountSid) {
        this.subAccountSid = subAccountSid;
    }

    public UserVoip getVoip() {
        return voip;
    }

    public void setVoip(UserVoip voip) {
        this.voip = voip;
    }

    public boolean wasNew() {
        return wasNew;
    }

    public void setWasNew(boolean wasNew) {
        this.wasNew = wasNew;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getShowname() {
        return showname;
    }

    public void setShowname(String showname) {
        this.showname = showname;
    }

    public boolean isWasNew() {
        return wasNew;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void update(Bus bus) {
        if (preference == null) {
            User user = (User) MainApp.PREF_UTIL.getObject(Constant.USER, User.class);
            if (user != null) {
                setPreference(user.getPreference());
            }
        }
        MainApp.PREF_UTIL.putObject(Constant.USER, this);
        bus.post(new UpdateUserEvent());
    }

}
