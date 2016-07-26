package com.teambition.talk.presenter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.teambition.common.PinyinUtil;
import com.teambition.talk.entity.Contact;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.view.LocalContactsView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by zeatual on 15/9/14.
 */
public class LocalContactsPresenter extends BasePresenter {

    private LocalContactsView callback;
    private Context context;

    public LocalContactsPresenter(LocalContactsView callback, Context context) {
        this.callback = callback;
        this.context = context;
    }

    public void getContacts() {
        Observable.create(new Observable.OnSubscribe<List<Contact>>() {
            @Override
            public void call(Subscriber<? super List<Contact>> subscriber) {
                List<Contact> contacts = getContactsSync();
                Collections.sort(contacts);
                subscriber.onNext(contacts);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Contact>>() {
                    @Override
                    public void call(List<Contact> contacts) {
                        callback.onLoadContactsFinish(contacts);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    private List<Contact> getContactsSync() {
        List<Contact> contacts = new ArrayList<>();
        ContentResolver contResv = context.getContentResolver();
        Cursor cursor = contResv.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                //Create a plain class with following variables - id, name, contactNumber, email
                Contact objContactDO = new Contact();

                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                objContactDO.setName(name);
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                Cursor emails = contResv.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + id,
                        null,
                        null);
                while (emails.moveToNext()) {
                    String emailAddress = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    objContactDO.setEmailAddress(emailAddress);
                    break;
                }
                emails.close();

                String index = PinyinUtil.converterToFirstSpell(objContactDO.getName());
                int thumbnailIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.PHOTO_THUMBNAIL_URI);
                String avatar = StringUtil.isBlank(cursor.getString(thumbnailIndex)) ? null :
                        "content://com.android.contacts/contacts/" + id;
                if (index.length() < 1 || index.charAt(0) < 'A' || index.charAt(0) > 'Z') {
                    index = "#";
                }
                objContactDO.setAvatar(avatar);
                objContactDO.setIndex(index);

                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = contResv.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id},
                            null);
                    while (pCur.moveToNext()) {
                        String phoneNum = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phoneNum = phoneNum.replace("\\Q+\\E", "").replace("\\Q-\\E", "").replace(" ", "");
                        Contact copied = (Contact) objContactDO.clone();
                        copied.setPhoneNum(phoneNum);
                        contacts.add(copied);
                        break;
                    }
                    pCur.close();
                }

                if (StringUtil.isNotBlank(objContactDO.getEmailAddress())) {
                    contacts.add(objContactDO);
                }

            } while (cursor.moveToNext());
        }

        cursor.close();
        return contacts;
    }
}
