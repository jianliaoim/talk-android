package com.teambition.talk.ui.activity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.teambition.talk.BizLogic;
import com.teambition.talk.R;
import com.teambition.talk.adapter.MessageSearchAdapter;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Room;
import com.teambition.talk.entity.Tag;
import com.teambition.talk.imageloader.RecyclerViewPauseOnScrollListener;
import com.teambition.talk.presenter.SearchPresenter;
import com.teambition.talk.realm.MemberRealm;
import com.teambition.talk.realm.RoomRealm;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.ui.fragment.MessageSearchFragment;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.view.SearchView;
import com.umeng.analytics.MobclickAgent;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by wlanjie on 16/2/15.
 */
public class MessageSearchActivity extends BaseActivity implements TextWatcher, SearchView, MessageSearchFragment.OnItemClick, MessageSearchAdapter.OnItemClick, MessageSearchFragment.OnRemoveFragmentListener {

    final static int MAX_PAGE = 10;
    public final static String KEY = "keyword";
    public final static String MEMBER = "member";
    public final static String ROOM = "room";
    public final static String TAG = "tag";
    public final static String TYPE = "type";
    public final static String TIME = "time";
    private List<Member> members;
    private List<Room> rooms;
    private List<Tag> tags;
    String memberId;
    String roomId = null;
    Boolean isDirectMessage = null;
    Boolean hasTag = null;
    String tagId = null;
    String type = null;
    String time = "quarter";
    private final String[] types = new String[]{null, "file", "rtf", "url", "snippet"};
    private final String[] times = new String[]{"day", "week", "month", "quarter"};

    @InjectView(R.id.btn_clear)
    View clearView;

    @InjectView(R.id.et_keyword)
    EditText etKeyword;

    @InjectView(R.id.text)
    TextView emptyText;

    @InjectView(R.id.progress_bar)
    View progressBar;

    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;

    @InjectView(R.id.member_layout)
    View memberView;

    @InjectView(R.id.tv_member)
    TextView memberText;

    @InjectView(R.id.member_arrow)
    View memberArrowView;

    @InjectView(R.id.topic_layout)
    View topicView;

    @InjectView(R.id.tv_topic)
    TextView topicText;

    @InjectView(R.id.topic_arrow)
    View topicArrowView;

    @InjectView(R.id.tag_layout)
    View tagView;

    @InjectView(R.id.tv_tag)
    TextView tagText;

    @InjectView(R.id.tag_arrow)
    View tagArrowView;

    @InjectView(R.id.type_layout)
    View typeView;

    @InjectView(R.id.tv_type)
    TextView typeText;

    @InjectView(R.id.type_arrow)
    View typeArrowView;

    @InjectView(R.id.time_filter_layout)
    View timeFilterView;

    @InjectView(R.id.tv_time_filter)
    TextView timeFilterText;

    @InjectView(R.id.time_arrow)
    View timeArrowView;

    private TextView textView;

    private View currentArrowView;

    private int selectViewId;

    private int memberPosition;
    private int roomPosition;
    private int tagPosition;
    private int typePosition;
    private int timePosition = 3;

    private InputMethodManager imm;

    private final MessageSearchAdapter adapter = new MessageSearchAdapter();

    private SearchPresenter presenter;

    private LinearLayoutManager layoutManager;

    private MessageSearchFragment fragment;

    private boolean canLoadMore = false;

