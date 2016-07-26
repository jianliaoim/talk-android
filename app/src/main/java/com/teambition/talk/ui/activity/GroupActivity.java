package com.teambition.talk.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.squareup.otto.Subscribe;
import com.talk.dialog.TalkDialog;
import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.R;
import com.teambition.talk.adapter.GroupAdapter;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.data.GroupRequestData;
import com.teambition.talk.entity.Group;
import com.teambition.talk.event.RemoveGroupEvent;
import com.teambition.talk.event.UpdateGroupEvent;
import com.teambition.talk.realm.GroupRealm;
import com.teambition.talk.rx.ApiErrorAction;
import com.teambition.talk.rx.EmptyAction;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.ui.widget.MaterialSearchView;
import com.teambition.talk.util.TransactionUtil;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by wlanjie on 15/10/27.
 */
public class GroupActivity extends BaseActivity implements GroupAdapter.OnItemLongClickListener {

    final int CREATE_GROUP = 735;

    final static int EDIT_GROUP_NAME_REQUEST = 0;

    final static int SEARCH_MENU = 736;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @InjectView(R.id.search_view)
    MaterialSearchView mSearchView;

    final GroupAdapter mAdapter = new GroupAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        BusProvider.getInstance().register(this);
        ButterKnife.inject(this);

        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(getString(R.string.my_group));

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter.setOnItemLongClick(this);

        mSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
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
    protected void onResume() {
        super.onResume();
        loadGroups();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void onUpdateGroupEvent(UpdateGroupEvent event) {
        if (event.group != null) {
            mAdapter.updateOne(event.group);
        }
    }

    @Subscribe
    public void onRemoveGroupEvent(RemoveGroupEvent event) {
        if (event.group != null) {
            mAdapter.removeOne(event.group);
        }
    }

    private void loadGroups() {
        GroupRealm.getInstance().getAllGroups()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Group>>() {
                    @Override
                    public void call(List<Group> groups) {
                        mAdapter.clear();
                        mAdapter.addItems(groups);
                    }
                }, new RealmErrorAction());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (BizLogic.isAdmin()) {
            menu.add(Menu.NONE, CREATE_GROUP, Menu.NONE, R.string.action_add_group).setIcon(R.drawable.ic_add_group).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        MenuItem searchItem = menu.add(Menu.NONE, SEARCH_MENU, Menu.NONE, R.string.search_hint).setIcon(R.drawable.ic_search);
        searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        mSearchView.setMenuItem(searchItem);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (mSearchView.isSearchOpen()) {
            mSearchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == CREATE_GROUP) {
            TransactionUtil.goTo(this, AddGroupActivity.class);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case EDIT_GROUP_NAME_REQUEST:
                    if (data != null) {
                        Group group = Parcels.unwrap(data.getParcelableExtra(EditGroupNameActivity.GROUP));
                        if (group != null) {
                            mAdapter.updateOne(group);
                            GroupRealm.getInstance().addOrUpdate(group)
                                    .subscribe(new EmptyAction<Group>(), new RealmErrorAction());
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onLongClick(final Group group) {
        final CharSequence[] items = new CharSequence[]{getResources().getString(R.string.delete), getResources().getString(R.string.edit_name)};
        new TalkDialog.Builder(this)
                .items(items)
                .itemsCallback(new TalkDialog.ListCallback() {
                    @Override
                    public void onSelection(TalkDialog dialog, View itemView, int which, CharSequence text) {
                        if (text.equals(getResources().getString(R.string.delete))) {
                            deleteGroup(group);
                        } else if (text.equals(getResources().getString(R.string.edit_name))) {
                            Intent intent = new Intent(GroupActivity.this, EditGroupNameActivity.class);
                            intent.putExtra(EditGroupNameActivity.GROUP, Parcels.wrap(group));
                            startActivityForResult(intent, EDIT_GROUP_NAME_REQUEST);
                        }
                    }
                }).show();

    }

    void deleteGroup(final Group group) {
        new TalkDialog.Builder(this)
                .title(R.string.title_remove_group)
                .titleColorRes(R.color.white)
                .titleBackgroundColorRes(R.color.colorPrimary)
                .content(getResources().getString(R.string.info_remove_group_confirm))
                .positiveColorRes(R.color.colorPrimary)
                .positiveText(R.string.confirm)
                .negativeColorRes(R.color.material_grey_700)
                .negativeText(R.string.cancel)
                .callback(new TalkDialog.ButtonCallback() {
                    @Override
                    public void onPositive(TalkDialog dialog, View v) {
                        super.onPositive(dialog, v);
                        TalkClient.getInstance().getTalkApi()
                                .removeGroup(group.get_id())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Group>() {
                                    @Override
                                    public void call(Group group) {
                                        if (group != null) {
                                            mAdapter.removeOne(group);
                                            GroupRealm.getInstance().remove(group)
                                                    .subscribe(new EmptyAction<>(), new RealmErrorAction());
                                        }
                                    }
                                }, new ApiErrorAction());
                    }
                })
                .show();
    }
}
