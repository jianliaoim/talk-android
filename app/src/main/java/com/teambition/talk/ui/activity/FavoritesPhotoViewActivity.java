package com.teambition.talk.ui.activity;

import android.os.Bundle;

import com.teambition.talk.client.data.SearchRequestData;
import com.teambition.talk.entity.Message;
import com.teambition.talk.presenter.SearchPresenter;
import com.teambition.talk.view.SearchView;

import org.parceler.Parcels;

import java.util.List;

/**
 * Created by zeatual on 15/5/7.
 */
public class FavoritesPhotoViewActivity extends PhotoViewActivity implements SearchView {

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
        searchData.type = "file";

        presenter = new SearchPresenter(this);

        if (msgId != null && searchData != null) {
            originPage = searchData.page;
            leftPage = searchData.page;
            rightPage = searchData.page;
            presenter.searchFavorites(searchData);
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

    }

    @Override
    public void onDownloadFinish(String path) {

    }

    @Override
    public void onDownloadProgress(Integer progress) {

    }

    @Override
    void loadLeft() {
        leftPage--;
        searchData.page = leftPage;
        setLoading(true);
        presenter.searchFavorites(searchData);
    }

    @Override
    void loadRight() {
        rightPage++;
        searchData.page = rightPage;
        setLoading(true);
        presenter.searchFavorites(searchData);
    }
}
