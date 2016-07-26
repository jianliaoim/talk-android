package com.teambition.talk.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.IntentCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Base64;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.gson.Gson;
import com.squareup.otto.Subscribe;
import com.talk.dialog.TalkDialog;
import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.Constant;
import com.teambition.talk.MainApp;
import com.teambition.talk.MediaController;
import com.teambition.talk.R;
import com.teambition.talk.adapter.DrawerTeamAdapter;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.data.ErrorResponseData;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.QRCodeData;
import com.teambition.talk.entity.Room;
import com.teambition.talk.entity.Story;
import com.teambition.talk.entity.Team;
import com.teambition.talk.entity.User;
import com.teambition.talk.event.CallPhoneEvent;
import com.teambition.talk.event.LeaveTeamEvent;
import com.teambition.talk.event.NetworkEvent;
import com.teambition.talk.event.NewTeamEvent;
import com.teambition.talk.event.SyncFinishEvent;
import com.teambition.talk.event.UpdateMemberEvent;
import com.teambition.talk.event.UpdateNotificationEvent;
import com.teambition.talk.event.UpdateTeamEvent;
import com.teambition.talk.event.UpdateUserEvent;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.presenter.AccountPresenter;
import com.teambition.talk.presenter.HomePresenter;
import com.teambition.talk.realm.MemberRealm;
import com.teambition.talk.realm.MessageRealm;
import com.teambition.talk.realm.StoryRealm;
import com.teambition.talk.realm.TeamRealm;
import com.teambition.talk.receiver.XiaomiPushReceiver;
import com.teambition.talk.rx.EmptyAction;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.service.MessageService;
import com.teambition.talk.ui.GuideDialog;
import com.teambition.talk.ui.VCodeDialogHelper;
import com.teambition.talk.ui.fragment.CallInFragment;
import com.teambition.talk.ui.fragment.ContactsCoverFragment;
import com.teambition.talk.ui.fragment.ContactsFragment;
import com.teambition.talk.ui.fragment.MoreFragment;
import com.teambition.talk.ui.fragment.NotificationFragment;
import com.teambition.talk.ui.fragment.SearchFragment;
import com.teambition.talk.ui.fragment.TeamActivityFragment;
import com.teambition.talk.ui.widget.ThemeButton;
import com.teambition.talk.util.AnalyticsHelper;
import com.teambition.talk.util.Connectivity;
import com.teambition.talk.util.DateUtil;
import com.teambition.talk.util.DensityUtil;
import com.teambition.talk.util.Logger;
import com.teambition.talk.util.NotificationUtil;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.ThemeUtil;
import com.teambition.talk.util.TransactionUtil;
import com.teambition.talk.view.AccountView;
import com.teambition.talk.view.HomeView;
import com.teambition.talk.view.SimpleAccountViewImpl;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageHelper;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by zeatual on 14-10-15.
 */
