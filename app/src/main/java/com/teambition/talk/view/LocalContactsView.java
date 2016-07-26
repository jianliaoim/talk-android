package com.teambition.talk.view;

import com.teambition.talk.entity.Contact;

import java.util.List;

/**
 * Created by zeatual on 15/9/14.
 */
public interface LocalContactsView extends BaseView {

    void onLoadContactsFinish(List<Contact> contacts);

}
