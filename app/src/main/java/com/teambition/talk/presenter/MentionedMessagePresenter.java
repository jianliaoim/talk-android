package com.teambition.talk.presenter;

import com.teambition.talk.entity.Message;
import com.teambition.talk.util.Logger;
import com.teambition.talk.view.MentionedMessageView;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by nlmartian on 1/30/16.
 */
public class MentionedMessagePresenter extends BasePresenter {
    public static final String TAG = MentionedMessagePresenter.class.getSimpleName();

    private MentionedMessageView view;

    public MentionedMessagePresenter(MentionedMessageView view) {
        this.view = view;
    }

    public void getMessageMentionedMe(String teamId, String maxId) {
        view.showProgressBar();
        Observable<List<Message>> messageStream =
                maxId == null ? talkApi.getMentionedMessages(teamId, 30)
                        : talkApi.getMoreMentionedMessages(teamId, maxId, 30);
        messageStream.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Message>>() {
                    @Override
                    public void call(List<Message> messages) {
                        view.dismissProgressBar();
                        view.showMessages(messages);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        view.dismissProgressBar();
                        Logger.e(TAG, "get mentioned msg error", e);
                    }
                });
    }
}
