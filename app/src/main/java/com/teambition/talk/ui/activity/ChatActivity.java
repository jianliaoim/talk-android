package com.teambition.talk.ui.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.PointTarget;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.rockerhieu.emojicon.EmojiconEditText;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;
import com.squareup.otto.Subscribe;
import com.talk.dialog.TalkDialog;
import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.Constant;
import com.teambition.talk.FileDownloader;
import com.teambition.talk.event.MentionMessageClickEvent;
import com.teambition.talk.event.MentionReadEvent;
import com.teambition.talk.event.StoryDetailExpandEvent;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.MediaController;
import com.teambition.talk.R;
import com.teambition.talk.rx.EmptyAction;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.adapter.AddonsAdapter;
import com.teambition.talk.adapter.MessageAdapter;
import com.teambition.talk.client.data.FileUploadResponseData;
import com.teambition.talk.entity.AddonsItem;
import com.teambition.talk.entity.Attachment;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Notification;
import com.teambition.talk.entity.Draft;
import com.teambition.talk.entity.Room;
import com.teambition.talk.entity.Story;
import com.teambition.talk.entity.Tag;
import com.teambition.talk.entity.User;
import com.teambition.talk.event.AudioProgressChangeEvent;
import com.teambition.talk.event.AudioRecordProgressEvent;
import com.teambition.talk.event.AudioRecordStartEvent;
import com.teambition.talk.event.AudioRecordStopEvent;
import com.teambition.talk.event.AudioResetEvent;
import com.teambition.talk.event.AudioRouteChangeEvent;
import com.teambition.talk.event.CallPhoneEvent;
import com.teambition.talk.event.ClearNotificationUnreadEvent;
import com.teambition.talk.event.DeleteMessageEvent;
import com.teambition.talk.event.LeaveRoomEvent;
import com.teambition.talk.event.NetworkEvent;
import com.teambition.talk.event.NewMessageEvent;
import com.teambition.talk.event.DraftEvent;
import com.teambition.talk.event.RePostEvent;
import com.teambition.talk.event.RemoveStoryEvent;
import com.teambition.talk.event.RoomRemoveEvent;
import com.teambition.talk.event.StoryEvent;
import com.teambition.talk.event.UpdateMessageEvent;
import com.teambition.talk.event.UpdateNotificationEvent;
import com.teambition.talk.event.UpdateStoryEvent;
import com.teambition.talk.model.MessageModelImpl;
import com.teambition.talk.presenter.AccountPresenter;
import com.teambition.talk.presenter.ChatPresenter;
import com.teambition.talk.realm.MemberDataProcess;
import com.teambition.talk.realm.MessageDataProcess;
import com.teambition.talk.realm.MessageRealm;
import com.teambition.talk.realm.NotificationRealm;
import com.teambition.talk.realm.DraftRealm;
import com.teambition.talk.realm.StoryDataProcess;
import com.teambition.talk.realm.StoryRealm;
import com.teambition.talk.service.MessageService;
import com.teambition.talk.ui.GuideDialog;
import com.teambition.talk.ui.MemberInfoDialog;
import com.teambition.talk.ui.MessageFormatter;
import com.teambition.talk.ui.RowFactory;
import com.teambition.talk.ui.VCodeDialogHelper;
import com.teambition.talk.ui.fragment.CallInFragment;
import com.teambition.talk.ui.fragment.FileStoryFragment;
import com.teambition.talk.ui.fragment.LinkStoryFragment;
import com.teambition.talk.ui.fragment.TopicStoryFragment;
import com.teambition.talk.ui.row.ImageRow;
import com.teambition.talk.ui.row.InfoRow;
import com.teambition.talk.ui.row.Row;
import com.teambition.talk.ui.row.SpeechRecordRow;
import com.teambition.talk.ui.row.SpeechRow;
import com.teambition.talk.ui.span.ActionSpan;
import com.teambition.talk.ui.widget.KeyBoardLayout;
import com.teambition.talk.util.DateUtil;
import com.teambition.talk.util.DensityUtil;
import com.teambition.talk.util.FileUtil;
import com.teambition.talk.util.Logger;
import com.teambition.talk.util.MessageDialogBuilder;
import com.teambition.talk.util.SimpleMessageActionCallback;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.ThemeUtil;
import com.teambition.talk.util.TransactionUtil;
import com.teambition.talk.view.AccountView;
import com.teambition.talk.view.ChatView;
import com.teambition.talk.view.SimpleAccountViewImpl;
import com.umeng.analytics.MobclickAgent;

import org.parceler.Parcels;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by zeatual On 11/4/14.
 */
