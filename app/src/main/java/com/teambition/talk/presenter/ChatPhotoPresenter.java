package com.teambition.talk.presenter;

import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.Message;
import com.teambition.talk.util.Logger;
import com.teambition.talk.view.ChatPhotoView;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by zeatual on 15/5/11.
 */
public class ChatPhotoPresenter extends BasePresenter {
    private static final String TAG = ChatPhotoPresenter.class.getSimpleName();

    private ChatPhotoView callback;

    public ChatPhotoPresenter(ChatPhotoView callback) {
        this.callback = callback;
    }

    public void loadOld(Map<String, String> data) {
        talkApi.getMessage(data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Message>>() {
                    @Override
                    public void call(List<Message> messages) {
                        Collections.reverse(messages);
                        callback.onLoadOldFinish(messages);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }

    public void loadNew(Map<String, String> data) {
        talkApi.getMessage(data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Message>>() {
                    @Override
                    public void call(List<Message> messages) {
                        callback.onLoadNewFinish(messages);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }

    public void init(Map<String, String> data) {
        callback.showProgressBar();
        talkApi.getMessage(data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Message>>() {
                    @Override
                    public void call(List<Message> messages) {
                        Collections.reverse(messages);
                        callback.onInitFinish(messages);
                        callback.dismissProgressBar();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                        callback.dismissProgressBar();
                    }
                });
    }

    public void deleteMessage(final String messageId) {
        talkApi.deleteMessage(messageId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        callback.onDeleteMessageSuccess();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }

    public void favoriteMessage(String messageId) {
        talkApi.favoriteMessage(messageId)
                .observeOn(AndroidSchedulers.mainThread())
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
