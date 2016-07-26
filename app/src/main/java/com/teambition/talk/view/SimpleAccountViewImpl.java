package com.teambition.talk.view;

import com.teambition.talk.entity.User;

/**
 * Created by zeatual on 15/9/17.
 */
public class SimpleAccountViewImpl implements AccountView {
    @Override
    public void onBindTeambition(User user) {

    }

    @Override
    public void onUnbindTeambition(User user) {

    }

    @Override
    public void onBindPhone(User user) {

    }

    @Override
    public void onBindPhoneFailed(String error) {

    }

    @Override
    public void onBindEmail(User user) {

    }

    @Override
    public void onBindEmailFailed(String error) {

    }

    @Override
    public void onBindEmailConflict(String account, String bindCode) {

    }

    @Override
    public void onPhoneConflict(String account, String bindCode) {

    }

    @Override
    public void onTeambitionConflict(String account, String bindCode) {

    }
}
