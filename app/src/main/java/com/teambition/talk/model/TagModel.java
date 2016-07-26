package com.teambition.talk.model;

import com.teambition.talk.client.data.CreateTagRequestData;
import com.teambition.talk.client.data.SearchRequestData;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Tag;
import com.teambition.talk.entity.TagSearchMessage;

import java.util.List;

import rx.Observable;

/**
 * Created by wlanjie on 15/7/28.
 */
public interface TagModel {

    Observable<Tag> createTagObservable(CreateTagRequestData data);

    Observable<List<Tag>> getTagsObservable();

    Observable<Message> createMessageTagObservable(String messageId, List<String> tagIds);

    Observable<List<Message>> createTagSearchMessageObservable(String tagId, String maxId);

    Observable<Tag> removeTagObservable(String tagId);

    Observable<Tag> updateTagObservable(String tagId, String name);
}
