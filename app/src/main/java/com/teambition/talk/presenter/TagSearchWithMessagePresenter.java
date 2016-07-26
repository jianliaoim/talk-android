package com.teambition.talk.presenter;

import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.client.data.SearchRequestData;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.TagSearchMessage;
import com.teambition.talk.model.TagModel;
import com.teambition.talk.view.TagSearchWithMessageView;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by wlanjie on 15/7/16.
 */
public class TagSearchWithMessagePresenter extends BasePresenter {

    final TagSearchWithMessageView mView;

    private TagModel mModule;

    public TagSearchWithMessagePresenter(TagSearchWithMessageView view) {
        mView = view;
    }

    public void attachModule(TagModel module) {
        mModule = module;
    }

    public void readTagWithMessage(String tagId, String maxId) {
        if (maxId == null) {
            maxId = "";
        }
        mModule.createTagSearchMessageObservable(tagId, maxId)
                .subscribe(new Action1<List<Message>>() {
                    @Override
                    public void call(List<Message> messages) {
                        mView.dismissProgressBar();
                        mView.readTagWithMessage(messages);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mView.readTagWithMessageError();
                        MainApp.showToastMsg(R.string.network_failed);
                        mView.dismissProgressBar();
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
                        mView.deleteComplete(messageId);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }
}
