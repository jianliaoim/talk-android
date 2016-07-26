package com.teambition.talk.rx;

import rx.functions.Action1;

/**
 * Created by wlanjie on 15/10/19.
 */
public class RealmErrorAction implements Action1<Throwable> {

    @Override
    public void call(Throwable throwable) {
        throwable.printStackTrace();
        onCallback(throwable);
    }

    protected void onCallback(Throwable e) {

    }
}
