package com.teambition.talk.client.data;

import java.util.List;

/**
 * Created by zeatual on 15/9/25.
 */
public class CreateStoryRequestData {

    public String _teamId;
    public String category;
    public Object data;
    public List<String> _memberIds;

    public CreateStoryRequestData() {
    }

    public CreateStoryRequestData(String _teamId, String category, Object data,
                                  List<String> _memberIds) {
        this._teamId = _teamId;
        this.category = category;
        this.data = data;
        this._memberIds = _memberIds;
    }


}
