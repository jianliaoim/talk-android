package com.teambition.talk.client.data;

import java.util.List;

/**
 * Created by wlanjie on 15/7/15.
 */
public class MessageAddTagData {

    public List<String> _tagIds;

    public MessageAddTagData(List<String> tags) {
        this._tagIds = tags;
    }

    public List<String> getTags() {
        return _tagIds;
    }

    public void setTags(List<String> tags) {
        this._tagIds = tags;
    }
}

