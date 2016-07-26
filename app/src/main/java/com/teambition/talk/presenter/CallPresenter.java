package com.teambition.talk.presenter;

import com.teambition.talk.client.TalkClient;

/**
 * Created by wlanjie on 15/9/12.
 */
public class CallPresenter {

    public void call() {
        TalkClient.getInstance().getCallApi();
    }
}
