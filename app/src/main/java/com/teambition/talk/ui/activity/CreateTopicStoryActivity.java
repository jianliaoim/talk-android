package com.teambition.talk.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.teambition.talk.BizLogic;
import com.teambition.talk.R;
import com.teambition.talk.rx.ApiErrorAction;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.data.CreateStoryRequestData;
import com.teambition.talk.entity.IdeaDraft;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Story;
import com.teambition.talk.entity.Topic;
import com.teambition.talk.realm.IdeaDraftRealm;
import com.teambition.talk.realm.StoryDataProcess;
import com.teambition.talk.rx.EmptyAction;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.util.AnalyticsHelper;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.TransactionUtil;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zeatual on 15/10/21.
 */
public class CreateTopicStoryActivity extends BaseActivity {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.title)
    EditText titleEdit;
    @InjectView(R.id.description)
    EditText descriptionEdit;

    private MenuItem actionNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_topic_story);
        ButterKnife.inject(this);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_create_story);

        titleEdit.addTextChangedListener(titleTextWatcher);
    }

    final TextWatcher titleTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            actionNext.setVisible(StringUtil.isNotBlank(s.toString()));
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        titleEdit.removeTextChangedListener(titleTextWatcher);
        saveIdeaDraft();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ChooseMemberActivity.REQUEST_CHOOSE_MEMBER:
                    List<Member> members = Parcels.unwrap(data.getParcelableExtra(ChooseMemberActivity.MEMBERS));
                    List<String> memberIds = new ArrayList<>(members.size());
                    for (Member member : members) {
                        if (member != null) {
                            memberIds.add(member.get_id());
                        }
                    }
                    final String meId = BizLogic.getUserInfo().get_id();
                    if (!memberIds.contains(meId)) {
                        memberIds.add(0, meId);
                    }
                    Topic topic = new Topic();
                    topic.setTitle(titleEdit.getText().toString());
                    topic.setText(descriptionEdit.getText().toString());
                    CreateStoryRequestData requestData = new CreateStoryRequestData(BizLogic.getTeamId(),
                            StoryDataProcess.Category.TOPIC.value, topic, memberIds);
                    TalkClient.getInstance().getTalkApi().createStory(requestData)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Story>() {
                                   @Override
                                   public void call(Story story) {
                                       Bundle bundle = new Bundle();
                                       bundle.putParcelable(ChatActivity.EXTRA_STORY, Parcels.wrap(story));
                                       TransactionUtil.goTo(CreateTopicStoryActivity.this,
                                               ChatActivity.class, bundle, true);

                            }
                        }, new ApiErrorAction());
                    break;
            }
        }
    }

    private void saveIdeaDraft() {
        final String title = titleEdit.getText().toString();
        final String description = descriptionEdit.getText().toString();
        if (StringUtil.isNotBlank(title) || StringUtil.isNotBlank(description)) {
            IdeaDraft draft = new IdeaDraft();
            draft.setTitle(title);
            draft.setDescription(description);
            IdeaDraftRealm.getInstance().add(draft)
                    .subscribe(new EmptyAction<>());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_next, menu);
        actionNext = menu.findItem(R.id.action_next);
        actionNext.setVisible(false);


        IdeaDraftRealm.getInstance().getIdeaDraft()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<IdeaDraft>() {
                    @Override
                    public void call(IdeaDraft draft) {
                        if (draft != null) {
                            if (StringUtil.isNotBlank(draft.getTitle())) {
                                titleEdit.setText(draft.getTitle());
                                titleEdit.setSelection(draft.getTitle().length());
                            }
                            if (StringUtil.isNotBlank(draft.getDescription())) {
                                descriptionEdit.setText(draft.getDescription());
                                descriptionEdit.setText(draft.getDescription().length());
                            }
                        }
                    }
                }, new RealmErrorAction());
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_next:
                startActivityForResult(new Intent(this, ChooseMemberActivity.class),
                        ChooseMemberActivity.REQUEST_CHOOSE_MEMBER);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
