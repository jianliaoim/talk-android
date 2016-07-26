package com.teambition.talk.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.teambition.talk.BizLogic;
import com.teambition.talk.R;
import com.teambition.talk.adapter.RoomMemberAdapter;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Room;
import com.teambition.talk.entity.Story;
import com.teambition.talk.realm.MemberRealm;
import com.teambition.talk.rx.EmptyAction;
import com.umeng.analytics.MobclickAgent;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * Created by zeatual on 14/12/9.
 */
public class SelectMemberActivity extends BaseActivity {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.listView)
    ListView listView;

    private Room room;
    private Story story;
    private Member member;
    private RoomMemberAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_member);
        ButterKnife.inject(this);

        if (getIntent().hasExtra("room")) {
            room = Parcels.unwrap(getIntent().getExtras().getParcelable("room"));
        } else if (getIntent().hasExtra("story")) {
            story = Parcels.unwrap(getIntent().getExtras().getParcelable("story"));
        } else if (getIntent().hasExtra("member")) {
            member = Parcels.unwrap(getIntent().getExtras().getParcelable("member"));
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_choose_member);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new RoomMemberAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("data", Parcels.wrap(adapter.getItem(position)));
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        listView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));

        Observable<List<Member>> memberStream = null;
        if (room != null) {
            memberStream = TalkClient.getInstance().getTalkApi()
                    .readOneRoom(room.get_id())
                    .map(new Func1<Room, List<Member>>() {
                        @Override
                        public List<Member> call(Room room) {
                            return room.getMembers();
                        }
                    });
        } else if (story != null) {
            story = Parcels.unwrap(getIntent().getExtras().getParcelable("story"));
            memberStream = MemberRealm.getInstance().getMembersByIds(story.get_memberIds());
        } else if (member != null) {
            memberStream = Observable.defer(new Func0<Observable<List<Member>>>() {
                @Override
                public Observable<List<Member>> call() {
                    List<Member> members = new ArrayList<>();
                    members.add(member);
                    return Observable.just(members);
                }
            });
        }
        memberStream.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Member>>() {
                    @Override
                    public void call(List<Member> members) {
                        int position = -1;
                        for (int i = 0; i < members.size(); i++) {
                            if (BizLogic.isMe(members.get(i).get_id())) {
                                position = i;
                                break;
                            }
                        }
                        if (position != -1) {
                            members.remove(position);
                        }
                        if (member == null) {
                            Member all = new Member();
                            all.set_id("all");
                            all.setAlias(getString(R.string.at_all));
                            members.add(0, all);
                        }
                        adapter.updateData(members);
                    }
                }, new EmptyAction<Throwable>());
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

    public void onLoadMembersFinish(List<Member> members) {
        int position = -1;
        for (int i = 0; i < members.size(); i++) {
            if (BizLogic.isMe(members.get(i).get_id())) {
                position = i;
                break;
            }
        }
        if (position != -1) {
            members.remove(position);
        }
        adapter.updateData(members);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
