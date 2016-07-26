package com.teambition.talk.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.teambition.talk.BizLogic;
import com.teambition.talk.BuildConfig;
import com.teambition.talk.R;
import com.teambition.talk.client.ApiConfig;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.data.FeedbackRequestData;
import com.teambition.talk.rx.ApiErrorAction;
import com.teambition.talk.util.StringUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by wlanjie on 16/1/8.
 */
public class FeedbackActivity extends BaseActivity {

    final static int DONE = 0;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.feedback_content)
    EditText mContentEdit;

    private MenuItem mDoneItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        ButterKnife.inject(this);

        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.preference_feedback);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mContentEdit.addTextChangedListener(textWatcher);
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mDoneItem.setVisible(StringUtil.isNotBlank(mContentEdit.getText().toString()));
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mDoneItem = menu.add(Menu.NONE, DONE, Menu.NONE, R.string.done).setIcon(R.drawable.ic_done);
        mDoneItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        mDoneItem.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case DONE:
                postFeedback();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void postFeedback() {
        FeedbackRequestData data = new FeedbackRequestData();
        data.setTitle("Android Feedback");
        String builder = ("Content: " + mContentEdit.getText().toString()) +
                "\n" +
                "VersionName: " + BuildConfig.VERSION_NAME +
                "\n" +
                "BuildNO: " + BuildConfig.VERSION_CODE +
                "\n" +
                "SystemVersion: " + Build.VERSION.RELEASE +
                "\n" +
                "Device: " + Build.MODEL +
                "\n" +
                "Brand: " + Build.BRAND +
                "\n" +
                "Email: " + BizLogic.getUserInfo().getEmail() +
                "\n" +
                "Phone: " + BizLogic.getUserInfo().getPhoneForLogin() +
                "\n" +
                "UserId: " + BizLogic.getUserInfo().get_id();
        data.setText(builder);
        TalkClient.getInstance().getTalkApi().feedback(ApiConfig.FEEDBACK_INTEGRATION_ID, data)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        finish();
                    }
                }, new ApiErrorAction());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContentEdit.removeTextChangedListener(textWatcher);
    }
}
