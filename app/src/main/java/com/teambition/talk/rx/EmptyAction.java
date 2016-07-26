package com.teambition.talk.rx;

import rx.functions.Action1;

/**
 * Created by zeatual on 15/11/6.
 */
public class EmptyAction<T> implements Action1<T> {
    @Override
    public void call(T t) {

    }
}
