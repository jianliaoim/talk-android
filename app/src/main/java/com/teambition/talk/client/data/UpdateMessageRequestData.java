package com.teambition.talk.client.data;

import com.teambition.talk.entity.Content;

import java.util.List;

/**
 * Created by zeatual on 14/12/10.
 */
public class UpdateMessageRequestData {

    public List<Content> content;

    public UpdateMessageRequestData(List<Content> content) {
        this.content = content;
    }

}
