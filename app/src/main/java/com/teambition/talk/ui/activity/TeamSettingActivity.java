package com.teambition.talk.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.talk.dialog.TalkDialog;
import com.teambition.talk.BizLogic;
import com.teambition.talk.Constant;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.data.RefreshSignCodeRequestData;
import com.teambition.talk.entity.QRCodeData;
import com.teambition.talk.entity.Team;
import com.teambition.talk.presenter.TeamSettingPresenter;
import com.teambition.talk.realm.TeamRealm;
import com.teambition.talk.rx.EmptyAction;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.ui.ShareDialogHelper;
import com.teambition.talk.ui.widget.ThemeButton;
import com.teambition.talk.util.DensityUtil;
import com.teambition.talk.util.EmojiUtil;
import com.teambition.talk.util.QRCodeUtil;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.ThemeUtil;
import com.teambition.talk.util.TransactionUtil;
import com.teambition.talk.view.TeamSettingView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zeatual on 15/4/3.
 */
public class TeamSettingActivity extends BaseActivity implements TeamSettingView, TextWatcher {

    private final static String TEAM_NAME = "team_name";

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.team_name)
    EditText teamName;
    @InjectView(R.id.btn_save)
    ThemeButton btnSave;
    @InjectView(R.id.btn_discard)
    Button btnDiscard;
    @InjectView(R.id.line)
    View line;

    private TeamSettingPresenter presenter;
    private String teamId = BizLogic.getTeamId();
    private Team team;
    private String color;
    private String formerColor;
    private String formerName;
    private boolean isChanged;
    private ShareDialogHelper shareDialogHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_setting);
        ButterKnife.inject(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.action_team_setting);

        presenter = new TeamSettingPresenter(this);
        team = (Team) MainApp.PREF_UTIL.getObject(Constant.TEAM, Team.class);
        formerColor = team.getColor() + "";
        color = team.getColor() + "";
        formerName = team.getName() + "";

        teamName.setText(savedInstanceState != null ? savedInstanceState.getString(TEAM_NAME, team.getName()) : team.getName());
        teamName.setSelection(teamName.getText().toString().length());

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_color_team, null);
        dealColorLayout(view);

        teamName.addTextChangedListener(this);
        if (!BizLogic.isAdmin()) {
            teamName.setEnabled(false);
            btnSave.setVisibility(View.GONE);
            btnDiscard.setVisibility(View.GONE);
        }

        String shareContent = getString(R.string.share_invite_content, team.getName(), team.getInviteUrl(),
                EmojiUtil.BALLOON + team.getInviteCode() + EmojiUtil.BALLOON);
        shareDialogHelper = new ShareDialogHelper(this, shareContent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            restartHomeIfNecessary();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @OnClick({R.id.btn_save, R.id.btn_discard, R.id.section_show_qr_code, R.id.section_share_invitation})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save:
                if (StringUtil.isNotBlank(teamName.getText().toString())) {
                    presenter.updateTeam(color, teamName.getText().toString());
                } else {
                    MainApp.showToastMsg(R.string.name_empty);
                }
                break;
            case R.id.btn_discard:
                teamName.setText(formerName);
                teamName.setSelection(formerName.length());
                color = formerColor;
                renderColor();
                resetButton();
                break;
            case R.id.section_show_qr_code:
                try {
                    QRCodeData data = new QRCodeData(teamId, BizLogic.getTeamName(),
                            BizLogic.getTeamColor(), BizLogic.getSignCode());
                    View viewQRCode = LayoutInflater.from(this).inflate(R.layout.dialog_show_qr_code, null);
                    TextView tvTeamName = (TextView) viewQRCode.findViewById(R.id.tv_team_name);
                    final ImageView imageQRCode = (ImageView) viewQRCode.findViewById(R.id.image_qr_code);
                    TextView btnReset = (TextView) viewQRCode.findViewById(R.id.btn_reset);
                    if (!BizLogic.isAdmin()) {
                        btnReset.setVisibility(View.GONE);
                    }
                    tvTeamName.setText(BizLogic.getTeamName());
                    final int size = DensityUtil.dip2px(this, 144);
                    imageQRCode.setImageBitmap(QRCodeUtil.encode(Base64.encodeToString(data.toString().getBytes("UTF-8"), Base64.DEFAULT),
                            size));
                    btnReset.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            TalkClient.getInstance().getTalkApi()
                                    .refreshSignCode(teamId, new RefreshSignCodeRequestData())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Action1<Team>() {
                                        @Override
                                        public void call(Team team) {
                                            if (team != null) {
                                                MainApp.PREF_UTIL.putObject(Constant.TEAM, team);
                                                QRCodeData data = new QRCodeData(teamId, BizLogic.getTeamName(),
                                                        BizLogic.getTeamColor(), BizLogic.getSignCode());
                                                imageQRCode.setImageBitmap(QRCodeUtil.encode(Base64.encodeToString(data.toString().getBytes(), Base64.DEFAULT),
                                                        size));
                                            }
                                        }
                                    }, new Action1<Throwable>() {
                                        @Override
                                        public void call(Throwable throwable) {
                                            MainApp.showToastMsg(R.string.qr_code_refresh_error);
                                        }
                                    });
                        }
                    });
                    new TalkDialog.Builder(this)
                            .customView(viewQRCode, false)
                            .show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.section_share_invitation:
                shareDialogHelper.showDialog();
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TEAM_NAME, teamName.getText().toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_team_setting, menu);
        if (!BizLogic.isAdmin()) {
            menu.findItem(R.id.action_palette).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                restartHomeIfNecessary();
                break;
            case R.id.action_quit_team:
                new TalkDialog.Builder(this)
                        .title(R.string.title_quit_team)
                        .titleColorRes(R.color.white)
                        .titleBackgroundColorRes(R.color.talk_warning)
                        .content(R.string.confirm_quit_team)
                        .positiveText(R.string.confirm)
                        .positiveColorRes(R.color.talk_warning)
                        .negativeText(R.string.cancel)
                        .negativeColorRes(R.color.material_grey_700)
                        .callback(new TalkDialog.ButtonCallback() {
                            @Override
                            public void onPositive(TalkDialog materialDialog, View v) {
                                presenter.leaveTeam();
                            }
                        })
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        resetButton();
    }

    @Override
    public void onUpdateSuccess(boolean isSuccess) {
        if (isSuccess) {
            isChanged = true;
            formerName = teamName.getText().toString();
            formerColor = color;
            resetButton();
        }
    }

    @Override
    public void onQuitTeamFinish() {
        finish();
        TeamRealm.getInstance().deleteTeam(BizLogic.getTeamId())
                .subscribe(new EmptyAction<Team>(), new RealmErrorAction());
        Intent intent = new Intent(this, ChooseTeamActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void resetButton() {
        btnDiscard.setEnabled(isChanged());
        btnSave.setEnabled(isChanged());
    }

    private boolean isChanged() {
        return !teamName.getText().toString().equals(formerName) ||
                !color.equals(formerColor);
    }


    private void restartHomeIfNecessary() {
        if (isChanged) {
            TransactionUtil.goAndRestartHome(this);
        } else {
            finish();
        }
    }

    @SuppressLint("NewApi")
    private void renderColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int darkColor = ThemeUtil.getThemeColorDarkRes(color);
            getWindow().setStatusBarColor(getResources().getColor(darkColor));
        }
        int primaryColor = ThemeUtil.getThemeColorRes(color);
        toolbar.setBackgroundColor(getResources().getColor(primaryColor));
        btnSave.setThemeBackground(color);
        line.setBackgroundResource(ThemeUtil.getThemeColorRes(color));
    }

    private void dealColorLayout(View view) {
        FrameLayout grapeLayout = (FrameLayout) view.findViewById(R.id.grape_layout);
        FrameLayout blueberryLayout = (FrameLayout) view.findViewById(R.id.blueberry_layout);
        FrameLayout oceanLayout = (FrameLayout) view.findViewById(R.id.ocean_layout);
        FrameLayout mintLayout = (FrameLayout) view.findViewById(R.id.mint_layout);
        FrameLayout teaLayout = (FrameLayout) view.findViewById(R.id.tea_layout);
        FrameLayout inkLayout = (FrameLayout) view.findViewById(R.id.ink_layout);
        List<FrameLayout> colorList = new ArrayList<>();
        colorList.add(grapeLayout);
        colorList.add(blueberryLayout);
        colorList.add(oceanLayout);
        colorList.add(mintLayout);
        colorList.add(teaLayout);
        colorList.add(inkLayout);
        ImageView grape = (ImageView) view.findViewById(R.id.grape);
        ImageView blueberry = (ImageView) view.findViewById(R.id.blueberry);
        ImageView ocean = (ImageView) view.findViewById(R.id.ocean);
        ImageView mint = (ImageView) view.findViewById(R.id.mint);
        ImageView tea = (ImageView) view.findViewById(R.id.tea);
        ImageView ink = (ImageView) view.findViewById(R.id.ink);
        ImageView grapeCheck = (ImageView) view.findViewById(R.id.selected_grape);
        ImageView blueberryCheck = (ImageView) view.findViewById(R.id.selected_blueberry);
        ImageView oceanCheck = (ImageView) view.findViewById(R.id.selected_ocean);
        ImageView mintCheck = (ImageView) view.findViewById(R.id.selected_mint);
        ImageView teaCheck = (ImageView) view.findViewById(R.id.selected_tea);
        ImageView inkCheck = (ImageView) view.findViewById(R.id.selected_ink);
        final Map<String, ImageView> colorMap = new HashMap<>();
        colorMap.put("grape", grape);
        colorMap.put("blueberry", blueberry);
        colorMap.put("ocean", ocean);
        colorMap.put("mint", mint);
        colorMap.put("tea", tea);
        colorMap.put("ink", ink);
        final Map<String, ImageView> colorCheckMap = new HashMap<>();
        colorCheckMap.put("grape", grapeCheck);
        colorCheckMap.put("blueberry", blueberryCheck);
        colorCheckMap.put("ocean", oceanCheck);
        colorCheckMap.put("mint", mintCheck);
        colorCheckMap.put("tea", teaCheck);
        colorCheckMap.put("ink", inkCheck);

        if (colorMap.containsKey(color)) {
            colorMap.get(color).setImageResource(ThemeUtil.getThemeRoundDrawableId(color));
            colorCheckMap.get(color).setVisibility(View.VISIBLE);

            for (FrameLayout colorLayout : colorList) {
                colorLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        colorMap.get(color).setImageResource(ThemeUtil.getThemeCircleDrawableId(color));
                        colorCheckMap.get(color).setVisibility(View.GONE);
                        color = (String) v.getTag();
                        colorMap.get(color).setImageResource(ThemeUtil.getThemeRoundDrawableId(color));
                        colorCheckMap.get(color).setVisibility(View.VISIBLE);
                        renderColor();
                    }
                });
            }
        }
    }
}