public class HomeActivity extends BaseActivity implements HomeView, View.OnClickListener,
        AdapterView.OnItemClickListener, SearchFragment.SearchListener {


    private static final String SURVEY = "survey";

    public static final String SHOW_PROGRESS_BAR = "show_progress_bar";
    public static final int CREATE_TEAM_REQUEST = 1;
    public static final int REQUEST_SYNC_TEAMBITION = 2;
    public static final String NOTIFICATION = "notification";

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @InjectView(R.id.navigation_view)
    NavigationView navigationView;
    @InjectView(R.id.tab_layout)
    TabLayout tabLayout;
    @InjectView(R.id.viewPager)
    ViewPager viewPager;
    @InjectView(R.id.imageView_avatar)
    ImageView imgAvatar;
    @InjectView(R.id.textView_name)
    TextView tvName;
    @InjectView(R.id.flag_other_unread)
    View otherUnread;
    @InjectView(R.id.listView)
    ListView listView;
    @InjectView(R.id.tv_team_key)
    TextView tvTeamKey;
    @InjectView(R.id.tv_team_name)
    TextView tvTeamName;

    @InjectView(R.id.layout_network_status)
    View layoutNetworkStatus;
    @InjectView(R.id.connected)
    View layoutNWSConnected;
    @InjectView(R.id.connecting)
    View layoutNWSConnecting;
    @InjectView(R.id.disconnected)
    View layoutNWSDisconnected;

    @InjectView(R.id.drawer_me)
    View drawerMe;

    @InjectView(R.id.view_overlay)
    View vOverlay;
    @InjectView(R.id.fab_menu)
    FloatingActionsMenu fabMenu;
    @InjectView(R.id.fab_add_member)
    FloatingActionButton fabAddMember;

    private ActionBarDrawerToggle drawerToggle;
    private HomePresenter presenter;
    private Menu menu;
    private DrawerTeamAdapter adapter;
    private Team currentTeam;
    private Gson gson = new Gson();
    private String newTeamId;
    private View footerDivider;
    private VCodeDialogHelper vCodeHelper;
    private AccountPresenter accountPresenter;
    public MiPushMessage message;
    private ShowcaseView showcaseView;
    private SearchFragment searchFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#1E2024"));
        }


        setContentView(R.layout.activity_home);
        ButterKnife.inject(this);

        NotificationUtil.startPush(this);
        UmengUpdateAgent.update(this);
        BusProvider.getInstance().register(this);
        presenter = new HomePresenter(this);
        accountPresenter = new AccountPresenter(mAccountsView);

        searchFragment = SearchFragment.getInstance(this);
        currentTeam = (Team) MainApp.PREF_UTIL.getObject(Constant.TEAM, Team.class);
        if (currentTeam == null) {
            startActivity(new Intent(this, ChooseTeamActivity.class));
            finish();
            return;
        }
        toolbar.setBackgroundColor(Color.parseColor("#393C40"));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(currentTeam.getName());
        adapter = new DrawerTeamAdapter(this);

        progressBar = findViewById(R.id.progress_bar);
        if (getIntent().getBooleanExtra(SHOW_PROGRESS_BAR, false)) {
            showProgressBar();
        }
        if (StringUtil.isNotBlank(BizLogic.getTeamName())) {
            tvTeamKey.setText(BizLogic.getTeamName().substring(0, 1));
            tvTeamName.setText(BizLogic.getTeamName());
        }
        tvTeamKey.setBackgroundDrawable(ThemeUtil.getThemeDrawable(getResources(),
                R.drawable.bg_round_64_dp, BizLogic.getTeamColor()));

        initView();
        initDrawer();
        initData();
        writeContacts();
        checkExtras(getIntent());

        startService(MessageService.startIntent(this));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                MainApp.showToastMsg(R.string.read_phone_state_permission_denied);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkExtras(intent);
    }

    public void checkExtras(Intent intent) {
        if (!intent.hasExtra(PushMessageHelper.KEY_MESSAGE)) {
            return;
        }
        message = (MiPushMessage) intent.getSerializableExtra(PushMessageHelper.KEY_MESSAGE);
        if (message != null) {
            final Map<String, String> extra = message.getExtra();
            final String teamId = extra.get(XiaomiPushReceiver._TEAM_ID);
            if (!currentTeam.get_id().equalsIgnoreCase(teamId)) {
                Observable.create(new Observable.OnSubscribe<Team>() {
                    @Override
                    public void call(Subscriber<? super Team> subscriber) {
                        List<Team> teamInfos = TeamRealm.getInstance().getTeamWithCurrentThread();
                        for (Team team : teamInfos) {
                            if (team.get_id().equals(teamId)) {
                                subscriber.onNext(team);
                                break;
                            }
                        }
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Team>() {
                            @Override
                            public void call(Team team) {
                                if (team != null) {
                                    switchTeam(team);
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {

                            }
                        });
            } else {
                if (!intent.getBooleanExtra(SHOW_PROGRESS_BAR, false)) {
                    final String messageType = message.getExtra().get(XiaomiPushReceiver.MESSAGE_TYPE);
                    final String targetId = message.getExtra().get(XiaomiPushReceiver._TARGET_ID);
                    startNotificationWithChatActivity(messageType, targetId);
                }
            }
        }
    }

    public void startNotificationWithChatActivity(final String messageType, final String _targetId) {
        final Bundle bundle = new Bundle();
        bundle.putBoolean(NOTIFICATION, true);
        if (XiaomiPushReceiver.DMS.equalsIgnoreCase(messageType)) {
            final Member member = MainApp.globalMembers.get(_targetId);
            bundle.putParcelable(ChatActivity.EXTRA_MEMBER, Parcels.wrap(member));
            TransactionUtil.goTo(this, ChatActivity.class, bundle);
        } else if (XiaomiPushReceiver.ROOM.equalsIgnoreCase(messageType)) {
            final Room room = MainApp.globalRooms.get(_targetId);
            bundle.putParcelable(ChatActivity.EXTRA_ROOM, Parcels.wrap(room));
            TransactionUtil.goTo(this, ChatActivity.class, bundle);
        } else if (XiaomiPushReceiver.STORY.equalsIgnoreCase(messageType)) {
            StoryRealm.getInstance().getSingleStory(_targetId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Story>() {
                        @Override
                        public void call(Story story) {
                            if (story != null) {
                                bundle.putParcelable(ChatActivity.EXTRA_STORY, Parcels.wrap(story));
                                TransactionUtil.goTo(HomeActivity.this, ChatActivity.class, bundle);
                            }
                        }
                    }, new RealmErrorAction());
        }
    }

    private void writeContacts() {
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                final ContentResolver resolver = getContentResolver();
                final String[] projection = {ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
                Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, ContactsContract.CommonDataKinds.Phone.NUMBER + "=?", new String[]{MainApp.TALK_BUSINESS_CALL}, null);
                if (cursor != null) {
                    if (!cursor.moveToFirst()) {
                        ArrayList<ContentProviderOperation> contentProviderOperations = new ArrayList<>();
                        contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null).withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());
                        //insert contact display name using Data.CONTENT_URI
                        contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, getString(R.string.app_name)).build());
                        //insert mobile number using Data.CONTENT_URI
                        contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, MainApp.TALK_BUSINESS_CALL).withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build());
                        try {
                            getContentResolver().
                                    applyBatch(ContactsContract.AUTHORITY, contentProviderOperations);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    cursor.close();
                }
            }
        }).subscribeOn(Schedulers.computation()).subscribe();
    }

    @Override
    protected void onResume() {
        super.onResume();
        clearNotification();
        MobclickAgent.onResume(this);
        if (!BizLogic.isNetworkConnected()) {
            layoutNWSConnected.setVisibility(View.GONE);
            layoutNWSConnecting.setVisibility(View.GONE);
            layoutNWSDisconnected.setVisibility(View.VISIBLE);
            layoutNetworkStatus.setVisibility(View.VISIBLE);
        }
        boolean isConnected = Connectivity.isConnected(this);
        BusProvider.getInstance().post(new NetworkEvent(
                isConnected ? NetworkEvent.STATE_CONNECTED : NetworkEvent.STATE_DISCONNECTED));

        // send alive event every one hour
        Observable.just(null).observeOn(Schedulers.computation())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        sendAliveEvent();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        Logger.e("Home", "send alive error", e);
                    }
                });

        presenter.getCachedTeams();
    }

    private String sendAliveEvent() {
        String lastUseTimestampStr = MainApp.PREF_UTIL.getString(Constant.LAST_USE_TIMESTAMP, null);
        Date currentTime = new Date();
        if (lastUseTimestampStr != null) {
            Date lastUseTimestamp = DateUtil.parseISO8601(lastUseTimestampStr, DateUtil.DATE_FORMAT_JSON);
            if (currentTime.getTime() - lastUseTimestamp.getTime() > DateUtils.HOUR_IN_MILLIS) {
                AnalyticsHelper.getInstance().sendEvent(AnalyticsHelper.Category.retention, "alive", null);
                Logger.d("Home", "send alive " + currentTime);
            }
        }
        String currentTimeStr = DateUtil.formatISO8601(currentTime, DateUtil.DATE_FORMAT_JSON);
        MainApp.PREF_UTIL.putString(Constant.LAST_USE_TIMESTAMP, currentTimeStr);
        return currentTimeStr;
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        BusProvider.getInstance().unregister(this);
        AnalyticsHelper.getInstance().flushEvent();
        super.onDestroy();
    }

    private void initView() {
        drawerMe.setOnClickListener(this);
        final PagerAdapter pagerAdapter = new PagerAdapter(this.getSupportFragmentManager());
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            private float y = DensityUtil.dip2px(HomeActivity.this, 100);

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 0) {
                    fabMenu.setTranslationY(y * positionOffset);
                    fabAddMember.setTranslationY(y * (1 - positionOffset));
                } else if (position == 1) {
                    fabAddMember.setTranslationY(y * positionOffset);
                } else if (position == 2) {
                    fabMenu.setTranslationY(y);
                    fabAddMember.setTranslationY(y);
                }
            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    tabLayout.getTabAt(i).getIcon().setAlpha(104);
                }
                tabLayout.getTabAt(position).getIcon().setAlpha(255);
            }
        });
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            switch (i) {
                case 0:
                    tab.setIcon(R.drawable.ic_tab_notifications);
                    break;
                case 1:
                    tab.setIcon(R.drawable.ic_tab_contacts);
                    tab.getIcon().setAlpha(104);
                    break;
                case 2:
                    tab.setIcon(R.drawable.ic_tab_activity);
                    tab.getIcon().setAlpha(104);
                    break;
                default:
                    tab.setIcon(R.drawable.ic_tab_more);
                    tab.getIcon().setAlpha(104);
                    break;
            }
        }


        fabMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuCollapsed() {
                vOverlay.animate()
                        .alpha(0.0F)
                        .setDuration(300L)
                        .setInterpolator(new FastOutSlowInInterpolator())
                        .start();
                vOverlay.setClickable(false);
            }

            @Override
            public void onMenuExpanded() {
                hideNewFeatureTips();

                vOverlay.animate()
                        .alpha(0.9F)
                        .setDuration(300L)
                        .setInterpolator(new FastOutSlowInInterpolator())
                        .start();
                vOverlay.setClickable(true);
                vOverlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fabMenu.collapse();
                    }
                });
                MediaController.getInstance().playSoundEffect();
            }
        });
    }

    private void showNewFeatureTips() {
        boolean showStartTalkTips = MainApp.PREF_UTIL.getBoolean(Constant.SHOW_START_TALK_TIPS, true);
        if (showStartTalkTips) {
            try {
                showcaseView = new ShowcaseView.Builder(this)
                        .withNewStyleShowcase()
                        .setTarget(new ViewTarget(R.id.show_case_anchor, this))
                        .setContentTitle(R.string.tips_start_chat)
                        .setStyle(R.style.ShowcaseTheme)
                        .hideOnTouchOutside()
                        .build();
                showcaseView.hideButton();
                MainApp.PREF_UTIL.putBoolean(Constant.SHOW_START_TALK_TIPS, false);
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

    @OnClick({R.id.fab_group, R.id.fab_dms, R.id.fab_topic, R.id.fab_file, R.id.fab_link,
            R.id.fab_add_member})
    public void onNewStory(View v) {
        switch (v.getId()) {
            case R.id.fab_group:
                TransactionUtil.goTo(this, TopicActivity.class);
                break;
            case R.id.fab_dms:
                TransactionUtil.goTo(this, MemberActivity.class);
                break;
            case R.id.fab_topic:
                TransactionUtil.goTo(this, CreateTopicStoryActivity.class);
                break;
            case R.id.fab_file:
                TransactionUtil.goTo(this, CreateFileStoryActivity.class);
                break;
            case R.id.fab_link:
                TransactionUtil.goTo(this, CreateLinkStoryActivity.class);
                break;
            case R.id.fab_add_member:
                TransactionUtil.goTo(this, AddTeamMemberActivity.class);
                break;
        }
        if (fabMenu.isExpanded()) {
            fabMenu.collapse();
        }
    }

    private void initDrawer() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                drawerLayout.closeDrawers();
                return true;
            }
        });
        drawerLayout.setDrawerListener(drawerToggle);

        View footer = LayoutInflater.from(this).inflate(R.layout.footer_drawer_team, null);
        footerDivider = footer.findViewById(R.id.view_divider);
        listView.addFooterView(footer, null, false);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    private void initData() {
        presenter.getTeams();
        presenter.getUser();
        presenter.syncUser();
        presenter.getStrikerToken();
        BizLogic.initGlobalData();
        BizLogic.syncData();

        BizLogic.syncTeamData();


        SharedPreferences preferences = getSharedPreferences(MessageService.PREF_NAME, MODE_PRIVATE);
        boolean useIncrementallySyncFirstTime = preferences.getBoolean(MessageService.PREF_INCREMENTALLY_SYNC_FIRST_TIME, true);
        if (useIncrementallySyncFirstTime) {
            preferences.edit().putBoolean(MessageService.PREF_INCREMENTALLY_SYNC_FIRST_TIME, false).commit();
            MessageRealm.getInstance().deleteTeamMessage(BizLogic.getTeamId())
                    .subscribe(new EmptyAction<Void>(), new RealmErrorAction());
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        message = null;
        Team team = adapter.getItem(position);
        switchTeam(team);

    }

    @Override
    public void onValidateFail() {
        TransactionUtil.goTo(this, Oauth2Activity.class, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        resetMenu();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out)
                        .add(R.id.drawer_layout, searchFragment)
                        .addToBackStack(null)
                        .commit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_MENU || super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (fabMenu.isExpanded()) {
            fabMenu.collapse();
            return;
        }
        if (searchFragment.isAdded()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out)
                    .remove(searchFragment)
                    .commit();
            getSupportFragmentManager().popBackStack();
            return;
        }
        moveTaskToBack(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_create_team:
                TransactionUtil.goToForResult(this, CreateTeamActivity.class, CREATE_TEAM_REQUEST);

                break;
            case R.id.layout_scan_qr_code:
                TransactionUtil.goToForResult(this, ScannerActivity.class,
                        ScannerActivity.SCAN_QR_CODE);

                AnalyticsHelper.getInstance().sendEvent(AnalyticsHelper.Category.switch_team,
                        "scan team", null);
                break;
            case R.id.layout_sync_teambition:
                TransactionUtil.goToForResult(this, SyncTeambitionActivity.class,
                        REQUEST_SYNC_TEAMBITION);

                AnalyticsHelper.getInstance().sendEvent(AnalyticsHelper.Category.switch_team,
                        "sync team", null);
                break;
            case R.id.drawer_me:
                TransactionUtil.goTo(this, PreferenceActivity.class);

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ScannerActivity.SCAN_QR_CODE) {
                String format = data.getStringExtra(ScannerActivity.SCAN_RESULT_FORMAT);
                String str = data.getStringExtra(ScannerActivity.SCAN_RESULT);
                try {
                    String json = new String(Base64.decode(str, Base64.DEFAULT));
                    final QRCodeData qrCodeData = gson.fromJson(json, QRCodeData.class);
                    if ("QR_CODE".equalsIgnoreCase(format) && qrCodeData != null && qrCodeData.verify()) {
                        View viewTeam = LayoutInflater.from(this).inflate(R.layout.dialog_join_team, null);
                        ImageView imgTeamColor = (ImageView) viewTeam.findViewById(R.id.img_team_color_dialog);
                        TextView tvTeamKey = (TextView) viewTeam.findViewById(R.id.tv_team_key_dialog);
                        TextView tvTeamName = (TextView) viewTeam.findViewById(R.id.tv_team_name_dialog);
                        ThemeButton btnJoin = (ThemeButton) viewTeam.findViewById(R.id.btn_join);
                        imgTeamColor.setImageResource(ThemeUtil.getThemeRoundDrawableId(qrCodeData.color));
                        btnJoin.setThemeBackground(getResources().getColor(R.color.colorPrimary),
                                getResources().getColor(R.color.colorPrimaryDark));
                        if (StringUtil.isNotBlank(qrCodeData.name)) {
                            tvTeamName.setText(qrCodeData.name);
                            tvTeamKey.setText(qrCodeData.name.substring(0, 1));
                        }
                        btnJoin.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TalkClient.getInstance().getTalkApi()
                                        .joinBySignCode(qrCodeData._id, qrCodeData.signCode)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Action1<Team>() {
                                            @Override
                                            public void call(Team team) {
                                                presenter.unsubscribeTeam(BizLogic.getTeamId());
                                                MainApp.PREF_UTIL.putObject(Constant.TEAM, team);
                                                getSupportActionBar().setTitle(team.getName());
                                                Intent intent = getIntent();
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                overridePendingTransition(R.anim.anim_empty, R.anim.anim_empty);
                                                drawerLayout.closeDrawer(Gravity.LEFT);
                                                new Handler().post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        viewPager.setCurrentItem(0, true);
                                                    }
                                                });
                                            }
                                        }, new Action1<Throwable>() {
                                            @Override
                                            public void call(Throwable throwable) {
                                                if (throwable instanceof RetrofitError) {
                                                    try {
                                                        ErrorResponseData error = (ErrorResponseData) ((RetrofitError) throwable)
                                                                .getBodyAs(ErrorResponseData.class);
                                                        MainApp.showToastMsg(error.message);
                                                    } catch (Exception e) {
                                                        MainApp.showToastMsg(R.string.network_failed);
                                                    }
                                                }
                                            }
                                        });
                            }
                        });
                        new TalkDialog.Builder(this)
                                .customView(viewTeam, false).show();
                    } else {
                        MainApp.showToastMsg(R.string.no_team_error);
                    }
                } catch (Exception e) {
                    MainApp.showToastMsg(R.string.no_team_error);
                }
            } else if (requestCode == CREATE_TEAM_REQUEST) {
                Team team = Parcels.unwrap(data.getParcelableExtra("team"));
                switchTeam(team);
            } else if (requestCode == REQUEST_SYNC_TEAMBITION) {
                List<Team> teams = Parcels.unwrap(data.getParcelableExtra("teams"));
                dealTeams(teams);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> fragmentList = new ArrayList<>();

        public PagerAdapter(FragmentManager fm) {
            super(fm);
            fragmentList.add(NotificationFragment.getInstance());
            fragmentList.add(ContactsFragment.getInstance());
            fragmentList.add(TeamActivityFragment.newInstance());
            fragmentList.add(MoreFragment.getInstance());
        }

        @Override
        public Fragment getItem(int i) {
            return fragmentList.get(i);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return object != null && ((Fragment) object).getView() == view;
        }
    }

    @Override
    public void onLoadUserFinish(User user) {
        MainApp.IMAGE_LOADER.displayImage(user.getAvatarUrl(), imgAvatar,
                ImageLoaderConfig.AVATAR_OPTIONS);
        tvName.setText(user.getName());
    }

    @Override
    public void onLoadTeamFinish(List<Team> teams) {
        dealTeams(teams);
    }

    private void dealTeams(final List<Team> teams) {
        if (teams == null) return;
        if (teams.isEmpty()) {
            footerDivider.setVisibility(View.GONE);
        } else {
            footerDivider.setVisibility(View.VISIBLE);
            adapter.updateData(teams);
            adapter.setNewTeamId(newTeamId);
            if (adapter.checkUnread() || newTeamId != null) {
                otherUnread.setVisibility(View.VISIBLE);
            } else {
                otherUnread.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Subscribe
    public void onUpdateMemberEvent(UpdateMemberEvent event) {
        processSurvey();

        Observable.create(new Observable.OnSubscribe<List<Member>>() {
            @Override
            public void call(Subscriber<? super List<Member>> subscriber) {
                List<Member> members = MemberRealm.getInstance().getNotQuitAndNotRobotMemberWithCurrentThread();
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                subscriber.onNext(members);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Member>>() {
                    @Override
                    public void call(List<Member> members) {
                        boolean showContacts = MainApp.PREF_UTIL.getBoolean(Constant.SHOW_CONTACTS_PAGE + BizLogic.getTeamId(), true);
                        if (showContacts && members.size() < 5) {
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.container, ContactsCoverFragment.newInstance())
                                    .addToBackStack(ContactsCoverFragment.BACK_STACK_TAG)
                                    .commit();
                            MainApp.PREF_UTIL.putBoolean(Constant.SHOW_CONTACTS_PAGE + BizLogic.getTeamId(), false);
                            hideNewFeatureTips();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });

    }

    private void processSurvey() {
        int survey = MainApp.PREF_UTIL.getInt(SURVEY, -1);
        if (survey == -1) {
            @SuppressLint("SimpleDateFormat") final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            String currentDate = format.format(date);
            String datePref = MainApp.PREF_UTIL.getString("date");
            if (StringUtil.isBlank(datePref)) {
                MainApp.PREF_UTIL.putString("date", currentDate);
            } else {
                try {
                    final long day = (date.getTime() - format.parse(datePref).getTime()) / (1000 * 60 * 60 * 24);
                    if (day >= 3) {
                        final Member me = MainApp.globalMembers.get(BizLogic.getUserInfo().get_id());
                        final long createdDay = (date.getTime() - format.parse(format.format(me.getCreatedAt())).getTime()) / (1000 * 60 * 60 * 24);
                        final String url = createdDay > 7 ? Constant.SURVEY_OLD_USER : Constant.SURVEY_NEW_USER;
                        showSurveyDialog(url);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Subscribe
    public void onLeaveTeamEvent(LeaveTeamEvent event) {
        if (BizLogic.getTeamId().equals(event.teamId)) {
            Intent intent = new Intent(this, ChooseTeamActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            presenter.getTeams();
        }
    }

    @Subscribe
    public void onNewTeamEvent(NewTeamEvent event) {
        newTeamId = event.teamId;
        otherUnread.setVisibility(View.VISIBLE);
        presenter.getTeams();
    }

    @Subscribe
    public void onSyncFinish(SyncFinishEvent event) {
        showNewFeatureTips();
        dismissProgressBar();
        presenter.getTeams();
        resetMenu();
        if (message == null || !getIntent().getBooleanExtra(SHOW_PROGRESS_BAR, false)) return;
        final String messageType = message.getExtra().get(XiaomiPushReceiver.MESSAGE_TYPE);
        final String targetId = message.getExtra().get(XiaomiPushReceiver._TARGET_ID);
        startNotificationWithChatActivity(messageType, targetId);

        AnalyticsHelper.getInstance().sendEvent(AnalyticsHelper.Category.team, "enter team", null);
    }

    @Subscribe
    public void onUpdateNotificationEvent(UpdateNotificationEvent event) {
        if (event.notification.getOldUnreadNum() == null) {
            event.notification.setOldUnreadNum(0);
        }
        adapter.updateUnread(event.notification.get_teamId(), event.notification.getIsMute(), event.notification.getUnreadNum(), event.notification.getOldUnreadNum());
        if (BizLogic.isCurrentTeam(event.notification.get_teamId())) {
            otherUnread.setVisibility(View.VISIBLE);
        }
    }

    @Subscribe
    public void onUpdateTeamEvent(UpdateTeamEvent event) {
        currentTeam = (Team) MainApp.PREF_UTIL.getObject(Constant.TEAM, Team.class);
        getSupportActionBar().setTitle(currentTeam.getName());
    }

    @Subscribe
    public void onUpdateUserEvent(UpdateUserEvent event) {
        presenter.getUser();
    }

    @Subscribe
    public void onCallPhoneEvent(final CallPhoneEvent event) {
        if (event != null && event.member != null) {
            if (BizLogic.getUserInfo() != null && TextUtils.isEmpty(BizLogic.getUserInfo().getPhoneForLogin())) {
                showBindDialog();
            } else if (TextUtils.isEmpty(event.member.getPhoneForLogin())) {
                new GuideDialog.Builder(this, R.style.Talk_Dialog)
                        .setTitle(R.string.not_bind_phone)
                        .setContent(R.string.not_bind_phone_tip)
                        .setContentImageRes(R.drawable.ic_not_bind_phone)
                        .setNegativeTextRes(R.string.i_know)
                        .setPositiveVisible(false)
                        .show();
            } else {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("call");
                if (fragment == null) {
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.drawer_layout, CallInFragment.getInstance(event.member, null), "call")
                            .addToBackStack(null)
                            .commitAllowingStateLoss();
                }
            }
        }
    }

    AccountView mAccountsView = new SimpleAccountViewImpl() {
        @Override
        public void onBindPhone(User user) {
            vCodeHelper.dismiss();
            new TalkDialog.Builder(HomeActivity.this)
                    .title(R.string.action_done)
                    .titleColorRes(R.color.white)
                    .titleBackgroundColorRes(R.color.talk_grass)
                    .backgroundColorRes(R.color.white)
                    .content(getString(R.string.verification_success_content))
                    .positiveText(R.string.action_done)
                    .positiveColorRes(R.color.talk_grass)
                    .negativeText(R.string.cancel)
                    .negativeColorRes(R.color.material_grey_700)
                    .callback(new TalkDialog.ButtonCallback() {
                        @Override
                        public void onPositive(TalkDialog dialog, View v) {
                            super.onPositive(dialog, v);
                            dialog.dismiss();
                        }
                    })
                    .show();
        }

        @Override
        public void onBindPhoneFailed(String error) {
            vCodeHelper.buildVCodeError(error);
        }

        @Override
        public void onPhoneConflict(String account, final String bindCode) {
            vCodeHelper.dismiss();
            new TalkDialog.Builder(HomeActivity.this)
                    .title(R.string.delete_origin_account)
                    .titleColorRes(R.color.white)
                    .titleBackgroundColorRes(R.color.talk_warning)
                    .backgroundColorRes(R.color.white)
                    .content(String.format(getString(R.string.delete_origin_account_content), account))
                    .negativeText(R.string.cancel)
                    .negativeColorRes(R.color.material_grey_700)
                    .positiveText(R.string.confirm)
                    .positiveColorRes(R.color.talk_warning)
                    .callback(new TalkDialog.ButtonCallback() {
                        @Override
                        public void onPositive(TalkDialog dialog, View v) {
                            accountPresenter.forceBindPhone(bindCode);
                        }
                    }).show();
        }
    };

    private void showBindDialog() {
        new GuideDialog.Builder(this, R.style.Talk_Dialog)
                .setTitle(R.string.talk_free_call)
                .setContent(R.string.free_call_tip)
                .setContentImageRes(R.drawable.ic_talk_robot)
                .setPositiveTextRes(R.string.immediately_using)
                .setOnPositiveClickListener(new GuideDialog.OnPositiveClickListener() {
                    @Override
                    public void onPositiveClick(View view) {
                        vCodeHelper = new VCodeDialogHelper(HomeActivity.this, getString(R.string.bind_mobile), new VCodeDialogHelper.VCodeDialogCallback() {
                            @Override
                            public void onPassThrough(String randomCode, String vCode) {
                                accountPresenter.bindPhone(randomCode, vCode);
                            }
                        });
                        vCodeHelper.show();
                    }
                })
                .show();
    }

    private void showSurveyDialog(final String url) {
        TalkDialog dialog = new TalkDialog.Builder(this)
                .backgroundColorRes(R.color.colorPrimary)
                .title(R.string.survey_title)
                .titleColorRes(android.R.color.white)
                .content(R.string.survey_content)
                .contentColorRes(android.R.color.white)
                .negativeText(R.string.no_need)
                .negativeColorRes(android.R.color.white)
                .positiveText(R.string.talk_about_feelings)
                .positiveColorRes(android.R.color.white)
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        MainApp.PREF_UTIL.putInt(SURVEY, 1);
                    }
                })
                .callback(new TalkDialog.ButtonCallback() {

                    @Override
                    public void onPositive(TalkDialog dialog, View v) {
                        super.onPositive(dialog, v);
                        Intent intent = WebContainerActivity.newIntent(HomeActivity.this, url, getString(R.string.survey_title));
                        startActivity(intent);
                        dialog.dismiss();
                    }
                }).build();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void clearNotification() {
        NotificationManager manager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(Constant.NOTIFICATION_ID);
        MainApp.PREF_UTIL.putInt(Constant.NOTIFICATION_COUNT, 0);
        MainApp.PREF_UTIL.putString(Constant.NOTIFICATION_CONTENT, "");
    }

    private void resetMenu() {
        if (menu != null) {
            menu.clear();
            getMenuInflater().inflate(R.menu.menu_home, menu);
        }
    }

    @Override
    public void onDismiss() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Subscribe
    public void oneNetworkEvent(NetworkEvent event) {
        switch (event.state) {
            case NetworkEvent.STATE_CONNECTED:
                if (layoutNetworkStatus.getVisibility() == View.VISIBLE) {
                    layoutNWSConnected.setVisibility(View.VISIBLE);
                    layoutNWSConnecting.setVisibility(View.GONE);
                    layoutNWSDisconnected.setVisibility(View.GONE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            layoutNetworkStatus.setVisibility(View.GONE);
                        }
                    }, 2000);
                }
                break;
            case NetworkEvent.STATE_CONNECTING:
                layoutNWSConnected.setVisibility(View.GONE);
                layoutNWSConnecting.setVisibility(View.VISIBLE);
                layoutNWSDisconnected.setVisibility(View.GONE);
                break;
            case NetworkEvent.STATE_DISCONNECTED:
                layoutNWSConnected.setVisibility(View.GONE);
                layoutNWSConnecting.setVisibility(View.GONE);
                layoutNWSDisconnected.setVisibility(View.VISIBLE);
                layoutNetworkStatus.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void switchTeam(Team team) {
        if (team != null) {
            drawerLayout.closeDrawer(Gravity.LEFT);
            if (!BizLogic.getTeamId().equals(team.get_id())) {
                presenter.unsubscribeTeam(BizLogic.getTeamId());
                startService(MessageService.switchTeamIntent(this));
                MainApp.PREF_UTIL.putObject(Constant.TEAM, team);
                getSupportActionBar().setTitle(team.getName());
                finish();
                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra(HomeActivity.SHOW_PROGRESS_BAR, true);
                intent.putExtra(PushMessageHelper.KEY_MESSAGE, message);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.anim_empty, R.anim.anim_empty);
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        viewPager.setCurrentItem(0, true);
                    }
                });
            }
        }
    }
}
