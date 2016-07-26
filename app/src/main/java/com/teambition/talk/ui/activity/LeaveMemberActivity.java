package com.teambition.talk.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.teambition.talk.R;
import com.teambition.talk.adapter.LeaveMemberAdapter;
import com.teambition.talk.entity.Member;

import org.parceler.Parcels;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by wlanjie on 15/8/5.
 */
public class LeaveMemberActivity extends BaseActivity implements LeaveMemberAdapter.OnItemClickListener {

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.listView)
    RecyclerView mRecyclerView;

    private LeaveMemberAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_leave_member);
        ButterKnife.inject(this);
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.leave_member);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter = new LeaveMemberAdapter();
        mAdapter.setListener(this);
        mRecyclerView.setAdapter(mAdapter);
        List<Member> items = Parcels.unwrap(getIntent().getParcelableExtra("leave_members"));
        mAdapter.setItems(items);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(Member member) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ChatActivity.EXTRA_MEMBER, Parcels.wrap(member));
        bundle.putParcelable(ChatActivity.EXTRA_MEMBER, Parcels.wrap(member));
        bundle.putBoolean("is_quit", member.getIsQuit());
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
