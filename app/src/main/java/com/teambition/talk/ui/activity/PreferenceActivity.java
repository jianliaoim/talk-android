package com.teambition.talk.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.talk.dialog.TalkDialog;
import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.Constant;
import com.teambition.talk.MainApp;
import com.teambition.talk.NotificationConfig;
import com.teambition.talk.R;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.apis.TalkApi;
import com.teambition.talk.client.data.FileUploadResponseData;
import com.teambition.talk.client.data.TeamUpdateRequestData;
import com.teambition.talk.client.data.UserUpdateData;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Preference;
import com.teambition.talk.entity.Prefs;
import com.teambition.talk.entity.Team;
import com.teambition.talk.entity.User;
import com.teambition.talk.event.UpdateMemberEvent;
import com.teambition.talk.event.UpdateUserEvent;
import com.teambition.talk.presenter.SettingPresenter;
import com.teambition.talk.realm.MemberRealm;
import com.teambition.talk.realm.RealmConfig;
import com.teambition.talk.service.MessageService;
import com.teambition.talk.ui.widget.ProgressImageView;
import com.teambition.talk.util.AnalyticsHelper;
import com.teambition.talk.util.NotificationUtil;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.ThemeUtil;
import com.teambition.talk.util.TransactionUtil;
import com.teambition.talk.view.SettingView;
import com.umeng.analytics.MobclickAgent;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;

/**
 * Created by zeatual on 14/12/4.
 */
