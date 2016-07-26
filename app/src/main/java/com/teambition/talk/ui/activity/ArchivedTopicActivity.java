package com.teambition.talk.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.talk.dialog.TalkDialog;
import com.teambition.talk.BizLogic;
import com.teambition.talk.R;
import com.teambition.talk.adapter.ArchivedTopicAdapter;
import com.teambition.talk.entity.Room;
import com.teambition.talk.presenter.ArchivedTopicPresenter;
import com.teambition.talk.realm.RoomRealm;
import com.teambition.talk.rx.EmptyAction;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.util.TransactionUtil;
import com.teambition.talk.view.ArchivedTopicView;

import org.parceler.Parcels;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.schedulers.Schedulers;

/**
 * Created by zeatual on 15/3/16.
 */
public class ArchivedTopicActivity extends BaseActivity implements ArchivedTopicView,
        ArchivedTopicAdapter.OnItemClickListener {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;
    @InjectView(R.id.layout_placeholder)
    View placeholder;

    ArchivedTopicPresenter presenter;
    private ArchivedTopicAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archived_topic);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.archived_room);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ArchivedTopicAdapter(this);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        presenter = new ArchivedTopicPresenter(this);
        presenter.getArchivedRooms();
        if (BizLogic.isNetworkConnected()) {
            presenter.syncArchivedRooms();
        }
    }

    @Override
    public void onItemClick(int position) {
        final Room room = adapter.getItem(position);
        Bundle bundle = new Bundle();
        bundle.putParcelable(ChatActivity.EXTRA_ROOM, Parcels.wrap(room));
        bundle.putBoolean(ChatActivity.IS_ARCHIVE, true);
        TransactionUtil.goTo(this, ChatActivity.class, bundle);
    }

    @Override
    public void onLongItemClick(int position) {
        final Room room = adapter.getItem(position);
        new TalkDialog.Builder(this)
                .title(R.string.restore_room)
                .titleColorRes(R.color.white)
                .titleBackgroundColorRes(R.color.colorPrimary)
                .content(String.format(getString(R.string.restore_room_confirm), room.getTopic()))
                .positiveText(R.string.confirm)
                .negativeText(R.string.cancel)
                .negativeColorRes(R.color.material_grey_700)
                .callback(new TalkDialog.ButtonCallback() {
                    @Override
                    public void onPositive(TalkDialog dialog, View v) {
                        presenter.undoArchive(room);
                    }
                }).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoadArchivedRoomsFinish(List<Room> rooms) {
        if (rooms.isEmpty()) {
            placeholder.setVisibility(View.VISIBLE);
        } else {
            placeholder.setVisibility(View.GONE);
            adapter.updateData(rooms);
        }
    }

    @Override
    public void onUndoArchiveFinish(Room room) {
        room.setIsArchived(false);
        RoomRealm.getInstance().addOrUpdate(room)
                .subscribeOn(Schedulers.io())
                .subscribe(new EmptyAction<Room>(), new RealmErrorAction());
        adapter.removeItem(room);
        if (adapter.getItemCount() == 0) {
            placeholder.setVisibility(View.VISIBLE);
        }
    }
}