public class ChatActivity extends BaseActivity implements ChatView, TextWatcher,
        EmojiconsFragment.OnEmojiconBackspaceClickedListener, AbsListView.OnScrollListener,
        EmojiconGridFragment.OnEmojiconClickedListener, CompoundButton.OnCheckedChangeListener,
        KeyBoardLayout.OnSoftKeyboardListener, AddonsAdapter.OnAddOnItemClickListener,
        Row.OnAvatarClickListener {
    public static final String TEAM_ID = "team_id";
    public static final String EXTRA_MEMBER = "extra_member";
    public static final String EXTRA_ROOM = "extra_room";
    public static final String EXTRA_STORY = "extra_story";
    public static final String EXTRA_MESSAGE = "extra_message";
    public static final String IS_PREVIEW = "is_preview";
    public static final String IS_ARCHIVE = "is_archive";

    public static final int STATE_SEND = 0;
    public static final int STATE_VOICE = 1;
    public static final int STATE_KEYBOARD = 2;
    private static final int REQUEST_VIDEO_CAPTURE = 30;

    public static final int REQUEST_SELECT_FILE = 5;
    private static final int RECORD_AUDIO_PERMISSION = 0;

    @InjectView(R.id.message_list)
    ListView msgListView;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.et_msg)
    EmojiconEditText etMessage;
    @InjectView(R.id.img_emoji)
    CheckBox cbEmoji;
    @InjectView(R.id.emojicons)
    View emojicons;
    @InjectView(R.id.recycler_view_more)
    RecyclerView recyclerViewMore;
    @InjectView(R.id.layout_root)
    KeyBoardLayout rootLayout;

    @InjectView(R.id.layout_input)
    LinearLayout layoutInput;
    @InjectView(R.id.preview_layout)
    LinearLayout layoutPreview;
    @InjectView(R.id.preview_name_tv)
    TextView tvPreviewName;
    @InjectView(R.id.msg_send)
    ImageButton btnSend;
    @InjectView(R.id.voice_bar)
    TextView voiceBar;
    @InjectView(R.id.voice_tip_layer)
    View voiceTipLayout;
    @InjectView(R.id.img_voice_tip)
    ImageView imgVoiceTip;
    @InjectView(R.id.voice_tip_text)
    TextView voiceTipText;

    @InjectView(R.id.layout_unread_tip)
    RelativeLayout layoutUnreadTip;
    @InjectView(R.id.unread_arrow)
    ImageView unreadArrow;
    @InjectView(R.id.unread_tip)
    TextView tvUnreadTip;
    @InjectView(R.id.chat_leave_member_text)
    View leaveMemberView;

    @InjectView(R.id.layout_network_status)
    View layoutNetworkStatus;
    @InjectView(R.id.connected)
    View layoutNWSConnected;
    @InjectView(R.id.connecting)
    View layoutNWSConnecting;
    @InjectView(R.id.disconnected)
    View layoutNWSDisconnected;
    @InjectView(R.id.view_overlay)
    View overlay;
    @InjectView(R.id.view_shadow)
    View shadow;

    private int sendButtonState = STATE_VOICE;
    private EditText updateMsgET;
    private View emptyFooter;

    private InputMethodManager imm;
    private Menu menu;

    private boolean isQuit = false;
    private boolean isArchive = false;
    private boolean isPreview;
    private String teamId;
    private MessageAdapter adapter;
    private Member member;
    private Room room;
    private Story story;
    private Message message;
    private ChatPresenter presenter;
    private AccountPresenter accountsPresenter;
    private VCodeDialogHelper vCodeHelper;
    private CompositeSubscription subscription = new CompositeSubscription();
    private ProgressDialog proDialog;
    private boolean isPrivate = true;
    private boolean isSearchResult = false;

    private String maxId;
    private String minId;
    private Date maxCreateTime = new Date();
    private Date minCreateTime;
    private String fileType;
    private int tempUnreadCount;

    private boolean isRecording;
    private boolean isPinned;

    private boolean canLoadNewMessage = false;
    private boolean canLoadOldMessage = false;
    private boolean isLoading = false;
    // for send message during steel can load new message
    private String messageToSend = null;
    private ArrayList<String> imageToSend = null;

    private boolean readyToCancel = false;
    private AnimationDrawable recordingAnimation;

    private View headerView;
    private View footerView;
    private View footerLoading;
    private View footerEmpty;
    private View headerPrivate;
    private View headerPublic;
    private View headerGeneral;
    private View headerXiaoai;
    private View headerLoading;
    private View headerStory;

    private MediaMetadataRetriever retriever;
    private ShowcaseView showcaseView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        initArgs(getIntent());

        if (member == null && room == null && story == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_chat);
        ButterKnife.inject(this);
        progressBar = findViewById(R.id.progress_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.emojicons, EmojiconsFragment.newInstance(false))
                .commit();
        if (story != null) {
            shadow.setVisibility(View.GONE);
        }
        unreadArrow.setImageDrawable(ThemeUtil.getThemeDrawable(getResources(),
                R.drawable.ic_arrow_down_thin, BizLogic.getTeamColor()));

        headerView = LayoutInflater.from(this).inflate(R.layout.header_chat, null);
        footerView = LayoutInflater.from(this).inflate(R.layout.item_loading, null);
        footerLoading = footerView.findViewById(R.id.progress);
        footerEmpty = footerView.findViewById(R.id.empty);
        footerLoading.setVisibility(View.GONE);
        headerPrivate = headerView.findViewById(R.id.header_private);
        headerPublic = headerView.findViewById(R.id.header_public);
        headerGeneral = headerView.findViewById(R.id.header_general);
        headerXiaoai = headerView.findViewById(R.id.header_xiaoai);
        headerLoading = headerView.findViewById(R.id.header_loading);
        headerStory = headerView.findViewById(R.id.header_story);

        recyclerViewMore.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        String idName = "";
        String id = "";
        if (member != null) {
            idName = "_toId=";
            id = member.get_id();
        } else if (room != null) {
            idName = "_roomId=";
            id = room.get_id();
        } else if (story != null) {
            idName = "_storyId=";
            id = story.get_id();
        }
        recyclerViewMore.setAdapter(new AddonsAdapter(this, idName, id, this));

        if (isPreview) {
            layoutInput.setVisibility(View.GONE);
            layoutPreview.setVisibility(View.VISIBLE);
            tvPreviewName.setText(" #" + room.getTopic());
            emptyFooter = LayoutInflater.from(this).inflate(R.layout.footer_empty, null);
            msgListView.addFooterView(emptyFooter, null, false);
        }

        //如果是该成员已经离开团队,则隐藏输入框,显示成员离开的文字
        if (isQuit) {
            layoutInput.setVisibility(View.GONE);
            leaveMemberView.setVisibility(View.VISIBLE);
        }

        if (isArchive) {
            layoutInput.setVisibility(View.GONE);
        }

        rootLayout.setOnSoftKeyboardListener(this);

        retriever = new MediaMetadataRetriever();
        proDialog = new ProgressDialog(this);
        proDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        proDialog.setMessage(getResources().getString(R.string.wait));
        proDialog.setMax(100);

        adapter = new MessageAdapter();
        cbEmoji.setOnCheckedChangeListener(this);
        msgListView.addHeaderView(headerView, null, false);
        msgListView.addFooterView(footerView, null, false);
        msgListView.setAdapter(adapter);
        msgListView.setOnScrollListener(new PauseOnScrollListener(MainApp.IMAGE_LOADER, true,
                true, this));

        presenter = new ChatPresenter(this, new MessageModelImpl(), teamId);
        accountsPresenter = new AccountPresenter(mAccountsView);

        initAndClearData();

        etMessage.addTextChangedListener(this);
        Drawable tipBg = ThemeUtil.getThemeDrawable(getResources(), R.drawable.bg_voice_tip, BizLogic.getTeamColor());
        voiceTipText.setBackgroundDrawable(tipBg);
        voiceBar.setOnTouchListener(new View.OnTouchListener() {
                                        @Override
                                        public boolean onTouch(View v, MotionEvent event) {
                                            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                                Logger.d("audio", "start directly");
                                                voiceTipLayout.setVisibility(View.VISIBLE);
                                                imgVoiceTip.setBackgroundResource(R.drawable.ic_voice_tip);
                                                recordingAnimation = (AnimationDrawable) imgVoiceTip.getBackground();
                                                recordingAnimation.start();
                                                voiceBar.setBackgroundResource(R.drawable.bg_voice_bar_pressed);
                                                voiceBar.setText(R.string.release_to_stop);
                                                MediaController.getInstance().startRecording();
                                                voiceBar.getParent().requestDisallowInterceptTouchEvent(true);
                                                isRecording = true;
                                            } else if (event.getAction() == MotionEvent.ACTION_UP
                                                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                                                if (readyToCancel) {
                                                    voiceTipLayout.setVisibility(View.GONE);
                                                    voiceTipText.setText(R.string.voice_cancel_tip);
                                                    voiceBar.setBackgroundResource(R.drawable.bg_voice_bar);
                                                    voiceBar.setText(R.string.hold_to_speak);
                                                    MediaController.getInstance().stopRecording(false);
                                                    isRecording = false;
                                                } else {
                                                    Logger.d("audio", "stop directly");
                                                    voiceTipLayout.setVisibility(View.GONE);
                                                    voiceBar.setBackgroundResource(R.drawable.bg_voice_bar);
                                                    voiceBar.setText(R.string.hold_to_speak);
                                                    Message audioMessage = MediaController.getInstance().stopRecording(true);
                                                    if (audioMessage != null) {
                                                        sendAudioMessage(audioMessage);
                                                    }
                                                }
                                                isRecording = false;
                                            } else if (event.getAction() == MotionEvent.ACTION_MOVE && isRecording) {
                                                Logger.d("touch", "event:" + event.getX() + " btnVoice:" + voiceBar.getX());
                                                if (event.getY() < voiceBar.getY() - DensityUtil.dip2px(ChatActivity.this, 80)) {
                                                    voiceTipText.setText(R.string.voice_delete_tip);
                                                    if (recordingAnimation != null) {
                                                        recordingAnimation.stop();
                                                    }
                                                    imgVoiceTip.setBackgroundResource(R.drawable.ic_voice_delete);
                                                    readyToCancel = true;
                                                } else if (readyToCancel) {
                                                    voiceTipText.setText(R.string.voice_cancel_tip);
                                                    imgVoiceTip.setBackgroundResource(R.drawable.ic_voice_tip);
                                                    recordingAnimation = (AnimationDrawable) imgVoiceTip.getBackground();
                                                    recordingAnimation.start();
                                                    readyToCancel = false;
                                                }
                                            }
                                            v.onTouchEvent(event);
                                            return true;
                                        }
                                    }
        );
        if (MainApp.PREF_UTIL.getBoolean(Constant.IS_FIRST_CHAT)) {
            final View popView = LayoutInflater.from(this).inflate(R.layout.popup_window_chat, null);
            findViewById(R.id.layout_root).postDelayed(new Runnable() {
                @Override
                public void run() {
                    PopupWindow popupWindow = new PopupWindow(popView,
                            DensityUtil.dip2px(MainApp.CONTEXT, 248),
                            DensityUtil.dip2px(MainApp.CONTEXT, 112));
                    popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    popupWindow.setOutsideTouchable(true);
                    popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            MainApp.PREF_UTIL.putBoolean(Constant.IS_FIRST_CHAT, false);
                        }
                    });
                    popupWindow.showAsDropDown(toolbar, DensityUtil.screenWidthInPix(MainApp.CONTEXT) -
                            (isPrivate ? 0 : DensityUtil.dip2px(MainApp.CONTEXT, 48)) -
                            popupWindow.getWidth(), DensityUtil.dip2px(MainApp.CONTEXT, -14));
                }
            }, 100L);
        }

        vCodeHelper = new VCodeDialogHelper(this, getString(R.string.bind_mobile),
                new VCodeDialogHelper.VCodeDialogCallback() {
                    @Override
                    public void onPassThrough(String randomCode, String vCode) {
                        accountsPresenter.bindPhone(randomCode, vCode);
                    }
                }

        );

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (story != null) {
                    showNewFeatureTips();
                }
            }
        }, 1500);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initArgs(intent);
        initAndClearData();
        adapter.clear();
        invalidateOptionsMenu();
    }

    private void initArgs(Intent intent) {
        teamId = intent.getStringExtra(TEAM_ID);
        if (StringUtil.isBlank(teamId)) {
            teamId = BizLogic.getTeamId();
        }
        member = Parcels.unwrap(intent.getParcelableExtra(EXTRA_MEMBER));
        room = Parcels.unwrap(intent.getParcelableExtra(EXTRA_ROOM));
        story = Parcels.unwrap(intent.getParcelableExtra(EXTRA_STORY));
        message = Parcels.unwrap(intent.getParcelableExtra(EXTRA_MESSAGE));
        isQuit = intent.getBooleanExtra("is_quit", false);
        isArchive = intent.getBooleanExtra(IS_ARCHIVE, false);
        if (story != null) {
            StoryRealm.getInstance().addOrUpdate(story).subscribe(new EmptyAction<Story>(), new RealmErrorAction());
        }
        if (message != null) {
            isSearchResult = true;
            if (BizLogic.isMe(message.get_creatorId())) {
                member = MainApp.globalMembers.get(message.get_toId());
            } else {
                member = MainApp.globalMembers.get(message.get_creatorId());
            }
            room = message.getRoom();
        }
        isPreview = getIntent().getBooleanExtra(IS_PREVIEW, false);
        isPrivate = (member != null && room == null && story == null);
    }

    /**
     * 初始化数据，并根据时间戳决定是否清除本地
     */
    private void initAndClearData() {
        // 草稿
        String draftId = "";
        if (member != null) {
            draftId = member.get_id();
        } else if (room != null) {
            draftId = room.get_id();
        } else if (story != null) {
            draftId = story.get_id();
        }
        DraftRealm.getInstance()
                .getDraft(BizLogic.getTeamId() + draftId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Draft>() {
                    @Override
                    public void call(Draft draft) {
                        if (draft != null) {
                            etMessage.setText(draft.getContent());
                            etMessage.setSelection(draft.getContent().length());
                            etMessage.requestFocus();
                            etMessage.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    imm.toggleSoftInputFromWindow(etMessage.getWindowToken(), 0, 0);
                                    scrollToBottom();
                                }
                            }, 10);
                        }
                    }
                }, new RealmErrorAction());
        //发送清除消息的bus事件
        BusProvider.getInstance().post(new ClearNotificationUnreadEvent(draftId));

        boolean clearData = false;
        SharedPreferences preferences = getSharedPreferences(MessageService.PREF_NAME, MODE_PRIVATE);
        String lastMsgTimestamp = preferences.getString(MessageService.PREF_LAST_MSG_TIMESTAMP, "");
        boolean useIncrementallySyncFirstTime = preferences.getBoolean(MessageService.PREF_INCREMENTALLY_SYNC_FIRST_TIME, true);
        if (useIncrementallySyncFirstTime) {
            clearData = true;
            preferences.edit().putBoolean(MessageService.PREF_INCREMENTALLY_SYNC_FIRST_TIME, false).apply();
        }
        if (StringUtil.isNotBlank(lastMsgTimestamp)) {
            Date lastMsgTime = DateUtil.parseISO8601(lastMsgTimestamp, DateUtil.DATE_FORMAT_JSON);
            if (System.currentTimeMillis() - lastMsgTime.getTime() > Constant.FULL_SYNC_INTERVAL) {
                clearData = true;
            }
        }

        if (clearData) {
            MessageRealm.getInstance().deleteTeamMessage(BizLogic.getTeamId())
                    .subscribe(new Action1<Void>() {
                        @Override
                        public void call(Void aVoid) {
                            initData();
                        }
                    }, new RealmErrorAction());
        } else {
            initData();
        }
    }

    private void initData() {
        if (isSearchResult) {
            presenter.getSearchResult(message, isPrivate, story);
            String title = story != null ? story.getTitle()
                    : (isPrivate ? member.getAlias() : room.getTopic());
            getSupportActionBar().setTitle(title);
        } else if (member != null) {
            isPinned = MemberDataProcess.getInstance().isPinned(member);
            presenter.getMessages(member.get_id(), ChatPresenter.TYPE_PRIVATE);
            if (BizLogic.isNetworkConnected()) {
                presenter.syncPrivateMessages(member.get_id());
            }
            final String alias = MainApp.globalMembers.containsKey(member.get_id()) ? MainApp.globalMembers.get(member.get_id()).getAlias() : member.getAlias();
            getSupportActionBar().setTitle(alias);
        } else if (room != null) {
            isPinned = room.getPinnedAt() != null;
            presenter.getMessages(room.get_id(), ChatPresenter.TYPE_PUBLIC);
            getSupportActionBar().setTitle(room.getTopic());
            showProgressBar();
        } else if (story != null) {
            if (overlay != null) {
                overlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        resetStory();
                    }
                });
            }
            presenter.getMessages(story.get_id(), ChatPresenter.TYPE_STORY);
            if (BizLogic.isNetworkConnected()) {
                presenter.syncStoryMessages(story.get_id());
            }
            showProgressBar();
            resetStory();
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (story != null) {
            resetStory();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SelectImageActivity.SELECT_IMAGES:
                if (resultCode == RESULT_OK) {
                    String path = data.getStringExtra(SelectImageActivity.IMAGE_PATH);
                    if (StringUtil.isNotBlank(path)) {
                        if (isSearchResult) {
                            imageToSend = new ArrayList<>();
                            imageToSend.add(path);
                            initMessages();
                        } else {
                            sendImageMessage(path);
                        }
                    }
                }
                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    Member member1 = Parcels.unwrap(data.getParcelableExtra("data"));
                    SpannableStringBuilder ssb = getAtInfo(member1);
                    etMessage.setText(ssb);
                    etMessage.setSelection(ssb.length());
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        imm.showSoftInput(etMessage, 0);
                    }
                }, 200);

                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    Member member2 = Parcels.unwrap(data.getParcelableExtra("data"));
                    String str2 = updateMsgET.getText().toString() + member2.getAlias() + " ";
                    updateMsgET.setText(str2);
                    updateMsgET.setSelection(str2.length());
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        imm.showSoftInput(updateMsgET, 0);
                    }
                }, 200);
                break;

            // update room
            case 3:
                if (resultCode == RESULT_OK) {
                    if (data.getIntExtra("isFinish", -1) == 1) {
                        finish();
                        return;
                    }
                    room = Parcels.unwrap(data.getParcelableExtra(TopicSettingActivity.EXTRA_NAME));
                    getSupportActionBar().setTitle(room.getTopic());
                }
                break;
            case 4:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        Message message = Parcels.unwrap(data.getParcelableExtra("message"));
                        MessageRealm.getInstance().addOrUpdate(message)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Message>() {
                                    @Override
                                    public void call(Message message) {

                                    }
                                }, new RealmErrorAction());
                        adapter.updateOne(message.get_id(), RowFactory.getInstance()
                                .makeRows(message, this, messageActionCallback));
                    }
                }
                break;
            case REQUEST_SELECT_FILE:
                if (resultCode == RESULT_OK) {
                    final String filePath = FileUtil.getFilePath(this, data.getData());
                    File file = new File(filePath);
                    if (file.exists() && file.length() > 0) {
                        if (BizLogic.isImg(filePath)) {
                            sendImageMessage(filePath);
                        } else {
                            sendFileMessage(file);
                        }
                    }
                }
                break;
            case EditStoryActivity.REQUEST_EDIT_STORY:
                if (resultCode == RESULT_OK) {
                    story = Parcels.unwrap(data.getParcelableExtra("story"));
                }
                break;
            case StorySettingActivity.REQUEST_STORY_SETTING:
                if (resultCode == StorySettingActivity.RESULT_CLOSE) {
                    finish();
                }
                break;
            case REQUEST_VIDEO_CAPTURE:
                if (resultCode == RESULT_OK) {
                    final Uri videoUri = data.getData();
                    File file = null;
                    String[] filePathColumn = {MediaStore.MediaColumns.DATA};
                    Cursor cursor = getContentResolver().query(videoUri, filePathColumn, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        final String filePath = cursor.getString(columnIndex);
                        file = new File(filePath);
                        cursor.close();
                    }
                    if (file != null) {
                        sendVideoMessage(file);
                    }
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        MobclickAgent.onPageEnd(getClass().getName());
        MediaController.getInstance().stopPlaying();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        if (!isPreview && getIntent().getStringExtra("data") == null) {
            if (isPrivate) {
                getMenuInflater().inflate(R.menu.menu_private_chat, menu);
                if (getIntent().getBooleanExtra("is_quit", false)) {
                    menu.findItem(R.id.action_call).setVisible(false);
                }
            } else if (room != null) {
                getMenuInflater().inflate(R.menu.menu_topic, menu);
                if (getIntent().getBooleanExtra(IS_ARCHIVE, false)) {
                    menu.findItem(R.id.action_call).setVisible(false);
                }
            } else if (story != null) {
                getMenuInflater().inflate(R.menu.menu_story, menu);
                if (!BizLogic.isAdmin() && !BizLogic.isMe(story.get_creatorId())) {
                    menu.findItem(R.id.action_story_edit).setVisible(false);
                }
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        final String data = getIntent().getStringExtra(RepostAndShareActivity.SHARE_DATA);
        final ArrayList<Uri> uris = getIntent().getParcelableArrayListExtra(RepostAndShareActivity.MULTIPLE_SHARE_DATA);
        if (!TextUtils.isEmpty(data) || getIntent().getBooleanExtra(RepostAndShareActivity.REPOST_SHARE_CREATE_STORY, false) || uris != null) {
            TransactionUtil.goAndRestartHome(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Bundle bundle = new Bundle();
        switch (item.getItemId()) {
            case android.R.id.home:
                final String data = getIntent().getStringExtra(RepostAndShareActivity.SHARE_DATA);
                final ArrayList<Uri> uris = getIntent().getParcelableArrayListExtra(RepostAndShareActivity.MULTIPLE_SHARE_DATA);
                if (!TextUtils.isEmpty(data) || getIntent().getBooleanExtra(RepostAndShareActivity.REPOST_SHARE_CREATE_STORY, false) || uris != null) {
                    TransactionUtil.goAndRestartHome(this);
                }
                finish();
                break;
            case R.id.action_topic_setting: {
                Intent intent = new Intent(this, TopicSettingActivity.class);
                bundle.putParcelable("room", Parcels.wrap(room));
                intent.putExtras(bundle);
                startActivityForResult(intent, 3);
                break;
            }
            case R.id.action_file_arrange: {
                Intent intent = new Intent(this, ItemsActivity.class);
                intent.putExtra(ItemsActivity.EXTRA_FILTER_TYPE, isPrivate ? 0 : 1);
                intent.putExtra(ItemsActivity.EXTRA_ID, isPrivate ? member.get_id() : room.get_id());
                intent.putExtra(ItemsActivity.EXTRA_CHAT_NAME, isPrivate ? member.getName() : room.getTopic());
                startActivity(intent);
                break;
            }
            case R.id.action_call:
                if (isPrivate) {
                    callAction();
                } else {
                    TransactionUtil.goTo(this, MultiCallActivity.class);
                }
                break;
            case R.id.action_story_setting:
                bundle.putParcelable("story", Parcels.wrap(story));
                TransactionUtil.goToForResult(ChatActivity.this, StorySettingActivity.class, bundle,
                        StorySettingActivity.REQUEST_STORY_SETTING);
                return true;
            case R.id.action_story_edit:
                Intent intent;
                switch (StoryDataProcess.Category.getEnum(story.getCategory())) {
                    case FILE:
                        intent = new Intent(ChatActivity.this, EditFileStoryActivity.class);
                        intent.putExtra("story", Parcels.wrap(story));
                        intent.putExtra(EditStoryActivity.IS_EDIT, true);
                        startActivityForResult(intent, EditStoryActivity.REQUEST_EDIT_STORY);
                        overridePendingTransition(R.anim.anim_fade_transition_in, 0);
                        break;
                    case TOPIC:
                        intent = new Intent(ChatActivity.this, EditTopicStoryActivity.class);
                        intent.putExtra("story", Parcels.wrap(story));
                        intent.putExtra(EditStoryActivity.IS_EDIT, true);
                        startActivityForResult(intent, EditStoryActivity.REQUEST_EDIT_STORY);
                        overridePendingTransition(R.anim.anim_fade_transition_in, 0);
                        break;
                    case LINK:
                        intent = new Intent(ChatActivity.this, EditLinkStoryActivity.class);
                        intent.putExtra("story", Parcels.wrap(story));
                        intent.putExtra(EditStoryActivity.IS_EDIT, true);
                        startActivityForResult(intent, EditStoryActivity.REQUEST_EDIT_STORY);
                        overridePendingTransition(R.anim.anim_fade_transition_in, 0);
                        break;
                }
                return true;
            default:
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    private void callAction() {
        if (BizLogic.getUserInfo() != null && TextUtils.isEmpty(BizLogic.getUserInfo().getPhoneForLogin())) {
            new GuideDialog.Builder(this, R.style.Talk_Dialog)
                    .setTitle(R.string.talk_free_call)
                    .setContent(R.string.free_call_tip)
                    .setContentImageRes(R.drawable.ic_talk_robot)
                    .setPositiveTextRes(R.string.immediately_using)
                    .setOnPositiveClickListener(new GuideDialog.OnPositiveClickListener() {
                        @Override
                        public void onPositiveClick(final View view) {
                            vCodeHelper.setOnDismissListener(new VCodeDialogHelper.OnDismissListener() {
                                @Override
                                public void onDismiss(TalkDialog dialog) {
                                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                                            .hideSoftInputFromWindow(etMessage.getWindowToken(), 0);
                                }
                            });
                            vCodeHelper.show();
                        }
                    })
                    .show();
            return;
        }
        if (member != null) {
            if (!TextUtils.isEmpty(member.getPhoneForLogin())) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("call");
                if (fragment == null) {
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.layout_root, CallInFragment.getInstance(member, null), "call")
                            .addToBackStack(null)
                            .commitAllowingStateLoss();
                }
            } else {
                showBindPhoneDialog();
            }
        }
    }

    private void showBindPhoneDialog() {
        new GuideDialog.Builder(this, R.style.Talk_Dialog)
                .setTitle(R.string.not_bind_phone)
                .setContent(R.string.not_bind_phone_tip)
                .setContentImageRes(R.drawable.ic_not_bind_phone)
                .setNegativeTextRes(R.string.i_know)
                .setPositiveVisible(false)
                .show();
    }

    AccountView mAccountsView = new SimpleAccountViewImpl() {
        @Override
        public void onBindPhone(User user) {
            vCodeHelper.dismiss();
            new TalkDialog.Builder(ChatActivity.this)
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
            new TalkDialog.Builder(ChatActivity.this)
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
                            accountsPresenter.forceBindPhone(bindCode);
                        }
                    }).show();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
        MobclickAgent.onResume(this);
        MobclickAgent.onPageStart(getClass().getName());
        if (!BizLogic.isNetworkConnected()) {
            layoutNWSConnected.setVisibility(View.GONE);
            layoutNWSConnecting.setVisibility(View.GONE);
            layoutNWSDisconnected.setVisibility(View.VISIBLE);
            layoutNetworkStatus.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            final String content = etMessage.getText().toString();
            String id = "";
            if (member != null) {
                id = member.get_id();
            } else if (room != null) {
                id = room.get_id();
            } else if (story != null) {
                id = story.get_id();
            }
            //草稿
            BusProvider.getInstance().post(new DraftEvent(id, content));
            //发送bus消除RecentFragment中的未读数
            BusProvider.getInstance().post(new ClearNotificationUnreadEvent(id));
            BusProvider.getInstance().unregister(this);
            if (subscription != null && subscription.isUnsubscribed()) {
                subscription.unsubscribe();
            }
            Observable.create(new Observable.OnSubscribe<Object>() {
                @Override
                public void call(Subscriber<? super Object> subscriber) {
                    retriever.release();
                }
            }).subscribeOn(Schedulers.io())
                    .subscribe(new EmptyAction<Object>(), new RealmErrorAction());
        } catch (Exception e) {
            // do nothing
        }
        super.onDestroy();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        resetKeyBoardButton();
        resetAdditionalButton();
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (totalItemCount >= visibleItemCount) {
            if (firstVisibleItem <= 10 && canLoadOldMessage && !isLoading) {
                isLoading = true;
                showLoadingHeader();
                if (isPrivate) {
                    presenter.getMoreOldPrivateMessages(member.get_id(), maxId, maxCreateTime);
                } else if (room != null) {
                    presenter.getMoreOldPublicMessages(room.get_id(), maxId, maxCreateTime);
                } else if (story != null) {
                    presenter.getMoreOldStoryMessages(story.get_id(), maxId, maxCreateTime);
                }
            } else if (firstVisibleItem + visibleItemCount + 6 > adapter.getCount() && canLoadNewMessage && !isLoading) {
                isLoading = true;
                showLoadingFooter();
                if (isPrivate) {
                    presenter.getMoreNewPrivateMessages(member.get_id(), minId, minCreateTime);
                } else if (room != null) {
                    presenter.getMoreNewPublicMessages(room.get_id(), minId, minCreateTime);
                } else if (story != null) {
                    presenter.getMoreNewStoryMessages(story.get_id(), minId, minCreateTime);
                }
            }
        }
        if (firstVisibleItem + visibleItemCount + 1 == totalItemCount && !canLoadNewMessage) {
            if (layoutUnreadTip.getVisibility() == View.VISIBLE) {
                resetUnreadLayout();
            }
        }
    }

    @Override
    public void showLocalMessages(List<Message> messages) {
        showMessages(messages, true);
    }

    @Override
    public void showLatestMessages(List<Message> messages) {
        showMessages(messages, false);
        if (StringUtil.isNotBlank(messageToSend)) {
            sendTextMessage(messageToSend);
        }
        if (imageToSend != null) {
            Observable.from(imageToSend)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String s) {
                            sendImageMessage(s);
                        }
                    });
        }
        checkShareData();
    }

    private void checkShareData() {
        final String sharedData = getIntent().getStringExtra(RepostAndShareActivity.SHARE_DATA);
        final ArrayList<Uri> uris = getIntent().getParcelableArrayListExtra(RepostAndShareActivity.MULTIPLE_SHARE_DATA);
        if (!TextUtils.isEmpty(sharedData)) {
            if (BizLogic.isImg(sharedData)) {
                View v = LayoutInflater.from(this).inflate(R.layout.dialog_image, null);
                final ImageView imageView = (ImageView) v.findViewById(R.id.image);
                new TalkDialog.Builder(this)
                        .customView(v, false)
                        .positiveText(R.string.share_send)
                        .negativeText(R.string.share_cancel)
                        .negativeColorRes(R.color.material_grey_700)
                        .callback(new TalkDialog.ButtonCallback() {
                            @Override
                            public void onPositive(TalkDialog dialog, View v) {
                                sendImageMessage(sharedData);
                            }
                        }).showListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        MainApp.IMAGE_LOADER.displayImage(Message.SCHEME_FILE + sharedData, imageView,
                                ImageLoaderConfig.EMPTY_OPTIONS);
                    }
                }).show();
            } else {
                View v = LayoutInflater.from(this).inflate(R.layout.dialog_file, null);
                final TextView textView = (TextView) v.findViewById(R.id.file_scheme);
                textView.setText(FileUtil.getFileScheme(sharedData));
                new TalkDialog.Builder(this)
                        .customView(v, false)
                        .positiveText(R.string.share_send)
                        .negativeText(R.string.share_cancel)
                        .negativeColorRes(R.color.material_grey_700)
                        .callback(new TalkDialog.ButtonCallback() {
                            @Override
                            public void onPositive(TalkDialog dialog, View v) {
                                sendFileMessage(new File(sharedData));
                            }
                        }).show();
            }
        } else if (uris != null && !uris.isEmpty()) {
            CharSequence charSequences[] = new CharSequence[uris.size()];
            final File files[] = new File[uris.size()];
            for (int i = 0; i < uris.size(); i++) {
                final Uri uri = uris.get(i);
                if (uri != null) {
                    File file = new File(FileUtil.getFilePath(this, uri));
                    files[i] = file;
                    charSequences[i] = file.getName();
                }
            }
            new TalkDialog.Builder(this)
                    .items(charSequences)
                    .adapter(new MultipleSendTextAdapter(charSequences), new TalkDialog.ListCallback() {
                        @Override
                        public void onSelection(TalkDialog dialog, View itemView, int which, CharSequence text) {

                        }
                    })
                    .positiveText(R.string.share_send)
                    .negativeText(R.string.share_cancel)
                    .callback(new TalkDialog.ButtonCallback() {
                        @Override
                        public void onPositive(TalkDialog dialog, View v) {
                            super.onPositive(dialog, v);
                            for (File file : files) {
                                if (BizLogic.isImg(file.getPath())) {
                                    sendImageMessage(file.getPath());
                                } else {
                                    sendFileMessage(file);
                                }
                            }
                        }
                    })
                    .show();
        }
    }

    static class MultipleSendTextAdapter extends BaseAdapter {

        final CharSequence[] items;

        MultipleSendTextAdapter(final CharSequence[] items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return items[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                TextView textView = new TextView(parent.getContext());
                final int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, parent.getResources().getDisplayMetrics());
                textView.setLayoutParams(new AbsListView.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, height));
                textView.setGravity(Gravity.CENTER_VERTICAL);
                textView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
                textView.setSingleLine();
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                final int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, parent.getResources().getDisplayMetrics());
                textView.setPadding(padding, 0, padding, 0);
                convertView = textView;
                viewHolder.textView = (TextView) convertView;
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.textView.setText(items[position]);
            return convertView;
        }
    }

    static class ViewHolder {
        public TextView textView;
    }

    private void showMessages(List<Message> messages, boolean isLocal) {
        dismissProgressBar();
        showLoadingHeader();
        if (messages == null || messages.isEmpty()) {
            if (!isLocal) {
                showFinishHeader();
            }
            canLoadOldMessage = false;
            canLoadNewMessage = false;
            return;
        }
        maxId = messages.get(0).get_id();
        maxCreateTime = messages.get(0).getCreatedAt();
        if (!isLocal && messages.size() < 30) {
            showFinishHeader();
            canLoadOldMessage = false;
        } else {
            showLoadingHeader();
            canLoadOldMessage = true;
        }
        adapter.replace(RowFactory.getInstance().makeRows(messages, this, messageActionCallback, retriever));
        scrollToBottom();
    }

    @Override
    public void showSearchResult(List<Message> messages) {
        int position = -1;
        int oldCount;
        int newCount;

        for (int i = 0; i < messages.size(); i++) {
            Message msg = messages.get(i);
            if (message.get_id().equals(msg.get_id())) {
                position = i;
                break;
            }
        }
        if (position != -1) {
            oldCount = position;
            newCount = messages.size() - position - 1;
            if (oldCount < 15) {
                canLoadOldMessage = false;
                showFinishHeader();
            } else {
                canLoadOldMessage = true;
            }
            if (newCount < 15) {
                canLoadNewMessage = false;
                isSearchResult = false;
            } else {
                canLoadNewMessage = true;
            }
            maxId = messages.get(0).get_id();
            maxCreateTime = messages.get(0).getCreatedAt();
            minId = messages.get(messages.size() - 1).get_id();
            minCreateTime = messages.get(messages.size() - 1).getCreatedAt();
            List<Row> rows = RowFactory.getInstance().makeRows(messages, this, messageActionCallback, retriever);
            for (int i = 0; i < rows.size(); i++) {
                Row row = rows.get(i);
                if (message.get_id().equals(row.getMessage().get_id())) {
                    position = i;
                    break;
                }
            }
            adapter.replace(rows);
            msgListView.setSelection(position == 0 ? 0 : position - 1);
        }
    }

    @Override
    public void showMoreOldMessages(List<Message> messages, boolean isLocal) {
        if (!isLocal && messages.size() < 30) {
            showFinishHeader();
            canLoadOldMessage = false;
        } else {
            maxId = messages.get(0).get_id();
            maxCreateTime = messages.get(0).getCreatedAt();
        }
        List<Row> rows = RowFactory.getInstance().makeRows(messages, this, messageActionCallback, retriever);
        adapter.addToStart(rows);
        msgListView.setSelection(msgListView.getFirstVisiblePosition() + rows.size());
        isLoading = false;
    }

    @Override
    public void showMoreNewMessages(List<Message> messages, boolean isLocal) {
        if (!isLocal && messages.size() < 30) {
            canLoadNewMessage = false;
            isSearchResult = false;
        } else {
            minId = messages.get(messages.size() - 1).get_id();
            minCreateTime = messages.get(messages.size() - 1).getCreatedAt();
        }
        adapter.addToEnd(RowFactory.getInstance().makeRows(messages, this, messageActionCallback, retriever));
        hideLoadingFooter();
        isLoading = false;
    }

    @Subscribe
    public void onMentionReadEvent(MentionReadEvent event) {
        presenter.sendMessageReceipt(event.getMessageId());
    }

    @Subscribe
    public void onCallBindPhoneEvent(CallPhoneEvent event) {
        vCodeHelper = new VCodeDialogHelper(this, getString(R.string.bind_mobile), new VCodeDialogHelper.VCodeDialogCallback() {
            @Override
            public void onPassThrough(String randomCode, String vCode) {
                accountsPresenter.bindPhone(randomCode, vCode);
            }
        });
        vCodeHelper.show();
    }

    @Subscribe
    public void onRoomRemove(RoomRemoveEvent event) {
        if (room != null && event.roomId.equals(room.get_id())) {
            finish();
        }
    }

    @Subscribe
    public void onLeaveRoom(LeaveRoomEvent event) {
        if (room != null && event.roomId.equals(room.get_id())) {
            finish();
        }
    }

    @Subscribe
    public void onNewMessage(NewMessageEvent event) {
        boolean isViewingOld = msgListView.getLastVisiblePosition() < msgListView.getCount() - 6;
        try {
            Message msg = event.message;
            if ((isPrivate && msg.getForeignId().equals(member.get_id())) ||
                    (!isPrivate && room != null && msg.getForeignId().equals(room.get_id())) ||
                    (!isPrivate && story != null && msg.getForeignId().equals(story.get_id()))) {
                if (!isSearchResult) {
                    adapter.addToEnd(RowFactory.getInstance().makeRows(msg, this,
                            messageActionCallback, retriever));
                    if (!isViewingOld) {
                        scrollToBottom();
                    }
                }
                if (!isPreview && isViewingOld) {
                    layoutUnreadTip.setVisibility(View.VISIBLE);
                    tempUnreadCount++;
                    tvUnreadTip.setText(String.format(getString(R.string.unread_tip), tempUnreadCount));
                } else {
                    resetUnreadLayout();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onUpdateMessage(UpdateMessageEvent event) {
        Message msg = event.message;
        if (msg != null) {
            adapter.updateOne(msg.get_id(), RowFactory.getInstance()
                    .makeRows(msg, this, messageActionCallback, retriever));
            if (MessageDataProcess.DisplayMode.SPEECH.toString().equals(msg.getDisplayMode())) {
                msg.setIsRead(false);
            }
        }
    }

    @Subscribe
    public void onDeleteMessage(DeleteMessageEvent event) {
        if (event.msgId != null) {
            adapter.deleteOne(event.msgId);
        }
    }

    @Subscribe
    public void onAudioProgressChangeEvent(AudioProgressChangeEvent event) {
        if (MediaController.getInstance().isPlayingAudio(event.message)) {
            for (int i = 0; i < msgListView.getChildCount(); i++) {
                Object tag = msgListView.getChildAt(i).getTag();
                if (tag != null && tag instanceof SpeechRow.SpeechRowHolder) {
                    SpeechRow.SpeechRowHolder holder = (SpeechRow.SpeechRowHolder) tag;
                    if (event.message.get_id().equals(holder.messageId)) {
                        holder.updateProgress(event.message.getAudioProgress(),
                                event.message.getAudioProgressSec());
                        break;
                    }
                }
            }
        }
    }

    @Subscribe
    public void onAudioResetEvent(AudioResetEvent event) {
        for (int i = 0; i < msgListView.getChildCount(); i++) {
            Object tag = msgListView.getChildAt(i).getTag();
            if (tag != null && tag instanceof SpeechRow.SpeechRowHolder) {
                SpeechRow.SpeechRowHolder holder = (SpeechRow.SpeechRowHolder) tag;
                if (event.message.get_id().equals(holder.messageId)) {
                    holder.updateButtonState(SpeechRow.STATE_STOP);
                    holder.updateProgress(0, MessageDataProcess.getInstance().getFile(event.message).getDuration());
                    break;
                }
            }
        }
    }

    @Subscribe
    public void onAudioRecordStartEvent(AudioRecordStartEvent event) {
        adapter.setIsRecording(true);
        scrollToBottom();
    }

    @Subscribe
    public void onAudioRecordStopEvent(AudioRecordStopEvent event) {
        if (isRecording) {
            isRecording = false;
            adapter.setIsRecording(false);

            if (event.tooShort) {
                Toast.makeText(this, R.string.audio_too_short, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Subscribe
    public void onAudioRecordProgressEvent(AudioRecordProgressEvent event) {
        for (int i = 0; i < msgListView.getChildCount(); i++) {
            Object tag = msgListView.getChildAt(i).getTag();
            if (tag != null && tag instanceof SpeechRecordRow.SpeechRecordRowHolder) {
                SpeechRecordRow.SpeechRecordRowHolder holder = (SpeechRecordRow.SpeechRecordRowHolder) tag;
                holder.updateRecordingTime(event.sec);
                break;
            }
        }
    }

    @Subscribe
    public void onAudioRouteChangeEvent(AudioRouteChangeEvent event) {
        int streamType = event.frontSpeaker ? AudioManager.STREAM_VOICE_CALL : AudioManager.USE_DEFAULT_STREAM_TYPE;
        setVolumeControlStream(streamType);
    }

    @Subscribe
    public void onMentionMessageClickEvent(MentionMessageClickEvent event) {
        Intent intent = new Intent(this, MentionReceiptActivity.class);
        intent.putStringArrayListExtra(MentionReceiptActivity.EXTRA_MENTIONED_IDS,
                (ArrayList<String>) event.mentions);
        intent.putStringArrayListExtra(MentionReceiptActivity.EXTRA_RECEIVED_IDS,
                (ArrayList<String>) event.receiptors);
        if (room != null) {
            intent.putExtra(MentionReceiptActivity.EXTRA_ROOM_ID, room.get_id());
        } else if (story != null) {
            intent.putExtra(MentionReceiptActivity.EXTRA_STORY_ID, story.get_id());
        }
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RECORD_AUDIO_PERMISSION:
                voiceBar.setEnabled(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
                break;
        }
    }

    @OnClick({R.id.msg_send, R.id.img_more, R.id.join_btn, R.id.layout_unread_tip})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.msg_send:
                if (sendButtonState == STATE_SEND) {
                    if (StringUtil.isNotBlank(etMessage.getText().toString())) {
                        if (isSearchResult) {
                            messageToSend = etMessage.getText().toString();
                            initMessages();
                        } else {
                            sendTextMessage(MessageFormatter.formatToPost(etMessage.getText()));
                        }
                    }
                } else if (sendButtonState == STATE_VOICE) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                                MainApp.showToastMsg(R.string.record_audio_permission_denied);
                                voiceBar.setEnabled(false);
                            } else {
                                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION);
                            }
                        } else {
                            voiceBar.setEnabled(true);
                        }
                    }
                    btnSend.setBackgroundDrawable(getResources().getDrawable(R.drawable.selector_keyboard));
                    sendButtonState = STATE_KEYBOARD;
                    voiceBar.setVisibility(View.VISIBLE);
                    etMessage.setVisibility(View.GONE);
                    emojicons.setVisibility(View.GONE);
                    if (cbEmoji.isChecked()) {
                        cbEmoji.setChecked(false);
                    }
                    imm.hideSoftInputFromWindow(etMessage.getWindowToken(), 0);
                } else if (sendButtonState == STATE_KEYBOARD) {
                    checkSendButton();
                    sendButtonState = STATE_VOICE;
                    voiceBar.setVisibility(View.GONE);
                    etMessage.setVisibility(View.VISIBLE);
                    imm.toggleSoftInputFromWindow(etMessage.getWindowToken(), 0, 0);
                    etMessage.requestFocus();
                }
                break;
            case R.id.img_more:
                resetKeyBoardButton();
                if (recyclerViewMore.getVisibility() == View.VISIBLE) {
                    recyclerViewMore.setVisibility(View.GONE);
                } else {
                    recyclerViewMore.setVisibility(View.VISIBLE);
                    scrollToBottom();
                }
                break;
            case R.id.join_btn:
                presenter.joinRoom(room.get_id());
                break;
            case R.id.layout_unread_tip:
                resetUnreadLayout();
                if (isSearchResult) {
                    initMessages();
                } else {
                    scrollToBottom();
                }
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        checkSendButton();
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() > 0) {
            if ("@".equals(String.valueOf(s.charAt(s.length() - 1)))) {
                Bundle bundle = new Bundle();
                if (room != null) {
                    bundle.putParcelable("room", Parcels.wrap(room));
                } else if (story != null) {
                    bundle.putParcelable("story", Parcels.wrap(story));
                } else if (member != null) {
                    bundle.putParcelable("member", Parcels.wrap(member));
                }
                Intent intent = new Intent(this, SelectMemberActivity.class);
                intent.putExtras(bundle);
                if (s.length() == 1) {
                    startActivityForResult(intent, 1);
                } else {
                    Pattern p = Pattern.compile("[a-zA-Z0-9]");
                    Matcher m = p.matcher(String.valueOf(s.charAt(s.length() - 2)));
                    if (!m.matches()) {
                        startActivityForResult(intent, 1);
                    }
                }
            }
        }
    }

    private void checkSendButton() {
        String message = etMessage.getText().toString();
        if (message.length() > 0) {
            if (sendButtonState != STATE_SEND) {
                sendButtonState = STATE_SEND;
                btnSend.setBackgroundDrawable(getResources().getDrawable(R.drawable.selector_send));
                voiceBar.setVisibility(View.GONE);
                etMessage.setVisibility(View.VISIBLE);
            }
        } else {
            if (sendButtonState != STATE_VOICE) {
                sendButtonState = STATE_VOICE;
                btnSend.setBackgroundDrawable(getResources().getDrawable(R.drawable.selector_voice));
                voiceBar.setVisibility(View.GONE);
                etMessage.setVisibility(View.VISIBLE);
            }
        }
    }

    private void sendTextMessage(String text) {
        if (StringUtil.isNotBlank(text)) {
            Message msg = Message.newPreSendTextInstance(text);
            adapter.addToEnd(RowFactory.getInstance().makeRows(msg, this, messageActionCallback));
            if (isPrivate) {
                msg.setTo(member);
                msg.set_toId(member.get_id());
                msg.setForeignId(member.get_id());
                presenter.sendPrivateMessage(member.get_id(), text, msg.get_id());
            } else if (room != null) {
                msg.setRoom(room);
                msg.set_roomId(room.get_id());
                msg.setForeignId(room.get_id());
                presenter.sendPublicMessage(room.get_id(), text, msg.get_id());
            } else if (story != null) {
                msg.setStory(story);
                msg.set_storyId(story.get_id());
                msg.setForeignId(story.get_id());
                presenter.sendStoryMessage(story.get_id(), text, msg.get_id());
            }
            msg.setCreatedAt(new Date(System.currentTimeMillis()));
            MessageDataProcess.getInstance().processForPersistent(msg);
            MessageRealm.getInstance().addOrUpdate(msg)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new EmptyAction<Message>(), new RealmErrorAction());
            NotificationRealm.getInstance().updateNotificationWithLocalMessage(msg)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Notification>() {
                        @Override
                        public void call(Notification notification) {
                            BusProvider.getInstance().post(new UpdateNotificationEvent(notification));
                        }
                    }, new RealmErrorAction());
            scrollToBottom();
            etMessage.setText("");
        }
    }

    private void sendFileMessage(File file) {
        final String fileExtension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
        if (StringUtil.isBlank(mimeType)) {
            mimeType = "application/octet-stream";
        }
        Message msg = Message.newPreSendFileInstance(file, mimeType);
        if (isPrivate) {
            msg.setTo(member);
            msg.set_toId(member.get_id());
            msg.setForeignId(member.get_id());
        } else if (room != null) {
            msg.setRoom(room);
            msg.set_roomId(room.get_id());
            msg.setForeignId(room.get_id());
        } else if (story != null) {
            msg.setStory(story);
            msg.set_storyId(story.get_id());
            msg.setForeignId(story.get_id());
        }
        MessageDataProcess.getInstance().processForPersistent(msg);
        MessageRealm.getInstance().addOrUpdate(msg)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new EmptyAction<Message>(), new RealmErrorAction());
        adapter.addToEnd(RowFactory.getInstance().makeRows(msg, this, messageActionCallback));
        scrollToBottom();
        Subscription sp = presenter.uploadFile(mimeType, file.getPath(), msg.get_id());
        subscription.add(sp);
        if (sp == null) {
            MessageRealm.getInstance().deleteMessage(msg.get_id())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Message>() {
                        @Override
                        public void call(Message message) {
                            adapter.deleteOne(message.get_id());
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {

                        }
                    });
        }
    }

    private void sendImageMessage(String path) {
        Message msg = Message.newPreSendImageInstance(new File(path));
        if (isPrivate) {
            msg.setTo(member);
            msg.set_toId(member.get_id());
            msg.setForeignId(member.get_id());
        } else if (room != null) {
            msg.setRoom(room);
            msg.set_roomId(room.get_id());
            msg.setForeignId(room.get_id());
        } else if (story != null) {
            msg.setStory(story);
            msg.set_storyId(story.get_id());
            msg.setForeignId(story.get_id());
        }
        MessageDataProcess.getInstance().processForPersistent(msg);
        NotificationRealm.getInstance().updateNotificationWithLocalMessage(msg)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Notification>() {
                    @Override
                    public void call(Notification notification) {
                        BusProvider.getInstance().post(new UpdateNotificationEvent(notification));
                    }
                }, new RealmErrorAction());
        MessageRealm.getInstance().addOrUpdate(msg)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new EmptyAction<Message>(), new RealmErrorAction());
        adapter.addToEnd(RowFactory.getInstance().makeRows(msg, this, messageActionCallback));
        scrollToBottom();
        Subscription sp = presenter.uploadFile("image/*", path, msg.get_id());
        subscription.add(sp);
        if (sp == null) {
            MessageRealm.getInstance().deleteMessage(msg.get_id())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Message>() {
                        @Override
                        public void call(Message message) {
                            adapter.deleteOne(message.get_id());
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {

                        }
                    });
        }
    }

    private void sendVideoMessage(final File file) {
        final Message msg = Message.newPreSendVideoInstance(file);
        if (isPrivate) {
            msg.setTo(member);
            msg.set_toId(member.get_id());
            msg.setForeignId(member.get_id());
        } else if (room != null) {
            msg.setRoom(room);
            msg.set_roomId(room.get_id());
            msg.setForeignId(room.get_id());
        } else if (story != null) {
            msg.setStory(story);
            msg.set_storyId(story.get_id());
            msg.setForeignId(story.get_id());
        }
        MessageDataProcess.getInstance().processForPersistent(msg);
        MessageRealm.getInstance().addOrUpdate(msg)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new EmptyAction<Message>(), new RealmErrorAction());
        adapter.addToEnd(RowFactory.getInstance().makeRows(msg, this, messageActionCallback, retriever));
        scrollToBottom();

        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                retriever.setDataSource(file.getAbsolutePath());
                File cacheFile = new File(Constant.FILE_DIR_COMPRESSED +
                        "/VIDEO_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".mp4" );
                com.teambition.talk.yuv.MediaController.Result result = com.teambition.talk.yuv.MediaController.getInstance().convertVideo(ChatActivity.this, file.getAbsolutePath(), cacheFile);
                retriever.setDataSource(cacheFile.getAbsolutePath());
                String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                Subscription sp = presenter.uploadFile("video", result.file.getAbsolutePath(), msg.get_id(), MessageDataProcess.getInstance().getFile(msg).getDuration(), Integer.parseInt(width), Integer.parseInt(height));
                if (sp == null) {
                    MessageRealm.getInstance().deleteMessage(msg.get_id())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Message>() {
                                @Override
                                public void call(Message message) {
                                    adapter.deleteOne(message.get_id());
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {

                                }
                            });
                }
                subscriber.onNext(null);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        MessageRealm.getInstance().deleteMessage(msg.get_id())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Message>() {
                                    @Override
                                    public void call(Message message) {
                                        adapter.deleteOne(message.get_id());
                                    }
                                }, new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {

                                    }
                                });
                    }
                });
    }

    private void sendAudioMessage(Message msg) {
        if (isPrivate) {
            msg.setTo(member);
            msg.set_toId(member.get_id());
            msg.setForeignId(member.get_id());
        } else if (room != null) {
            msg.setRoom(room);
            msg.set_roomId(room.get_id());
            msg.setForeignId(room.get_id());
        } else if (story != null) {
            msg.setStory(story);
            msg.set_storyId(story.get_id());
            msg.setForeignId(story.get_id());
        }
        MessageDataProcess.getInstance().processForPersistent(msg);
        MessageRealm.getInstance().addOrUpdate(msg)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new EmptyAction<Message>(), new RealmErrorAction());
        adapter.addToEnd(RowFactory.getInstance().makeRows(msg, this, messageActionCallback));
        scrollToBottom();
        Subscription sp = presenter.uploadFile("audio/amr", msg.getAudioLocalPath(), msg.get_id(), MessageDataProcess.getInstance().getFile(msg).getDuration());
        subscription.add(sp);
        if (sp == null) {
            MessageRealm.getInstance().deleteMessage(msg.get_id())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Message>() {
                        @Override
                        public void call(Message message) {
                            adapter.deleteOne(message.get_id());
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {

                        }
                    });
        }
    }

    @Subscribe
    public void onRePostEvent(RePostEvent event) {
        List<Attachment> attachments;
        Message msg = event.getMessage();
        if (msg != null && adapter != null) {
            if (event.getAction() == RePostEvent.RE_SEND) {
                adapter.updateStatus(msg.get_id(), MessageDataProcess.Status.SENDING.ordinal());
                if (isPrivate) {
                    if (StringUtil.isNotBlank(msg.getBody())) {
                        presenter.sendPrivateMessage(member.get_id(), msg.getBody(), msg.get_id());
                    } else {
                        attachments = new ArrayList<>();
                        attachments.add(new Attachment(MessageDataProcess.getInstance().getUploadData(msg)));
                        presenter.sendPrivateMsgWithFile(attachments, member.get_id(), msg.get_id());
                    }
                } else {
                    if (StringUtil.isNotBlank(msg.getBody())) {
                        if (room != null) {
                            presenter.sendPublicMessage(room.get_id(), msg.getBody(), msg.get_id());
                        } else if (story != null) {
                            presenter.sendStoryMessage(story.get_id(), msg.getBody(), msg.get_id());
                        }
                    } else {
                        attachments = new ArrayList<>();
                        attachments.add(new Attachment(MessageDataProcess.getInstance().getUploadData(msg)));
                        if (room != null) {
                            presenter.sendPublicMsgWithFile(attachments, room.get_id(), msg.get_id());
                        } else if (story != null) {
                            presenter.sendStoryMsgWithFile(attachments, story.get_id(), msg.get_id());
                        }
                    }
                }
            } else if (event.getAction() == RePostEvent.RE_UPLOAD) {
                adapter.updateStatus(msg.get_id(), MessageDataProcess.Status.UPLOADING.ordinal());
                Subscription sp = null;
                if (msg.getDisplayMode().equals(MessageDataProcess.DisplayMode.IMAGE.toString())) {
                    String path = MessageDataProcess.getInstance().getUploadData(msg).getThumbnailUrl();
                    path = path.substring(Message.SCHEME_FILE.length(), path.length());
                    sp = presenter.uploadFile("image/*", path, msg.get_id());
                } else if (msg.getDisplayMode().equals(MessageDataProcess.DisplayMode.SPEECH.toString())) {
                    String path = msg.getAudioLocalPath();
                    sp = presenter.uploadFile("audio/amr", path, msg.get_id(), MessageDataProcess.getInstance().getFile(msg).getDuration());
                }
                subscription.add(sp);
            }
        }
    }

    @Override
    public void onSendMessageSuccess(String msgId, Message message) {
        messageToSend = null;
        imageToSend = null;
        if ("image".equals(message.getDisplayMode())) {
            View childView = msgListView.getChildAt(msgListView.getChildCount() - 2);
            TextView statusText = (TextView) childView.findViewById(R.id.tv_status);
            if (message.getStatus() == MessageDataProcess.Status.NONE.ordinal()) {
                statusText.setVisibility(View.GONE);
                statusText.setOnClickListener(null);
            }
            TextView timeText = (TextView) childView.findViewById(R.id.tv_time);
            timeText.setText(MessageFormatter.formatCreateTime(message.getCreatedAt()));
            TextView nameText = (TextView) childView.findViewById(R.id.tv_name);
            nameText.setText(message.getCreatorName());
            Row row = adapter.getRows().get(adapter.getRows().size() - 1);
            if (row instanceof InfoRow) {
                InfoRow infoRow = (InfoRow) row;
                infoRow.setMessage(message);
                infoRow.setStatus(MessageDataProcess.Status.NONE.ordinal());
            }
            if (adapter.getRows().get(adapter.getRows().size() - 2) instanceof ImageRow) {
                ImageRow imageRow = (ImageRow) adapter.getRows().get(adapter.getRows().size() - 2);
                imageRow.setMessage(message);
            }
            return;
        }
        adapter.updateOne(msgId, RowFactory.getInstance().makeRows(message, this,
                messageActionCallback, retriever));
    }

    @Override
    public void onSendMessageFailed(String msgId) {
        MessageRealm.getInstance().updateSendFailedMessage(msgId, MessageDataProcess.Status.SEND_FAILED.ordinal())
                .flatMap(new Func1<Message, Observable<Notification>>() {
                    @Override
                    public Observable<Notification> call(Message message) {
                        return NotificationRealm.getInstance().updateNotificationWithLocalMessage(message);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Notification>() {
                    @Override
                    public void call(Notification notification) {
                        BusProvider.getInstance().post(new UpdateNotificationEvent(notification));
                    }
                }, new RealmErrorAction());

        adapter.updateStatus(msgId, MessageDataProcess.Status.SEND_FAILED.ordinal());
    }

    public void onUploadFileSuccess(final FileUploadResponseData file, final String msgId) {
        MessageRealm.getInstance().getMessage(msgId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Message>() {
                    @Override
                    public void call(Message message) {
                        if (message == null) return;
                        String id;
                        if (story != null) {
                            id = story.get_id();
                        } else if (room != null) {
                            id = room.get_id();
                        } else {
                            id = member.get_id();
                        }
                        MessageDataProcess.getInstance().setImage(file, message);
                        List<Attachment> attachments = new ArrayList<>();
                        attachments.add(new Attachment(MessageDataProcess.getInstance().getUploadData(message)));
                        if (isPrivate) {
                            presenter.sendPrivateMsgWithFile(attachments, id, msgId);
                        } else if (room != null) {
                            presenter.sendPublicMsgWithFile(attachments, id, msgId);
                        } else if (story != null) {
                            presenter.sendStoryMsgWithFile(attachments, id, msgId);
                        }
                    }
                }, new RealmErrorAction());
    }

    @Override
    public void onUploadFileFailed(String msgId) {
        MessageRealm.getInstance().updateSendFailedMessage(msgId, MessageDataProcess.Status.UPLOAD_FAILED.ordinal())
                .flatMap(new Func1<Message, Observable<Notification>>() {
                    @Override
                    public Observable<Notification> call(Message message) {
                        return NotificationRealm.getInstance().updateNotificationWithLocalMessage(message);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Notification>() {
                    @Override
                    public void call(Notification notification) {
                        BusProvider.getInstance().post(new UpdateNotificationEvent(notification));
                    }
                }, new RealmErrorAction());
        adapter.updateStatus(msgId, MessageDataProcess.Status.UPLOAD_FAILED.ordinal());
    }

    @Override
    public void onUploadFileInvalid(String msgId) {
        adapter.deleteOne(msgId);
        MessageRealm.getInstance().deleteMessage(msgId)
                .subscribe(new EmptyAction<Message>(), new RealmErrorAction());
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(etMessage);
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(etMessage, emojicon);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        resetAdditionalButton();
        if (isChecked) {
            imm.hideSoftInputFromWindow(etMessage.getWindowToken(), 0);
            if (sendButtonState == STATE_KEYBOARD) {
                btnSend.setBackgroundDrawable(getResources().getDrawable(R.drawable.selector_voice));
                sendButtonState = STATE_VOICE;
                voiceBar.setVisibility(View.GONE);
                etMessage.setVisibility(View.VISIBLE);
                etMessage.requestFocus();
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    emojicons.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scrollToBottom();
                        }
                    }, 200);
                }
            }, 100);
        } else {
            emojicons.setVisibility(View.GONE);
            imm.toggleSoftInputFromWindow(etMessage.getWindowToken(), 0, 0);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (emojicons.isShown()) {
                emojicons.setVisibility(View.GONE);
                cbEmoji.setChecked(false);
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

    @Override
    public void onShown() {
        resetAdditionalButton();
        emojicons.setVisibility(View.GONE);
        cbEmoji.setOnCheckedChangeListener(null);
        cbEmoji.setChecked(false);
        cbEmoji.setOnCheckedChangeListener(this);
        if (!isSearchResult) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollToBottom();
                }
            }, 200);
        }
    }

    @Override
    public void onHidden() {
        System.out.println();
    }

    @Override
    public void onDownloadProgress(int progress) {
        proDialog.setProgress(progress);
        proDialog.show();
    }

    @Override
    public void onDownloadFinish(String path) {
        proDialog.dismiss();
        final File file = new File(path);
        if (file.exists()) {
            if (BizLogic.isImg(path)) {
                MediaController.updateSystemGallery(path);
            }
            new TalkDialog.Builder(this)
                    .title(R.string.download_finish)
                    .titleColorRes(R.color.white)
                    .titleBackgroundColorRes(R.color.talk_grass)
                    .content(String.format(getString(R.string.download_finish_message), path))
                    .positiveText(R.string.confirm)
                    .positiveColorRes(R.color.talk_grass)
                    .negativeColorRes(R.color.material_grey_700)
                    .negativeText(R.string.cancel)
                    .callback(new TalkDialog.ButtonCallback() {
                        @Override
                        public void onPositive(TalkDialog materialDialog, View v) {
                            FileUtil.openFileByType(ChatActivity.this, fileType, file);
                        }
                    }).show();
        }
    }

    @Override
    public void onJoinTopic(Room room) {
        this.room = room;
        isPreview = false;
        msgListView.removeFooterView(emptyFooter);
        layoutInput.setVisibility(View.VISIBLE);
        layoutPreview.setVisibility(View.GONE);
        getMenuInflater().inflate(R.menu.menu_topic, menu);
        scrollToBottom();
    }

    private MessageDialogBuilder.MessageActionCallback messageActionCallback = new SimpleMessageActionCallback() {
        @Override
        public void deleteMessage(Message msg) {
            if (msg.getStatus() == MessageDataProcess.Status.NONE.ordinal()) {
                presenter.deleteMessage(msg.get_id());
            } else {
                MessageRealm.getInstance().deleteMessage(msg.get_id())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Message>() {
                            @Override
                            public void call(Message message) {

                            }
                        }, new RealmErrorAction());
                adapter.deleteOne(msg.get_id());
            }
        }

        @Override
        public void editMessage(Message msg, String text) {
            if (msg.getStatus() == MessageDataProcess.Status.NONE.ordinal()) {
                presenter.updateMessage(msg.get_id(), text);
            } else {
                msg.setBody(text);
                MessageRealm.getInstance().addOrUpdate(msg)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Message>() {
                            @Override
                            public void call(Message message) {
                                BusProvider.getInstance().post(new NewMessageEvent(message));
                            }
                        }, new RealmErrorAction());
                adapter.updateOne(msg.get_id(), RowFactory.getInstance()
                        .makeRows(msg, ChatActivity.this, messageActionCallback, retriever));
            }
        }

        @Override
        public void saveFile(String fileName, String fileType, String downloadUrl) {
            ChatActivity.this.fileType = fileType;
            downloadFile(downloadUrl, FileDownloader.getDownloadPath(fileName));
        }

        @Override
        public void copyText(CharSequence text) {
            copyToClipboard(text);
        }

        @Override
        public void favorite(String msgId) {
            presenter.favoriteMessage(msgId);
        }

        @Override
        public void tag(String msgId, List<Tag> tags) {
            Intent intent = new Intent(ChatActivity.this, AddTagActivity.class);
            intent.putExtra("messageId", msgId);
            if (tags != null && !tags.isEmpty()) {
                String[] s = new String[tags.size()];
                for (int index = 0; index < tags.size(); index++) {
                    Tag tag = tags.get(index);
                    if (tag != null) {
                        s[index] = tag.getName();
                    }
                }
                intent.putExtra("tags", s);
            }
            startActivityForResult(intent, 4);
        }

        @Override
        public void forward(String msgId) {
            Intent intent = new Intent(ChatActivity.this, RepostAndShareActivity.class);
            intent.putExtra("id", msgId);
            startActivity(intent);
        }
    };

    private SpannableStringBuilder getAtInfo(Member member) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(etMessage.getText());
        if (ssb.length() > 0 && ssb.charAt(ssb.length() - 1) == '@') {
            ssb.delete(ssb.length() - 1, ssb.length());
        }
        String dsl = "<$at|" + member.get_id() + "|@" + member.getAlias() + " $>";
        ActionSpan span = new ActionSpan(dsl, "at", member.get_id(),
                "@" + member.getAlias() + " ");
        int start = ssb.length();
        ssb.append("～");
        ssb.setSpan(span, start, start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ssb;
    }

    private void downloadFile(String url, String path) {
        proDialog.show();
        presenter.downloadFile(url, path);
    }

    private void scrollToBottom() {
        msgListView.setSelectionFromTop(msgListView.getCount() - 1, -100000);
        msgListView.post(new Runnable() {
            @Override
            public void run() {
                msgListView.setSelectionFromTop(msgListView.getCount() - 1, -100000);
            }
        });
    }

    private void copyToClipboard(CharSequence string) {
        ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        manager.setText(string);
    }

    @Override
    public void onAvatarClick(String memberId) {
        Member member = MainApp.globalMembers.get(memberId);
        if (getIntent().getStringExtra("data") == null && member != null) {
            new MemberInfoDialog
                    .Builder(this, R.style.Talk_Dialog)
                    .setMember(member)
                    .show();
        }
    }

    @Override
    public void onAvatarLongClick(String memberId) {
        if (!isPrivate) {
            Member member = MainApp.globalMembers.get(memberId);
            if (member != null) {
                SpannableStringBuilder ssb = getAtInfo(member);
                etMessage.setText(ssb);
                etMessage.setSelection(ssb.length());
            }
        }
    }

    private void resetUnreadLayout() {
        tempUnreadCount = 0;
        layoutUnreadTip.setVisibility(View.GONE);
    }

    private void initMessages() {
        canLoadOldMessage = true;
        canLoadNewMessage = false;
        isSearchResult = false;
        if (isPrivate) {
            presenter.syncPrivateMessages(member.get_id());
        } else if (room != null) {
            presenter.syncPublicMessages(room.get_id());
        } else if (story != null) {
            presenter.syncStoryMessages(story.get_id());
        }
    }

    private void showFinishHeader() {
        headerLoading.setVisibility(View.GONE);
        if (isPrivate) {
            if (BizLogic.isXiaoai(member)) {
                headerXiaoai.setAlpha(0.0f);
                headerXiaoai.setVisibility(View.VISIBLE);
                headerXiaoai.animate()
                        .alpha(1.0f)
                        .setDuration(1000)
                        .start();
            } else {
                headerPrivate.setAlpha(0.0f);
                headerPrivate.setVisibility(View.VISIBLE);
                headerPrivate.animate()
                        .alpha(1.0f)
                        .setDuration(1000)
                        .start();
            }
        } else if (room != null && room.getIsGeneral()) {
            headerGeneral.setAlpha(0.0f);
            headerGeneral.setVisibility(View.VISIBLE);
            headerGeneral.animate()
                    .alpha(1.0f)
                    .setDuration(1000)
                    .start();
        } else if (room != null) {
            Member m;
            if (MainApp.globalMembers.get(room.get_creatorId()) == null) {
                m = new Member();
                m.setName(getString(R.string.anonymous_user));
            } else {
                m = MainApp.globalMembers.get(room.get_creatorId());
            }
            TextView tv = (TextView) headerView.findViewById(R.id.tv_finish_topic);
            tv.setText(String.format(getString(R.string.message_of_public_talk), m.getAlias(),
                    DateUtil.formatDate(room.getCreatedAt(), MainApp.CONTEXT.
                            getString(R.string.date_format_full))));
            headerPublic.setAlpha(0.0f);
            headerPublic.setVisibility(View.VISIBLE);
            headerPublic.animate()
                    .alpha(1.0f)
                    .setDuration(1000)
                    .start();
        } else if (story != null) {
            Member m;
            if (MainApp.globalMembers.get(story.get_creatorId()) == null) {
                m = new Member();
                m.setName(getString(R.string.anonymous_user));
            } else {
                m = MainApp.globalMembers.get(story.get_creatorId());
            }
            TextView tv = (TextView) headerView.findViewById(R.id.tv_finish_story);
            tv.setText(String.format(getString(R.string.message_of_story_talk), m.getAlias(),
                    DateUtil.formatDate(story.getCreatedAt(), MainApp.CONTEXT.
                            getString(R.string.date_format_full))));
            headerStory.setAlpha(0.0f);
            headerStory.setVisibility(View.VISIBLE);
            headerStory.animate()
                    .alpha(1.0f)
                    .setDuration(1000)
                    .start();
        }
    }

    private void showLoadingHeader() {
        headerLoading.setVisibility(View.VISIBLE);
        if (isPrivate) {
            if (BizLogic.isXiaoai(member)) {
                headerXiaoai.setVisibility(View.GONE);
            } else {
                headerPrivate.setVisibility(View.GONE);
            }
        } else if (room != null && room.getIsGeneral()) {
            headerGeneral.setVisibility(View.GONE);
        } else if (room != null) {
            headerPublic.setVisibility(View.GONE);
        }
    }

    private void showLoadingFooter() {
        footerLoading.setVisibility(View.VISIBLE);
        footerEmpty.setVisibility(View.GONE);
    }

    private void hideLoadingFooter() {
        footerLoading.setVisibility(View.GONE);
        footerEmpty.setVisibility(View.VISIBLE);
    }

    public void resetKeyBoardButton() {
        imm.hideSoftInputFromWindow(etMessage.getWindowToken(), 0);
        emojicons.setVisibility(View.GONE);
        cbEmoji.setOnCheckedChangeListener(null);
        cbEmoji.setChecked(false);
        cbEmoji.setOnCheckedChangeListener(this);
    }

    private void resetAdditionalButton() {
        if (recyclerViewMore.getVisibility() == View.VISIBLE) {
            recyclerViewMore.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAddOnItemClick(int position, AddonsItem item) {
        switch (position) {
            // 图片
            case 0: {
                Intent intent = new Intent(this, SelectImageActivity.class);
                startActivityForResult(intent, SelectImageActivity.SELECT_IMAGES);
                break;
            }
            case 1: {
                Intent localIntent = new Intent(Intent.ACTION_GET_CONTENT);
                localIntent.setType("*/*");
                localIntent.addCategory(Intent.CATEGORY_OPENABLE);
                localIntent = Intent.createChooser(localIntent, "attachment");
                startActivityForResult(localIntent, REQUEST_SELECT_FILE);
                break;
            }
            case 2: {
                Intent intent = new Intent(this, FavoritesActivity.class);
                intent.putExtra("is_chat_join", true);
                intent.putExtra("title", getSupportActionBar().getTitle().toString());
                intent.putExtra("is_private", isPrivate);
                String id = "";
                if (member != null) {
                    id = member.get_id();
                } else if (room != null) {
                    id = room.get_id();
                } else if (story != null) {
                    id = story.get_id();
                }
                intent.putExtra("id", id);
                startActivity(intent);
                break;
            }
            case 3: {
                Intent intent = new Intent(this, AddonsWebView.class);
                intent.putExtra("item", Parcels.wrap(item));
                startActivity(intent);
                break;
            }
            case 4:
                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
                }
                break;
        }
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

    public void resetStory() {
        getSupportActionBar().setTitle(story.getTitle());
        overlay.setClickable(false);
        overlay.animate()
                .alpha(0.0F)
                .setDuration(200L)
                .setInterpolator(new FastOutSlowInInterpolator())
                .start();
        switch (StoryDataProcess.Category.getEnum(story.getCategory())) {
            case FILE:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, FileStoryFragment.getInstance(story, false))
                        .commit();
                break;
            case TOPIC:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, TopicStoryFragment.getInstance(story, false))
                        .commit();
                break;
            case LINK:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, LinkStoryFragment.getInstance(story, false))
                        .commit();
                break;
        }
    }

    @Subscribe
    public void onUpdateStory(UpdateStoryEvent event) {
        if (event.story == null || this.story == null
                || !TextUtils.equals(event.story.get_id(), this.story.get_id())) return;
        this.story = event.story;
    }

    @Subscribe
    public void onRemoveStory(RemoveStoryEvent event) {
        if (event.story == null || story == null || !TextUtils.equals(event.story.get_id(), story.get_id()))
            return;
        new TalkDialog.Builder(this)
                .title(R.string.story_already_delete_title)
                .titleColorRes(R.color.white)
                .titleBackgroundColorRes(R.color.colorPrimary)
                .positiveText(R.string.confirm)
                .positiveColorRes(R.color.colorPrimary)
                .negativeColorRes(R.color.material_grey_700)
                .negativeText(R.string.cancel)
                .content(R.string.story_delete_content)
                .callback(new TalkDialog.ButtonCallback() {
                    @Override
                    public void onPositive(TalkDialog dialog, View v) {
                        super.onPositive(dialog, v);
                        finish();
                    }

                    @Override
                    public void onNegative(TalkDialog dialog) {
                        super.onNegative(dialog);
                        layoutInput.setVisibility(View.GONE);
                        menu.findItem(R.id.action_story_edit).setVisible(false);
                        menu.findItem(R.id.action_story_setting).setVisible(false);
                    }
                }).show();

    }

    @Subscribe
    public void onStoryExpand(StoryDetailExpandEvent event) {
        if (event.isExpand) {
            hideNewFeatureTips();
        }
    }

    @Subscribe
    public void onStoryEvent(StoryEvent event) {
        if (story == null) return;
        final List<String> memberIds = event.memberIds;
        if (memberIds != null && !memberIds.isEmpty()) {
            story.get_memberIds().clear();
            story.get_memberIds().addAll(memberIds);
        }
        final String memberId = event.memberId;
        if (memberId != null) {
            story.get_memberIds().remove(memberId);
        }
    }

    private void showNewFeatureTips() {
        boolean showTips = MainApp.PREF_UTIL.getBoolean(Constant.SHOW_EXPAND_STORY_TIPS, true);
        if (showTips) {
            View container = findViewById(R.id.container);
            try {
                showcaseView = new ShowcaseView.Builder(this)
                        .withNewStyleShowcase()
                        .setTarget(new PointTarget(DensityUtil.screenWidthInPix(this) / 2,
                                (int) container.getY() + DensityUtil.dip2px(this, 56)))
                        .setContentTitle(R.string.tips_expand_story)
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
                MainApp.PREF_UTIL.putBoolean(Constant.SHOW_EXPAND_STORY_TIPS, false);
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