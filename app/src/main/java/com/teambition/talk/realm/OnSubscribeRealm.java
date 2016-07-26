package com.teambition.talk.realm;

import android.support.annotation.Nullable;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmException;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

abstract class OnSubscribeRealm<T> implements Observable.OnSubscribe<T> {

    RealmConfiguration mConfiguration;

    public OnSubscribeRealm() {}

    public OnSubscribeRealm(final RealmConfiguration configuration) {
        mConfiguration = configuration;
    }

    @Nullable
    private Thread thread;

    @Override
    public void call(final Subscriber<? super T> subscriber) {
        thread = Thread.currentThread();
        subscriber.add(Subscriptions.create(new Action0() {
            @Override
            public void call() {
                if (thread != null && !thread.isInterrupted()) {
                    thread.interrupt();
                }
            }
        }));

        Realm realm = RealmProvider.getInstance();
        boolean interrupted = false;
        boolean withError = false;

        T object = null;
        try {
            realm.beginTransaction();
            object = get(realm);
            interrupted = thread.isInterrupted();
            if (!interrupted) {
                realm.commitTransaction();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            realm.cancelTransaction();
            subscriber.onError(new RealmException("Error during transaction.", e));
            withError = true;
        } catch (Error e) {
            e.printStackTrace();
            realm.cancelTransaction();
            subscriber.onError(e);
            withError = true;
        }
        if (!interrupted && !withError) {
            subscriber.onNext(object);
        }

        try {
            realm.close();
        } catch (RealmException ex) {
            subscriber.onError(ex);
            withError = true;
        }
        thread = null;
        if (!withError) {
            subscriber.onCompleted();
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    public abstract T get(Realm realm);
}
