package com.teambition.talk.rx;

import rx.Observable.Operator;
import rx.Subscriber;
import rx.functions.Action1;


/**
 * 使用此Operator可以防止进入onError以后subscriber从stream unsubscribe
 * @param <T>
 */
public final class OperatorSuppressError<T> implements Operator<T, T> {
    final Action1<Throwable> onError;
    
    public OperatorSuppressError(Action1<Throwable> onError) {
        this.onError = onError;
    }

    @Override
    public Subscriber<? super T> call(final Subscriber<? super T> t1) {
        return new Subscriber<T>(t1) {

            @Override
            public void onNext(T t) {
                t1.onNext(t);
            }

            @Override
            public void onError(Throwable e) {
                onError.call(e);
            }

            @Override
            public void onCompleted() {
                t1.onCompleted();
            }
            
        };
    }
}
