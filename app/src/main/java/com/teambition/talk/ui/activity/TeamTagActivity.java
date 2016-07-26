package com.teambition.talk.ui.activity;

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
import com.teambition.talk.R;
import com.teambition.talk.adapter.TeamTagAdapter;
import com.teambition.talk.entity.Tag;
import com.teambition.talk.model.TagModelImpl;
import com.teambition.talk.presenter.TeamTagPresenter;
import com.teambition.talk.view.TeamTagView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by wlanjie on 15/7/16.
 */
public class TeamTagActivity extends BaseActivity implements TeamTagView, TeamTagAdapter.OnItemListener {

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.progress_bar)
    View mProgressBar;

    @InjectView(R.id.team_tag_empty)
    View mEmptyView;

    @InjectView(R.id.team_tag_recycler)
    RecyclerView mRecyclerView;

    private TeamTagPresenter mPresenter;

    private TeamTagAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_tag);

        ButterKnife.inject(this);

        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.tag);

        progressBar = mProgressBar;
        mPresenter = new TeamTagPresenter(this);
        mPresenter.attachModule(new TagModelImpl());
        mAdapter = new TeamTagAdapter(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);

        mPresenter.getTags();
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
    public void getTags(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            mEmptyView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mEmptyView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mAdapter.setItems(tags);
        }
    }

    @Override
    public void updateTag(Tag tag) {
    }

    @Override
    public void removeTag(Tag tag) {
        mAdapter.removeItem(tag);
        if (mAdapter.getItemCount() == 0) {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK && data != null) {
            final String _id = data.getStringExtra("_id");
            final String id = data.getStringExtra("id");
            final String name = data.getStringExtra("name");
            mAdapter.updateItem(_id == null ? "" : _id, id == null ? "" : id, name == null ? "" : name);
        }
    }

    @Override
    public void onItemClickListener(Tag tag) {
        Intent intent = new Intent(this, TagSearchWithMessageActivity.class);
        intent.putExtra("name", tag.getName());
        intent.putExtra("tagId", tag.get_id());
        startActivity(intent);
    }

    @Override
    public void onItemLongClickListener(final Tag tag) {
        if (tag.get_creatorId().equals(BizLogic.getUserInfo().get_id())
                || BizLogic.isAdmin()) {
            CharSequence[] items = new CharSequence[]{getString(R.string.edit_tag), getString(R.string.delete_tag)};
            new TalkDialog.Builder(this)
                    .items(items)
                    .itemsCallback(new TalkDialog.ListCallback() {
                        @Override
                        public void onSelection(TalkDialog materialDialog, View view, int i, CharSequence charSequence) {
                            final String action = charSequence.toString();
                            if (getString(R.string.edit_tag).equals(action)) {
                                Bundle bundle = new Bundle();
                                bundle.putString("tagId", tag.get_id());
                                bundle.putString("name", tag.getName());
                                Intent intent = new Intent(TeamTagActivity.this, TagEditActivity.class);
                                intent.putExtras(bundle);
                                startActivityForResult(intent, 0);
                            } else if (getString(R.string.delete_tag).equals(action)) {
                                showDeleteDialog(tag);
                            }
                        }
                    })
                    .show();
        }
    }

    void showDeleteDialog(final Tag tag) {
        new TalkDialog.Builder(this)
                .title(R.string.delete_tag_title)
                .titleColorRes(R.color.white)
                .titleBackgroundColorRes(R.color.talk_warning)
                .content(R.string.delete_tag_message)
                .contentColorRes(R.color.color_grey)
                .positiveText(R.string.delete)
                .positiveColorRes(R.color.material_deep_orange_500)
                .negativeText(R.string.cancel)
                .negativeColorRes(R.color.material_grey_900)
                .callback(new TalkDialog.ButtonCallback() {
                    @Override
                    public void onPositive(TalkDialog dialog, View v) {
                        mPresenter.removeTag(tag.get_id() == null ? "" : tag.get_id());
                    }
                })
                .show();
    }
}
