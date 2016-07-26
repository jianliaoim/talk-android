package com.teambition.talk.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.talk.dialog.TalkDialog;
import com.teambition.talk.R;
import com.teambition.talk.adapter.GroupMemberAdapter;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.data.GroupRequestData;
import com.teambition.talk.entity.Group;
import com.teambition.talk.entity.Member;
import com.teambition.talk.realm.GroupRealm;
import com.teambition.talk.realm.MemberRealm;
import com.teambition.talk.rx.ApiErrorAction;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.ui.widget.MaterialSearchView;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by wlanjie on 15/12/28.
 */
public class GroupMemberActivity extends BaseActivity implements GroupMemberAdapter.OnRemoveClickListener, GroupMemberAdapter.OnItemClickListener {

    final static int SEARCH_MENU = 1;
    public final static int CHOOSE_MEMBER_REQUEST = 0;
    public final static String MEMBER_IDS = "member_ids";
    public final static String GROUP= "group";

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.search_view)
    MaterialSearchView mSearchView;

    @InjectView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    GroupMemberAdapter mAdapter;
    private Group mGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_member);

        ButterKnife.inject(this);
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.group_manager);

        mGroup = Parcels.unwrap(getIntent().getParcelableExtra(GROUP));
        mAdapter = new GroupMemberAdapter(mGroup);

        final LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);

        List<String> memberIds = Parcels.unwrap(getIntent().getParcelableExtra(MEMBER_IDS));
        MemberRealm.getInstance().getMembersByIds(memberIds)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Member>>() {
                    @Override
                    public void call(List<Member> members) {
                        mAdapter.setMembers(members);
                    }
                }, new RealmErrorAction());
        mAdapter.setOnRemoveListener(this);
        mAdapter.setOnItemClickListener(this);

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
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CHOOSE_MEMBER_REQUEST:
                    if (data != null) {
                        List<Member> members = Parcels.unwrap(data.getParcelableExtra(ChooseMemberActivity.MEMBERS));
                        addMember(members);
                    }
                    break;
            }
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem searchItem = menu.add(Menu.NONE, SEARCH_MENU, Menu.NONE, R.string.search_hint).setIcon(R.drawable.ic_search);
        searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        mSearchView.setMenuItem(searchItem);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRemove(final Member member) {
        if (member == null) return;
        new TalkDialog.Builder(this)
                .title(R.string.title_remove_member)
                .titleColorRes(R.color.white)
                .titleBackgroundColorRes(R.color.colorPrimary)
                .content(String.format(getResources().getString(R.string.info_remove_story_confirm), ""))
                .positiveColorRes(R.color.colorPrimary)
                .positiveText(R.string.confirm)
                .negativeColorRes(R.color.material_grey_700)
                .negativeText(R.string.cancel)
                .callback(new TalkDialog.ButtonCallback() {
                    @Override
                    public void onPositive(TalkDialog dialog, View v) {
                        super.onPositive(dialog, v);
                        List<String> removeMemberIds = new ArrayList<>();
                        removeMemberIds.add(member.get_id());
                        final GroupRequestData data = new GroupRequestData();
                        data.removeMembers = removeMemberIds;
                        TalkClient.getInstance().getTalkApi()
                                .updateGroup(mGroup.get_id(), data)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Group>() {
                                    @Override
                                    public void call(Group group) {
                                        mAdapter.removeMemberById(member.get_id());
                                        if (group != null) {
                                            GroupRealm.getInstance().addOrUpdate(group)
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(new Action1<Group>() {
                                                        @Override
                                                        public void call(Group group) {

                                                        }
                                                    }, new RealmErrorAction());
                                        }
                                    }
                                }, new ApiErrorAction());
                    }
                })
                .show();
    }

    @Override
    public void onItemClick(int position) {
        if (position == mAdapter.getItems().size()) {
            Intent intent = new Intent(this, ChooseMemberActivity.class);
            intent.putExtra(ChooseMemberActivity.MEMBERS, Parcels.wrap(mAdapter.getItems()));
            startActivityForResult(intent, CHOOSE_MEMBER_REQUEST);
        }
    }

    void addMember(final List<Member> members) {
        if (members == null || members.isEmpty()) return;
        final List<String> memberIds = new ArrayList<>(members.size());
        for (Member member : members) {
            if (member != null) {
                memberIds.add(member.get_id());
            }
        }
        final GroupRequestData data = new GroupRequestData();
        data.addMembers = memberIds;
        TalkClient.getInstance().getTalkApi()
                .updateGroup(mGroup.get_id(), data)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Group>() {
                    @Override
                    public void call(Group group) {
                        mAdapter.addMembers(members);
                        if (group != null) {
                            GroupRealm.getInstance().addOrUpdate(group)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Action1<Group>() {
                                        @Override
                                        public void call(Group group) {

                                        }
                                    }, new RealmErrorAction());
                        }
                    }
                }, new ApiErrorAction());
    }
}
