package com.teambition.talk.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.teambition.talk.GsonProvider;
import com.teambition.talk.R;
import com.teambition.talk.client.data.UpdateStoryRequestData;
import com.teambition.talk.entity.Topic;
import com.teambition.talk.realm.StoryDataProcess;
import com.teambition.talk.util.StringUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by zeatual on 15/10/29.
 */
public class EditTopicStoryActivity extends EditStoryActivity {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.et_title)
    EditText etTitle;
    @InjectView(R.id.et_text)
    EditText etText;
    @InjectView(R.id.til_title)
    TextInputLayout tilTitle;
    @InjectView(R.id.til_text)
    TextInputLayout tilText;

    private Topic topic;
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
            checkUpdate();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_topic_story);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        topic = GsonProvider.getGson().fromJson(story.getData(), Topic.class);
        if (StringUtil.isNotBlank(topic.getTitle())) {
            originTitle = topic.getTitle();
        }
        if (StringUtil.isNotBlank(topic.getText())) {
            originText = topic.getText();
        }

        etTitle.setText(originTitle);
        etText.setText(originText);

        etTitle.addTextChangedListener(watcher);
        etText.addTextChangedListener(watcher);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                tilTitle.setVisibility(View.VISIBLE);
                tilText.setVisibility(View.VISIBLE);
            }
        }, 200);
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
                updateData.category = StoryDataProcess.Category.TOPIC.value;
                Topic topic = new Topic();
                if (!originText.equals(etText.getText().toString())) {
                    topic.setText(etText.getText().toString());
                }
                if (!originTitle.equals(etTitle.getText().toString())) {
                    topic.setText(etTitle.getText().toString());
                }
                updateData.data = topic;
                updateStory(story.get_id(), updateData);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void hideInputMethodManager() {
        super.hideInputMethodManager();
        inputMethodManager.hideSoftInputFromWindow(etText.getWindowToken(), 0);
    }

    private void checkUpdate() {
        actionDone.setVisible(!originTitle.equals(etTitle.getText().toString()) ||
                !originText.equals(etText.getText().toString()));
    }
}
