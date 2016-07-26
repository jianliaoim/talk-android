package com.teambition.talk.tag;

import com.google.gson.Gson;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.client.data.CreateTagRequestData;
import com.teambition.talk.client.data.SearchRequestData;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Tag;
import com.teambition.talk.entity.TagSearchMessage;
import com.teambition.talk.model.TagModel;
import com.teambition.talk.util.DateUtil;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by wlanjie on 15/7/29.
 */
public class TestTagModel implements TagModel {

    private final String content = "{\"_id\":\"55b59054ff804d037b63ae37\",\"team\":\"555e9be15d95811b06c15f70\",\"room\":{\"_id\":\"555e9be1b62b47a6de23f618\",\"team\":\"555e9be15d95811b06c15f70\",\"creator\":\"555e9bd85d95811b06c15f6f\",\"pinyin\":\"general\",\"topic\":\"general\",\"__v\":0,\"email\":\"general.rcd4589503i@talk.ai\",\"py\":\"general\",\"purpose\":\"测试\",\"updatedAt\":\"2015-07-14T07:57:25.208Z\",\"createdAt\":\"2015-05-22T03:00:49.129Z\",\"memberCount\":56,\"pys\":[\"general\"],\"pinyins\":[\"general\"],\"isGuestVisible\":true,\"color\":\"blue\",\"isPrivate\":false,\"isArchived\":false,\"isGeneral\":true,\"popRate\":168,\"_creatorId\":\"555e9bd85d95811b06c15f6f\",\"_teamId\":\"555e9be15d95811b06c15f70\",\"id\":\"555e9be1b62b47a6de23f618\"},\"content\":[\"哈哈哈\"],\"creator\":{\"_id\":\"55656000703e2eca4eeef93c\",\"sourceId\":\"5565600012f5d1b94f302d73\",\"source\":\"teambition\",\"email\":\"test@zzq.com\",\"emailDomain\":\"zzq.com\",\"name\":\"大将青雉\",\"pinyin\":\"dajiangqingzhi\",\"__v\":4,\"mobile\":\"13311112222\",\"py\":\"djqz\",\"phoneForLogin\":\"13311112222\",\"globalRole\":\"user\",\"hasPwd\":true,\"isActived\":false,\"isRobot\":false,\"pys\":[\"djqz\"],\"pinyins\":[\"dajiangqingzhi\"],\"from\":\"register\",\"updatedAt\":\"2015-07-22T08:20:48.149Z\",\"createdAt\":\"2015-05-27T06:11:12.162Z\",\"avatarUrl\":\"http://striker.project.ci/thumbnail/bf/15/b07e6af3f7216aa3c3db05f18f98.png/w/200/h/200\",\"id\":\"55656000703e2eca4eeef93c\"},\"__v\":1,\"tags\":[{\"_id\":\"55a871c1ad9495d56adc6179\",\"creator\":\"555e9b735d95811b06c15f6e\",\"name\":\"design\",\"team\":\"555e9be15d95811b06c15f70\",\"__v\":0,\"updatedAt\":\"2015-07-17T03:08:49.755Z\",\"createdAt\":\"2015-07-17T03:08:49.755Z\",\"_creatorId\":\"555e9b735d95811b06c15f6e\",\"_teamId\":\"555e9be15d95811b06c15f70\",\"id\":\"55a871c1ad9495d56adc6179\"}],\"updatedAt\":\"2015-07-28T09:52:42.437Z\",\"createdAt\":\"2015-07-27T01:58:44.911Z\",\"icon\":\"normal\",\"isMailable\":true,\"isPushable\":true,\"isSearchable\":true,\"isEditable\":true,\"isManual\":true,\"isStarred\":false,\"attachments\":[],\"displayMode\":\"message\",\"_teamId\":\"555e9be15d95811b06c15f70\",\"_roomId\":\"555e9be1b62b47a6de23f618\",\"_creatorId\":\"55656000703e2eca4eeef93c\",\"id\":\"55b59054ff804d037b63ae37\",\"_score\":null}";

