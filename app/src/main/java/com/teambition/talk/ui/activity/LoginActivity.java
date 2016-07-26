package com.teambition.talk.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.teambition.talk.R;
import com.teambition.talk.rx.ApiErrorAction;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.entity.CountryModel;
import com.teambition.talk.entity.User;
import com.teambition.talk.util.AnalyticsHelper;
import com.teambition.talk.util.DensityUtil;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.TransactionUtil;

import org.parceler.Parcels;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by nlmartian on 11/3/15.
 */
public class LoginActivity extends BaseActivity {
    public static final int AUTH_TYPE_EMAIL = 0;
    public static final int AUTH_TYPE_MOBILE = 1;

    private static final int REQUEST_TEAMBITION_CODE = 0;
    public static final int REQUEST_COUNTRY_CODE = 1;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.et_login)
    EditText etLogin;
    @InjectView(R.id.et_password)
    EditText etPassword;
    @InjectView(R.id.country_code)
    TextView tvCountryCode;
    @InjectView(R.id.tv_auth_type)
    TextView tvAuthType;

    private int authType = AUTH_TYPE_EMAIL;

    private long loginStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.login);

        AnalyticsHelper.getInstance().startTiming(AnalyticsHelper.Category.login, "login duration");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_TEAMBITION_CODE) {
            String code = data.getStringExtra(UnionsActivity.CODE);
            TalkClient.getInstance().getAccountApi()
                    .signInByTeambition(code)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<User>() {
                        @Override
                        public void call(User user) {
                            if (StringUtil.isNotBlank(user.getAccountToken())) {
                                RegisterActivity.initUserData(LoginActivity.this, user);
                                TransactionUtil.goTo(LoginActivity.this, ChooseTeamActivity.class, true);
                            }
                        }
                    }, new ApiErrorAction());
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_COUNTRY_CODE) {
            if (data != null) {
                CountryModel model = Parcels.unwrap(data.getParcelableExtra(PickCountryCodeActivity.COUNTRY_CODE_DATA));
                tvCountryCode.setText("+ " +  String.valueOf(model.callingCode));
            }
        }
    }
    @OnClick({R.id.forget_password, R.id.btn_login, R.id.tv_auth_type, R.id.country_code})
    public void onClick(View view) {
        if (view.getId() == R.id.forget_password) {
            startActivity(new Intent(this, FindPasswordActivity.class));
        } else if (view.getId() == R.id.btn_login) {
            String passwordStr = etPassword.getText().toString();
            String loginStr = etLogin.getText().toString().trim();
            if (passwordStr.length() < 6) {
                etPassword.setError(getString(R.string.password_less_than_6));
                return;
            }
            if (StringUtil.isEmail(loginStr)) {
                emailLogin(loginStr, passwordStr);
            } else if (StringUtil.isNumber(loginStr)) {
                mobileLogin(tvCountryCode.getText().toString().trim() + loginStr, passwordStr);
            } else {
                etLogin.setError(getString(R.string.login_error));
                return;
            }
        } else if (view.getId() == R.id.tv_auth_type) {
            etLogin.setText(null);
            if (authType == AUTH_TYPE_EMAIL) {
                authType = AUTH_TYPE_MOBILE;
                etLogin.setHint(R.string.mobile_login);
                tvCountryCode.setVisibility(View.VISIBLE);
                etLogin.setPadding(DensityUtil.dip2px(this, 50), etLogin.getPaddingTop(),
                        etLogin.getPaddingRight(), etLogin.getPaddingBottom());
                etLogin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                tvAuthType.setText(R.string.email_auth);
            } else {
                authType = AUTH_TYPE_EMAIL;
                etLogin.setHint(R.string.email_login);
                tvCountryCode.setVisibility(View.GONE);
                etLogin.setPadding(etLogin.getPaddingRight(), etLogin.getPaddingTop(),
                        etLogin.getPaddingRight(), etLogin.getPaddingBottom());
                etLogin.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                tvAuthType.setText(R.string.oauth_begin);
            }
        } else if (view.getId() == R.id.country_code) {
            Intent intent = new Intent(this, PickCountryCodeActivity.class);
            startActivityForResult(intent, REQUEST_COUNTRY_CODE);
            overridePendingTransition(R.anim.anim_fade_transition_in, 0);
        }
    }

    private void emailLogin(String email, String password) {
        AnalyticsHelper.getInstance().sendEvent(AnalyticsHelper.Category.login, "login ready", "with email");
        TalkClient.getInstance().getAccountApi()
                .emailSignIn(email, password)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        RegisterActivity.initUserData(LoginActivity.this, user);
                        TransactionUtil.goTo(LoginActivity.this, ChooseTeamActivity.class, true);

                        AnalyticsHelper.getInstance().sendEvent(AnalyticsHelper.Category.login,
                                "login succ", "with email");
                        AnalyticsHelper.getInstance().endTiming(AnalyticsHelper.Category.login, "login duration");
                    }
                }, new ApiErrorAction());

    }

    private void mobileLogin(String phoneNumber, String password) {
        AnalyticsHelper.getInstance().sendEvent(AnalyticsHelper.Category.login, "login ready", "with phone");
        TalkClient.getInstance().getAccountApi()
                .mobileSignIn(phoneNumber, password)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        RegisterActivity.initUserData(LoginActivity.this, user);
                        TransactionUtil.goTo(LoginActivity.this, ChooseTeamActivity.class, true);

                        AnalyticsHelper.getInstance().sendEvent(AnalyticsHelper.Category.login,
                                "login succ", "with phone");
                        AnalyticsHelper.getInstance().endTiming(AnalyticsHelper.Category.login, "login duration");

                    }
                }, new ApiErrorAction());
    }
}
