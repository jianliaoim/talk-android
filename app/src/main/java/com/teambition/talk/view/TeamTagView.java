package com.teambition.talk.view;

import com.teambition.talk.entity.Tag;

import java.util.List;

/**
 * Created by wlanjie on 15/7/16.
 */
public interface TeamTagView extends BaseView {

    void getTags(List<Tag> tags);

    void updateTag(Tag tag);

    void removeTag(Tag tag);

}
