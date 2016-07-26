package com.teambition.talk.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.otto.Subscribe;
import com.talk.dialog.TalkDialog;
import com.teambition.talk.BizLogic;
import com.teambition.talk.Constant;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.NotificationConfig;
import com.teambition.talk.R;
import com.teambition.talk.rx.ApiErrorAction;
import com.teambition.talk.adapter.TeamAdapter;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.entity.QRCodeData;
import com.teambition.talk.entity.Team;
import com.teambition.talk.entity.User;
import com.teambition.talk.event.UpdateUserEvent;
import com.teambition.talk.presenter.TeamPresenter;
import com.teambition.talk.presenter.UserPresenter;
import com.teambition.talk.ui.widget.ThemeButton;
import com.teambition.talk.util.AnalyticsHelper;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.ThemeUtil;
import com.teambition.talk.util.TransactionUtil;
import com.teambition.talk.view.TeamView;
import com.teambition.talk.view.UserView;
import com.umeng.analytics.MobclickAgent;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zeatual on 15/8/31.
 */
public class ChooseTeamActivity extends BaseActivity implements TeamView, UserView,
        AdapterView.OnItemClickListener {

    public static final int REQUEST_CREATE_TEAM = 1;
    public static final int REQUEST_SYNC_TEAMBITION = 2;

    @InjectView(R.id.listView)
    ListView listView;
    @InjectView(R.id.name)
    TextView tvName;
    @InjectView(R.id.avatar)
    RoundedImageView imgAvatar;
    @InjectView(R.id.layout_me)
    RelativeLayout layoutMe;

    View footerDivider;

    private UserPresenter userPresenter;
    private TeamPresenter presenter;
    private TeamAdapter adapter;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_team);
        ButterKnife.inject(this);

        View footer = LayoutInflater.from(this).inflate(R.layout.footer_choose_team, null);
        footerDivider = footer.findViewById(R.id.view_divider);

        userPresenter = new UserPresenter(this);
        presenter = new TeamPresenter(this);
        adapter = new TeamAdapter(this);

        listView.addFooterView(footer);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        presenter.getTeams();

        handleInviteIntent();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getClass().getName());
        userPresenter.getUser();
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getClass().getName());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CREATE_TEAM:
                    Team team = Parcels.unwrap(data.getParcelableExtra("team"));
                    if (team != null) {
                        chooseTeam(team);
                    }
                    break;
                case ScannerActivity.SCAN_QR_CODE:
                    String format = data.getStringExtra(ScannerActivity.SCAN_RESULT_FORMAT);
                    String str = data.getStringExtra(ScannerActivity.SCAN_RESULT);
                    try {
                        String json = new String(Base64.decode(str, Base64.DEFAULT));
                        final QRCodeData qrCodeData = gson.fromJson(json, QRCodeData.class);
                        if ("QR_CODE".equalsIgnoreCase(format) && qrCodeData != null && qrCodeData.verify()) {
                            showTeamInfoDialog(qrCodeData.name, qrCodeData.color, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    joinTeamBySignCode(qrCodeData._id, qrCodeData.signCode);
                                }
                            });
                        } else {
                            MainApp.showToastMsg(R.string.no_team_error);
                        }
                    } catch (Exception e) {
                        MainApp.showToastMsg(R.string.no_team_error);
                    }
                    break;
                case REQUEST_SYNC_TEAMBITION:
                    List<Team> teams = Parcels.unwrap(data.getParcelableExtra("teams"));
                    if (teams != null && !teams.isEmpty()) {
                        footerDivider.setVisibility(View.VISIBLE);
                        adapter.updateData(teams);
                    } else {
                        footerDivider.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Team team = adapter.getItem(position);
        chooseTeam(team);
    }

    @Override
    public void onLoadUserFinish(User user) {
        renderUserInfo(user);
        layoutMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransactionUtil.goTo(ChooseTeamActivity.this, PreferenceActivity.class);
            }
        });
    }

    @Override
    public void onEmpty() {
        footerDivider.setVisibility(View.GONE);
    }

    @Override
    public void onGetTeamsFinish(ArrayList<Team> teams) {
        footerDivider.setVisibility(View.VISIBLE);
        adapter.updateData(teams);
    }

    private void showTeamInfoDialog(String name, String color, View.OnClickListener onClickListener) {
        View viewTeam = LayoutInflater.from(this).inflate(R.layout.dialog_join_team, null);
        ImageView imgTeamColor = (ImageView) viewTeam.findViewById(R.id.img_team_color_dialog);
        TextView tvTeamKey = (TextView) viewTeam.findViewById(R.id.tv_team_key_dialog);
        TextView tvTeamName = (TextView) viewTeam.findViewById(R.id.tv_team_name_dialog);
        ThemeButton btnJoin = (ThemeButton) viewTeam.findViewById(R.id.btn_join);
        imgTeamColor.setImageResource(ThemeUtil.getThemeRoundDrawableId(color));
        btnJoin.setThemeBackground(getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorPrimaryDark));
        if (StringUtil.isNotBlank(name)) {
            tvTeamName.setText(name);
            tvTeamKey.setText(name.substring(0, 1));
        }
        btnJoin.setOnClickListener(onClickListener);
        new TalkDialog.Builder(this)
                .customView(viewTeam, false).show();
    }

    private void handleInviteIntent() {
        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();
        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            final String inviteCode = data.getQueryParameter("inviteCode");
            if (StringUtil.isNotBlank(inviteCode)) {
                TalkClient.getInstance().getTalkApi()
                        .getTeamByInviteCode(inviteCode)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Team>() {
                            @Override
                            public void call(Team team) {
                                showTeamInfoDialog(team.getName(), team.getColor(), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        joinTeamByInviteCode(inviteCode);
                                    }
                                });
                            }
                        }, new ApiErrorAction());
            }
        }
    }

    private void joinTeamByInviteCode(String inviteCode) {
        TalkClient.getInstance().getTalkApi()
                .joinByInviteCode(inviteCode)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Team>() {
                    @Override
                    public void call(Team team) {
                        chooseTeam(team);
                    }
                }, new ApiErrorAction());
    }

    private void joinTeamBySignCode(String teamId, String signCode) {
        TalkClient.getInstance().getTalkApi()
                .joinBySignCode(teamId, signCode)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Team>() {
                    @Override
                    public void call(Team team) {
                        chooseTeam(team);
                    }
                }, new ApiErrorAction());
    }

    private void renderUserInfo(final User user) {
        tvName.setText(user.getName());
        MainApp.IMAGE_LOADER.displayImage(user.getAvatarUrl(), imgAvatar,
                ImageLoaderConfig.AVATAR_OPTIONS);
        if (user.getPreference() == null) {
            User u = (User) MainApp.PREF_UTIL.getObject(Constant.USER, User.class);
            if (u != null) {
                user.setPreference(u.getPreference());
            }
        }
        MainApp.PREF_UTIL.putObject(Constant.USER, user);
        if (user.getPreference() != null && user.getPreference().isNotifyOnRelated()) {
            MainApp.PREF_UTIL.putInt(Constant.NOTIFY_PREF,
                    NotificationConfig.NOTIFICATION_ONLY_MENTION);
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_create_team:
                TransactionUtil.goToForResult(this, CreateTeamActivity.class,
                        REQUEST_CREATE_TEAM);
                break;
            case R.id.layout_scan_qr_code:
                TransactionUtil.goToForResult(this, ScannerActivity.class,
                        ScannerActivity.SCAN_QR_CODE);
                break;
            case R.id.layout_sync_teambition:
                TransactionUtil.goToForResult(this, SyncTeambitionActivity.class,
                        REQUEST_SYNC_TEAMBITION);
                break;
        }
    }

    private void chooseTeam(Team team) {
        if (team != null) {
            MainApp.PREF_UTIL.putObject(Constant.TEAM, team);
            BizLogic.syncData();
            BizLogic.syncLeaveMemberData();
            TransactionUtil.goAndRestartHome(this);
        }
    }

    @Subscribe
    public void onUpdateUserEvent(UpdateUserEvent event) {
        User user = BizLogic.getUserInfo();
        renderUserInfo(user);
    }
}
