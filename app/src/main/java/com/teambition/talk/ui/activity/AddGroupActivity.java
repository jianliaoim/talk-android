package com.teambition.talk.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.rx.ApiErrorAction;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.adapter.MemberAddAdapter;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.data.GroupRequestData;
import com.teambition.talk.entity.Group;
import com.teambition.talk.entity.Member;
import com.teambition.talk.event.UpdateGroupEvent;
import com.teambition.talk.realm.GroupRealm;
import com.teambition.talk.util.StringUtil;
import com.umeng.analytics.MobclickAgent;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by zeatual on 15/2/3.
 */
public class AddGroupActivity extends BaseActivity implements MemberAddAdapter.OnItemClickListener {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;
    @InjectView(R.id.tv_name)
    EditText tvName;

    private MemberAddAdapter adapter;
    private MenuItem done;

    private LinkedList<Member> members = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);
        ButterKnife.inject(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.create_group);

        initData();
        initView();
    }

    private void initData() {
        members.add(MainApp.globalMembers.get(BizLogic.getUserInfo().get_id()));
        adapter = new MemberAddAdapter(this, members, this);
    }

    private void initView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            List<Member> result = Parcels.unwrap(data.getParcelableExtra(ChooseMemberActivity.MEMBERS));
            if (result != null) {
                Iterator<Member> iterator = result.iterator();
                while (iterator.hasNext()) {
                    Member member = iterator.next();
                    if (member != null && member.get_id().equals(BizLogic.getUserInfo().get_id())) {
                        iterator.remove();
                    }
                }
                members.addAll(result);
                adapter.notifyDataSetChanged();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        MobclickAgent.onPageStart(getClass().getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        MobclickAgent.onPageEnd(getClass().getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        done = menu.findItem(R.id.action_done);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_done:
                final String topicText = tvName.getText().toString();
                if (StringUtil.isBlank(topicText)) {
                    MainApp.showToastMsg(R.string.name_empty);
                } else {
                    GroupRealm.getInstance().getAllGroups()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<List<Group>>() {
                                @Override
                                public void call(List<Group> groups) {
                                    for (Group group : groups) {
                                        if (group != null && group.getName().equals(topicText)) {
                                            MainApp.showToastMsg(R.string.group_name_exist);
                                            return;
                                        }
                                    }
                                    createGroup();
                                }
                            }, new RealmErrorAction());
                    done.setEnabled(false);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(int position) {
        if (position == adapter.getItemCount() - 1) {
            Intent intent = new Intent(this, ChooseMemberActivity.class);
            intent.putExtra(ChooseMemberActivity.MEMBERS, Parcels.wrap(members));
            startActivityForResult(intent, 0);
        }
    }

    private void createGroup() {
        List<String> memberIds = new ArrayList<>();
        for (Member member : members) {
            memberIds.add(member.get_id());
        }
        GroupRequestData data = new GroupRequestData();
        data._teamId = BizLogic.getTeamId();
        data.name = tvName.getText().toString();
        data._memberIds = memberIds;
        TalkClient.getInstance().getTalkApi()
                .createGroup(data)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Group>() {
                    @Override
                    public void call(Group group) {
                        GroupRealm.getInstance().addOrUpdate(group)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Group>() {
                                    @Override
                                    public void call(Group group) {
                                        BusProvider.getInstance().post(new UpdateGroupEvent(group));
                                    }
                                }, new RealmErrorAction());
                        finish();
                    }
                }, new ApiErrorAction() {
                    @Override
                    protected void call() {
                        done.setEnabled(true);
                    }
                });
    }

}
