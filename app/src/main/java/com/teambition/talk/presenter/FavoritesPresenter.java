package com.teambition.talk.presenter;

import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.client.data.RemoveFavoritesRequestData;
import com.teambition.talk.client.data.RepostData;
import com.teambition.talk.entity.Message;
import com.teambition.talk.util.Logger;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.view.FavoritesView;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by nlmartian on 5/27/15.
 */
public class FavoritesPresenter extends BasePresenter {
    public static final String TAG = FavoritesPresenter.class.getSimpleName();

    private FavoritesView favoritesView;

    public FavoritesPresenter(FavoritesView view) {
        this.favoritesView = view;
    }

    public void getFavorites(String teamId, String maxId) {
        favoritesView.showProgressBar();
        Observable<List<Message>> observable = StringUtil.isBlank(maxId) ? talkApi.getFavorites(teamId)
                : talkApi.getFavorites(teamId, maxId);
        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Message>>() {
                    @Override
                    public void call(List<Message> favorites) {
                        favoritesView.dismissProgressBar();
                        favoritesView.showFavorites(favorites);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        favoritesView.dismissProgressBar();
                        Logger.e(TAG, "get favorites error", e);
                    }
                });
    }

    public void batchRemove(final List<String> removeIds) {
        favoritesView.showProgressBar();
        talkApi.batchRemoveFavorites(new RemoveFavoritesRequestData(removeIds))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        favoritesView.removeFavoritesSuccess(removeIds);
                        favoritesView.dismissProgressBar();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                        favoritesView.dismissProgressBar();
                    }
                });

    }

    public void repostMessage(String messageId, String teamId, String roomId, String userId) {
        talkApi.favoritesRepostMessage(messageId, new RepostData(teamId, roomId, userId, null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Message>() {
                    @Override
                    public void call(Message message) {
                        favoritesView.onRepostFinish(message);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }
}
