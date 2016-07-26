package com.teambition.talk.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.talk.dialog.TalkDialog;
import com.teambition.talk.BizLogic;
import com.teambition.talk.FileDownloader;
import com.teambition.talk.MainApp;
import com.teambition.talk.MediaController;
import com.teambition.talk.R;
import com.teambition.talk.client.data.MessageRequestData;
import com.teambition.talk.entity.File;
import com.teambition.talk.entity.Message;
import com.teambition.talk.presenter.ChatPhotoPresenter;
import com.teambition.talk.realm.MessageDataProcess;
import com.teambition.talk.util.FileUtil;
import com.teambition.talk.util.Logger;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.view.ChatPhotoView;

import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by zeatual on 15/5/11.
 */
public class ChatPhotoViewActivity extends PhotoViewActivity implements ChatPhotoView {

    private ChatPhotoPresenter presenter;

    private String msgId;
    private Map<String, String> oldMap;
    private Map<String, String> newMap;

    public static Intent getIntent(Context context, String msgId, String roomId, String memberId,
                                   String storyId, String creatorId) {
        Intent intent = new Intent(context, ChatPhotoViewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("msgId", msgId);
        bundle.putString("creatorId", creatorId);
        if (StringUtil.isNotBlank(roomId)) {
            bundle.putString("roomId", roomId);
        }
        if (StringUtil.isNotBlank(memberId)) {
            bundle.putString("memberId", memberId);
        }
        if (StringUtil.isNotBlank(storyId)) {
            bundle.putString("storyId", storyId);
        }
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        msgId = getIntent().getExtras().getString("msgId");
        String roomId = getIntent().getExtras().getString("roomId", null);
        String memberId = getIntent().getExtras().getString("memberId", null);
        String storyId = getIntent().getExtras().getString("storyId", null);
        String creatorId = getIntent().getExtras().getString("creatorId", null);
        Map<String, String> initData = MessageRequestData.getImageQueryMap(BizLogic.getTeamId(),
                roomId, memberId, storyId, null, null, msgId, creatorId);
        oldMap = MessageRequestData.getImageQueryMap(BizLogic.getTeamId(), roomId, memberId, storyId,
                null, null, null, creatorId);
        newMap = MessageRequestData.getImageQueryMap(BizLogic.getTeamId(), roomId, memberId, storyId,
                null, null, null, creatorId);

        presenter = new ChatPhotoPresenter(this);
        presenter.init(initData);
    }

    @Override
    public void onInitFinish(List<Message> messages) {
        if (!messages.isEmpty()) {
            oldMap.put("_maxId", messages.get(0).get_id());
            newMap.put("_minId", messages.get(messages.size() - 1).get_id());
        }
        int position = 0;
        for (int i = 0; i < messages.size(); i++) {
            if (msgId.equals(messages.get(i).get_id())) {
                position = i;
                break;
            }
        }
        setCanLoadLeft(position == 14);
        setCanLoadRight(messages.size() - position == 16);
        initPager(position, messages);
        setLoading(false);
    }

    @Override
    public void onLoadOldFinish(List<Message> messages) {
        setCanLoadLeft(!messages.isEmpty());
        if (!messages.isEmpty()) {
            oldMap.put("_maxId", messages.get(0).get_id());
            addToLeft(messages);
        }
        setLoading(false);
    }

    @Override
    public void onLoadNewFinish(List<Message> messages) {
        setCanLoadRight(!messages.isEmpty());
        if (!messages.isEmpty()) {
            addToRight(messages);
            newMap.put("_minId", messages.get(messages.size() - 1).get_id());
        }
        setLoading(false);
    }

    @Override
    public void onDeleteMessageSuccess() {
        finish();
    }

    @Override
    protected void initPager(int position, List<Message> messages) {
        View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Message currentMsg = adapter.getItem(mViewPager.getCurrentItem());
                showDialog(currentMsg);
                return true;
            }
        };
        int p = resetPosition(position, messages);
        adapter = new PhotoPagerAdapter(this, removePSD(messages), longClickListener);
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(p);
        if (canLoadLeft && position == 0) {
            loadLeft();
        }
    }

    @Override
    void loadLeft() {
        setLoading(true);
        if (oldMap != null) {
            presenter.loadOld(oldMap);
        }
    }

    @Override
    void loadRight() {
        setLoading(true);
        if (newMap != null) {
            presenter.loadNew(newMap);
        }
    }

    private void showDialog(final Message message) {
        CharSequence[] actions = {};
        if (BizLogic.isMe(message.get_creatorId()) || BizLogic.isAdmin()) {
            actions = new CharSequence[]{getString(R.string.favorite),
                    getString(R.string.save_to_local),
                    getString(R.string.delete)};
        } else {
            actions = new CharSequence[]{getString(R.string.favorite),
                    getString(R.string.save_to_local)};
        }
        new TalkDialog.Builder(this)
                .items(actions)
                .itemsCallback(new TalkDialog.ListCallback() {
                    @Override
                    public void onSelection(TalkDialog materialDialog, View view, int i, CharSequence charSequence) {
                        String action = charSequence.toString();
                        if (getString(R.string.delete).equals(action)) {
                            presenter.deleteMessage(message.get_id());
                        } else if (getString(R.string.save_to_local).equals(action)) {
                            saveCacheToDownload(message);
                        } else if (getString(R.string.favorite).equals(action)) {
                            presenter.favoriteMessage(message.get_id());
                        }
                    }
                }).show();
    }

    private void saveCacheToDownload(Message message) {
        final File file = MessageDataProcess.getInstance().getImages(message).get(0);
        final String sourcePath = FileDownloader.getCachePath(file.getFileKey(), file.getFileType());
        final String savePath = FileDownloader.getDownloadPath(file.getFileName());
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    FileUtil.copyFile(sourcePath, savePath);
                    subscriber.onNext(savePath);
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String path) {
                        MediaController.updateSystemGallery(path);
                        MainApp.showToastMsg(getString(R.string.save_finish_message, path));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        Logger.e("ItemPhotoViewActivity", "saving photo error", e);
                    }
                });
    }
}
