package com.teambition.talk.client.data.call;

import java.util.List;

/**
 * Created by nlmartian on 3/17/16.
 */
public class SaveCallUsageData {

    public String _teamId;
    public List<String> callSids;

    public SaveCallUsageData(String _teamId, List<String> callSids) {
        this._teamId = _teamId;
        this.callSids = callSids;
    }
}
