package com.teambition.talk.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.PointTarget;
import com.talk.dialog.TalkDialog;
import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.Constant;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.adapter.StorySettingMemberAdapter;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.data.UpdateStoryRequestData;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Notification;
import com.teambition.talk.entity.Story;
import com.teambition.talk.event.StoryEvent;
import com.teambition.talk.event.UpdateNotificationEvent;
import com.teambition.talk.imageloader.RecyclerViewPauseOnScrollListener;
import com.teambition.talk.realm.MemberRealm;
import com.teambition.talk.realm.NotificationRealm;
import com.teambition.talk.realm.StoryRealm;
import com.teambition.talk.rx.ApiErrorAction;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.util.DensityUtil;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zeatual on 15/11/4.
 */
public class StorySettingActivity extends BaseActivity implements StorySettingMemberAdapter.OnRemoveListener {

    private final static int REQUEST_CODE = 0;
    public final static int REQUEST_STORY_SETTING = 336;
    public final static int RESULT_CLOSE = 337;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.recycler_view)
    RecyclerView recyclerView;

    private Story story;
    private StorySettingMemberAdapter adapter;
    final RecyclerViewPauseOnScrollListener listener = new RecyclerViewPauseOnScrollListener(MainApp.IMAGE_LOADER, true, true, null);

    private ShowcaseView showcaseView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_setting);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        story = Parcels.unwrap(getIntent().getParcelableExtra("story"));

        adapter = new StorySettingMemberAdapter(BizLogic.isAdmin() ||
                BizLogic.isMe(story.get_creatorId()));
        adapter.setOnRemoveListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(listener);

        story.get_memberIds().remove(BizLogic.getUserInfo().get_id());
        MemberRealm.getInstance().getMembersByIds(story.get_memberIds())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Member>>() {
                    @Override
                    public void call(List<Member> members) {
                        Collections.sort(members, new Comparator<Member>() {
                            @Override
                            public int compare(Member lhs, Member rhs) {
                                return lhs.getAliasPinyin().compareTo(rhs.getAliasPinyin());
                            }
                        });
                        boolean hasMe = false;
                        for (Member member : members) {
                            if (BizLogic.isMe(member.get_id())) {
                                hasMe = true;
                                break;
                            }
                        }
                        if (!hasMe) {
                            members.add(0, MainApp.globalMembers.get(BizLogic.getUserInfo().get_id()));
                        }
                        adapter.updateData(members);
                    }
                }, new RealmErrorAction());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showNewFeatureTips();
            }
        }, 1500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recyclerView.removeOnScrollListener(listener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_story_setting, menu);
        menu.findItem(R.id.action_add_member).setVisible(true);
        if (!BizLogic.isAdmin() && !BizLogic.isMe(story.get_creatorId())) {
            menu.findItem(R.id.action_delete_story).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            final List<Member> members = Parcels.unwrap(data.getParcelableExtra(ChooseMemberActivity.MEMBERS));
            final List<String> memberIds = new ArrayList<>();
            List<Member> originalMembers = adapter.getMembers();
            final HashSet<String> addMemberIds = new HashSet<>();
            HashSet<String> removeMemberIds = new HashSet<>();
            for (Member originalMember : originalMembers) {
                boolean retain = false;
                for (Member returnMember : members) {
                    if (originalMember.get_id().equals(returnMember.get_id())) {
                        retain = true;
                        break;
                    }
                }
                if (!retain) {
                    removeMemberIds.add(originalMember.get_id());
                }
            }
            for (Member returnMember : members) {
                memberIds.add(returnMember.get_id());
                boolean isOld = false;
                for (Member originalMember : originalMembers) {
                    if (originalMember.get_id().equals(returnMember.get_id())) {
                        isOld = true;
                        break;
                    }
                }
                if (!isOld) {
                    addMemberIds.add(returnMember.get_id());
                }
            }

            final UpdateStoryRequestData requestData = new UpdateStoryRequestData();
            if (!addMemberIds.isEmpty()) {
                requestData.addMembers = new ArrayList<>(addMemberIds);
            }
            if (!removeMemberIds.isEmpty()) {
                requestData.removeMembers = new ArrayList<>(removeMemberIds);
            }
            if (!addMemberIds.isEmpty() || !removeMemberIds.isEmpty()) {
                TalkClient.getInstance().getTalkApi()
                        .updateStory(story.get_id(), requestData)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Story>() {
                            @Override
                            public void call(Story story) {
                                updateNotification(story);
                                BusProvider.getInstance().post(new StoryEvent(memberIds));
                                if (requestData.addMembers != null) {
                                    MemberRealm.getInstance().getMembersByIds(requestData.addMembers)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new Action1<List<Member>>() {
                                                @Override
                                                public void call(List<Member> members) {
                                                    adapter.addMembers(members);
                                                }
                                            }, new RealmErrorAction());
                                }
                                if (requestData.removeMembers != null) {
                                    MemberRealm.getInstance().getMembersByIds(requestData.removeMembers)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new Action1<List<Member>>() {
                                                @Override
                                                public void call(List<Member> members) {
                                                    adapter.removeMembers(members);
                                                }
                                            }, new RealmErrorAction());
                                }

                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                MainApp.showToastMsg(R.string.network_failed);
                            }
                        });
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_quit_story:
                new TalkDialog.Builder(this)
                        .title(R.string.action_quit_story)
                        .titleColorRes(R.color.white)
                        .titleBackgroundColorRes(R.color.talk_warning)
                        .content(R.string.content_quit_story)
                        .positiveText(R.string.confirm)
                        .positiveColorRes(R.color.talk_warning)
                        .negativeText(R.string.cancel)
                        .negativeColorRes(R.color.material_grey_700)
                        .callback(new TalkDialog.ButtonCallback() {
                            @Override
                            public void onPositive(TalkDialog materialDialog, View v) {
                                TalkClient.getInstance().getTalkApi()
                                        .leaveStory(story.get_id())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Action1<Story>() {
                                            @Override
                                            public void call(Story story) {
                                                StoryRealm.getInstance().removeWithCurrentThread(story.get_id());
                                                setResult(RESULT_CLOSE);
                                                finish();
                                            }
                                        }, new ApiErrorAction());
                            }
                        })
                        .show();
                break;
            case R.id.action_delete_story:
                new TalkDialog.Builder(this)
                        .title(R.string.action_delete_story)
                        .titleColorRes(R.color.white)
                        .titleBackgroundColorRes(R.color.talk_warning)
                        .content(R.string.content_delete_story)
                        .positiveText(R.string.confirm)
                        .positiveColorRes(R.color.talk_warning)
                        .negativeText(R.string.cancel)
                        .negativeColorRes(R.color.material_grey_700)
                        .callback(new TalkDialog.ButtonCallback() {
                            @Override
                            public void onPositive(TalkDialog materialDialog, View v) {
                                TalkClient.getInstance().getTalkApi()
                                        .deleteStory(story.get_id())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Action1<Story>() {
                                            @Override
                                            public void call(Story story) {
                                                StoryRealm.getInstance().removeWithCurrentThread(story.get_id());
                                                setResult(RESULT_CLOSE);
                                                finish();
                                            }
                                        }, new ApiErrorAction());
                            }
                        })
                        .show();
                break;
            case R.id.action_add_member:
                Intent intent = new Intent(this, ChooseMemberActivity.class);
                intent.putExtra(ChooseMemberActivity.MEMBERS, Parcels.wrap(adapter.getMembers()));
                boolean isOrdinary = !BizLogic.isAdmin() && !BizLogic.isMe(story.get_creatorId());
                intent.putExtra(ChooseMemberActivity.IS_ORDINARY, isOrdinary);
                startActivityForResult(intent, REQUEST_CODE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRemove(final Member member) {
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
                        final List<String> removeMembers = new ArrayList<>();
                        removeMembers.add(member.get_id());
                        final UpdateStoryRequestData data = new UpdateStoryRequestData();
                        data.removeMembers = removeMembers;
                        TalkClient.getInstance().getTalkApi()
                                .updateStory(story.get_id(), data)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Story>() {
                                    @Override
                                    public void call(Story story) {
                                        updateNotification(story);
                                        adapter.removeMember(member);
                                        BusProvider.getInstance().post(new StoryEvent(member.get_id()));
                                    }
                                }, new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        MainApp.showToastMsg(R.string.network_failed);
                                    }
                                });
                    }
                })
                .show();

    }

    private void updateNotification(final Story story) {
        NotificationRealm.getInstance()
                .getSingleNotificationByTargetId(story.get_id())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Notification>() {
                    @Override
                    public void call(Notification notification) {
                        notification.setStory(story);
                        BusProvider.getInstance().post(new UpdateNotificationEvent(notification));
                    }
                }, new RealmErrorAction());
    }

    private void showNewFeatureTips() {
        boolean showTips = MainApp.PREF_UTIL.getBoolean(Constant.SHOW_STORY_USER_MGR_TIPS, true);
        if (showTips) {
            try {
                showcaseView = new ShowcaseView.Builder(this)
                        .withNewStyleShowcase()
                        .setTarget(new PointTarget(DensityUtil.screenWidthInPix(this) - DensityUtil.dip2px(this, 112),
                                DensityUtil.dip2px(this, 56)))
                        .setContentTitle(R.string.tips_manage_story_member)
                        .setStyle(R.style.ShowcaseTheme)
                        .hideOnTouchOutside()
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                hideNewFeatureTips();
                            }
                        })
                        .build();
                showcaseView.hideButton();
                MainApp.PREF_UTIL.putBoolean(Constant.SHOW_STORY_USER_MGR_TIPS, false);
            } catch (Exception e) {

            }
        }
    }

    private void hideNewFeatureTips() {
        if (showcaseView != null) {
            showcaseView.hide();
            showcaseView = null;
        }
    }
}
