package com.teambition.talk.client.data;

import java.util.List;

/**
 * Created by zeatual on 14/11/25.
 */
public class BatchInviteRequestData {

    List<String> _userIds;

    public BatchInviteRequestData(List<String> _userIds) {
        this._userIds = _userIds;
    }
}
