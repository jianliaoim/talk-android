package com.teambition.talk.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.rx.ApiErrorAction;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.apis.AccountApi;
import com.teambition.talk.client.data.ErrorResponseData;
import com.teambition.talk.client.data.RandomCodeData;
import com.teambition.talk.entity.User;
import com.teambition.talk.ui.CaptchaDialogHelper;
import com.teambition.talk.ui.fragment.ResetPasswordFragment;
import com.teambition.talk.ui.fragment.VerificationCodeFragment;
import com.teambition.talk.util.DensityUtil;
import com.teambition.talk.util.StringUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.RetrofitError;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by nlmartian on 11/4/15.
 */
public class FindPasswordActivity extends BaseActivity implements
        VerificationCodeFragment.OnFragmentInteractionListener,
        ResetPasswordFragment.OnFragmentInteractionListener {

    @InjectView(R.id.et_login)
    EditText etLogin;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.country_code)
    TextView tvCountryCode;
    @InjectView(R.id.tv_auth_type)
    TextView tvAuthType;

    private int authType = LoginActivity.AUTH_TYPE_EMAIL;
    private CaptchaDialogHelper captchaDialogHelper;
    private String captchaUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_password);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.find_password);
        captchaDialogHelper = new CaptchaDialogHelper(this, new CaptchaDialogHelper.OnValidSuccessListener() {
            @Override
            public void onValidSuccess(String uid) {
                captchaUid = uid;
                String loginStr = etLogin.getText().toString().trim();
                sendVCodeToMobile(loginStr, uid);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick({R.id.btn_next, R.id.tv_auth_type, R.id.country_code})
    public void onClick(View view) {
        if (view.getId() == R.id.btn_next) {
            String loginStr = etLogin.getText().toString().trim();
            if (StringUtil.isEmail(loginStr)) {
                sendVLinkToEmail(loginStr);
            } else if (StringUtil.isNumber(loginStr)) {
                captchaDialogHelper.showDialog();
            } else {
                etLogin.setError(getString(R.string.login_error));
                return;
            }
        } else if (view.getId() == R.id.tv_auth_type) {
            etLogin.setText(null);
            if (authType == LoginActivity.AUTH_TYPE_EMAIL) {
                authType = LoginActivity.AUTH_TYPE_MOBILE;
                etLogin.setHint(R.string.mobile_login);
                tvCountryCode.setVisibility(View.VISIBLE);
                etLogin.setPadding(DensityUtil.dip2px(this, 50), etLogin.getPaddingTop(),
                        etLogin.getPaddingRight(), etLogin.getPaddingBottom());
                etLogin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                tvAuthType.setText(R.string.email_find_pwd);
            } else {
                authType = LoginActivity.AUTH_TYPE_EMAIL;
                etLogin.setHint(R.string.email_login);
                tvCountryCode.setVisibility(View.GONE);
                etLogin.setPadding(etLogin.getPaddingRight(), etLogin.getPaddingTop(),
                        etLogin.getPaddingRight(), etLogin.getPaddingBottom());
                etLogin.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                tvAuthType.setText(R.string.mobile_find_pwd);
            }
        } else if (view.getId() == R.id.country_code) {
            Intent intent = new Intent(this, PickCountryCodeActivity.class);
            startActivityForResult(intent, LoginActivity.REQUEST_COUNTRY_CODE);
            overridePendingTransition(R.anim.anim_fade_transition_in, 0);
        }
    }

    @Override
    public void onGetVerifyCode(boolean isMobile, String randomCode, String vCode) {
        Observable<User> observable = null;
        AccountApi api =  TalkClient.getInstance().getAccountApi();
        if (isMobile) {
            observable = api.signInByVerifyCodeWithMobile(randomCode, vCode, "resetpassword");
        } else {
            observable = api.signInByVerifyCodeWithEmail(randomCode, vCode, "resetpassword");
        }
        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        RegisterActivity.initUserData(FindPasswordActivity.this, user);
                        showRestPasswordFragment();
                    }
                }, new ApiErrorAction());
    }

    @Override
    public void resendVerifyCode(boolean isMobile) {
        String loginStr = etLogin.getText().toString().trim();
        if (isMobile) {
            sendVCodeToMobile(loginStr, captchaUid);
        } else {
            sendVLinkToEmail(loginStr);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private void showRestPasswordFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container,
                        ResetPasswordFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    private void showVCodeFragment(String randomCode, boolean isMobile) {
        if (getSupportFragmentManager().findFragmentByTag(VerificationCodeFragment.TAG) == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container,
                            VerificationCodeFragment.newInstance(randomCode, isMobile))
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void sendVLinkToEmail(String emailAddress) {
        TalkClient.getInstance().getAccountApi()
                .sendEmailVLink(emailAddress, "resetpassword")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RandomCodeData>() {
                    @Override
                    public void call(RandomCodeData randomCodeData) {
                        showVCodeFragment(randomCodeData.getRandomCode(), false);
                        Toast.makeText(FindPasswordActivity.this, R.string.check_password_reset_email,
                                Toast.LENGTH_SHORT).show();
                    }
                }, new ApiErrorAction());
    }

    private void sendVCodeToMobile(final String phoneNumber, String uid) {
        TalkClient.getInstance().getAccountApi()
                .sendVerifyCode(phoneNumber, "resetpassword", uid)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RandomCodeData>() {
                    @Override
                    public void call(RandomCodeData randomCodeData) {
                        showVCodeFragment(randomCodeData.getRandomCode(), true);
                        String msg = getString(R.string.send_code_to_phone, phoneNumber);
                        Toast.makeText(FindPasswordActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (throwable instanceof RetrofitError) {
                            try {
                                ErrorResponseData error = (ErrorResponseData) ((RetrofitError) throwable)
                                        .getBodyAs(ErrorResponseData.class);
                                if (error.code == 227) { // 发送过于频繁
                                    Toast.makeText(FindPasswordActivity.this, error.message, Toast.LENGTH_SHORT).show();
                                } else if (error.code == 233) {
                                    etLogin.setError(getString(R.string.account_exist_error));
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
}
