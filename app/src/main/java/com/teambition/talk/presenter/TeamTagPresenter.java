package com.teambition.talk.presenter;

import com.teambition.talk.R;
import com.teambition.talk.entity.Tag;
import com.teambition.talk.model.TagModel;
import com.teambition.talk.view.TeamTagView;

import java.util.List;

import rx.functions.Action1;

/**
 * Created by wlanjie on 15/7/16.
 */
public class TeamTagPresenter extends BasePresenter {

    final TeamTagView mTagView;

    private TagModel mModule;

    public TeamTagPresenter(TeamTagView tagView) {
        mTagView = tagView;
    }

    public void attachModule(TagModel module) {
        mModule = module;
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

    public void removeTag(String tagId) {
        mTagView.showProgressDialog(R.string.wait);
        mModule.removeTagObservable(tagId)
                .subscribe(new Action1<Tag>() {
                    @Override
                    public void call(Tag tag) {
                        mTagView.dismissProgressDialog();
                        mTagView.removeTag(tag);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mTagView.dismissProgressDialog();
                    }
                });
    }

    public void updateTag(String tagId, String name) {
        mTagView.showProgressDialog(R.string.wait);
        mModule.updateTagObservable(tagId, name)
                .subscribe(new Action1<Tag>() {
                    @Override
                    public void call(Tag tag) {
                        mTagView.dismissProgressDialog();
                        mTagView.updateTag(tag);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mTagView.dismissProgressDialog();
                    }
                });
    }

}
