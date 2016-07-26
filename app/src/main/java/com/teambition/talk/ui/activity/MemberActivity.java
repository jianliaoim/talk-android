package com.teambition.talk.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.imageloader.RecyclerViewPauseOnScrollListener;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.adapter.MemberAdapter;
import com.teambition.talk.entity.Member;
import com.teambition.talk.realm.MemberRealm;
import com.teambition.talk.ui.widget.MaterialSearchView;
import com.teambition.talk.util.AnalyticsHelper;

import org.parceler.Parcels;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by wlanjie on 15/11/17.
 */
public class MemberActivity extends BaseActivity implements MemberAdapter.OnItemClickListener {

    private final static int SEARCH = 0;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @InjectView(R.id.search_view)
    MaterialSearchView searchView;

    final MemberAdapter mAdapter = new MemberAdapter();

    final RecyclerViewPauseOnScrollListener listener = new RecyclerViewPauseOnScrollListener(MainApp.IMAGE_LOADER, true, true, null);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        ButterKnife.inject(this);

        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(getString(R.string.tab_contact));

        mAdapter.setShowMe(false);
        mAdapter.setShowAdmin(false);
        mAdapter.setShowGroup(false);
        mAdapter.setListener(this);

        mRecyclerView.addOnScrollListener(listener);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        Observable.create(new Observable.OnSubscribe<List<Member>>() {
            @Override
            public void call(Subscriber<? super List<Member>> subscriber) {
                subscriber.onNext(MemberRealm.getInstance().getAllMemberExceptMeWithCurrentThread());
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Member>>() {
                    @Override
                    public void call(List<Member> members) {
                        mAdapter.addItems(members);
                    }
                }, new RealmErrorAction());
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.filter(newText);
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecyclerView.removeOnScrollListener(listener);
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem searchItem = menu.add(Menu.NONE, SEARCH, Menu.NONE, R.string.action_search).setIcon(R.drawable.ic_search);
        searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        searchView.setMenuItem(searchItem);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onClick(Member member) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ChatActivity.EXTRA_MEMBER, Parcels.wrap(member));
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);

    }
}
