package com.teambition.talk.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.imageloader.RecyclerViewPauseOnScrollListener;
import com.teambition.talk.adapter.MultiCallAdapter;
import com.teambition.talk.entity.Member;
import com.teambition.talk.presenter.MemberPresenter;
import com.teambition.talk.ui.fragment.CallInFragment;
import com.teambition.talk.view.MemberView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by wlanjie on 15/9/11.
 */
public class MultiCallActivity extends BaseActivity implements MemberView {

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @InjectView(R.id.progress_bar)
    View mProgressBar;

    public final static int MAX_CALL = 20;

    private MultiCallAdapter mAdapter;

    public List<Member> mMembers = new ArrayList<>();

    private ActionBar mActionBar;

    private final RecyclerViewPauseOnScrollListener mListener = new RecyclerViewPauseOnScrollListener(MainApp.IMAGE_LOADER, true, true, null);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_call);
        ButterKnife.inject(this);

        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setTitle(String.format(getResources().getString(R.string.multi_call_number), "1/" + MAX_CALL));

        MemberPresenter presenter = new MemberPresenter(this);
        mAdapter = new MultiCallAdapter(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(mListener);
        mAdapter.setListener(listener);
        presenter.getMembers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecyclerView.removeOnScrollListener(mListener);
    }

    MultiCallAdapter.OnItemClickListener listener = new MultiCallAdapter.OnItemClickListener() {
        @Override
        public void onClick(View view, Member member) {
            if (mMembers.isEmpty()) {
                mActionBar.setTitle(String.format(getResources().getString(R.string.multi_call_number), "1/" + MAX_CALL));
            } else {
                mActionBar.setTitle(String.format(getResources().getString(R.string.multi_call_number), mMembers.size() + "/" + MAX_CALL));
                invalidateOptionsMenu();
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mMembers.size() > 1) {
            getMenuInflater().inflate(R.menu.menu_done, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_done:
                if (mMembers.size() > 1) {
                    String number = "";
                    for (int i = 0; i < mMembers.size(); i++) {
                        if (i != 0) {
                            number += ("#" + mMembers.get(i).getPhoneForLogin());
                        } else {
                            number += mMembers.get(i).getPhoneForLogin();
                        }
                    }
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag("call");
                    if (fragment == null) {
                        getSupportFragmentManager().beginTransaction()
                                .add(R.id.layout_root, CallInFragment.getInstance(null, number), "call")
                                .addToBackStack(null)
                                .commitAllowingStateLoss();
                    }
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoadMembersFinish(List<Member> members) {
        mProgressBar.setVisibility(View.GONE);
        mAdapter.setItems(members);
    }

    @Override
    public void onLoadLeaveMembersFinish(List<Member> members) {

    }
}
