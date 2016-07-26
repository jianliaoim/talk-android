package com.teambition.talk.client.data;

import java.util.List;

/**
 * Created by nlmartian on 5/29/15.
 */
public class RemoveFavoritesRequestData {
    public List<String> _favoriteIds;

    public RemoveFavoritesRequestData(List<String> _favoriteIds) {
        this._favoriteIds = _favoriteIds;
    }
}
