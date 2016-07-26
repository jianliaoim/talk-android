package com.teambition.talk.ui.activity;

import android.os.Bundle;
import android.view.View;

import com.talk.dialog.TalkDialog;
import com.teambition.talk.BizLogic;
import com.teambition.talk.FileDownloader;
import com.teambition.talk.MainApp;
import com.teambition.talk.MediaController;
import com.teambition.talk.R;
import com.teambition.talk.client.data.SearchRequestData;
import com.teambition.talk.entity.Message;
import com.teambition.talk.presenter.SearchPresenter;
import com.teambition.talk.realm.MessageDataProcess;
import com.teambition.talk.util.FileUtil;
import com.teambition.talk.util.Logger;
import com.teambition.talk.view.SearchView;

import org.parceler.Parcels;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by zeatual on 15/5/7.
 */
public class ItemPhotoViewActivity extends PhotoViewActivity implements SearchView {

    private SearchPresenter presenter;

    private String msgId;
    private SearchRequestData searchData;

    private int originPage;
    private int leftPage;
    private int rightPage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        msgId = getIntent().getExtras().getString("msgId");
        searchData = Parcels.unwrap(getIntent().getExtras().getParcelable("data"));

        presenter = new SearchPresenter(this);

        if (msgId != null && searchData != null) {
            originPage = searchData.page;
            leftPage = searchData.page;
            rightPage = searchData.page;
            presenter.search(searchData);
        }
    }

    @Override
    public void onSearchFinish(List<Message> messages) {
        if (searchData.page == originPage) {
            setCanLoadRight(messages.size() != 0);
            setCanLoadLeft(originPage > 1);
            int position = messages.size();
            for (int i = messages.size() - 1; i >= 0; i--) {
                if (msgId.equals(messages.get(i).get_id())) {
                    position = i;
                    break;
                }
            }
            initPager(position, messages);
        } else if (searchData.page < originPage) {
            setCanLoadLeft(leftPage > 1);
            if (!messages.isEmpty()) {
                addToLeft(messages);
            }
        } else if (searchData.page > originPage) {
            setCanLoadRight(messages.size() != 0);
            if (!messages.isEmpty()) {
                addToRight(messages);
            }
        }
        setLoading(false);
    }

    @Override
    public void onDeleteMessageSuccess(String messageId) {
        finish();
    }

    @Override
    public void onDownloadFinish(String path) {
        MainApp.showToastMsg(R.string.save_finish_message);
    }

    @Override
    public void onDownloadProgress(Integer progress) {
        // no use
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
        leftPage--;
        searchData.page = leftPage;
        setLoading(true);
        presenter.search(searchData);
    }

    @Override
    void loadRight() {
        rightPage++;
        searchData.page = rightPage;
        setLoading(true);
        presenter.search(searchData);
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
        final String sourcePath = FileDownloader.getCachePath(MessageDataProcess.getInstance().getFile(message).getFileKey(),
                MessageDataProcess.getInstance().getFile(message).getFileType());
        final String savePath = FileDownloader.getDownloadPath(MessageDataProcess.getInstance().getFile(message).getFileName());
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
                        onDownloadFinish(path);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        Logger.e("ItemPhotoViewActivity", "saving photo error", e);
                    }
                });
    }

}
