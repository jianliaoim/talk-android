package com.teambition.talk.view;

import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Tag;

import java.util.List;

/**
 * Created by wlanjie on 15/7/14.
 */
public interface TagView extends BaseView {

    void onCreateTagComplete(Tag tag);

    void getTags(List<Tag> tags);

    void onCreateMessageTag(Message message);
}
