package com.teambition.talk.model;

import com.teambition.talk.BizLogic;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.data.CreateTagRequestData;
import com.teambition.talk.client.data.MessageAddTagData;
import com.teambition.talk.client.data.SearchRequestData;
import com.teambition.talk.client.data.UpdateTagRequestData;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Tag;
import com.teambition.talk.entity.TagSearchMessage;
import com.teambition.talk.util.StringUtil;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by wlanjie on 15/7/28.
 */
public class TagModelImpl implements TagModel {

    @Override
    public Observable<Tag> createTagObservable(CreateTagRequestData data) {
        return TalkClient.getInstance().getTalkApi().createTag(data)
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<List<Tag>> getTagsObservable() {
        final String teamId = BizLogic.getTeamId();
        return TalkClient.getInstance().getTalkApi().getTags(teamId)
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<Message> createMessageTagObservable(String messageId, List<String> tagIds) {
        return TalkClient.getInstance().getTalkApi().createMessageTag(messageId, new MessageAddTagData(tagIds))
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<List<Message>> createTagSearchMessageObservable(String tagId, String maxId) {
        final String teamId = BizLogic.getTeamId();
        if (StringUtil.isBlank(maxId)) {
            return TalkClient.getInstance().getTalkApi().readTagWithMessage(teamId, tagId, false, 30)
                    .observeOn(AndroidSchedulers.mainThread());
        } else {
            return TalkClient.getInstance().getTalkApi().readMoreTagWithMessage(teamId, tagId, maxId, false, 30)
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }

    @Override
    public Observable<Tag> removeTagObservable(String tagId) {
        return TalkClient.getInstance().getTalkApi().removeTag(tagId)
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<Tag> updateTagObservable(String tagId, String name) {
        return TalkClient.getInstance().getTalkApi().updateTag(tagId, new UpdateTagRequestData(name))
                .observeOn(AndroidSchedulers.mainThread());
    }

}
