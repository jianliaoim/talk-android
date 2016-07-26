package com.teambition.talk.ui.activity;

import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.talk.dialog.TalkDialog;
import com.teambition.talk.BizLogic;
import com.teambition.talk.FileDownloader;
import com.teambition.talk.MediaController;
import com.teambition.talk.R;
import com.teambition.talk.adapter.TagSearchWithMessageAdapter;
import com.teambition.talk.client.data.FileUploadResponseData;
import com.teambition.talk.client.data.SearchRequestData;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Room;
import com.teambition.talk.entity.Tag;
import com.teambition.talk.entity.TagSearchMessage;
import com.teambition.talk.model.MessageModelImpl;
import com.teambition.talk.model.TagModelImpl;
import com.teambition.talk.presenter.ChatPresenter;
import com.teambition.talk.presenter.TagSearchWithMessagePresenter;
import com.teambition.talk.realm.MessageDataProcess;
import com.teambition.talk.ui.OnMessageClickExecutor;
import com.teambition.talk.util.FileUtil;
import com.teambition.talk.util.MessageDialogBuilder;
import com.teambition.talk.util.SimpleMessageActionCallback;
import com.teambition.talk.util.TransactionUtil;
import com.teambition.talk.view.ChatView;
import com.teambition.talk.view.TagSearchWithMessageView;

import org.parceler.Parcels;

import java.io.File;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by wlanjie on 15/7/16.
 */
