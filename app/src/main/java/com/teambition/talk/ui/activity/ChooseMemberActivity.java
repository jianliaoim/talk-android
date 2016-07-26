package com.teambition.talk.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.teambition.talk.BizLogic;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.imageloader.RecyclerViewPauseOnScrollListener;
import com.teambition.talk.adapter.ChooseMemberAdapter;
import com.teambition.talk.adapter.ChooseMemberResultAdapter;
import com.teambition.talk.entity.Group;
import com.teambition.talk.entity.Member;
import com.teambition.talk.realm.GroupRealm;
import com.teambition.talk.realm.MemberRealm;
import com.teambition.talk.ui.widget.MaterialSearchView;
import com.teambition.talk.util.Logger;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Created by zeatual on 15/10/20.
 */
public class ChooseMemberActivity extends BaseActivity implements
        ChooseMemberAdapter.OnItemClickListener, ChooseMemberResultAdapter.OnItemClickListener {
    public static final String TAG = ChooseMemberActivity.class.getSimpleName();

    private final static int SEARCH = 0;
    private final static int DONE = 1;

    public static final int REQUEST_CHOOSE_MEMBER = 886;
    public static final String MEMBERS_IDS = "member_ids";
    public static final String MEMBERS = "members";
    public static final String REMOVE_MEMBER_IDS = "remove_member_ids";
    public static final String IS_ORDINARY = "is_ordinary";

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.recycler_view_results)
    RecyclerView recyclerViewResults;
    @InjectView(R.id.recycler_view_data)
    RecyclerView recyclerViewData;
    @InjectView(R.id.search_view)
    MaterialSearchView searchView;

    private LinearLayoutManager resultManager;
    private LinearLayoutManager dataManager;
    private ChooseMemberResultAdapter resultAdapter;
    private ChooseMemberAdapter dataAdapter;
    private List<Member> members;
    private List<Group> groups;
    private boolean isOrdinary; // 是否是普通成员
    private List<String> originalMemberIds = new ArrayList<>();
    final RecyclerViewPauseOnScrollListener listener = new RecyclerViewPauseOnScrollListener(MainApp.IMAGE_LOADER, true, true, null);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_member);
        ButterKnife.inject(this);

        toolbar.setTitle(R.string.title_choose_member);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        resultManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        dataManager = new LinearLayoutManager(this);

        dataAdapter = new ChooseMemberAdapter(this);
        resultAdapter = new ChooseMemberResultAdapter(this);
        recyclerViewData.setLayoutManager(dataManager);
        recyclerViewResults.setLayoutManager(resultManager);
        recyclerViewResults.addOnScrollListener(listener);
        recyclerViewData.addOnScrollListener(listener);

        initData();
        initView();
        initSearch();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recyclerViewData.removeOnScrollListener(listener);
        recyclerViewResults.removeOnScrollListener(listener);
    }

    private void initSearch() {
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    dataAdapter.reset();
                } else {
                    dataAdapter.filter(newText);
                }
                return false;
            }
        });
    }

    private void initData() {
        isOrdinary = getIntent().getBooleanExtra(IS_ORDINARY, false);
        final List<Member> items = Parcels.unwrap(getIntent().getParcelableExtra(MEMBERS));
        if (isOrdinary) {
            for (Member member : items) {
                originalMemberIds.add(member.get_id());
            }
        }
        members = new ArrayList<>();
        Observable.zip(Observable.create(new Observable.OnSubscribe<List<Member>>() {
            @Override
            public void call(Subscriber<? super List<Member>> subscriber) {
                subscriber.onNext(MemberRealm.getInstance().getMemberWithCurrentThread());
            }
        }), Observable.create(new Observable.OnSubscribe<List<Group>>() {
            @Override
            public void call(Subscriber<? super List<Group>> subscriber) {
                subscriber.onNext(GroupRealm.getInstance().getAllGroupsWithCurrentThread());
            }
        }), new Func2<List<Member>, List<Group>, Object>() {
            @Override
            public Object call(List<Member> members, List<Group> groups) {
                ChooseMemberActivity.this.members = members;
                ChooseMemberActivity.this.groups = groups;
                return null;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        dataAdapter.addMemberIds(items);
                        dataAdapter.setData(members, groups);
                        for (Member member : members) {
                            if (member != null && member.get_id().equals(BizLogic.getUserInfo().get_id())) {
                                resultAdapter.addMe(member);
                            }
                        }
                        resultAdapter.addMembers(items);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.e(TAG, "initData error", throwable);
                    }
                });
    }

    private void initView() {
        dataAdapter.setIsOrdinary(isOrdinary);
        recyclerViewData.setLayoutManager(dataManager);
        recyclerViewData.setAdapter(dataAdapter);
        recyclerViewResults.setLayoutManager(resultManager);
        recyclerViewResults.setAdapter(resultAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem searchItem = menu.add(Menu.NONE, SEARCH, Menu.NONE, R.string.action_search).setIcon(R.drawable.ic_search);
        searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        searchView.setMenuItem(searchItem);
        menu.add(Menu.NONE, DONE, Menu.NONE, R.string.action_done).setIcon(R.drawable.ic_done).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (dataAdapter.getIsGroup()) {
            dataAdapter.setIsGroup(false);
        } else if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (dataAdapter.getIsGroup()) {
                    dataAdapter.setIsGroup(false);
                } else {
                    finish();
                }
                break;
            case DONE:
                Intent intent = new Intent();
                intent.putExtra(MEMBERS, Parcels.wrap(resultAdapter.getMembers()));
                intent.putExtra(REMOVE_MEMBER_IDS, Parcels.wrap(dataAdapter.getRemoveMemberIds()));
                setResult(RESULT_OK, intent);
                finish();
                break;
            case SEARCH:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onGroupClick(Group group) {
        dataAdapter.setIsGroup(false);
        final List<String> memberIds = group.get_memberIds();
        Observable.create(new Observable.OnSubscribe<List<Member>>() {
            @Override
            public void call(Subscriber<? super List<Member>> subscriber) {
                List<Member> results = new ArrayList<>();
                for (String id : memberIds) {
                    for (Member member : members) {
                        if (member.get_id().equals(id)) {
                            results.add(member);
                            break;
                        }
                    }
                }
                subscriber.onNext(results);
            }
        }).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Member>>() {
                    @Override
                    public void call(List<Member> members) {
                        dataAdapter.rangeSelect(members);
                        resultAdapter.addMembers(members);
                        recyclerViewResults.scrollToPosition(resultAdapter.getItemCount() - 1);
                    }
                });

    }

    @Override
    public void onMemberClick(Member member) {
        // 普通成员(非管理员非创建者)可以添加story成员，不能移除
        if (!isOrdinary || !originalMemberIds.contains(member.get_id())) {
            resultAdapter.updateMember(member);
            recyclerViewResults.scrollToPosition(resultAdapter.getItemCount() - 1);
        }
    }

    @Override
    public void onItemClick(Member member) {
        // 普通成员(非管理员非创建者)可以添加story成员，不能移除
        if (!isOrdinary || !originalMemberIds.contains(member.get_id())) {
            dataAdapter.updateSelectedMember(member);
        }
    }
}
