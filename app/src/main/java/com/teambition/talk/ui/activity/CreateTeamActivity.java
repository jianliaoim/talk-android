package com.teambition.talk.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.teambition.talk.R;
import com.teambition.talk.entity.Team;
import com.teambition.talk.presenter.CreateTeamPresenter;
import com.teambition.talk.util.AnalyticsHelper;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.view.CreateTeamView;
import com.umeng.analytics.MobclickAgent;

import org.parceler.Parcels;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by nlmartian on 4/14/15.
 */
public class CreateTeamActivity extends BaseActivity implements TextWatcher, CreateTeamView {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.team_name)
    EditText teamName;

    private CreateTeamPresenter presenter;
    private MenuItem next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_team);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.create_team);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        presenter = new CreateTeamPresenter(this);
        teamName.addTextChangedListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getClass().getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getClass().getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        next = menu.findItem(R.id.action_done);
        updateActionVisibility();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_done) {
            presenter.createTeam(teamName.getText().toString());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateTeamFinish(Team team) {
        AnalyticsHelper.getInstance().sendEvent(AnalyticsHelper.Category.switch_team,
                "create team", null);

        Intent intent = new Intent();
        intent.putExtra("team", Parcels.wrap(team));
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        updateActionVisibility();
    }

    private void updateActionVisibility() {
        next.setVisible(StringUtil.isNotBlank(teamName.getText().toString()));
    }
}
