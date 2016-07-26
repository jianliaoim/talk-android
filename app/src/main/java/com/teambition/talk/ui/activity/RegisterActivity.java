package com.teambition.talk.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.talk.dialog.TalkDialog;
import com.teambition.talk.Constant;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.rx.ApiErrorAction;
import com.teambition.talk.client.ApiConfig;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.data.ErrorResponseData;
import com.teambition.talk.client.data.RandomCodeData;
import com.teambition.talk.client.data.StrikerTokenResponseData;
import com.teambition.talk.entity.CountryModel;
import com.teambition.talk.entity.User;
import com.teambition.talk.ui.CaptchaDialogHelper;
import com.teambition.talk.ui.fragment.SetupUserFragment;
import com.teambition.talk.ui.fragment.VerificationCodeFragment;
import com.teambition.talk.util.AnalyticsHelper;
import com.teambition.talk.util.DensityUtil;
import com.teambition.talk.util.NotificationUtil;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.TransactionUtil;

import org.parceler.Parcels;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.RetrofitError;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class RegisterActivity extends BaseActivity implements
        SetupUserFragment.OnFragmentInteractionListener,
        VerificationCodeFragment.OnFragmentInteractionListener {

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

    private String captchaUid;
    private int authType = LoginActivity.AUTH_TYPE_EMAIL;
    private CaptchaDialogHelper captchaDialogHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.register);
        captchaDialogHelper = new CaptchaDialogHelper(this, new CaptchaDialogHelper.OnValidSuccessListener() {
            @Override
            public void onValidSuccess(String uid) {
                captchaUid = uid;
                String passwordStr = etPassword.getText().toString();
                String loginStr = etLogin.getText().toString().trim();
                sendVCodeToMobile(tvCountryCode.getText().toString().trim() + loginStr, passwordStr, uid);
            }
        });

        AnalyticsHelper.getInstance().startTiming(AnalyticsHelper.Category.login, "register duration");
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
                                initUserData(RegisterActivity.this, user);
                                getStrikeToken();
                                TransactionUtil.goTo(RegisterActivity.this, ChooseTeamActivity.class, true);
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

    @OnClick({R.id.btn_signup, R.id.tv_auth_type, R.id.country_code, R.id.tv_policy})
    public void onClick(View view) {
        if (view.getId() == R.id.btn_signup) {
            final String passwordStr = etPassword.getText().toString();
            final String loginStr = etLogin.getText().toString().trim();
            if (passwordStr.length() < 6) {
                etPassword.setError(getString(R.string.password_less_than_6));
                return;
            }
            if (StringUtil.isEmail(loginStr)) {
                if (loginStr.endsWith("qq.com")) {
                    new TalkDialog.Builder(this)
                            .titleColorRes(R.color.white)
                            .titleBackgroundColorRes(R.color.talk_red)
                            .title(R.string.tips)
                            .content(R.string.recommend_not_qqmail)
                            .negativeText(R.string.modify)
                            .positiveText(R.string.confirm)
                            .callback(new TalkDialog.ButtonCallback() {
                                @Override
                                public void onPositive(TalkDialog dialog, View v) {
                                    emailSignUp(loginStr, passwordStr);
                                }
                            })
                            .build()
                            .show();
                } else {
                    emailSignUp(loginStr, passwordStr);
                }
            } else if (StringUtil.isNumber(loginStr)) {
                captchaDialogHelper.showDialog();
            } else {
                etLogin.setError(getString(R.string.login_error));
                return;
            }
        } else if (view.getId() == R.id.tv_auth_type) {
            etLogin.setText(null);
            etLogin.setError(null);
            if (authType == LoginActivity.AUTH_TYPE_EMAIL) {
                authType = LoginActivity.AUTH_TYPE_MOBILE;
                etLogin.setHint(R.string.mobile_login);
                tvCountryCode.setVisibility(View.VISIBLE);
                etLogin.setPadding(DensityUtil.dip2px(this, 50), etLogin.getPaddingTop(),
                        etLogin.getPaddingRight(), etLogin.getPaddingBottom());
                etLogin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                tvAuthType.setText(R.string.email_register);
            } else {
                authType = LoginActivity.AUTH_TYPE_EMAIL;
                etLogin.setHint(R.string.email_login);
                tvCountryCode.setVisibility(View.GONE);
                etLogin.setPadding(etLogin.getPaddingRight(), etLogin.getPaddingTop(),
                        etLogin.getPaddingRight(), etLogin.getPaddingBottom());
                etLogin.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                tvAuthType.setText(R.string.mobile_register);
            }
        } else if (view.getId() == R.id.country_code) {
            Intent intent = new Intent(this, PickCountryCodeActivity.class);
            startActivityForResult(intent, REQUEST_COUNTRY_CODE);
            overridePendingTransition(R.anim.anim_fade_transition_in, 0);
        } else if (view.getId() == R.id.tv_policy) {
            startActivity(WebContainerActivity.newIntent(this, ApiConfig.POLICY_URL, null));
        }
    }

    private void emailSignUp(String email, String password) {
        AnalyticsHelper.getInstance().sendEvent(AnalyticsHelper.Category.login,
                "register ready", "with email");

        TalkClient.getInstance().getAccountApi()
                .emailSignUp(email, password)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        initUserData(RegisterActivity.this, user);
                        getStrikeToken();
                        TransactionUtil.goTo(RegisterActivity.this, ChooseTeamActivity.class);

                        AnalyticsHelper.getInstance().sendEvent(AnalyticsHelper.Category.login,
                                "register succ", "with email");
                        AnalyticsHelper.getInstance().endTiming(AnalyticsHelper.Category.login, "register duration");
                    }
                }, new ApiErrorAction());
    }

    private void sendVCodeToMobile(final String mobile, String password, String uid) {
        TalkClient.getInstance().getAccountApi()
                .sendVerifyCode(mobile, "signup", password, uid)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RandomCodeData>() {
                    @Override
                    public void call(RandomCodeData randomCodeData) {
                        showVCodeFragment(randomCodeData.getRandomCode(), true);
                        String msg = getString(R.string.send_code_to_phone, mobile);
                        Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (throwable instanceof RetrofitError) {
                            try {
                                ErrorResponseData error = (ErrorResponseData) ((RetrofitError) throwable)
                                        .getBodyAs(ErrorResponseData.class);
                                if (error.code == 227) { // 发送过于频繁
                                    Toast.makeText(RegisterActivity.this, error.message, Toast.LENGTH_SHORT).show();
                                } else if (error.code == 233) {
                                    etLogin.setError(error.message);
//                                    etLogin.setError(getString(R.string.account_exist_error));
                                } else {
                                    etLogin.setError(error.message);
                                }
                            } catch (Exception e) {
                                MainApp.showToastMsg(R.string.network_failed);
                            }
                        }
                    }
                });
    }

    private void showVCodeFragment(String randomCode, boolean isMobile) {
        if (getSupportFragmentManager().findFragmentByTag(VerificationCodeFragment.TAG) == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container,
                            VerificationCodeFragment.newInstance(randomCode, isMobile),
                            VerificationCodeFragment.TAG)
                    .addToBackStack(null)
                    .commit();
        }
    }

    public static void initUserData(Context context, User user) {
        MainApp.PREF_UTIL.putString(Constant.ACCESS_TOKEN, user.getAccountToken());
        TalkClient.getInstance().setAccessToken(user.getAccountToken());
        TalkClient.getInstance().getTalkApi().subscribeUser(MainApp.PREF_UTIL
                .getString(Constant.SOCKET_ID)).subscribe();
        if (user.getPreference() == null) {
            User u = (User) MainApp.PREF_UTIL.getObject(Constant.USER, User.class);
            if (u != null) {
                user.setPreference(u.getPreference());
            }
        }
        MainApp.PREF_UTIL.putObject(Constant.USER, user);
        NotificationUtil.startPush(context);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onGetVerifyCode(boolean isMobile, String randomCode, String vCode) {
        AnalyticsHelper.getInstance().sendEvent(AnalyticsHelper.Category.login, "register ready", "with phone");

        String passwordStr = etPassword.getText().toString();
        String loginStr = etLogin.getText().toString().trim();
        TalkClient.getInstance().getAccountApi()
                .mobileSignUp(loginStr, passwordStr, randomCode, vCode)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        initUserData(RegisterActivity.this, user);
                        getStrikeToken();
                        TransactionUtil.goTo(RegisterActivity.this, ChooseTeamActivity.class);

                        AnalyticsHelper.getInstance().sendEvent(AnalyticsHelper.Category.login,
                                "register succ", "with phone");
                        AnalyticsHelper.getInstance().endTiming(AnalyticsHelper.Category.login, "register duration");
                    }
                }, new ApiErrorAction());
    }

    @Override
    public void resendVerifyCode(boolean isMobile) {
        String loginStr = etLogin.getText().toString().trim();
        String passwordStr = etPassword.getText().toString();
        sendVCodeToMobile(loginStr, passwordStr, captchaUid);
    }

    private void getStrikeToken() {
        TalkClient.getInstance().getTalkApi().getStrikerToken().observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<StrikerTokenResponseData>() {
                    @Override
                    public void call(StrikerTokenResponseData responseData) {
                        if (responseData != null) {
                            String strikeToken = responseData.getToken();
                            MainApp.PREF_UTIL.putString(Constant.STRIKER_TOKEN, strikeToken);
                            TalkClient.getInstance().setStrikerToken(strikeToken);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                    }
                });
    }
}
