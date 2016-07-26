package com.teambition.talk.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.talk.dialog.TalkDialog;
import com.teambition.talk.BusProvider;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.rx.ApiErrorAction;
import com.teambition.talk.adapter.ThirdPartTeamAdapter;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.entity.Team;
import com.teambition.talk.entity.User;
import com.teambition.talk.event.NewTeamEvent;
import com.teambition.talk.presenter.AccountPresenter;
import com.teambition.talk.presenter.UserPresenter;
import com.teambition.talk.util.TransactionUtil;
import com.teambition.talk.view.AccountView;
import com.teambition.talk.view.SimpleAccountViewImpl;
import com.teambition.talk.view.UserView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zeatual on 15/9/2.
 */
public class SyncTeambitionActivity extends BaseActivity implements UserView {

    public static final int REQUEST_TEAMBITION_CODE = 0;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.tv_action)
    TextView tvAction;
    @InjectView(R.id.lst_teams)
    RecyclerView lstThirdTeams;
    @InjectView(R.id.card)
    View layoutCard;

    private UserPresenter userPresenter;
    private AccountPresenter accountPresenter;
    private ThirdPartTeamAdapter adapter;

    private AccountView callback = new SimpleAccountViewImpl() {
        @Override
        public void onBindTeambition(User user) {
            new TalkDialog.Builder(SyncTeambitionActivity.this)
                    .title(R.string.bind_success)
                    .titleColorRes(R.color.white)
                    .titleBackgroundColorRes(R.color.talk_grass)
                    .backgroundColorRes(R.color.white)
                    .content(R.string.bind_teambition_success)
                    .positiveText(R.string.action_done)
                    .positiveColorRes(R.color.talk_grass)
                    .show();
            setSyncReady();
        }

        @Override
        public void onTeambitionConflict(String account, final String bindCode) {
            new TalkDialog.Builder(SyncTeambitionActivity.this)
                    .title(R.string.delete_origin_account)
                    .titleColorRes(R.color.white)
                    .titleBackgroundColorRes(R.color.talk_warning)
                    .autoDismiss(true)
                    .backgroundColorRes(R.color.white)
                    .content(String.format(getString(R.string.delete_origin_account_content), account))
                    .negativeText(R.string.cancel)
                    .negativeColorRes(R.color.material_grey_700)
                    .positiveText(R.string.confirm)
                    .positiveColorRes(R.color.talk_warning)
                    .callback(new TalkDialog.ButtonCallback() {
                        @Override
                        public void onPositive(final TalkDialog dialog, View v) {
                            accountPresenter.forceBindTeambition(bindCode);
                        }
                    }).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_teambition);
        ButterKnife.inject(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.sync_teambition_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userPresenter = new UserPresenter(this);
        accountPresenter = new AccountPresenter(callback);

        userPresenter.getUser();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_TEAMBITION_CODE:
                    String code = data.getStringExtra(UnionsActivity.CODE);
                    accountPresenter.bindTeambition(code);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void setSyncReady() {
        adapter = new ThirdPartTeamAdapter();
        adapter.setOnItemClickListener(new ThirdPartTeamAdapter.OnItemClickListener() {
            @Override
            public void onSyncClickListener(final TextView syncView, String sourceId) {
                syncView.setTextColor(getResources().getColor(R.color.material_grey_500));
                syncView.setClickable(false);
                syncView.setText(R.string.syncing);
                TalkClient.getInstance().getTalkApi()
                        .syncOneTeam("teambition", sourceId)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Team>() {
                            @Override
                            public void call(Team team) {
                                syncView.setTextColor(getResources().getColor(R.color.talk_blue));
                                syncView.setClickable(true);
                                syncView.setText(R.string.sync_success);
                                BusProvider.getInstance().post(new NewTeamEvent(team.get_id()));
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                syncView.setTextColor(getResources().getColor(R.color.talk_red));
                                syncView.setClickable(true);
                                syncView.setText(R.string.sync_fail);
                            }
                        });
            }
        });

        lstThirdTeams.setLayoutManager(new LinearLayoutManager(this));
        lstThirdTeams.setAdapter(adapter);

        tvAction.setVisibility(View.GONE);

        TalkClient.getInstance().getTalkApi()
                .readTeambitionTeams()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Team>>() {
                    @Override
                    public void call(List<Team> teams) {
                        if (teams.isEmpty()) {
                            MainApp.showToastMsg(R.string.no_team_found);
                        } else {
                            layoutCard.setVisibility(View.GONE);
                            lstThirdTeams.setVisibility(View.VISIBLE);
                            adapter.setTeamList(teams);
                        }
                    }
                }, new ApiErrorAction());

        tvAction.setText(R.string.sync_team);
        tvAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TalkClient.getInstance().getTalkApi()
                        .syncTeambition()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<List<Team>>() {
                            @Override
                            public void call(List<Team> teams) {
                                Intent intent = new Intent();
                                ArrayList<Team> result = new ArrayList<>();
                                result.addAll(teams);
                                intent.putExtra("teams", result);
                                setResult(RESULT_OK, intent);
                                finish();
                                MainApp.showToastMsg(R.string.sync_success);
                            }
                        }, new ApiErrorAction());
            }
        });
    }

    @Override
    public void onLoadUserFinish(User user) {
        if (user.getTeambitionAccount() != null) {
            setSyncReady();
        } else {
            tvAction.setText(R.string.bind_teambition);
            tvAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TransactionUtil.goToForResult(SyncTeambitionActivity.this,
                            UnionsActivity.class, REQUEST_TEAMBITION_CODE);
                }
            });
        }
    }
}