public class TagSearchWithMessageActivity extends BaseActivity implements TagSearchWithMessageView,
        TagSearchWithMessageAdapter.OnClickListener {
    public static final int MESSAGE_LIMIT = 30;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @InjectView(R.id.progress_bar)
    View mProgressBar;

    @InjectView(R.id.placeholder)
    View placeholder;

    private ProgressDialog downloadProgressDialog;

    TagSearchWithMessageAdapter mAdapter;

    TagSearchWithMessagePresenter mPresenter;

    ChatPresenter chatPresenter;

    private String tagId;

    private String maxId;

    private String fileType;

    private LinearLayoutManager mLayoutManager;

    private boolean mHasMore = false;

    private ChatView chatView = new ChatView() {

        @Override
        public void showProgressDialog(int message) {

        }

        @Override
        public void dismissProgressDialog() {

        }

        @Override
        public void showProgressBar() {

        }

        @Override
        public void dismissProgressBar() {

        }

        @Override
        public void onSendMessageSuccess(String tempMsgId, Message message) {

        }

        @Override
        public void onSendMessageFailed(String tempMsgId) {

        }

        @Override
        public void showLocalMessages(List<Message> messages) {

        }

        @Override
        public void showLatestMessages(List<Message> messages) {

        }

        @Override
        public void showSearchResult(List<Message> messages) {

        }

        @Override
        public void showMoreOldMessages(List<Message> messages, boolean isLocal) {

        }

        @Override
        public void showMoreNewMessages(List<Message> messages, boolean isLocal) {

        }

        @Override
        public void onUploadFileSuccess(FileUploadResponseData file, String tempMsgId) {

        }

        @Override
        public void onUploadFileFailed(String tempMsgId) {

        }

        @Override
        public void onUploadFileInvalid(String tempMsgId) {

        }

        @Override
        public void onDownloadProgress(int progress) {
            downloadProgressDialog.setProgress(progress);
            downloadProgressDialog.show();
        }

        @Override
        public void onDownloadFinish(String path) {
            downloadProgressDialog.dismiss();
            final File file = new File(path);
            if (file.exists()) {
                if (BizLogic.isImg(path)) {
                    MediaController.updateSystemGallery(path);
                }
                new TalkDialog.Builder(TagSearchWithMessageActivity.this)
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
                                FileUtil.openFileByType(TagSearchWithMessageActivity.this, fileType, file);
                            }
                        }).show();
            }
        }

        @Override
        public void onJoinTopic(Room room) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tag_search_message);
        ButterKnife.inject(this);
        final String name = getIntent().getStringExtra("name");
        tagId = getIntent().getStringExtra("tagId");
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(name);

        progressBar = mProgressBar;

        mAdapter = new TagSearchWithMessageAdapter(name, this, mCallback);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mPresenter = new TagSearchWithMessagePresenter(this);
        mPresenter.attachModule(new TagModelImpl());
        mPresenter.readTagWithMessage(tagId, maxId);

        chatPresenter = new ChatPresenter(chatView, new MessageModelImpl(), BizLogic.getTeamId());

        downloadProgressDialog = new ProgressDialog(this);
        downloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        downloadProgressDialog.setMessage(getResources().getString(R.string.wait));
        downloadProgressDialog.setMax(100);

        mRecyclerView.addOnScrollListener(mScrollListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecyclerView.removeOnScrollListener(mScrollListener);
    }

    final RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            int visibleItemCount = mLayoutManager.getChildCount();
            int totalItemCount = mLayoutManager.getItemCount();
            int lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition();
            if (visibleItemCount > 0 && newState == RecyclerView.SCROLL_STATE_IDLE && (lastVisibleItemPosition >= (totalItemCount - 1)) && !mHasMore) {
                loadMore();
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }
    };

    private void loadMore() {
        mProgressBar.setVisibility(View.VISIBLE);
        mHasMore = true;
        mPresenter.readTagWithMessage(tagId, maxId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void readTagWithMessageError() {
        mHasMore = false;
    }

    @Override
    public void readTagWithMessage(List<Message> messages) {
        mHasMore = messages.size() < MESSAGE_LIMIT;
        if (messages.isEmpty()) {
            placeholder.setVisibility(View.VISIBLE);
        } else {
            maxId = messages.get(messages.size() - 1).get_id();
            placeholder.setVisibility(View.GONE);
            mAdapter.setItems(messages);
        }
    }

    MessageDialogBuilder.MessageActionCallback mCallback = new SimpleMessageActionCallback() {

        @Override
        public void favorite(String msgId) {
            super.favorite(msgId);
            chatPresenter.favoriteMessage(msgId);
        }

        @Override
        public void addTag(Message msg) {
            super.addTag(msg);
            Intent intent = new Intent(TagSearchWithMessageActivity.this, AddTagActivity.class);
            intent.putExtra("messageId", msg.get_id());
            List<Tag> tags = msg.getTags();
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
        public void deleteMessage(Message msg) {
            super.deleteMessage(msg);
            mPresenter.deleteMessage(msg.get_id());
        }

        @Override
        public void saveFile(String fileName, String fileType, String downloadUrl) {
            super.saveFile(fileName, fileType, downloadUrl);
            TagSearchWithMessageActivity.this.fileType = fileType;
            String path = FileDownloader.getDownloadPath(fileName);
            downloadFile(downloadUrl, path);
        }
    };

    @Override
    public void onItemClick(final Message msg) {
        if (msg != null) {
            new OnMessageClickExecutor(TagSearchWithMessageActivity.this, msg) {
                @Override
                public void onImageClick(Context context, Message message) {
                    SearchRequestData d = new SearchRequestData(BizLogic.getTeamId(), SearchRequestData.TYPE_FILE);
                    int imgCount = 0;
                    int page = 1;
                    for (int i = 0; i < mAdapter.getItemCount(); i++) {
                        if (MessageDataProcess.getInstance().getFile(mAdapter.getItem(i)) != null) {
                            if ("image".equals(MessageDataProcess.getInstance().getFile(mAdapter.getItem(i)).getFileCategory())) {
                                imgCount++;
                                if (msg.get_id().equals(mAdapter.getItem(i).get_id())) {
                                    break;
                                }
                            }
                        }
                    }
                    if (imgCount != 0) {
                        page = imgCount / d.limit + (imgCount % 30 == 0 ? 0 : 1);
                    }
                    d.fileCategory = "image";
                    d.page = page;
                    Bundle bundle = new Bundle();
                    bundle.putString("msgId", msg.get_id());
                    bundle.putParcelable("data", Parcels.wrap(d));
                    TransactionUtil.goTo(TagSearchWithMessageActivity.this,
                            ItemPhotoViewActivity.class, bundle);
                }
            }.execute();
        }
    }

    @Override
    public void deleteComplete(String messageId) {
        mAdapter.deleteItem(messageId);
    }

    private void copyToClipboard(String string) {
        ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        manager.setText(string);
    }

    private void downloadFile(String url, String path) {
        downloadProgressDialog.show();
        chatPresenter.downloadFile(url, path);
    }

}
