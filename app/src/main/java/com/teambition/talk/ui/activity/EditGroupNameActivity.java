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
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.data.GroupRequestData;
import com.teambition.talk.entity.Group;
import com.teambition.talk.rx.ApiErrorAction;

import org.parceler.Parcels;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by wlanjie on 15/12/28.
 */
public class EditGroupNameActivity extends BaseActivity {

    public final static String GROUP = "group";

    final static int DONE = 0;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.group_name)
    EditText mGroupNameEdit;

    private Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group_name);
        ButterKnife.inject(this);

        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");
        group = Parcels.unwrap(getIntent().getParcelableExtra(GROUP));
        if (group != null) {
            mGroupNameEdit.setText(group.getName());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, DONE, Menu.NONE, R.string.done).setIcon(R.drawable.ic_done).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == DONE){
            editGroupName();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    void editGroupName() {
        final String groupName = mGroupNameEdit.getText().toString();
        if (group == null || TextUtils.isEmpty(groupName)) return;
        GroupRequestData data = new GroupRequestData();
        data.name = groupName;
        TalkClient.getInstance().getTalkApi()
                .updateGroup(group.get_id(), data)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Group>() {
                    @Override
                    public void call(Group group) {
                        Intent intent = new Intent();
                        intent.putExtra(GROUP, Parcels.wrap(group));
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }, new ApiErrorAction());
    }
}
