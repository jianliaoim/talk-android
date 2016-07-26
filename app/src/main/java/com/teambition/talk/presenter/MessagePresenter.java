package com.teambition.talk.presenter;

import android.support.annotation.NonNull;

import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.model.MessageModel;
import com.teambition.talk.util.Logger;

import rx.functions.Action1;

/**
 * Created by wlanjie on 15/7/30.
 */
public abstract class MessagePresenter extends BasePresenter {

    private final static String TAG = MessagePresenter.class.getSimpleName();

    private MessageModel mModel;

    public MessagePresenter() {

    }

    public void attachModel(@NonNull MessageModel model) {
        mModel = model;
    }

    public abstract void deleteMessage(final String messageId);

    public void favoriteMessage(String messageId) {
        mModel.createFavoriteMessageObservable(messageId)
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        MainApp.showToastMsg(R.string.favorite_success);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        Logger.e(TAG, "favorite error", e);
                    }
                });
    }
}
