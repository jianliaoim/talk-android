package com.teambition.talk.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.teambition.talk.BizLogic;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.adapter.FavoritesAdapter;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Story;
import com.teambition.talk.presenter.MentionedMessagePresenter;
import com.teambition.talk.realm.StoryRealm;
import com.teambition.talk.rx.EmptyAction;
import com.teambition.talk.view.MentionedMessageView;

import org.parceler.Parcels;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by nlmartian on 1/30/16.
 */
public class MentionedMeActivity extends BaseActivity implements MentionedMessageView, AdapterView.OnItemClickListener {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.listView)
    ListView listView;
    @InjectView(R.id.layout_placeholder)
    View placeholder;

    private boolean isLoading;
    private boolean canLoadMore;
    private String maxId;
    private FavoritesAdapter adapter;
    private MentionedMessagePresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mentioned_me);
        ButterKnife.inject(this);
        progressBar = findViewById(R.id.progress_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.at_me);

        adapter = new FavoritesAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(new PauseOnScrollListener(MainApp.IMAGE_LOADER, true, true, new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount > visibleItemCount && canLoadMore && !isLoading) {
                    if (firstVisibleItem + visibleItemCount + 6 > adapter.getCount()) {
                        isLoading = true;
                        presenter.getMessageMentionedMe(BizLogic.getTeamId(), maxId);
                    }
                }
            }
        }));
        presenter = new MentionedMessagePresenter(this);
        presenter.getMessageMentionedMe(BizLogic.getTeamId(), null);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Message message = adapter.getItem(position);
        final Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_MESSAGE, Parcels.wrap(message));
        if (message.getStory() != null) {
            intent.putExtra(ChatActivity.EXTRA_STORY, Parcels.wrap(message.getStory()));
            startActivity(intent);
        } else {
            startActivity(intent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showMessages(List<Message> messageList) {
        canLoadMore = !messageList.isEmpty() && messageList.size() >= 30;
        if (maxId == null) {
            if (!messageList.isEmpty()) {
                adapter.updateData(messageList);
                placeholder.setVisibility(View.GONE);
            } else {
                placeholder.setVisibility(View.VISIBLE);
            }
        } else {
            adapter.addToEnd(messageList);
        }
        if (!messageList.isEmpty()) {
            maxId = messageList.get(messageList.size() - 1).get_id();
        }
        isLoading = false;
    }

}
