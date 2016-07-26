package com.teambition.talk.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.talk.dialog.TalkDialog;
import com.teambition.talk.BizLogic;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.adapter.FavoritesAdapter;
import com.teambition.talk.client.data.SearchRequestData;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Room;
import com.teambition.talk.entity.Story;
import com.teambition.talk.presenter.FavoritesPresenter;
import com.teambition.talk.realm.MemberRealm;
import com.teambition.talk.realm.MessageDataProcess;
import com.teambition.talk.realm.RoomRealm;
import com.teambition.talk.realm.StoryRealm;
import com.teambition.talk.rx.EmptyAction;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.ui.OnMessageClickExecutor;
import com.teambition.talk.ui.fragment.SearchFavoritesFragment;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.ThemeUtil;
import com.teambition.talk.util.TransactionUtil;
import com.teambition.talk.view.FavoritesView;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by nlmartian on 5/27/15.
 */
public class FavoritesActivity extends BaseActivity implements FavoritesView, FavoritesAdapter.OnSelectedChangedListener {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.listView)
    ListView listView;
    @InjectView(R.id.layout_placeholder)
    View placeholder;

    private FavoritesAdapter adapter;
    private FavoritesPresenter presenter;
    private SearchRequestData data = new SearchRequestData(BizLogic.getTeamId(), SearchRequestData.TYPE_FILE);
    private boolean isLoading;
    private boolean canLoadMore;
    private String maxId;
    private MenuItem deleteMenuItem;
    private MenuItem searchMenuItem;
    private MenuItem sendMenuItem;
    private MenuItem contextMenuItem;

    //如果是聊天界面进来的,就禁用长按功能,单击的功能也有所改变
    private boolean mIsChatJoin;
    //转发时需要的话题或者人的名字
    private String mTitle;
    //用户的Id或者话题的id
    private String mId;
    //是否是私聊
    private boolean mIsPrivate;

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final Message msg = adapter.getItem(position);
            if (mIsChatJoin) {
                new TalkDialog.Builder(FavoritesActivity.this)
                        .contentColorRes(R.color.material_grey_700)
                        .content(String.format(getString(R.string.confirm_send_to), mTitle))
                        .positiveText(R.string.send)
                        .positiveColor(getResources().getColor(R.color.colorPrimary))
                        .negativeColorRes(R.color.material_grey_700)
                        .negativeText(R.string.cancel)
                        .callback(new TalkDialog.ButtonCallback() {
                            @Override
                            public void onPositive(TalkDialog dialog, View v) {
                                if (mIsPrivate) {
                                    presenter.repostMessage(msg.get_id(), BizLogic.getTeamId(), null, mId);
                                } else {
                                    presenter.repostMessage(msg.get_id(), BizLogic.getTeamId(), mId, null);
                                }
                            }
                        })
                        .show();
                return;
            }
            if (adapter.isEditMode()) {
                CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
                checkBox.setChecked(!checkBox.isChecked());
            } else {
                if (msg != null) {
                    new OnMessageClickExecutor(FavoritesActivity.this, msg) {
                        @Override
                        public void onImageClick(Context context, Message message) {
                            SearchRequestData d = data.copy();
                            int imgCount = 0;
                            int page = 1;
                            for (int i = 0; i < adapter.getCount(); i++) {
                                if (MessageDataProcess.getInstance().getFile(adapter.getItem(i)) != null && "image".equals(
                                        MessageDataProcess.getInstance().getFile(adapter.getItem(i)).getFileCategory())) {
                                    imgCount++;
                                    if (msg.get_id().equals(adapter.getItem(i).get_id())) {
                                        break;
                                    }
                                }
                            }
                            if (imgCount != 0) {
                                page = imgCount / d.limit + (imgCount % d.limit == 0 ? 0 : 1);
                            }
                            d.fileCategory = "image";
                            d.page = page;
                            d.sort = new SearchRequestData.Sort(null);
                            d.sort.byFavoritedAt(SearchRequestData.Sort.DESC);
                            Bundle bundle = new Bundle();
                            bundle.putString("msgId", msg.get_id());
                            bundle.putParcelable("data", Parcels.wrap(d));
                            TransactionUtil.goTo(FavoritesActivity.this,
                                    FavoritesPhotoViewActivity.class, bundle);
                        }
                    }.execute();
                }
            }
        }
    };

    private AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            adapter.setEditMode(true);
            adapter.setItemSelection(position, true);
            toolbar.setTitle(String.format(getString(R.string.favorites_selected),
                    adapter.getSelectedFavorite().size()));
            toggleMenuStatus(true, true, true);
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        ButterKnife.inject(this);
        progressBar = findViewById(R.id.progress_bar);

        mIsChatJoin = getIntent().getBooleanExtra("is_chat_join", false);
        mTitle = getIntent().getStringExtra("title");
        mIsPrivate = getIntent().getBooleanExtra("is_private", false);
        mId = getIntent().getStringExtra("id");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mIsChatJoin ? R.string.send_favorite_content : R.string.favorites_items);

        presenter = new FavoritesPresenter(this);
        adapter = new FavoritesAdapter(this);
        adapter.setOnSelectedChangedListener(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onItemClickListener);
        if (!mIsChatJoin) {
            listView.setOnItemLongClickListener(onItemLongClickListener);
        }
        listView.setOnScrollListener(new PauseOnScrollListener(MainApp.IMAGE_LOADER, true, true, new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount > visibleItemCount && canLoadMore && !isLoading) {
                    if (firstVisibleItem + visibleItemCount + 6 > adapter.getCount()) {
                        isLoading = true;
                        presenter.getFavorites(BizLogic.getTeamId(), maxId);
                    }
                }
            }
        }));

        presenter.getFavorites(BizLogic.getTeamId(), null);
    }

    @Override
    public void showFavorites(List<Message> favoriteList) {
        canLoadMore = !favoriteList.isEmpty() && favoriteList.size() >= 30;
        if (maxId == null) {
            if (!favoriteList.isEmpty()) {
                adapter.updateData(favoriteList);
                placeholder.setVisibility(View.GONE);
            } else {
                placeholder.setVisibility(View.VISIBLE);
            }
        } else {
            adapter.addToEnd(favoriteList);
        }

        if (!favoriteList.isEmpty()) {
            maxId = favoriteList.get(favoriteList.size() - 1).get_id();
        }
        isLoading = false;
    }

    @Override
    public void removeFavoritesSuccess(List<String> removedIds) {
        adapter.removeSelected();
        toggleMenuStatus(false, false, false);
        if (adapter.getCount() == 0) {
            placeholder.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRepostFinish(Message message) {
        MainApp.showToastMsg(R.string.sent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            adapter.setEditMode(false);
            toggleMenuStatus(false, false, false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                if (adapter.isEditMode()) {
                    adapter.setEditMode(false);
                    toggleMenuStatus(false, false, false);
                } else {
                    finish();
                }
                break;
            }
            case R.id.action_delete: {
                new TalkDialog.Builder(this)
                        .title(R.string.title_delete_favorite)
                        .titleColorRes(R.color.white)
                        .titleBackgroundColorRes(R.color.talk_warning)
                        .content(R.string.message_delete_favorite)
                        .positiveText(R.string.confirm)
                        .positiveColorRes(R.color.talk_warning)
                        .negativeText(R.string.cancel)
                        .negativeColorRes(R.color.material_grey_700)
                        .callback(new TalkDialog.ButtonCallback() {
                            @Override
                            public void onPositive(TalkDialog materialDialog, View v) {
                                List<Message> messages = adapter.getSelectedFavorite();
                                if (messages.isEmpty()) return;
                                ArrayList<String> ids = new ArrayList<>();
                                for (Message message : messages) {
                                    ids.add(message.get_id());
                                }
                                presenter.batchRemove(ids);
                            }
                        })
                        .show();
                break;
            }
            case R.id.action_search: {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out)
                        .add(R.id.container, SearchFavoritesFragment.getInstance())
                        .addToBackStack(null)
                        .commit();
                break;
            }
            case R.id.action_send: {
                List<Message> favorites = adapter.getSelectedFavorite();
                if (favorites.isEmpty()) return false;
                Collections.sort(favorites, new Comparator<Message>() {
                    @Override
                    public int compare(Message lhs, Message rhs) {
                        return lhs.get_messageId().compareTo(rhs.get_messageId());
                    }
                });
                Intent intent = new Intent(this, RepostAndShareActivity.class);
                String[] ids = new String[favorites.size()];
                for (int i = 0; i < favorites.size(); i++) {
                    ids[i] = favorites.get(i).get_id();
                }
                intent.putExtra("favoritesIds", ids);
                startActivityForResult(intent, 0);
                break;
            }
            case R.id.action_context: {
                List<Message> favorites = adapter.getSelectedFavorite();
                if (favorites.isEmpty()) return false;
                final Message favorite = favorites.get(0);
                final Intent intent = new Intent(this, ChatActivity.class);
                if (StringUtil.isNotBlank(favorite.get_toId())) {
                    MemberRealm.getInstance().getMember(favorite.get_toId())
                            .observeOn(AndroidSchedulers.mainThread())
                            .filter(new Func1<Member, Boolean>() {
                                @Override
                                public Boolean call(Member member) {
                                    return member != null;
                                }
                            }).subscribe(new Action1<Member>() {
                        @Override
                        public void call(Member member) {
                            intent.putExtra(ChatActivity.EXTRA_MEMBER, Parcels.wrap(member));
                            intent.putExtra(ChatActivity.EXTRA_MESSAGE, Parcels.wrap(favorite));
                            startActivity(intent);
                        }
                    }, new RealmErrorAction());
                } else if (StringUtil.isNotBlank(favorite.get_roomId())) {
                    RoomRealm.getInstance().getRoom(favorite.get_roomId())
                            .observeOn(AndroidSchedulers.mainThread())
                            .filter(new Func1<Room, Boolean>() {
                                @Override
                                public Boolean call(Room room) {
                                    return room != null;
                                }
                            }).subscribe(new Action1<Room>() {
                        @Override
                        public void call(Room room) {
                            intent.putExtra(ChatActivity.EXTRA_ROOM, Parcels.wrap(room));
                            intent.putExtra(ChatActivity.EXTRA_MESSAGE, Parcels.wrap(favorite));
                            startActivity(intent);
                        }
                    }, new RealmErrorAction());

                }
                if (favorite.getStory() != null) {
                    intent.putExtra(ChatActivity.EXTRA_MESSAGE, Parcels.wrap(favorite));
                    intent.putExtra(ChatActivity.EXTRA_STORY, Parcels.wrap(favorite.getStory()));
                    startActivity(intent);
                }
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_favorites, menu);
        deleteMenuItem = menu.findItem(R.id.action_delete);
        searchMenuItem = menu.findItem(R.id.action_search);
        sendMenuItem = menu.findItem(R.id.action_send);
        contextMenuItem = menu.findItem(R.id.action_context);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (adapter.isEditMode()) {
            adapter.setEditMode(false);
            toggleMenuStatus(false, false, false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSelectedChanged(int count) {
        toggleMenuStatus(true, count > 0, count == 1);
        if (count == 0) {
            toolbar.setTitle(null);
        } else if (count > 1) {
            toolbar.setTitle(getString(R.string.favorites_selected, count));
        } else {
            toolbar.setTitle(getString(R.string.favorites_selected_one));
        }
    }

    private void toggleMenuStatus(boolean editMode, boolean showDeleteItem, boolean showContext) {
        if (editMode) {
            searchMenuItem.setVisible(false);
            deleteMenuItem.setVisible(showDeleteItem);
            sendMenuItem.setVisible(showDeleteItem);
            contextMenuItem.setVisible(showContext);
        } else {
            searchMenuItem.setVisible(true);
            deleteMenuItem.setVisible(false);
            sendMenuItem.setVisible(false);
            getSupportActionBar().setTitle(R.string.favorites_items);
            contextMenuItem.setVisible(false);
        }
    }
}