    private final String createTagContent = "{\"__v\":0,\"creator\":\"55a4a7fa7c5b40e357027f38\",\"name\":\"bbbbb\",\"team\":\"555e9be15d95811b06c15f70\",\"_id\":\"55b83733b29b2ed5514d936a\",\"updatedAt\":\"2015-07-29T02:15:15.056Z\",\"createdAt\":\"2015-07-29T02:15:15.056Z\",\"_creatorId\":\"55a4a7fa7c5b40e357027f38\",\"_teamId\":\"555e9be15d95811b06c15f70\",\"id\":\"55b83733b29b2ed5514d936a\"}";

    private final String getTagContent = "{\"_id\":\"55b60e1da9b18d00c204e4b6\",\"creator\":\"555e9bd85d95811b06c15f6f\",\"name\":\"a8\",\"team\":\"555e9be15d95811b06c15f70\",\"__v\":0,\"updatedAt\":\"2015-07-27T11:10:33.062Z\",\"createdAt\":\"2015-07-27T10:55:25.209Z\",\"_creatorId\":\"555e9bd85d95811b06c15f6f\",\"_teamId\":\"555e9be15d95811b06c15f70\",\"id\":\"55b60e1da9b18d00c204e4b6\"}";

    private final String createMessageContent = "{\"_id\":\"55b5f639f6d0edd6a0f94e1b\",\"content\":[\"think\"],\"team\":\"555e9be15d95811b06c15f70\",\"room\":{\"_id\":\"555e9be1b62b47a6de23f618\",\"team\":\"555e9be15d95811b06c15f70\",\"creator\":\"555e9bd85d95811b06c15f6f\",\"pinyin\":\"general\",\"topic\":\"general\",\"__v\":0,\"email\":\"general.rcd4589503i@talk.ai\",\"py\":\"general\",\"purpose\":\"测试\",\"updatedAt\":\"2015-07-14T07:57:25.208Z\",\"createdAt\":\"2015-05-22T03:00:49.129Z\",\"memberCount\":56,\"pys\":[\"general\"],\"pinyins\":[\"general\"],\"isGuestVisible\":true,\"color\":\"blue\",\"isPrivate\":false,\"isArchived\":false,\"isGeneral\":true,\"popRate\":168,\"_creatorId\":\"555e9bd85d95811b06c15f6f\",\"_teamId\":\"555e9be15d95811b06c15f70\",\"id\":\"555e9be1b62b47a6de23f618\"},\"creator\":{\"_id\":\"555e9bd85d95811b06c15f6f\",\"sourceId\":\"555e9bd8971f64e908baa0e8\",\"source\":\"teambition\",\"email\":\"yong@teambition.com\",\"emailDomain\":\"teambition.com\",\"name\":\"YongChen\",\"pinyin\":\"yongchen\",\"__v\":3,\"py\":\"yongchen\",\"mobile\":\"123581321\",\"globalRole\":\"user\",\"hasPwd\":true,\"isActived\":false,\"isRobot\":false,\"pys\":[\"yongchen\"],\"pinyins\":[\"yongchen\"],\"from\":\"register\",\"updatedAt\":\"2015-07-17T06:47:00.614Z\",\"createdAt\":\"2015-05-22T03:00:40.786Z\",\"avatarUrl\":\"http://striker.project.ci/thumbnail/05/ca/0df1aa1ab5b86e4e1abc00f99ccf.jpg/w/200/h/200\",\"id\":\"555e9bd85d95811b06c15f6f\"},\"__v\":2,\"tags\":[{\"_id\":\"55b85ba514cb00db5187fcca\",\"creator\":\"55a4a7fa7c5b40e357027f38\",\"name\":\"fds\",\"team\":\"555e9be15d95811b06c15f70\",\"__v\":0,\"updatedAt\":\"2015-07-29T04:50:45.360Z\",\"createdAt\":\"2015-07-29T04:50:45.360Z\",\"_creatorId\":\"55a4a7fa7c5b40e357027f38\",\"_teamId\":\"555e9be15d95811b06c15f70\",\"id\":\"55b85ba514cb00db5187fcca\"},{\"_id\":\"55b83733b29b2ed5514d936a\",\"creator\":\"55a4a7fa7c5b40e357027f38\",\"name\":\"bbbbb\",\"team\":\"555e9be15d95811b06c15f70\",\"__v\":0,\"updatedAt\":\"2015-07-29T02:15:15.056Z\",\"createdAt\":\"2015-07-29T02:15:15.056Z\",\"_creatorId\":\"55a4a7fa7c5b40e357027f38\",\"_teamId\":\"555e9be15d95811b06c15f70\",\"id\":\"55b83733b29b2ed5514d936a\"},{\"_id\":\"55b70db6dd8659fd7a38128b\",\"creator\":\"555e9b735d95811b06c15f6e\",\"name\":\"develop\",\"team\":\"555e9be15d95811b06c15f70\",\"__v\":0,\"updatedAt\":\"2015-07-28T05:05:58.809Z\",\"createdAt\":\"2015-07-28T05:05:58.809Z\",\"_creatorId\":\"555e9b735d95811b06c15f6e\",\"_teamId\":\"555e9be15d95811b06c15f70\",\"id\":\"55b70db6dd8659fd7a38128b\"}],\"updatedAt\":\"2015-07-29T04:50:48.835Z\",\"createdAt\":\"2015-07-27T09:13:29.910Z\",\"icon\":\"normal\",\"isMailable\":true,\"isPushable\":true,\"isSearchable\":true,\"isEditable\":true,\"isManual\":true,\"isStarred\":false,\"attachments\":[],\"displayMode\":\"message\",\"socketId\":\"339236b2-96b6-4842-a4b1-7ee09d81f3fa\",\"_teamId\":\"555e9be15d95811b06c15f70\",\"_roomId\":\"555e9be1b62b47a6de23f618\",\"_creatorId\":\"555e9bd85d95811b06c15f6f\",\"id\":\"55b5f639f6d0edd6a0f94e1b\"}";

