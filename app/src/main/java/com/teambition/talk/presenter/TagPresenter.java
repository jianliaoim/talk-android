package com.teambition.talk.presenter;

import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.client.data.CreateTagRequestData;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Tag;
import com.teambition.talk.model.TagModel;
import com.teambition.talk.view.TagView;

import java.util.List;

import retrofit.RetrofitError;
import rx.functions.Action1;

/**
 * Created by wlanjie on 15/7/14.
 */
public class TagPresenter extends BasePresenter {

    final TagView mTagView;

    private TagModel mModule;

    public TagPresenter(TagView tagView) {
        this.mTagView = tagView;
    }

    public void attachModule(TagModel module) {
        mModule = module;
    }

    public void createTag(CreateTagRequestData data) {
        mModule.createTagObservable(data)
                .subscribe(new Action1<Tag>() {
                    @Override
                    public void call(Tag tag) {
                        mTagView.onCreateTagComplete(tag);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (throwable instanceof RetrofitError) {
                            final int code = ((RetrofitError) throwable).getResponse().getStatus();
                            if (code == 402) {
                                MainApp.showToastMsg(R.string.create_tag_failed);
                            }
                        }
                    }
                });
    }

    public void getTags() {
        mModule.getTagsObservable()
                .subscribe(new Action1<List<Tag>>() {
                    @Override
                    public void call(List<Tag> tags) {
                        mTagView.dismissProgressBar();
                        mTagView.getTags(tags);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mTagView.dismissProgressBar();
                    }
                });
    }

    public void createMessageTag(String messageId, List<String> tagIds) {
        mTagView.showProgressDialog(R.string.wait);
        mModule.createMessageTagObservable(messageId, tagIds)
                .subscribe(new Action1<Message>() {
                    @Override
                    public void call(Message message) {
                        mTagView.dismissProgressDialog();
                        mTagView.onCreateMessageTag(message);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mTagView.dismissProgressDialog();
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }
}