public class PreferenceActivity extends BaseActivity implements SettingView, TextWatcher {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.avatar)
    ProgressImageView avatar;
    @InjectView(R.id.et_name)
    EditText etName;
    @InjectView(R.id.et_alias)
    EditText etAlias;
    @InjectView(R.id.et_email)
    EditText etEmail;
    @InjectView(R.id.switch_hide_phone)
    SwitchCompat swHidePhone;
    @InjectView(R.id.btn_save)
    Button btnSave;
    @InjectView(R.id.btn_discard)
    Button btnDiscard;
    @InjectView(R.id.layout_alias)
    LinearLayout layoutAlias;
    @InjectView(R.id.layout_hide_phone)
    LinearLayout layoutHidePhone;
    @InjectView(R.id.tv_linked_accounts)
    TextView tvLinkedAccounts;
    @InjectView(R.id.tv_notification)
    TextView tvNotification;
    @InjectView(R.id.tv_feedback)
    TextView tvFeedback;
    @InjectView(R.id.tv_sign_out)
    TextView tvSignOut;
    @InjectView(R.id.tv_version)
    TextView tvVersion;

    private SettingPresenter presenter;
    private int preSelected;
    private boolean pushOnWorkTime;

    private User user;
    private Team team;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);
        ButterKnife.inject(this);

        user = (User) MainApp.PREF_UTIL.getObject(Constant.USER, User.class);
        if (user != null && user.getPreference() != null) {
            pushOnWorkTime = user.getPreference().isPushOnWorkTime();
        }
        team = (Team) MainApp.PREF_UTIL.getObject(Constant.TEAM, Team.class);
        preSelected = MainApp.PREF_UTIL.getInt(Constant.NOTIFY_PREF, NotificationConfig.NOTIFICATION_ON);

        presenter = new SettingPresenter(this);

        initView();
    }

    private void initView() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.action_edit_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            tvVersion.setText(info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        tvLinkedAccounts.setCompoundDrawablesWithIntrinsicBounds(ThemeUtil.
                getDrawableWithColor(getResources(), R.drawable.ic_linked_accounts,
                        R.color.colorPrimary), null, null, null);
        tvNotification.setCompoundDrawablesWithIntrinsicBounds(ThemeUtil.
                getDrawableWithColor(getResources(), R.drawable.ic_notify,
                        R.color.colorPrimary), null, null, null);
        tvFeedback.setCompoundDrawablesWithIntrinsicBounds(ThemeUtil.
                getDrawableWithColor(getResources(), R.drawable.ic_edit,
                       R.color.colorPrimary), null, null, null);
        tvSignOut.setCompoundDrawablesWithIntrinsicBounds(ThemeUtil.
                getDrawableWithColor(getResources(), R.drawable.ic_sign_out,
                        R.color.colorPrimary), null, null, null);

        avatar.setImageUrl(user.getAvatarUrl());
        avatar.setOnUploadFinishListener(new ProgressImageView.OnUploadFinishListener() {
            @Override
            public void onUploadFinish(FileUploadResponseData file) {
                String avatarUrl = file.getThumbnailUrl();
                user.setAvatarUrl(avatarUrl);
                updateUserSettings(user, null);
            }
        });
        setBasicInfo();
        if (team == null) {
            layoutAlias.setVisibility(View.GONE);
            layoutHidePhone.setVisibility(View.GONE);
        } else {
            swHidePhone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (team.getPrefs() == null) {
                        team.setPrefs(new Prefs(isChecked));
                    } else {
                        team.getPrefs().setHideMobile(isChecked);
                    }
                    updateUserSettings(null, team);
                }
            });
        }
        etName.addTextChangedListener(this);
        etEmail.addTextChangedListener(this);
        etAlias.addTextChangedListener(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        MobclickAgent.onPageStart(getClass().getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        MobclickAgent.onPageEnd(getClass().getName());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SelectImageActivity.SELECT_IMAGES:
                    avatar.setLocalImageUrl(data.getStringExtra(SelectImageActivity.IMAGE_PATH));
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showDiscardDialogIfNecessary();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showDiscardDialogIfNecessary();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick({R.id.btn_save, R.id.btn_discard, R.id.btn_change_avatar, R.id.tv_linked_accounts,
            R.id.tv_notification, R.id.tv_feedback, R.id.tv_sign_out, R.id.tv_language})
    public void onClick(View view) {
        InputMethodManager inputManager =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(etName.getWindowToken(), 0);
        etName.clearFocus();
        etAlias.clearFocus();
        etEmail.clearFocus();
        switch (view.getId()) {
            case R.id.btn_save:
                user.setName(etName.getText().toString());
                user.setEmail(StringUtil.isBlank(etEmail.getText().toString()) ? null :
                        etEmail.getText().toString());
                if (team != null) {
                    if (team.getPrefs() == null) {
                        team.setPrefs(new Prefs());
                    }
                    team.getPrefs().setAlias(etAlias.getText().toString());
                    updateUserSettings(user, team);
                } else {
                    updateUserSettings(user, null);
                }
                break;
            case R.id.btn_discard:
                etName.setText(user.getName());
                etEmail.setText(user.getEmail());
                if (team != null) {
                    if (team.getPrefs() != null) {
                        etAlias.setText(team.getPrefs().getAlias());
                    } else {
                        etAlias.setText("");
                    }
                }
                break;
            case R.id.btn_change_avatar:
                Intent intent = new Intent(PreferenceActivity.this, SelectImageActivity.class);
                startActivityForResult(intent, SelectImageActivity.SELECT_IMAGES);
                break;
            case R.id.tv_linked_accounts:
                TransactionUtil.goTo(this, AccountsActivity.class);
                break;
            case R.id.tv_notification:
                showNotificationSettingsDialog();
                break;
            case R.id.tv_feedback:
                TransactionUtil.goTo(this, FeedbackActivity.class);
                break;
            case R.id.tv_sign_out:
                TalkClient.getInstance().getTalkApi()
                        .signOut().observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Object>() {
                            @Override
                            public void call(Object o) {
                                signOut();
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                signOut();
                            }
                        });
                break;
            case R.id.tv_language:
                final String currentLanguage = MainApp.PREF_UTIL.getString(Constant.LANGUAGE_PREF, "zh");
                final int currentChoice =  "zh".equals(currentLanguage) ? 0 : 1;
                new TalkDialog.Builder(this)
                        .title(R.string.preference_language)
                        .items(R.array.language_types)
                        .itemsCallbackSingleChoice(currentChoice, new TalkDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(TalkDialog dialog, View itemView, int which, CharSequence text) {
                                if (which == currentChoice) {
                                    return false;
                                } else {
                                    switchLanguage(which == 0 ? "zh" : "en");
                                    TransactionUtil.goAndRestartHome(PreferenceActivity.this);
                                    return true;
                                }
                            }
                        })
                .show();
                break;
        }
    }

    private void showNotificationSettingsDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_notification_settings, null);
        final RadioGroup radioGroup = (RadioGroup) dialogView.findViewById(R.id.radio_group);
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            FrameLayout item = (FrameLayout) radioGroup.getChildAt(i);
            final RadioButton radioButton = (RadioButton) item.getChildAt(0);
            radioButton.setChecked(preSelected == i);
            final int finalI = i;
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (int j = 0; j < radioGroup.getChildCount(); j++) {
                        FrameLayout item = (FrameLayout) radioGroup.getChildAt(j);
                        final RadioButton radioButton = (RadioButton) item.getChildAt(0);
                        radioButton.setChecked(j == finalI);
                    }
                    switch (finalI) {
                        case NotificationConfig.NOTIFICATION_ON:
                            presenter.updateNotifyOnRelated(false);
                            break;
                        case NotificationConfig.NOTIFICATION_ONLY_MENTION:
                            presenter.updateNotifyOnRelated(true);
                            break;
                        case NotificationConfig.NOTIFICATION_OFF:
                            MainApp.PREF_UTIL.putInt(Constant.NOTIFY_PREF,
                                    NotificationConfig.NOTIFICATION_OFF);
                            preSelected = finalI;
                            break;
                    }
                }
            });
        }
        SwitchCompat switchCompat = (SwitchCompat) dialogView.findViewById(R.id.push_on_work_time);
        switchCompat.setChecked(pushOnWorkTime);
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                pushOnWorkTime = b;
                presenter.updatePushOnWorkTime(b);
            }
        });
        new TalkDialog.Builder(this)
                .title(R.string.title_notification_setting)
                .titleColorRes(R.color.white)
                .titleBackgroundColorRes(R.color.colorPrimary)
                .customView(dialogView, true)
                .positiveText(R.string.confirm)
                .show();
    }

    @Override
    public void onEmailNotificationUpdate(boolean result) {
    }

    @Override
    public void onNotifyOnRelatedUpdate(boolean value, boolean result) {
        if (result) {
            MainApp.PREF_UTIL.putInt(Constant.NOTIFY_PREF, value ?
                    NotificationConfig.NOTIFICATION_ONLY_MENTION :
                    NotificationConfig.NOTIFICATION_ON);
            preSelected = value ? NotificationConfig.NOTIFICATION_ONLY_MENTION :
                    NotificationConfig.NOTIFICATION_ON;
        } else {
            MainApp.showToastMsg(R.string.network_failed);
        }
    }

    @Override
    public void onPushOnWorkTimeUpdate(boolean result) {
        if (!result) {
            pushOnWorkTime = !pushOnWorkTime;
        }
    }

    public void signOut() {
        RealmConfig.deleteRealm();
        MainApp.PREF_UTIL.clear();
        CookieManager cookieManager = CookieManager.getInstance();
        try {
            cookieManager.removeAllCookie();
        } catch (Exception e) {
        }
        NotificationUtil.stopPush(MainApp.CONTEXT);
        Intent intent = new Intent(this, Oauth2Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        BizLogic.cancelSync();
        startActivity(intent);

        // disconnect message service
        Intent closeMsgIntent = new Intent(this, MessageService.class);
        closeMsgIntent.setAction(MessageService.ACTION_CLOSE);
        startService(closeMsgIntent);
    }

    private void setBasicInfo() {
        etName.setText(user.getName());
        etEmail.setText(user.getEmail());
        if (team != null && team.getPrefs() != null) {
            etAlias.setText(team.getPrefs().getAlias());
            swHidePhone.setChecked(team.getPrefs().getHideMobile() == null ? false :
                    team.getPrefs().getHideMobile());
        }
    }

    private void showDiscardDialogIfNecessary() {
        if (isInfoChanged()) {
            new TalkDialog.Builder(this)
                    .title(R.string.title_discard)
                    .titleColorRes(R.color.white)
                    .titleBackgroundColorRes(R.color.talk_warning)
                    .content(R.string.confirm_discard)
                    .positiveText(R.string.discard)
                    .positiveColorRes(R.color.talk_warning)
                    .negativeText(R.string.cancel)
                    .negativeColorRes(R.color.material_grey_700)
                    .callback(new TalkDialog.ButtonCallback() {
                        @Override
                        public void onPositive(TalkDialog materialDialog, View v) {
                            finish();
                        }
                    })
                    .show();
        } else {
            finish();
        }
    }

    private void updateUserSettings(User user, Team team) {
        TalkApi api = TalkClient.getInstance().getTalkApi();

        Observable<Team> updateTeamScream;
        Observable<User> updateUserScream;
        if (team == null) {
            updateTeamScream = Observable.just(null);
        } else {
            updateTeamScream = api.updateTeam(team.get_id(),
                    new TeamUpdateRequestData(null, null, team.getPrefs()));
        }
        if (user == null) {
            updateUserScream = Observable.just(null);
        } else {
            updateUserScream = api.updateUser(user.get_id(),
                    new UserUpdateData(user.getName(), user.getEmail(), user.getAvatarUrl()));
        }

        updateTeamScream.zipWith(updateUserScream, new Func2<Team, User, UserTeamSetting>() {
            @Override
            public UserTeamSetting call(Team team, User user) {
                return new UserTeamSetting(user, team);
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<UserTeamSetting>() {
                    @Override
                    public void call(UserTeamSetting setting) {
                        if (setting.user != null) {
                            if (setting.user.getPreference() == null) {
                                User u = (User) MainApp.PREF_UTIL.getObject(Constant.USER, User.class);
                                if (u != null) {
                                    setting.user.setPreference(u.getPreference());
                                }
                            }
                            MainApp.PREF_UTIL.putObject(Constant.USER, setting.user);
                            BusProvider.getInstance().post(new UpdateUserEvent());
                            Member member = MainApp.globalMembers.get(setting.user.get_id());
                            member.setName(setting.user.getName());
                            member.setAvatarUrl(setting.user.getAvatarUrl());
                            member.setEmail(setting.user.getEmail());
                            MemberRealm.getInstance().updateMemberInfo(member)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Action1<Object>() {
                                        @Override
                                        public void call(Object o) {
                                            MainApp.IS_MEMBER_CHANGED = true;
                                            BusProvider.getInstance().post(new UpdateMemberEvent());
                                        }
                                    }, new RealmErrorAction());
                        }
                        if (setting.team != null) {
                            MainApp.PREF_UTIL.putObject(Constant.TEAM, setting.team);
                            if (setting.team.getPrefs() != null &&
                                    setting.team.getPrefs().getAlias() != null) {
                                Member member = MainApp.globalMembers.get(BizLogic.getUserInfo().get_id());
                                member.setPrefs(setting.team.getPrefs());
                                member.setAlias(setting.team.getPrefs().getAlias());
                                MemberRealm.getInstance().addOrUpdate(member)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Action1<Member>() {
                                            @Override
                                            public void call(Member member) {
                                                MainApp.IS_MEMBER_CHANGED = true;
                                                BusProvider.getInstance().post(new UpdateMemberEvent());
                                            }
                                        }, new RealmErrorAction());
                            }
                        }
                        MainApp.showToastMsg(R.string.update_success);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainApp.showToastMsg(R.string.network_failed);
                    }
                });
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        btnDiscard.setEnabled(isInfoChanged());
        btnSave.setEnabled(isInfoChanged());
    }

    private boolean isInfoChanged() {
        return (team != null && team.getPrefs() != null && team.getPrefs().getAlias() != null
                && !team.getPrefs().getAlias().equals(etAlias.getText().toString()))
                || ((team != null && (team.getPrefs() == null || team.getPrefs().getAlias() == null))
                && StringUtil.isNotBlank(etAlias.getText().toString()))
                || (user.getEmail() != null && !user.getEmail().equals(etEmail.getText().toString()))
                || (user.getEmail() == null && StringUtil.isNotBlank(etEmail.getText().toString()))
                || (user.getName() != null && !user.getName().equals(etName.getText().toString()));
    }

    private static class UserTeamSetting {
        public User user;
        public Team team;

        private UserTeamSetting(User user, Team team) {
            this.user = user;
            this.team = team;
        }
    }
}