    private final Gson mGson;

    public TestTagModel() {
        mGson = new GsonProvider.Builder().setDateFormat(DateUtil.DATE_FORMAT_JSON)
                .setDateAdapter()
                .setRoomAdapter()
                .setMessageAdapter()
                .create();
    }

    @Override
    public Observable<Tag> createTagObservable(CreateTagRequestData data) {
        return Observable.create(new Observable.OnSubscribe<Tag>() {
            @Override
            public void call(Subscriber<? super Tag> subscriber) {
                Tag tag = mGson.fromJson(createTagContent, Tag.class);
                subscriber.onNext(tag);
            }
        });
    }

    @Override
    public Observable<List<Tag>> getTagsObservable() {
        return Observable.create(new Observable.OnSubscribe<List<Tag>>() {
            @Override
            public void call(Subscriber<? super List<Tag>> subscriber) {
                List<Tag> tags = new ArrayList<>();
                Tag tag = mGson.fromJson(getTagContent, Tag.class);
                for (int i = 0; i < 30; i++) {
                    tags.add(tag);
                }
                subscriber.onNext(tags);
            }
        });
    }

    @Override
    public Observable<Message> createMessageTagObservable(String messageId, List<String> tagIds) {
        return Observable.create(new Observable.OnSubscribe<Message>() {
            @Override
            public void call(Subscriber<? super Message> subscriber) {
                Message message = mGson.fromJson(createMessageContent, Message.class);
                subscriber.onNext(message);
            }
        });
    }

    @Override
    public Observable<List<Message>> createTagSearchMessageObservable(String tagId, String maxId) {
        return null;
    }

    @Override
    public Observable<Tag> removeTagObservable(String tagId) {
        return Observable.create(new Observable.OnSubscribe<Tag>() {
            @Override
            public void call(Subscriber<? super Tag> subscriber) {
                Tag tag = new Tag();
                tag.set_id("55b60e1da9b18d00c204e4b6");
                subscriber.onNext(tag);
            }
        });
    }

    @Override
    public Observable<Tag> updateTagObservable(String tagId, String name) {
        return Observable.create(new Observable.OnSubscribe<Tag>() {
            @Override
            public void call(Subscriber<? super Tag> subscriber) {
                Tag tag = new Tag();
                tag.set_id("55b60e1da9b18d00c204e4b6");
                tag.setName("teambition");
                subscriber.onNext(tag);
            }
        }).observeOn(AndroidSchedulers.mainThread());
    }
}
