package com.teambition.talk.presenter;


import com.teambition.talk.BizLogic;
import com.teambition.talk.FileDownloader;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.client.data.SearchFavoriteResponseData;
import com.teambition.talk.client.data.SearchRequestData;
import com.teambition.talk.client.data.SearchResponseData;
import com.teambition.talk.entity.Message;
import com.teambition.talk.util.Logger;
import com.teambition.talk.view.SearchView;

import java.util.ArrayList;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by zeatual on 15/3/29.
 */
public class SearchPresenter extends BasePresenter {
    private static final String TAG = SearchPresenter.class.getSimpleName();

    private SearchView callback;

    public SearchPresenter(SearchView callback) {
        this.callback = callback;
    }

    public void searchMessages(String keyword) {
        callback.showProgressBar();
        talkApi.searchMessages(BizLogic.getTeamId(), keyword, 20)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<SearchResponseData>() {
                    @Override
                    public void call(SearchResponseData searchResponseData) {
                        callback.dismissProgressBar();
                        if (searchResponseData.messages != null) {
                            callback.onSearchFinish(searchResponseData.messages);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        callback.dismissProgressBar();
                        callback.onSearchFinish(new ArrayList<Message>());
                    }
                });
    }

    public void searchMessages(String keyword, int page, String creatorId, String roomId, Boolean isDirectMessage, Boolean hasTag, String tagId, String type, String timeRange) {
        callback.showProgressBar();
        talkApi.searchMessages(BizLogic.getTeamId(), keyword, page, 20, creatorId, roomId, isDirectMessage, hasTag, tagId, type, timeRange)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<SearchResponseData>() {
                    @Override
                    public void call(SearchResponseData searchResponseData) {
                        callback.dismissProgressBar();
                        if (searchResponseData.messages != null) {
                            callback.onSearchFinish(searchResponseData.messages);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        callback.dismissProgressBar();
                        callback.onSearchFinish(new ArrayList<Message>());
                    }
                });
    }

    public void search(SearchRequestData data) {
        callback.showProgressBar();
        talkApi.search(data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<SearchResponseData>() {
                    @Override
                    public void call(SearchResponseData searchResponseData) {
                        if (searchResponseData.messages != null) {
                            callback.onSearchFinish(searchResponseData.messages);
                        }
                        callback.dismissProgressBar();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                        callback.dismissProgressBar();
                    }
                });
    }

    public void deleteMessage(final String messageId) {
        talkApi.deleteMessage(messageId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        callback.onDeleteMessageSuccess(messageId);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }

    public void downloadFile(final String url, final String path) {
        FileDownloader.getInstance().startDownload(url, path, new Action1<Integer>() {
            @Override
            public void call(Integer progress) {
                if (progress == FileDownloader.FINISH) {
                    callback.onDownloadFinish(path);
                } else {
                    callback.onDownloadProgress(progress);
                }
            }
        }, null);
    }

    public void favoriteMessage(String messageId) {
        talkApi.favoriteMessage(messageId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        MainApp.showToastMsg(R.string.favorite_success);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        Logger.e(TAG, "favorite error", e);
                    }
                });
    }

    public void searchFavorites(SearchRequestData data) {
        callback.showProgressBar();
        talkApi.searchFavorites(data)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<SearchFavoriteResponseData>() {
                    @Override
                    public void call(SearchFavoriteResponseData data) {
                        if (data.favorites != null) {
                            callback.onSearchFinish(data.favorites);
                        }
                        callback.dismissProgressBar();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        MainApp.showToastMsg(R.string.network_failed);
                        callback.dismissProgressBar();
                    }
                });
    }
}
