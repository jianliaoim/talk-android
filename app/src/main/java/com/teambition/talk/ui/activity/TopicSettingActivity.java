package com.teambition.talk.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;

import com.squareup.otto.Subscribe;
import com.talk.dialog.TalkDialog;
import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.adapter.TopicSettingAdapter;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.data.BatchInviteRequestData;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Room;
import com.teambition.talk.event.UpdateMemberEvent;
import com.teambition.talk.presenter.TopicSettingPresenter;
import com.teambition.talk.realm.RoomRealm;
import com.teambition.talk.rx.EmptyAction;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.ui.MemberInfoDialog;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.view.TopicSettingView;
import com.umeng.analytics.MobclickAgent;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by zeatual on 15/3/12.
 */
public class TopicSettingActivity extends BaseActivity implements TopicSettingView, TopicSettingAdapter.TopicSettingListener {

    public static final String EXTRA_NAME = "room";
    private static final int ADD_MEMBER = 0;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.recycler_view)
    RecyclerView recyclerView;

    private TopicSettingPresenter presenter;
    private TopicSettingAdapter mAdapter;
    private List<String> memberIds;
    private Room room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_setting);
        ButterKnife.inject(this);
        room = Parcels.unwrap(getIntent().getExtras().getParcelable("room"));
        if (room.getPurpose() == null) {
            room.setPurpose("");
        }
        BusProvider.getInstance().register(this);
        progressBar = findViewById(R.id.progress_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.topic_setting);

        mAdapter = new TopicSettingAdapter(room);
        mAdapter.setListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(mAdapter);

        memberIds = new ArrayList<>();
        presenter = new TopicSettingPresenter(this);

        presenter.getTopicMembers(room.get_id());
        showProgressBar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_NAME, Parcels.wrap(room));
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    @Subscribe
    public void onUpdateMemberEvent(UpdateMemberEvent event) {
        if (event.member != null) {
            invalidateOptionsMenu();
            mAdapter.update(event.member);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            final List<Member> members = Parcels.unwrap(data.getParcelableExtra(ChooseMemberActivity.MEMBERS));
            Iterator<Member> iterator = members.iterator();
            while (iterator.hasNext()) {
                Member member = iterator.next();
                for (Member m : mAdapter.getItems()) {
                    if (m != null && member != null && m.get_id().equals(member.get_id())) {
                        iterator.remove();
                        break;
                    }
                }
            }
            addMembers(members);
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

    private void addMembers(final List<Member> members) {
        for (Member member : members) {
            memberIds.add(member.get_id());
        }
        final BatchInviteRequestData data = new BatchInviteRequestData(memberIds);
        TalkClient.getInstance().getTalkApi()
                .batchInviteToRoom(room.get_id(), data)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Member>>() {
                    @Override
                    public void call(List<Member> m) {
                        mAdapter.addItems(members);
                        RoomRealm.getInstance()
                                .updateRoomMemberIds(room.get_id(), memberIds)
                                .subscribe(new Action1<Room>() {
                                    @Override
                                    public void call(Room room) {

                                    }
                                }, new RealmErrorAction());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
    }

    @Override
    public void onItemClick(int position) {
        Member member = MainApp.globalMembers.get(mAdapter.getItems().get(position - 1).get_id());
        new MemberInfoDialog
                .Builder(this, R.style.Talk_Dialog)
                .setMember(member)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_topic_setting, menu);
        if (!isAdmin()) {
            menu.findItem(R.id.action_archive_topic).setVisible(false);
            menu.findItem(R.id.action_delete_topic).setVisible(false);
        }
        if (room.getIsGeneral()) {
            menu.findItem(R.id.action_quit_topic).setVisible(false);
            menu.findItem(R.id.action_archive_topic).setVisible(false);
            menu.findItem(R.id.action_delete_topic).setVisible(false);
            menu.findItem(R.id.action_add_member).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent data = new Intent();
                data.putExtra(EXTRA_NAME, Parcels.wrap(room));
                setResult(RESULT_OK, data);
                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(mAdapter.getNameEditText().getWindowToken(), 0);
                finish();
                break;
            case R.id.action_palette:
                View view = LayoutInflater.from(this).inflate(R.layout.dialog_color_topic, null);
                new TalkDialog.Builder(this)
                        .title(R.string.topic_color)
                        .titleColorRes(R.color.white)
                        .titleBackgroundColorRes(R.color.colorPrimary)
                        .customView(view, true)
                        .dismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                mAdapter.notifyItemChanged(0);
                            }
                        })
                        .show();
                break;
            case R.id.action_add_member: {
                Intent intent = new Intent(this, ChooseMemberActivity.class);
                intent.putExtra(ChooseMemberActivity.MEMBERS, Parcels.wrap(mAdapter.getItems()));
                startActivityForResult(intent, ADD_MEMBER);
                break;
            }
            case R.id.action_quit_topic:
                new TalkDialog.Builder(this)
                        .title(R.string.title_quit_topic)
                        .titleColorRes(R.color.white)
                        .titleBackgroundColorRes(R.color.talk_warning)
                        .content((room.getIsPrivate() != null && room.getIsPrivate()) ?
                                R.string.confirm_quit_topic :
                                R.string.confirm_quit_topic_private)
                        .positiveText(R.string.confirm)
                        .positiveColorRes(R.color.talk_warning)
                        .negativeText(R.string.cancel)
                        .negativeColorRes(R.color.material_grey_700)
                        .callback(new TalkDialog.ButtonCallback() {
                            @Override
                            public void onPositive(TalkDialog materialDialog, View v) {
                                presenter.leaveRoom(room.get_id());
                            }
                        })
                        .show();
                break;
            case R.id.action_archive_topic:
                new TalkDialog.Builder(this)
                        .title(R.string.title_archive_topic)
                        .titleColorRes(R.color.white)
                        .titleBackgroundColorRes(R.color.talk_warning)
                        .content(R.string.confirm_archive_topic)
                        .positiveText(R.string.confirm)
                        .positiveColorRes(R.color.talk_warning)
                        .negativeText(R.string.cancel)
                        .negativeColorRes(R.color.material_grey_700)
                        .callback(new TalkDialog.ButtonCallback() {
                            @Override
                            public void onPositive(TalkDialog materialDialog, View v) {
                                presenter.archiveRoom(room.get_id());
                            }
                        })
                        .show();
                break;
            case R.id.action_delete_topic:
                new TalkDialog.Builder(this)
                        .title(R.string.title_delete_topic)
                        .titleColorRes(R.color.white)
                        .titleBackgroundColorRes(R.color.talk_warning)
                        .content(R.string.confirm_delete_topic)
                        .positiveText(R.string.confirm)
                        .positiveColorRes(R.color.talk_warning)
                        .negativeText(R.string.cancel)
                        .negativeColorRes(R.color.material_grey_700)
                        .callback(new TalkDialog.ButtonCallback() {
                            @Override
                            public void onPositive(TalkDialog materialDialog, View v) {
                                presenter.deleteRoom(room.get_id());
                            }
                        })
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onUpdateTopic(Room room) {
        if (room != null) {
            List<Member> members = this.room.getMembers();
            this.room = room;
            this.room.setMembers(members);
            mAdapter.setRoom(this.room);
            mAdapter.notifyItemChanged(0);
        }
    }

    @Override
    public void onUpdateVisibility(boolean isSuccess) {
        if (isSuccess) {
            room.setIsPrivate(!room.getIsPrivate());
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onMemberRemove(String memberId) {
        mAdapter.remove(memberId);
    }

    @Override
    public void onDropTopic() {
        RoomRealm.getInstance().archive(room)
                .subscribeOn(Schedulers.io())
                .subscribe(new EmptyAction<Room>(), new RealmErrorAction());
        Intent intent = new Intent();
        intent.putExtra("isFinish", 1);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onLoadMembersFinish(List<Member> members) {
        if (!members.isEmpty()) {
            dismissProgressBar();
        }
        room.setMembers(members);
        mAdapter.addItems(members);
    }

    private boolean isAdmin() {
        return BizLogic.isAdmin() || BizLogic.isAdminOfRoom(room.get_id());
    }

    private void updateTopic() {
        if (StringUtil.isBlank(mAdapter.getNameEditText().getText().toString())) {
            MainApp.showToastMsg(R.string.name_empty);
        } else {
            presenter.updateRoom(room.get_id(), mAdapter.getNameEditText().getText().toString(),
                    mAdapter.getGoalEditText().getText().toString(), null);
        }
    }

    @Override
    public void onRemove(final Member member) {
        new TalkDialog.Builder(this)
                .title(R.string.title_remove_member)
                .titleColorRes(R.color.white)
                .titleBackgroundColorRes(R.color.talk_warning)
                .content(R.string.confirm_remove_member_from_room)
                .positiveText(R.string.confirm)
                .positiveColorRes(R.color.talk_warning)
                .negativeText(R.string.cancel)
                .negativeColorRes(R.color.material_grey_700)
                .callback(new TalkDialog.ButtonCallback() {
                    @Override
                    public void onPositive(TalkDialog materialDialog, View v) {
                        presenter.removeMember(room.get_id(), member.get_id());
                    }
                })
                .show();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        presenter.updateVisibility(room.get_id(), isChecked);
    }

    @Override
    public void onSave() {
        List<Room> rooms = new ArrayList<>();
        rooms.addAll(MainApp.globalRooms.values());
        String inputName = mAdapter.getNameEditText().getText().toString();
        String inputPurpose = mAdapter.getGoalEditText().getText().toString();
        for (Room r : rooms) {
            if (r != null && r.getTopic().equals(inputName)) {
                if (r.getPurpose() != null && r.getPurpose().equals(inputPurpose)) {
                    MainApp.showToastMsg(R.string.topic_name_exist);
                    return;
                }
            }
        }
        mAdapter.getNameEditText().clearFocus();
        mAdapter.getGoalEditText().clearFocus();
        updateTopic();
        room.setTopic(inputName);
        room.setPurpose(inputPurpose);
    }

    @Override
    public void onDiscard() {
        mAdapter.getNameEditText().clearFocus();
        mAdapter.getGoalEditText().clearFocus();
        mAdapter.getNameEditText().setText(room.getTopic());
        mAdapter.getGoalEditText().setText(room.getPurpose());
    }
}
