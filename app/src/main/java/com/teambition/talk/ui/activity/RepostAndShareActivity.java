package com.teambition.talk.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.talk.dialog.TalkDialog;
import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.Constant;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.imageloader.RecyclerViewPauseOnScrollListener;
import com.teambition.talk.adapter.RepostAndShareAdapter;
import com.teambition.talk.adapter.TeamAdapter;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.data.CreateStoryRequestData;
import com.teambition.talk.client.data.FileUploadResponseData;
import com.teambition.talk.entity.ChatItem;
import com.teambition.talk.entity.CountingTypedFile;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Room;
import com.teambition.talk.entity.Story;
import com.teambition.talk.entity.Team;
import com.teambition.talk.entity.Topic;
import com.teambition.talk.presenter.TeamPresenter;
import com.teambition.talk.realm.StoryDataProcess;
import com.teambition.talk.rx.ApiErrorAction;
import com.teambition.talk.util.FileUtil;
import com.teambition.talk.util.TransactionUtil;
import com.teambition.talk.view.RepostView;

import org.parceler.Parcels;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zeatual on 15/3/9.
 */
public class RepostAndShareActivity extends BaseActivity implements
        RepostView, AdapterView.OnItemClickListener, RepostAndShareAdapter.OnItemClickListener {
    public final static String SHARE_DATA = "share_data";
    public final static String MULTIPLE_SHARE_DATA = "multiple_share_data";
    public final static String REPOST_SHARE_CREATE_STORY = "repost_share_create_story";
    final static int TEXT = 0;
    final static int IMAGE = 1;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;
    @InjectView(R.id.listView)
    ListView listView;
    @InjectView(R.id.team_name)
    TextView tvTeamName;
    @InjectView(R.id.team_layout)
    RelativeLayout layoutTeam;
    @InjectView(R.id.layout_team_list)
    RelativeLayout layoutTeamList;
    @InjectView(R.id.mask)
    View mask;
    @InjectView(R.id.arrow)
    ImageView arrow;

    private RepostAndShareAdapter adapter;
    private TeamAdapter teamAdapter;
    private TeamPresenter presenter;
    private String messageId;
    private String[] favoritesIds;
    private String teamId;
    private MenuItem textMenuItem;
    private MenuItem imageMenuItem;
    private FileUploadResponseData data;
    private File shareFile;
    private Team currentTeam;
    final RecyclerViewPauseOnScrollListener listener = new RecyclerViewPauseOnScrollListener(MainApp.IMAGE_LOADER, true, true, null);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_handle);
        ButterKnife.inject(this);
        BusProvider.getInstance().register(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.send_to);

        presenter = new TeamPresenter(this);
        teamId = BizLogic.getTeamId();
        currentTeam = BizLogic.getTeam();

        LayoutTransition lt = new LayoutTransition();
        lt.setDuration(150);

        Intent intent = getIntent();
        messageId = intent.getStringExtra("id");
        favoritesIds = intent.getStringArrayExtra("favoritesIds");
        final Uri uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
        if (uri != null) {
            shareFile = new File(FileUtil.getFilePath(this, uri));
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RepostAndShareAdapter();
        teamAdapter = new TeamAdapter(this);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(listener);
        listView.setOnItemClickListener(this);
        listView.setAdapter(teamAdapter);

        layoutTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animate();
            }
        });
        mask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animate();
            }
        });

        if (!BizLogic.isNetworkConnected()) {
            MainApp.showToastMsg(R.string.notification_network);
        } else if (!BizLogic.isLogin()) {
            TransactionUtil.goTo(this, Oauth2Activity.class, true);
            MainApp.showToastMsg(R.string.notification_login);
        } else if (!BizLogic.hasChosenTeam()) {
            TransactionUtil.goTo(this, ChooseTeamActivity.class, true);
            MainApp.showToastMsg(R.string.notification_choose_team);
        } else {
            presenter.getTeamDetail(BizLogic.getTeamId());
            tvTeamName.setText(BizLogic.getTeamName());
            presenter.getTeams();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recyclerView.removeOnScrollListener(listener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MainApp.PREF_UTIL.putObject(Constant.TEAM, currentTeam);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                MainApp.PREF_UTIL.putObject(Constant.TEAM, currentTeam);
                finish();
                break;
            case 0:
                startActivityForResult(new Intent(RepostAndShareActivity.this,
                        ChooseMemberActivity.class), TEXT);
                break;
            case 1:
                uploadFile();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        textMenuItem = menu.add(Menu.NONE, 0, Menu.NONE, R.string.create_story);
//                .setIcon(R.drawable.ic_story_topic);
        textMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        textMenuItem.setVisible(false);
        imageMenuItem = menu.add(Menu.NONE, 1, Menu.NONE, R.string.create_story);
//        imageMenuItem.setIcon(R.drawable.ic_story_file);
        imageMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        imageMenuItem.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Intent intent = getIntent();
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            getData(intent);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void uploadFile() {
        if (shareFile == null || !shareFile.exists()) {
            MainApp.showToastMsg(R.string.file_not_exist);
            return;
        }
        final TalkDialog dialog = new TalkDialog.Builder(this)
                .title(R.string.uploading)
                .progress(false, (int) shareFile.length(), true)
                .build();
        dialog.show();
        TalkClient.getInstance().getUploadApi()
                .uploadFile(shareFile.getName(), "image/*", shareFile.length(), new CountingTypedFile("image/*", shareFile,
                        new CountingTypedFile.ProgressListener() {
                            @Override
                            public void transferred(long bytes) {
                                dialog.setProgress((int) bytes);
                            }
                        }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<FileUploadResponseData>() {
                    @Override
                    public void call(FileUploadResponseData data) {
                        RepostAndShareActivity.this.data = data;
                        dialog.dismiss();
                        startActivityForResult(new Intent(RepostAndShareActivity.this,
                                ChooseMemberActivity.class), IMAGE);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            List<Member> members = Parcels.unwrap(data.getParcelableExtra(ChooseMemberActivity.MEMBERS));
            List<String> memberIds = new ArrayList<>(members.size());
            for (Member member : members) {
                if (member != null) {
                    memberIds.add(member.get_id());
                }
            }
            final String meId = BizLogic.getUserInfo().get_id();
            if (!memberIds.contains(meId)) {
                memberIds.add(0, meId);
            }
            switch (requestCode) {
                case TEXT:
                    if (shareFile != null && shareFile.exists()) {
                        final String fileName = shareFile.getName().substring(0, shareFile.getName().indexOf("."));
                        final Topic topic = new Topic();
                        topic.setTitle(fileName);
                        topic.setText("");
                        CreateStoryRequestData textData = new CreateStoryRequestData(teamId,
                                StoryDataProcess.Category.TOPIC.value, topic, memberIds);
                        TalkClient.getInstance().getTalkApi().createStory(textData)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Story>() {
                                    @Override
                                    public void call(Story story) {
                                        Bundle bundle = new Bundle();
                                        bundle.putBoolean(REPOST_SHARE_CREATE_STORY, true);
                                        bundle.putParcelable(ChatActivity.EXTRA_STORY, Parcels.wrap(story));
                                        TransactionUtil.goTo(RepostAndShareActivity.this,
                                                ChatActivity.class, bundle, true);
                                    }
                                }, new ApiErrorAction());
                    }
                    break;
                case IMAGE:
                    CreateStoryRequestData requestData = new CreateStoryRequestData(teamId,
                            StoryDataProcess.Category.FILE.value, this.data, memberIds);
                    TalkClient.getInstance().getTalkApi().createStory(requestData)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Story>() {
                                @Override
                                public void call(Story story) {
                                    Bundle bundle = new Bundle();
                                    bundle.putBoolean(REPOST_SHARE_CREATE_STORY, true);
                                    bundle.putParcelable(ChatActivity.EXTRA_STORY, Parcels.wrap(story));
                                    TransactionUtil.goTo(RepostAndShareActivity.this,
                                            ChatActivity.class, bundle, true);
                                }
                            }, new ApiErrorAction());
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Team team = teamAdapter.getItem(position);
        if (team != null) {
            teamId = team.get_id();
            tvTeamName.setText(team.getName());
            presenter.getTeamDetail(team.get_id());
            MainApp.PREF_UTIL.putObject(Constant.TEAM, team);
            animate();
        }
    }

    @Override
    public void onEmpty() {

    }

    @Override
    public void onGetTeamsFinish(ArrayList<Team> teams) {
        teamAdapter.updateData(teams);
    }

    @Override
    public void onRepostFinish(Message message) {
        MainApp.showToastMsg(R.string.sent);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onGetTeamDetailFinish(Team data) {
        try {
            List<ChatItem> items = new ArrayList<>();
            List<Room> rooms = data.getRooms();
            for (Room room : rooms) {
                if (!room.getIsQuit()) {
                    if (room.getIsGeneral()) {
                        room.setTopic(getString(R.string.general));
                    }
                    items.add(new ChatItem(room));
                }
            }
            List<Member> members = data.getMembers();
            for (Member member : members) {
                if (BizLogic.isXiaoai(member)) {
                    member.setName(getString(R.string.talk_ai));
                    member.setPinyin(getString(R.string.talk_ai_py).toLowerCase());
                }
                if (!BizLogic.isMe(member.get_id())) {
                    if (member.getPrefs() != null && member.getPrefs().getAlias() != null) {
                        member.setName(member.getPrefs().getAlias());
                    }
                    items.add(new ChatItem(member));
                }
            }
            adapter.updateData(items);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(int position) {
        final ChatItem item = adapter.getItem(position);
        if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
            Intent intent = new Intent(this, ChatActivity.class);
            if (!item.isTopic) {
                intent.putExtra(ChatActivity.EXTRA_MEMBER, Parcels.wrap(item.member));
                intent.putExtra(ChatActivity.TEAM_ID, teamId);
                if (shareFile != null) {
                    intent.putExtra(SHARE_DATA, shareFile.getPath());
                }
            } else {
                intent.putExtra(ChatActivity.EXTRA_ROOM, Parcels.wrap(item.room));
                intent.putExtra(ChatActivity.TEAM_ID, teamId);
                if (shareFile != null) {
                    intent.putExtra(SHARE_DATA, shareFile.getPath());
                }
            }
            startActivity(intent);
            finish();
            return;
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(getIntent().getAction())) {
            Intent intent = new Intent(this, ChatActivity.class);
            if (!item.isTopic) {
                intent.putExtra(ChatActivity.EXTRA_MEMBER, Parcels.wrap(item.member));
                intent.putExtra(ChatActivity.TEAM_ID, teamId);
                intent.putParcelableArrayListExtra(MULTIPLE_SHARE_DATA, getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM));
            } else {
                intent.putExtra(ChatActivity.EXTRA_ROOM, Parcels.wrap(item.room));
                intent.putExtra(ChatActivity.TEAM_ID, teamId);
                intent.putParcelableArrayListExtra(MULTIPLE_SHARE_DATA, getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM));
            }
            startActivity(intent);
            finish();
            return;
        }
        new TalkDialog.Builder(this)
                .contentColorRes(R.color.material_grey_700)
                .content(String.format(getString(R.string.confirm_send_to), item.name))
                .positiveText(R.string.send)
                .positiveColor(getResources().getColor(R.color.colorPrimary))
                .negativeColorRes(R.color.material_grey_700)
                .negativeText(R.string.cancel)
                .callback(new TalkDialog.ButtonCallback() {
                    @Override
                    public void onPositive(TalkDialog dialog, View v) {
                        if (item.type == RepostAndShareAdapter.TOPIC) {
                            if (TextUtils.isEmpty(messageId)) {
                                presenter.repostFavoritesMessage(teamId, item.id, null, favoritesIds);
                            } else {
                                presenter.repostMessage(messageId, teamId, item.id, null);
                            }
                        } else {
                            if (TextUtils.isEmpty(messageId)) {
                                presenter.repostFavoritesMessage(teamId, null, item.id, favoritesIds);
                            } else {
                                presenter.repostMessage(messageId, teamId, null, item.id);
                            }
                        }
                    }
                })
                .show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (layoutTeamList.getVisibility() == View.VISIBLE) {
                animate();
                return false;
            } else {
                return super.onKeyDown(keyCode, event);
            }
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }

    private void animate() {
        if (layoutTeamList.getVisibility() == View.VISIBLE) {
            ObjectAnimator.ofFloat(arrow, "rotation", 180, 360).setDuration(300).start();
            AnimatorSet hideSet = new AnimatorSet();
            hideSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    layoutTeamList.setVisibility(View.GONE);
                    mask.setVisibility(View.GONE);
                }
            });
            hideSet.playTogether(ObjectAnimator.ofFloat(layoutTeamList, "alpha", 1, 0).
                    setDuration(200), ObjectAnimator.ofFloat(layoutTeamList, "y", 0,
                    -layoutTeamList.getMeasuredHeight()).setDuration(200), ObjectAnimator.
                    ofFloat(mask, "alpha", 1, 0).setDuration(200));
            hideSet.start();
        } else {

            AnimatorSet initSet = new AnimatorSet();
            initSet.playTogether(ObjectAnimator.ofFloat(layoutTeamList, "alpha", 1, 0)
                    .setDuration(0), ObjectAnimator.ofFloat(mask, "alpha", 1, 0).setDuration(0));
            initSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    layoutTeamList.setVisibility(View.VISIBLE);
                    mask.setVisibility(View.VISIBLE);
                    ObjectAnimator initY = ObjectAnimator.ofFloat(layoutTeamList, "y", 0,
                            -layoutTeamList.getMeasuredHeight()).setDuration(0);
                    initY.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ObjectAnimator.ofFloat(arrow, "rotation", 0, 180).setDuration(300).start();
                            ObjectAnimator.ofFloat(layoutTeamList, "alpha", 0, 1).setDuration(200).start();
                            ObjectAnimator.ofFloat(mask, "alpha", 0, 1).setDuration(200).start();
                            ObjectAnimator.ofFloat(layoutTeamList, "y", -layoutTeamList.getMeasuredHeight(), 0)
                                    .setDuration(200).start();
                        }
                    });
                    initY.start();
                }
            });
            initSet.start();
        }
    }

    @SuppressLint("NewApi")
    private void getData(Intent intent) {
        try {
            if (intent.getType() == null) return;
            final String mimeType = intent.getType();
            if ("text/plain".equals(mimeType)) {
                textMenuItem.setVisible(true);
            } else if (mimeType.startsWith("image/")) {
                imageMenuItem.setVisible(true);
            }
        } catch (Exception e) {
            MainApp.showToastMsg(R.string.not_support_file_type);
            finish();
        }
    }
}
