package com.teambition.talk.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.teambition.talk.GsonProvider;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.data.UpdateStoryRequestData;
import com.teambition.talk.entity.Link;
import com.teambition.talk.realm.StoryDataProcess;
import com.teambition.talk.rx.ApiErrorAction;
import com.teambition.talk.ui.widget.ThemeButton;
import com.teambition.talk.util.StringUtil;

import org.parceler.Parcels;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zeatual on 15/10/29.
 */
public class EditLinkStoryActivity extends EditStoryActivity {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.et_link)
    EditText etLink;
    @InjectView(R.id.et_title)
    EditText etTitle;
    @InjectView(R.id.et_text)
    EditText etText;
    @InjectView(R.id.til_link)
    TextInputLayout tilLink;
    @InjectView(R.id.til_title)
    TextInputLayout tilTitle;
    @InjectView(R.id.til_text)
    TextInputLayout tilText;
    @InjectView(R.id.image)
    ImageView imageView;
    @InjectView(R.id.clear_link)
    View clearLinkView;
    @InjectView(R.id.crawl)
    ThemeButton crawlView;

    private Link link;
    private String originLink = "";
    private String originTitle = "";
    private String originText = "";
    private MenuItem actionDone;

    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            clearLinkView.setEnabled(StringUtil.isNotBlank(s.toString()));
            crawlView.setEnabled(StringUtil.isNotBlank(s.toString()));
            checkUpdate();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_link_story);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        story = Parcels.unwrap(getIntent().getParcelableExtra("story"));
        if (getIntent().getBooleanExtra(IS_EDIT, false)) {
            crawlView.setVisibility(View.GONE);
        }
        link = GsonProvider.getGson().fromJson(story.getData(), Link.class);
        crawlView.setThemeBackground(getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorPrimaryDark));
        originLink = link.getUrl();
        originTitle = link.getTitle();
        originText = link.getText();

        etLink.setText(originLink);
        etTitle.setText(originTitle);
        etText.setText(originText);
        MainApp.IMAGE_LOADER.displayImage(link.getImageUrl(), imageView,
                ImageLoaderConfig.EMPTY_OPTIONS);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                tilLink.setVisibility(View.VISIBLE);
                tilTitle.setVisibility(View.VISIBLE);
                tilText.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.VISIBLE);
            }
        }, 200);

        etLink.addTextChangedListener(watcher);
        etTitle.addTextChangedListener(watcher);
        etText.addTextChangedListener(watcher);

        clearLinkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etLink.setText("");
                etLink.requestFocus();
            }
        });

        crawlView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = etLink.getText().toString();
                if (!TextUtils.isEmpty(url)) {
                    getMetaData(url);
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        etLink.removeTextChangedListener(watcher);
        etTitle.removeTextChangedListener(watcher);
        etText.removeTextChangedListener(watcher);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        actionDone = menu.findItem(R.id.action_done);
        actionDone.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(0, R.anim.anim_fade_transition_out);
                break;
            case R.id.action_done:
                UpdateStoryRequestData updateData = new UpdateStoryRequestData();
                updateData.category = StoryDataProcess.Category.LINK.value;
                Link link = new Link();
                if (originLink != etLink.getText().toString() || !originLink.equals(etLink.getText().toString())) {
                    link.setUrl(etLink.getText().toString());
                }
                if (originTitle != etTitle.getText().toString() || !originTitle.equals(etTitle.getText().toString())) {
                    link.setTitle(etTitle.getText().toString());
                }
                if (originText != etText.getText().toString() || !originText.equals(etText.getText().toString())) {
                    link.setText(etText.getText().toString());
                }
                link.setImageUrl(this.link.getImageUrl());
                link.setFaviconUrl(this.link.getFaviconUrl());
                updateData.data = link;
                updateStory(story.get_id(), updateData);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void hideInputMethodManager() {
        super.hideInputMethodManager();
        inputMethodManager.hideSoftInputFromWindow(etLink.getWindowToken(), 0);
    }

    private void checkUpdate() {
        actionDone.setVisible(!TextUtils.isEmpty(etLink.getText().toString()));
//        actionDone.setVisible(!originLink.equals(etLink.getText().toString()) ||
//                !originTitle.equals(etTitle.getText().toString()) ||
//                !originText.equals(etText.getText().toString()));
    }

    private void getMetaData(final String url) {
        TalkClient.getInstance().getTalkApi()
                .getUrlMeta(url)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Link>() {
                    @Override
                    public void call(Link link) {
                        if (link == null) return;
                        link.setUrl(url);
                        EditLinkStoryActivity.this.link = link;
                        etTitle.setText(link.getTitle() == null ? "" : link.getTitle());
                        etText.setText(link.getText() == null ? "" : link.getText());
                        MainApp.IMAGE_LOADER.displayImage(link.getImageUrl(), imageView,
                                ImageLoaderConfig.EMPTY_OPTIONS);
                    }
                }, new ApiErrorAction());
    }
}
