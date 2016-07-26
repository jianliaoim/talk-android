package com.teambition.talk.ui.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.squareup.otto.Subscribe;
import com.teambition.talk.BusProvider;
import com.teambition.talk.R;
import com.teambition.talk.adapter.TopicAdapter;
import com.teambition.talk.entity.Room;
import com.teambition.talk.event.RoomRemoveEvent;
import com.teambition.talk.event.UpdateRoomEvent;
import com.teambition.talk.realm.RoomRealm;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.util.TransactionUtil;

import org.parceler.Parcels;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by wlanjie on 15/10/27.
 */
public class TopicActivity extends BaseActivity implements TopicAdapter.OnItemClickListener {

    final int CREATE_ROOM = 736;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;

    private List<Room> roomsJoined;
    private List<Room> roomsToJoin;
    private TopicAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);
        BusProvider.getInstance().register(this);
        ButterKnife.inject(this);

        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(getString(R.string.group_name_topic));

        adapter = new TopicAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initData() {
        RoomRealm.getInstance().getJoinedRooms()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Room>>() {
                    @Override
                    public void call(List<Room> rooms) {
                        roomsJoined = rooms;
                        if (roomsJoined != null && roomsToJoin != null) {
                            adapter.updateData(roomsJoined, roomsToJoin);
                        }
                    }
                }, new RealmErrorAction());
        RoomRealm.getInstance().getToJoinRooms()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Room>>() {
                    @Override
                    public void call(List<Room> rooms) {
                        roomsToJoin = rooms;
                        if (roomsJoined != null && roomsToJoin != null) {
                            adapter.updateData(roomsJoined, roomsToJoin);
                        }
                    }
                }, new RealmErrorAction());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void onUpdateRoomEvent(UpdateRoomEvent event) {
        if (event.room != null) {
            adapter.updateOne(event.room);
        }
    }

    @Subscribe
    public void onRemoveRoomEvent(RoomRemoveEvent event) {
        if (event.roomId != null) {
            adapter.removeOne(event.roomId);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, CREATE_ROOM, Menu.NONE, R.string.action_add_topic)
                .setIcon(R.drawable.ic_plus)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == CREATE_ROOM) {
            TransactionUtil.goTo(this, AddTopicActivity.class);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTopicToJoinClick(Room room) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ChatActivity.EXTRA_ROOM, Parcels.wrap(room));
        bundle.putBoolean(ChatActivity.IS_PREVIEW, true);
        TransactionUtil.goTo(this, ChatActivity.class, bundle);

    }

    @Override
    public void onTopicJoinedClick(Room room) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ChatActivity.EXTRA_ROOM, Parcels.wrap(room));
        TransactionUtil.goTo(this, ChatActivity.class, bundle);
    }

    @Override
    public void onArchiveClick() {
        TransactionUtil.goTo(this, ArchivedTopicActivity.class);
    }

}
