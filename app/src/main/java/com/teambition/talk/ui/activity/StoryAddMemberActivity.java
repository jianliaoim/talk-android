package com.teambition.talk.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.teambition.talk.GsonProvider;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.adapter.MemberCheckAdapter;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.data.UpdateStoryRequestData;
import com.teambition.talk.entity.File;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Story;
import com.teambition.talk.entity.UpdateStory;
import com.teambition.talk.realm.MemberRealm;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by wlanjie on 15/11/17.
 */
public class StoryAddMemberActivity extends BaseActivity {

    private static final int MENU = 1;
    public static final String MEMBERS = "members";
    public static final String MEMBERS_ID = "membersId";
    public static final String STORY = "story";

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private List<Member> mItems;
    private Story mStory;

    final MemberCheckAdapter mAdapter = new MemberCheckAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        ButterKnife.inject(this);

        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(getString(R.string.action_add_member));

        mStory = Parcels.unwrap(getIntent().getParcelableExtra(STORY));
        mItems = Parcels.unwrap(getIntent().getParcelableExtra(MEMBERS));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        Observable.create(new Observable.OnSubscribe<List<Member>>() {
            @Override
            public void call(Subscriber<? super List<Member>> subscriber) {
                subscriber.onNext(MemberRealm.getInstance().getAllMemberExceptMeWithCurrentThread());
            }
        }).map(new Func1<List<Member>, List<Member>>() {
            @Override
            public List<Member> call(List<Member> members) {
                Iterator<Member> iterator = members.iterator();
                while (iterator.hasNext()) {
                    Member member = iterator.next();
                    for (Member item : mItems) {
                        if (item.get_id().equals(member.get_id())) {
                            iterator.remove();
                            break;
                        }
                    }
                }
                return members;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Member>>() {
                    @Override
                    public void call(List<Member> members) {
                        mAdapter.addItems(members);
                    }
                }, new RealmErrorAction());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU, Menu.NONE, R.string.action_done).setIcon(R.drawable.ic_done).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == MENU) {
            updateStory();
        }
        return super.onOptionsItemSelected(item);
    }

    void updateStory() {
        if (mStory == null || mAdapter.getSelectedMembers().isEmpty()) return;
        final List<String> memberIds = new ArrayList<>(mAdapter.getSelectedMembers().size());
        for (Member member : mAdapter.getSelectedMembers()) {
            memberIds.add(member.get_id());
        }
        File file = GsonProvider.getGson().fromJson(mStory.getData(), File.class);
        UpdateStoryRequestData data = new UpdateStoryRequestData();
        data.category = mStory.getCategory();
        data.data = new UpdateStory(file.getFileKey(), file.getFileName());
        data.addMembers = memberIds;
        TalkClient.getInstance().getTalkApi()
                .updateStory(mStory.get_id(), data)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Story>() {
                    @Override
                    public void call(Story story) {
                        if (story == null) {
                            finish();
                            return;
                        }
                        Intent intent = new Intent();
                        intent.putExtra(MEMBERS_ID, Parcels.wrap(memberIds));
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }
}
