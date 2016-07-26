package com.teambition.talk.view;

import com.teambition.talk.entity.Message;

import java.util.List;

/**
 * Created by nlmartian on 5/27/15.
 */
public interface FavoritesView extends BaseView {
    void showFavorites(List<Message> favoriteList);

    void removeFavoritesSuccess(List<String> removedIds);

    void onRepostFinish(Message message);
}
