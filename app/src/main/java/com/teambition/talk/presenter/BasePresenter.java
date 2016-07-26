package com.teambition.talk.presenter;

import com.squareup.otto.Bus;
import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.apis.TalkApi;
import com.teambition.talk.client.apis.UploadApi;

/**
 * Created by zeatual on 14/10/27.
 */
public class BasePresenter {

    protected TalkApi talkApi;
    protected UploadApi uploadApi;
    protected Bus bus;

    public BasePresenter() {
        talkApi = TalkClient.getInstance().getTalkApi();
        uploadApi = TalkClient.getInstance().getUploadApi();
        bus = BusProvider.getInstance();
    }

}
