package com.teambition.talk.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.adapter.MemberAddAdapter;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.data.BatchInviteRequestData;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Room;
import com.teambition.talk.event.UpdateRoomEvent;
import com.teambition.talk.realm.RoomRealm;
import com.teambition.talk.rx.EmptyAction;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.ui.widget.PopupSpinner;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.TransactionUtil;
import com.umeng.analytics.MobclickAgent;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Arrays;
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
public class AddTopicActivity extends BaseActivity implements MemberAddAdapter.OnItemClickListener {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;
    @InjectView(R.id.topic)
    EditText topic;
    @InjectView(R.id.layout_visibility)
    RelativeLayout visibilityLayout;
    @InjectView(R.id.visibility)
    TextView visibilityText;

    private MemberAddAdapter adapter;
    private MenuItem done;

    private LinkedList<Member> members = new LinkedList<>();
    private Room room;
    private boolean isPrivate = false;
    private int selection = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_topic);
        ButterKnife.inject(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.create_topic);

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
        visibilityText.setText(getString(R.string.topic_public));
        visibilityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupSpinner.showPopupSpinner(AddTopicActivity.this, visibilityLayout,
                        Arrays.asList(getString(R.string.topic_public),
                                getString(R.string.topic_private)),
                        new PopupSpinner.OnItemClickListener() {
                            @Override
                            public void onClick(int position, String value) {
                                visibilityText.setText(value);
                                selection = position;
                                isPrivate = position != 0;
                            }
                        }, selection);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            List<Member> result = Parcels.unwrap(data.getParcelableExtra(ChooseMemberActivity.MEMBERS));
            List<String> removeMemberIds = Parcels.unwrap(data.getParcelableExtra(ChooseMemberActivity.REMOVE_MEMBER_IDS));
            if (result != null) {
                Iterator<Member> iterator = result.iterator();
                while (iterator.hasNext()) {
                    Member member = iterator.next();
                    if (member != null && member.get_id().equals(BizLogic.getUserInfo().get_id())) {
                        iterator.remove();
                    }
                }
                adapter.addMembers(result, removeMemberIds);
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
                String topicText = topic.getText().toString();
                if (StringUtil.isBlank(topicText)) {
                    MainApp.showToastMsg(R.string.name_empty);
                } else {
                    List<Room> rooms = new ArrayList<>();
                    rooms.addAll(MainApp.globalRooms.values());
                    if (rooms != null && !rooms.isEmpty()) {
                        for (Room r : rooms) {
                            if (r != null && r.getTopic().equals(topicText)) {
                                MainApp.showToastMsg(R.string.topic_name_exist);
                                return super.onOptionsItemSelected(item);
                            }
                        }
                    }
                    createTopic();
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

    private void createTopic() {
        TalkClient.getInstance().getTalkApi()
                .createRoom(BizLogic.getTeamId(),
                        topic.getText().toString(), isPrivate)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Room>() {
                    @Override
                    public void call(Room roomNew) {
                        if (roomNew != null) {
                            room = roomNew;
                            room.setIsQuit(false);
                            room.setUnread(0);
                            MainApp.globalRooms.put(roomNew.get_id(), roomNew);
                            RoomRealm.getInstance().addOrUpdate(room)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Action1<Room>() {
                                        @Override
                                        public void call(Room room) {
                                            MainApp.IS_ROOM_CHANGED = true;
                                            BusProvider.getInstance().post(new UpdateRoomEvent());
                                        }
                                    }, new RealmErrorAction());
                            addMembers(room.get_id());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        done.setEnabled(true);
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }

    private void addMembers(final String roomId) {
        final List<String> emails = new ArrayList<>();
        for (Member member : members) {
            if (!BizLogic.isMe(member.get_id())) {
                emails.add(member.get_id());
            }
        }
        BatchInviteRequestData data = new BatchInviteRequestData(emails);
        TalkClient.getInstance().getTalkApi()
                .batchInviteToRoom(roomId, data)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<List<Member>>() {
                    @Override
                    public void call(List<Member> members) {
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(ChatActivity.EXTRA_ROOM, Parcels.wrap(room));
                        TransactionUtil.goTo(AddTopicActivity.this, ChatActivity.class, bundle, true);
                        RoomRealm.getInstance()
                                .updateRoomMemberIds(roomId, emails)
                                .subscribe(new EmptyAction<Room>(),
                                        new RealmErrorAction());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        done.setEnabled(true);
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }
}
