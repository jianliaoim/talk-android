package com.teambition.talk.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.util.Rfc822Tokenizer;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.android.ex.chips.TextChipsEditView;
import com.android.ex.chips.recipientchip.DrawableRecipientChip;
import com.teambition.talk.BizLogic;
import com.teambition.talk.R;
import com.teambition.talk.adapter.AddTagAdapter;
import com.teambition.talk.client.data.CreateTagRequestData;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Tag;
import com.teambition.talk.model.TagModelImpl;
import com.teambition.talk.presenter.TagPresenter;
import com.teambition.talk.util.DensityUtil;
import com.teambition.talk.util.ThemeUtil;
import com.teambition.talk.view.TagView;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by wlanjie on 15/7/13.
 */
public class AddTagActivity extends BaseActivity implements TagView, AddTagAdapter.TagCheckChangeListener, TextChipsEditView.OnRemoveChipListener, TextChipsEditView.OnInputListener {

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.tag_edit)
    TextChipsEditView mEditTextView;

    @InjectView(R.id.tag_recycler)
    RecyclerView mRecyclerView;

    @InjectView(R.id.tag_progress)
    View mProgress;

    private TagPresenter mPresenter;

    private AddTagAdapter mAdapter;

    String mMessageId;

    InputMethodManager mInputMethodManager;

    String[] mTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tag);
        ButterKnife.inject(this);
        setSupportActionBar(mToolbar);

        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.add_tag);

        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        mMessageId = getIntent().getStringExtra("messageId");
        mTags = getIntent().getStringArrayExtra("tags");

        ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
        final int size = DensityUtil.dip2px(this, 8.0f);
        drawable.setIntrinsicHeight(size);
        drawable.setIntrinsicWidth(size);
        drawable.setBounds(0, 0, size, size);
        drawable.getPaint().setColor(getResources().getColor(R.color.colorPrimary));

        mEditTextView.setDefaultContactPhoto(drawableToBitmap(drawable));
        mEditTextView.setOnInputListener(this);
        mEditTextView.setOnRemoveChipListener(this);
        mEditTextView.setTokenizer(new Rfc822Tokenizer());

        if (mTags != null && mTags.length > 0) {
            for (String s : mTags) {
                mEditTextView.append(s);
            }
        }

        mAdapter = new AddTagAdapter(this);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        progressBar = mProgress;
        mPresenter = new TagPresenter(this);
        mPresenter.attachModule(new TagModelImpl());
        mPresenter.getTags();

        mEditTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    DrawableRecipientChip[] chips = mEditTextView.getRecipients();
                    String text = mEditTextView.getText().toString();
                    int textIndex = text.lastIndexOf(",");
                    text = text.substring(textIndex + 1, text.length()).trim();
                    for (Tag tag : mAdapter.getItems()) {
                        if (tag.getName().trim().equals(text.trim())) {
                            mAdapter.selectItem(text);
                            Editable editable = mEditTextView.getText();
                            String s = mEditTextView.getText().toString();
                            int index = s.lastIndexOf(tag.getName());
                            editable.delete(index, s.length());
                            boolean isContains = false;
                            for (DrawableRecipientChip chip : chips) {
                                if (chip.getValue().equals(text)) {
                                    isContains = true;
                                    break;
                                }
                            }
                            if (!isContains) {
                                mEditTextView.append(tag.getName());
                            }
                            return true;
                        }
                    }
                    if (!TextUtils.isEmpty(text.trim())) {
                        final String teamId = BizLogic.getTeamId();
                        mPresenter.createTag(new CreateTagRequestData(teamId == null ? "" : teamId, text.trim()));
                    }
                    return true;
                }
                return false;
            }
        });
    }

    Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mInputMethodManager.hideSoftInputFromWindow(mEditTextView.getWindowToken(), 0);
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
                DrawableRecipientChip[] chips = mEditTextView.getRecipients();
                final List<String> tags = new ArrayList<>(chips.length);
                for (Tag tag : mAdapter.getItems()) {
                    for (DrawableRecipientChip chip : chips) {
                        if (tag.getName().equals(chip.getValue())) {
                            tags.add(tag.get_id());
                        }
                    }
                }
                mPresenter.createMessageTag(mMessageId, tags);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateTagComplete(Tag tag) {
        if (tag == null) return;
        mAdapter.addItem(tag);
        mAdapter.selectItem(tag.getName());
        mRecyclerView.scrollToPosition(0);
        Editable editable = mEditTextView.getText();
        String s = mEditTextView.getText().toString();
        int index = s.lastIndexOf(tag.getName());
        editable.delete(index, s.length());
        mEditTextView.append(tag.getName());
    }

    @Override
    public void getTags(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) return;
        Collections.sort(tags, new Comparator<Tag>() {
            @Override
            public int compare(Tag lhs, Tag rhs) {
                return rhs.getCreatedAt().compareTo(lhs.getCreatedAt());
            }
        });
        mAdapter.setItems(tags);
        if (mTags != null) {
            for (String s : mTags) {
                mAdapter.selectItem(s);
            }
            mTags = null;
        }
    }

    @Override
    public void onCreateMessageTag(Message message) {
        Intent intent = new Intent();
        intent.putExtra("message", Parcels.wrap(message));
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onItemCheckedChanged(TextView textView, boolean isChecked) {
        if (isChecked) {
            mEditTextView.append(textView.getText());
        } else {
            DrawableRecipientChip[] chips = mEditTextView.getRecipients();
            if (chips == null || chips.length <= 0) return;
            for (DrawableRecipientChip chip : chips) {
                if (chip != null) {
                    if (textView.getText().toString().trim().equals(chip.getValue().toString().trim())) {
                        mEditTextView.removeChip(chip);
                    }
                }
            }
        }
    }

    @Override
    public void removeChip(DrawableRecipientChip chip) {
        if (chip != null) {
            final String text = chip.getValue().toString();
            mAdapter.unselectItem(text);
        }
    }

    @Override
    public void onInput() {
        int end = mEditTextView.getSelectionEnd() == 0 ? 0 : mEditTextView.getSelectionEnd() - 1;
        int len = mEditTextView.length() - 1;
        Editable editable = mEditTextView.getText();
        if (end != len) {
            editable.delete(end, editable.length());
        } else {
            editable.delete(len, editable.length());
        }
    }
}
