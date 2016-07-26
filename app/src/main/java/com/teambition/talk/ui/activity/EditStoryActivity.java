package com.teambition.talk.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;

import com.teambition.talk.BusProvider;
import com.teambition.talk.R;
import com.teambition.talk.rx.ApiErrorAction;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.data.UpdateStoryRequestData;
import com.teambition.talk.entity.Notification;
import com.teambition.talk.entity.Story;
import com.teambition.talk.event.UpdateNotificationEvent;
import com.teambition.talk.realm.NotificationRealm;
import com.teambition.talk.rx.RealmErrorAction;

import org.parceler.Parcels;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zeatual on 15/10/29.
 */
public abstract class EditStoryActivity extends BaseActivity {

    public static final String IS_EDIT = "is_edit";
    public static final int REQUEST_EDIT_STORY = 766;

    protected Story story;
    protected InputMethodManager inputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        story = Parcels.unwrap(getIntent().getParcelableExtra("story"));
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    protected void updateStory(String id, UpdateStoryRequestData data) {
        TalkClient.getInstance().getTalkApi()
                .updateStory(id, data)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Story>() {
                    @Override
                    public void call(Story story) {
                        hideInputMethodManager();
                        updateNotification(story);
                        Intent intent = new Intent();
                        intent.putExtra("story", Parcels.wrap(story));
                        setResult(RESULT_OK, intent);
                        finish();
                        overridePendingTransition(0, R.anim.anim_fade_transition_out);
                    }
                }, new ApiErrorAction());
    }

    protected void hideInputMethodManager() {

    }

    private void updateNotification(final Story story){
        NotificationRealm.getInstance()
                .getSingleNotificationByTargetId(story.get_id())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Notification>() {
                    @Override
                    public void call(Notification notification) {
                        notification.setStory(story);
                        BusProvider.getInstance().post(new UpdateNotificationEvent(notification));
                    }
                }, new RealmErrorAction());
    }
}
