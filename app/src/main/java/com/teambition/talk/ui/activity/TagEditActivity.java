package com.teambition.talk.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.teambition.talk.R;
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
public class TagEditActivity extends BaseActivity implements TeamTagView {

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.tag_edit)
    EditText mEditText;

    TeamTagPresenter mPresenter;

    String mTagId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_edit);

        ButterKnife.inject(this);
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.add_tag);

        mTagId = getIntent().getExtras().getString("tagId");
        final String name = getIntent().getExtras().getString("name");
        mEditText.setText(name);
        mEditText.setSelection(name == null ? 0 : name.length());
        mPresenter = new TeamTagPresenter(this);
        mPresenter.attachModule(new TagModelImpl());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            case R.id.action_done:
                final String text = mEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(text)) {
                    mPresenter.updateTag(mTagId, text);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void getTags(List<Tag> tags) {

    }

    @Override
    public void updateTag(Tag tag) {
        Intent intent = new Intent();
        intent.putExtra("_id", tag.get_id());
        intent.putExtra("id", tag.getId());
        intent.putExtra("name", tag.getName());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void removeTag(Tag tag) {

    }
}