    private int currentPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_search);
        ButterKnife.inject(this);
        final String keyword = getIntent().getStringExtra(KEY);
        presenter = new SearchPresenter(this);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        super.progressBar = progressBar;
        etKeyword.addTextChangedListener(this);
        etKeyword.setText(keyword);
        etKeyword.setSelection(keyword.length());
        etKeyword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_ENTER) && StringUtil.isNotBlank(etKeyword.getText().toString())) {
                    hideKeyboard();
                    adapter.clear();
                    currentPage = 1;
                    presenter.searchMessages(etKeyword.getText().toString());
                    return true;
                }
                return false;
            }
        });
        fragment = MessageSearchFragment.getInstance();
        fragment.setOnItemClick(this);
        fragment.setOnRemoveFragment(this);
        adapter.setOnItemClick(this);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnScrollListener(mOnScrollListener);
        recyclerView.setAdapter(adapter);
        adapter.clear();
        presenter.searchMessages(keyword);
    }

    final RecyclerViewPauseOnScrollListener mOnScrollListener = new RecyclerViewPauseOnScrollListener(new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            hideKeyboard();
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (canLoadMore && BizLogic.isNetworkConnected() &&
                    layoutManager.getItemCount() - recyclerView.getChildCount() <= (layoutManager.findFirstVisibleItemPosition() + 5)
                    && currentPage <= MAX_PAGE) {
                canLoadMore = false;
                currentPage++;
                adapter.setIsLoading(true);
                presenter.searchMessages(etKeyword.getText().toString(), currentPage, memberId, roomId, isDirectMessage, hasTag, tagId, type, time);
            }
        }
    });

    public void hideKeyboard() {
        imm.hideSoftInputFromWindow(etKeyword.getWindowToken(), 0);
    }

    @OnClick({R.id.btn_back, R.id.btn_clear, R.id.member_layout, R.id.topic_layout, R.id.tag_layout, R.id.type_layout, R.id.time_filter_layout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_back:
                hideKeyboard();
                finish();
                break;
            case R.id.btn_clear:
                etKeyword.setText("");
                break;
            case R.id.member_layout:
                currentPage = 1;
                textView = memberText;
                hideKeyboard();
                Observable.create(new Observable.OnSubscribe<List<Member>>() {
                    @Override
                    public void call(Subscriber<? super List<Member>> subscriber) {
                        subscriber.onNext(MemberRealm.getInstance().getMemberWithCurrentThread());
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<List<Member>>() {
                            @Override
                            public void call(List<Member> members) {
                                if (members != null) {
                                    MessageSearchActivity.this.members = members;
                                    ArrayList<String> items = new ArrayList<>(members.size());
                                    items.add(getString(R.string.all_members));
                                    for (Member member : members) {
                                        items.add(member.getAlias() == null ? member.getName() : member.getAlias());
                                    }
                                    fragment.setMemberPosition(memberPosition);
                                    fragment.setItems(MEMBER, items);
                                    if (selectViewId == R.id.member_layout) {
                                        animate(fragment, memberArrowView);
                                    } else {
                                        addFragment();
                                        ObjectAnimator.ofFloat(memberArrowView, "rotation", 0, 180).setDuration(300).start();
                                        ObjectAnimator.ofFloat(currentArrowView, "rotation", 180, 360).setDuration(300).start();
                                    }
                                    currentArrowView = memberArrowView;
                                    selectViewId = R.id.member_layout;
                                }
                            }
                        }, new RealmErrorAction());
                break;
            case R.id.topic_layout:
                currentPage = 1;
                textView = topicText;
                hideKeyboard();
                Observable.create(new Observable.OnSubscribe<List<Room>>() {
                    @Override
                    public void call(Subscriber<? super List<Room>> subscriber) {
                        subscriber.onNext(RoomRealm.getInstance().getRoomOnNotQuitOnNotArchivedWithCurrentThread());
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<List<Room>>() {
                            @Override
                            public void call(List<Room> rooms) {
                                if (rooms != null) {
                                    MessageSearchActivity.this.rooms = rooms;
                                    ArrayList<String> items= new ArrayList<>(rooms.size());
                                    items.add(getString(R.string.all_position));
                                    items.add(getString(R.string.fab_type_dms));
                                    for (Room room : rooms) {
                                        items.add(room.getTopic());
                                    }
                                    fragment.setRoomPosition(roomPosition);
                                    fragment.setItems(ROOM, items);
                                    if (selectViewId == R.id.topic_layout) {
                                        animate(fragment, topicArrowView);
                                    } else {
                                        addFragment();
                                        ObjectAnimator.ofFloat(topicArrowView, "rotation", 0, 180).setDuration(300).start();
                                        ObjectAnimator.ofFloat(currentArrowView, "rotation", 180, 360).setDuration(300).start();
                                    }
                                    currentArrowView = topicArrowView;
                                    selectViewId = R.id.topic_layout;
                                }
                            }
                        }, new RealmErrorAction());
                break;
            case R.id.tag_layout:
                currentPage = 1;
                textView = tagText;
                hideKeyboard();
                TalkClient.getInstance().getTalkApi()
                        .getTags(BizLogic.getTeamId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<List<Tag>>() {
                            @Override
                            public void call(List<Tag> tags) {
                                if (tags == null) return;
                                MessageSearchActivity.this.tags = tags;
                                ArrayList<String> items = new ArrayList<>(tags.size());
                                items.add(getString(R.string.not_filter_tag));
                                items.add(getString(R.string.all_tag));
                                for (Tag tag : tags) {
                                    items.add(tag.getName());
                                }
                                fragment.setTagPosition(tagPosition);
                                fragment.setItems(TAG, items);
                                if (selectViewId == R.id.tag_layout) {
                                    animate(fragment, tagArrowView);
                                } else {
                                    addFragment();
                                    ObjectAnimator.ofFloat(tagArrowView, "rotation", 0, 180).setDuration(300).start();
                                    ObjectAnimator.ofFloat(currentArrowView, "rotation", 180, 360).setDuration(300).start();
                                }
                                currentArrowView = tagArrowView;
                                selectViewId = R.id.tag_layout;
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {

                            }
                        });
                break;
            case R.id.type_layout:
                currentPage = 1;
                textView = typeText;
                hideKeyboard();
                ArrayList<String> items = new ArrayList<>();
                items.add(getString(R.string.all_type));
                items.add(getString(R.string.label_new_file));
                items.add(getString(R.string.menu_filter_rtf));
                items.add(getString(R.string.menu_filter_link));
                items.add(getString(R.string.menu_snippet));
                fragment.setTypePosition(typePosition);
                fragment.setItems(TYPE, items);
                if (selectViewId == R.id.type_layout) {
                    animate(fragment, typeArrowView);
                } else {
                    addFragment();
                    ObjectAnimator.ofFloat(typeArrowView, "rotation", 0, 180).setDuration(300).start();
                    ObjectAnimator.ofFloat(currentArrowView, "rotation", 180, 360).setDuration(300).start();
                }
                currentArrowView = typeArrowView;
                selectViewId = R.id.type_layout;
                break;
            case R.id.time_filter_layout:
                currentPage = 1;
                textView = timeFilterText;
                hideKeyboard();
                ArrayList<String> times = new ArrayList<>();
                times.add(getString(R.string.one_day_within));
                times.add(getString(R.string.one_week_within));
                times.add(getString(R.string.january_within));
                times.add(getString(R.string.march_within));
                fragment.setTimePosition(timePosition);
                fragment.setItems(TIME, times);
                if (selectViewId == R.id.time_filter_layout) {
                    animate(fragment, timeArrowView);
                } else {
                    addFragment();
                    ObjectAnimator.ofFloat(timeArrowView, "rotation", 0, 180).setDuration(300).start();
                    ObjectAnimator.ofFloat(currentArrowView, "rotation", 180, 360).setDuration(300).start();
                }
                currentArrowView = timeArrowView;
                selectViewId = R.id.time_filter_layout;
                break;
        }
    }

    private void addFragment() {
        emptyText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        if (!fragment.isAdded()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out)
                    .add(R.id.container_search, fragment)
                    .commit();
        }
    }

    private void animate(Fragment fragment, View view) {
        if (fragment.isAdded()) {
            if (recyclerView.getChildCount() <= 1) {
                emptyText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out)
                    .remove(fragment)
                    .commit();
            ObjectAnimator.ofFloat(view, "rotation", 180, 360).setDuration(300).start();
        } else {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out)
                    .add(R.id.container_search, fragment)
                    .commit();
            ObjectAnimator.ofFloat(view, "rotation", 0, 180).setDuration(300).start();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getClass().getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getClass().getName());
    }

    @Override
    public void onBackPressed() {
        if (fragment.isAdded()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out)
                    .remove(fragment)
                    .commit();
            if (currentArrowView != null) {
                ObjectAnimator.ofFloat(currentArrowView, "rotation", 180, 360).setDuration(300).start();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (StringUtil.isBlank(s.toString())) {
            clearView.setVisibility(View.GONE);
        } else {
            clearView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSearchFinish(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            if (!fragment.isAdded()) {
                emptyText.setText(String.format(getString(R.string.search_result_empty), etKeyword.getText().toString()));
                emptyText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
            return;
        } else {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        adapter.setIsLoading(false);
        adapter.addItems(messages);
        canLoadMore = messages.size() >= 20;
    }

    @Override
    public void onDeleteMessageSuccess(String messageId) {

    }

    @Override
    public void onDownloadFinish(String path) {

    }

    @Override
    public void onDownloadProgress(Integer progress) {

    }

    @Override
    public void onClick(String tag, int position, String text) {
        animate(fragment, memberArrowView);
        textView.setText(text);
        if (MEMBER.equals(tag)) {
            memberPosition = position;
            if (position == 0) {
                memberId = null;
            } else if (members != null) {
                memberId = members.get(position - 1).get_id();
            }
        } else if (ROOM.equals(tag)) {
            roomPosition = position;
            if (position == 0) {
                roomId = null;
                isDirectMessage = null;
            } else if (position == 1){
                isDirectMessage = true;
            } else if (rooms != null) {
                isDirectMessage = null;
                roomId = rooms.get(position - 2).get_id();
            }
        } else if (TAG.equals(tag)) {
            tagPosition = position;
            if (position == 0) {
                hasTag = null;
                tagId = null;
            } else if (position == 1) {
                hasTag = true;
                tagId = null;
            } else if (tags != null) {
                hasTag = null;
                tagId = tags.get(position - 2).get_id();
            }
        } else if (TYPE.equals(tag)) {
            typePosition = position;
            type = types[position];
        } else if (TIME.equals(tag)) {
            timePosition = position;
            time = times[position];
        }
        adapter.clear();
        presenter.searchMessages(etKeyword.getText().toString(), currentPage, memberId, roomId, isDirectMessage, hasTag, tagId, type, time);
    }

    @Override
    public void onItemClick(Message message) {
        hideKeyboard();
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_MESSAGE, Parcels.wrap(message));
        if (message.getStory() != null) {
            intent.putExtra(ChatActivity.EXTRA_STORY, Parcels.wrap(message.getStory()));
        }
        startActivity(intent);
    }

    @Override
    public void onRemoveFragment() {
        ObjectAnimator.ofFloat(currentArrowView, "rotation", 180, 360).setDuration(300).start();
    }
}
