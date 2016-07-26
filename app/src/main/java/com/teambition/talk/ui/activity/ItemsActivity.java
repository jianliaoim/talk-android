package com.teambition.talk.ui.activity;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.talk.dialog.TalkDialog;
import com.teambition.talk.BizLogic;
import com.teambition.talk.Constant;
import com.teambition.talk.FileDownloader;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.adapter.ItemAdapter;
import com.teambition.talk.client.data.SearchRequestData;
import com.teambition.talk.entity.FilterItem;
import com.teambition.talk.entity.Message;
import com.teambition.talk.presenter.SearchPresenter;
import com.teambition.talk.realm.MessageDataProcess;
import com.teambition.talk.ui.OnMessageClickExecutor;
import com.teambition.talk.ui.fragment.FilterFragment;
import com.teambition.talk.util.FileUtil;
import com.teambition.talk.util.MessageDialogBuilder;
import com.teambition.talk.util.SimpleMessageActionCallback;
import com.teambition.talk.util.ThemeUtil;
import com.teambition.talk.util.TransactionUtil;
import com.teambition.talk.view.SearchView;

import org.parceler.Parcels;

import java.io.File;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by zeatual on 15/5/4.
 */
public class ItemsActivity extends BaseActivity implements SearchView, FilterFragment.FilterListener,
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    public static final String EXTRA_FILTER_TYPE = "extra_filter_type";
    public static final String EXTRA_ID = "extra_id";
    public static final String EXTRA_CHAT_NAME = "extra_chat_name";

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.tv_filter)
    TextView tvFilter;
    @InjectView(R.id.tv_keyword)
    TextView tvKeyword;
    @InjectView(R.id.arrow)
    View arrow;
    @InjectView(R.id.listView)
    ListView listView;
    @InjectView(R.id.layout_placeholder)
    LinearLayout placeholder;

    private PopupMenu popupMenu;
    private FilterFragment fragment;

    private SearchPresenter presenter;
    private ItemAdapter adapter;
    private SearchRequestData data;

    private boolean isLoading = false;
    private boolean canLoadMore = false;
    private ProgressDialog proDialog;
    private String fileType;
    public boolean isShowAllDoneIcon = true;
    public String showItemIconId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);
        ButterKnife.inject(this);
        progressBar = findViewById(R.id.progress_bar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        presenter = new SearchPresenter(this);
        adapter = new ItemAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        data = new SearchRequestData(BizLogic.getTeamId(), SearchRequestData.TYPE_FILE);

        int filerType = getIntent().getIntExtra(EXTRA_FILTER_TYPE, 3);
        if (filerType == FilterItem.TYPE_MEMBER) {
            data.setMemberId(getIntent().getStringExtra(EXTRA_ID));
            tvKeyword.setText(getIntent().getStringExtra(EXTRA_CHAT_NAME));
        } else if (filerType == FilterItem.TYPE_ROOM) {
            data.setRoomId(getIntent().getStringExtra(EXTRA_ID));
            tvKeyword.setText(getIntent().getStringExtra(EXTRA_CHAT_NAME));
        }

        fragment = FilterFragment.getInstance(this);
        popupMenu = new PopupMenu(this, tvFilter);
        popupMenu.inflate(R.menu.menu_filter);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_file:
                        data.setType(SearchRequestData.TYPE_FILE);
                        adapter.setDisplayMode(MessageDataProcess.DisplayMode.FILE);
                        break;
                    case R.id.action_link:
                        data.setType(SearchRequestData.TYPE_LINK);
                        adapter.setDisplayMode(MessageDataProcess.DisplayMode.INTEGRATION);
                        break;
                    case R.id.action_rtf:
                        data.setType(SearchRequestData.TYPE_RTF);
                        adapter.setDisplayMode(MessageDataProcess.DisplayMode.RTF);
                        break;
                    case R.id.action_snippet:
                        data.setType(SearchRequestData.TYPE_SNIPPET);
                        adapter.setDisplayMode(MessageDataProcess.DisplayMode.SNIPPET);
                        break;
                }
                tvFilter.setText(menuItem.getTitle());
                tvKeyword.setText(getString(R.string.all));
                presenter.search(data);
                if (fragment.isAdded()) {
                    animate();
                }
                return true;
            }
        });

        listView.setOnScrollListener(new PauseOnScrollListener(MainApp.IMAGE_LOADER, true, true, new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount > visibleItemCount && canLoadMore && !isLoading) {
                    if (firstVisibleItem + visibleItemCount + 6 > adapter.getCount()) {
                        isLoading = true;
                        data.page = data.page + 1;
                        presenter.search(data);
                    }
                }
            }
        }));
        presenter.search(data);

        proDialog = new ProgressDialog(this);
        proDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        proDialog.setMessage(getResources().getString(R.string.wait));
        proDialog.setMax(100);

        if (MainApp.PREF_UTIL.getBoolean(Constant.IS_FIRST_OPEN_ITEMS)) {
            new TalkDialog.Builder(this)
                    .title(R.string.make_up_new_tip_title)
                    .titleColorRes(R.color.white)
                    .titleBackgroundColorRes(R.color.colorPrimary)
                    .content(R.string.make_up_new_tip_message)
                    .positiveText(R.string.i_know)
                    .dismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            MainApp.PREF_UTIL.putBoolean(Constant.IS_FIRST_OPEN_ITEMS, false);
                        }
                    })
                    .show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (fragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Message msg = adapter.getItem(position);
        if (msg != null) {
            new OnMessageClickExecutor(ItemsActivity.this, msg) {
                @Override
                public void onImageClick(Context context, Message message) {
                    SearchRequestData d = data.copy();
                    int imgCount = 0;
                    int page = 1;
                    for (int i = 0; i < adapter.getCount(); i++) {
                        if ("image".equals(MessageDataProcess.getInstance().getFile(adapter.getItem(i)).getFileCategory())) {
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
                    Bundle bundle = new Bundle();
                    bundle.putString("msgId", msg.get_id());
                    bundle.putParcelable("data", Parcels.wrap(d));
                    TransactionUtil.goTo(ItemsActivity.this, ItemPhotoViewActivity.class, bundle);
                }
            }.execute();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Message msg = adapter.getItem(position);
        MessageDialogBuilder builder = new MessageDialogBuilder(this, msg, callback);
        builder.favorite();
        if (adapter.getDisplayMode() == MessageDataProcess.DisplayMode.FILE ||
                adapter.getDisplayMode() == MessageDataProcess.DisplayMode.IMAGE) {
            builder.saveFile();
        }
        if (BizLogic.isAdmin() || BizLogic.isMe(msg.get_creatorId())) {
            builder.delete();
        }
        builder.show();
        return true;
    }

    private MessageDialogBuilder.MessageActionCallback callback = new SimpleMessageActionCallback() {
        @Override
        public void saveFile(String fileName, String fileType, String downloadUrl) {
            ItemsActivity.this.fileType = fileType;
            String path = FileDownloader.getDownloadPath(fileName);
            downloadFile(downloadUrl, path);
        }

        @Override
        public void favorite(String msgId) {
            presenter.favoriteMessage(msgId);
        }

        @Override
        public void deleteMessage(Message msg) {
            presenter.deleteMessage(msg.get_id());
        }
    };

    @OnClick({R.id.tv_filter, R.id.layout_search})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_filter:
                popupMenu.show();
                break;
            case R.id.layout_search:
                animate();
                break;
        }
    }

    @Override
    public void onFilter(FilterItem filterItem) {
        animate();
        tvKeyword.setText(filterItem.getName());
        switch (filterItem.getType()) {
            case FilterItem.TYPE_MEMBER:
                data.setMemberId(filterItem.getKey());
                break;
            case FilterItem.TYPE_ROOM:
                data.setRoomId(filterItem.getKey());
                break;
            case FilterItem.TYPE_ALL:
                data.clearRestrictions();
                break;
        }
        presenter.search(data);
    }

    @Override
    public void onSearchFinish(List<Message> messages) {
        canLoadMore = messages.size() != 0;
        if (data.page == 1) {
            if (!messages.isEmpty()) {
                adapter.updateData(messages);
                placeholder.setVisibility(View.GONE);
            } else {
                placeholder.setVisibility(View.VISIBLE);
            }
        } else {
            adapter.addToEnd(messages);
        }
        isLoading = false;
    }

    @Override
    public void onDeleteMessageSuccess(String messageId) {
        adapter.removeMessage(messageId);
    }

    @Override
    public void onDownloadFinish(String path) {
        proDialog.dismiss();
        final File file = new File(path);
        if (file.exists()) {
            if (BizLogic.isImg(path)) {
                try {
                    //把文件插入到系统图库
                    MediaStore.Images.Media.insertImage(MainApp.CONTEXT.getContentResolver(),
                            file.getAbsolutePath(), file.getName(), null);
                    // 最后通知图库更新
                    MainApp.CONTEXT.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.parse("file://" + path)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                            FileUtil.openFileByType(ItemsActivity.this, fileType, file);
                        }
                    }).show();
        }
    }

    @Override
    public void onDownloadProgress(Integer progress) {
        proDialog.setProgress(progress);
        proDialog.show();
    }

    private void animate() {
        if (fragment.isAdded()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_down, R.anim.slide_out_up)
                    .remove(fragment).commit();
            ObjectAnimator.ofFloat(arrow, "rotation", 180, 360).setDuration(300).start();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_down, R.anim.slide_out_up)
                    .add(R.id.container_search, fragment)
                    .commit();
            ObjectAnimator.ofFloat(arrow, "rotation", 0, 180).setDuration(300).start();
        }
    }

    private void downloadFile(String url, String path) {
        proDialog.show();
        presenter.downloadFile(url, path);
    }
}
