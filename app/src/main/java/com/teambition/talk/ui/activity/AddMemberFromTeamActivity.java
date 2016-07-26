package com.teambition.talk.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.teambition.talk.R;
import com.teambition.talk.adapter.MemberCheckAdapter;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Room;
import com.teambition.talk.realm.MemberRealm;
import com.teambition.talk.realm.RoomRealm;
import com.umeng.analytics.MobclickAgent;

import org.parceler.Parcels;

import java.util.Iterator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by zeatual on 14/11/28.
 */
public class AddMemberFromTeamActivity extends BaseActivity {

    public final static String ROOM = "room";
    public final static String MEMBERS = "members";

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.listView)
    RecyclerView listView;

    private final MemberCheckAdapter adapter = new MemberCheckAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_member_from_team);
        ButterKnife.inject(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.add_from_team);

        listView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(adapter);
        final Room room = Parcels.unwrap(getIntent().getParcelableExtra(ROOM));
        final List<String> memberIds = Parcels.unwrap(getIntent().getParcelableExtra(MEMBERS));

        Observable.create(new Observable.OnSubscribe<List<Member>>() {
            @Override
            public void call(Subscriber<? super List<Member>> subscriber) {
                Room localRoom = RoomRealm.getInstance().getRoomWithCurrentThread(room.get_id());
                if (localRoom != null && memberIds.isEmpty() && localRoom.get_memberIds() != null && !localRoom.get_memberIds().isEmpty()) {
                    memberIds.addAll(localRoom.get_memberIds());
                }
                subscriber.onNext(MemberRealm.getInstance().getAllMemberExceptMeWithCurrentThread());
            }
        }).map(new Func1<List<Member>, List<Member>>() {
            @Override
            public List<Member> call(List<Member> members) {
                Iterator<Member> iterator = members.iterator();
                while (iterator.hasNext()) {
                    Member member = iterator.next();
                    if (member != null) {
                        for (String memberId : memberIds) {
                            if (member.get_id().equals(memberId)) {
                                iterator.remove();
                            }
                        }
                    }
                }
                return members;
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Member>>() {
                    @Override
                    public void call(List<Member> members) {
                        adapter.addItems(members);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_done:
                Intent data = new Intent();
                data.putExtra("members", Parcels.wrap(adapter.getSelectedMembers()));
                setResult(RESULT_OK, data);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
