package com.teambition.talk.view;

import com.teambition.talk.entity.User;

import java.util.List;

/**
 * Created by zeatual on 15/9/15.
 */
public interface AccountView {

    void onBindTeambition(User user);

    void onUnbindTeambition(User user);

    void onBindPhone(User user);

    void onBindPhoneFailed(String error);

    void onBindEmail(User user);

    void onBindEmailFailed(String error);

    void onBindEmailConflict(String account, String bindCode);

    void onPhoneConflict(String account, String bindCode);

    void onTeambitionConflict(String account, String bindCode);

}
