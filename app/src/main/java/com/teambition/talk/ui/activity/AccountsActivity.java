package com.teambition.talk.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.talk.dialog.TalkDialog;
import com.teambition.talk.BizLogic;
import com.teambition.talk.R;
import com.teambition.talk.entity.CountryModel;
import com.teambition.talk.entity.User;
import com.teambition.talk.presenter.AccountPresenter;
import com.teambition.talk.presenter.UserPresenter;
import com.teambition.talk.ui.VCodeDialogHelper;
import com.teambition.talk.util.ThemeUtil;
import com.teambition.talk.util.TransactionUtil;
import com.teambition.talk.view.AccountView;
import com.teambition.talk.view.UserView;

import org.parceler.Parcels;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by zeatual on 15/6/18.
 */
public class AccountsActivity extends BaseActivity implements AccountView, UserView {

    public static final int REQUEST_TEAMBITION_CODE = 0;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.tv_account_phone)
    TextView tvAccountPhone;
    @InjectView(R.id.tv_account_email)
    TextView tvAccountEmail;
    @InjectView(R.id.tv_account_teambition)
    TextView tvAccountTb;
    @InjectView(R.id.tv_status_phone)
    TextView tvStatusPhone;
    @InjectView(R.id.tv_status_email)
    TextView tvStatusEmail;
    @InjectView(R.id.tv_status_teambition)
    TextView tvStatusTb;

    private AccountPresenter presenter;
    private UserPresenter userPresenter;

    private VCodeDialogHelper vCodeHelper;
    private boolean hasPhoneNum;
    private boolean hasEmailAddr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);
        ButterKnife.inject(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.preference_linked_accounts);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        presenter = new AccountPresenter(this);
        userPresenter = new UserPresenter(this);
        userPresenter.getUser();


    }

    private void initView(User user) {
        if (user.getMobileAccount() != null) {
            hasPhoneNum = true;
            tvAccountPhone.setText(user.getMobileAccount().getShowName());
            setMobileListener(true);
        } else {
            setMobileListener(false);
        }
        if (user.getEmailAccount() != null) {
            hasEmailAddr = true;
            tvAccountEmail.setText(user.getEmailAccount().getShowName());
            setEmailListener(true);
        } else {
            setEmailListener(false);
        }
        if (user.getTeambitionAccount() != null) {
            tvAccountTb.setText(user.getTeambitionAccount().getShowName());
            if (hasPhoneNum) {
                setTbListener(true);
            }
        } else {
            setTbListener(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_TEAMBITION_CODE:
                    String code = data.getStringExtra(UnionsActivity.CODE);
                    presenter.bindTeambition(code);
                    break;
                case VCodeDialogHelper.REQUEST_COUNTRY_CODE:
                    if (data != null) {
                        CountryModel model = Parcels.unwrap(data.getParcelableExtra(PickCountryCodeActivity.COUNTRY_CODE_DATA));
                        vCodeHelper.setCountryCode("+ " + String.valueOf(model.callingCode));
                    }
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

    void showBindSuccessDialog(String content) {
        new TalkDialog.Builder(this)
                .title(R.string.bind_success)
                .titleColorRes(R.color.white)
                .titleBackgroundColorRes(R.color.talk_grass)
                .backgroundColorRes(R.color.white)
                .content(content)
                .positiveText(R.string.action_done)
                .positiveColorRes(R.color.talk_grass)
                .show();
    }

    @Override
    public void onBindTeambition(User user) {
        tvAccountTb.setText(user.getShowname());
        setTbListener(true);
        showBindSuccessDialog(getString(R.string.bind_teambition_success));
    }

    @Override
    public void onUnbindTeambition(User user) {
        tvAccountTb.setText(R.string.account_empty);
        setTbListener(false);
    }

    @Override
    public void onBindPhone(User user) {
        vCodeHelper.dismiss();
        tvAccountPhone.setText(user.getPhoneNumber());
        vCodeHelper.setTitle(getString(R.string.change_mobile));
        setMobileListener(true);
        showBindSuccessDialog(getString(R.string.bind_mobile_success));
    }

    @Override
    public void onBindPhoneFailed(String error) {
        vCodeHelper.buildVCodeError(error);
    }

    @Override
    public void onBindEmail(User user) {
        vCodeHelper.dismiss();
        tvAccountEmail.setText(user.getEmailAddress());
        vCodeHelper.setTitle(getString(R.string.change_email));
        setEmailListener(true);
        showBindSuccessDialog(getString(R.string.bind_email_success));
    }

    @Override
    public void onBindEmailFailed(String error) {
        vCodeHelper.buildVCodeError(error);
    }

    @Override
    public void onBindEmailConflict(String account, final String bindCode) {
        vCodeHelper.dismiss();
        new TalkDialog.Builder(AccountsActivity.this)
                .title(R.string.delete_origin_account)
                .titleColorRes(R.color.white)
                .titleBackgroundColorRes(R.color.talk_warning)
                .backgroundColorRes(R.color.white)
                .content(String.format(getString(R.string.delete_origin_account_content), account))
                .negativeText(R.string.cancel)
                .negativeColorRes(R.color.material_grey_700)
                .positiveText(R.string.confirm)
                .positiveColorRes(R.color.talk_warning)
                .callback(new TalkDialog.ButtonCallback() {
                    @Override
                    public void onPositive(TalkDialog dialog, View v) {
                        presenter.forceBindEmail(bindCode);
                    }
                }).show();
    }

    @Override
    public void onPhoneConflict(String account, final String bindCode) {
        vCodeHelper.dismiss();
        new TalkDialog.Builder(AccountsActivity.this)
                .title(R.string.delete_origin_account)
                .titleColorRes(R.color.white)
                .titleBackgroundColorRes(R.color.talk_warning)
                .backgroundColorRes(R.color.white)
                .content(String.format(getString(R.string.delete_origin_account_content), account))
                .negativeText(R.string.cancel)
                .negativeColorRes(R.color.material_grey_700)
                .positiveText(R.string.confirm)
                .positiveColorRes(R.color.talk_warning)
                .callback(new TalkDialog.ButtonCallback() {
                    @Override
                    public void onPositive(TalkDialog dialog, View v) {
                        presenter.forceBindPhone(bindCode);
                    }
                }).show();
    }

    @Override
    public void onTeambitionConflict(String account, final String bindCode) {
        new TalkDialog.Builder(AccountsActivity.this)
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
                        presenter.forceBindTeambition(bindCode);
                    }
                }).show();
    }

    @Override
    public void onLoadUserFinish(User user) {
        initView(user);
    }

    void setEmailListener(final boolean hasBind) {
        tvStatusEmail.setText(getString(hasBind ? R.string.account_change : R.string.account_bind));
        tvStatusEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vCodeHelper = new VCodeDialogHelper(false, AccountsActivity.this, getString(R.string.bind_mobile),
                        new VCodeDialogHelper.VCodeDialogCallback() {
                            @Override
                            public void onPassThrough(String randomCode, String vCode) {
                                if (hasPhoneNum) {
                                    presenter.changeEmail(randomCode, vCode);
                                } else {
                                    presenter.bindEmail(randomCode, vCode);
                                }
                            }
                        });
                vCodeHelper.setTitle(hasBind ? getString(R.string.change_email) : getString(R.string.bind_email));
                vCodeHelper.setvCodeAction(hasBind ? "change" : "bind");
                vCodeHelper.buildContentRes(R.string.change_email_binding_tip);
                vCodeHelper.show();
            }
        });
    }

    void setMobileListener(final boolean hasBind) {
        tvStatusPhone.setText(getString(hasBind ? R.string.account_change : R.string.account_bind));
        tvStatusPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vCodeHelper = new VCodeDialogHelper(AccountsActivity.this, getString(R.string.bind_mobile),
                        new VCodeDialogHelper.VCodeDialogCallback() {
                            @Override
                            public void onPassThrough(String randomCode, String vCode) {
                                if (hasPhoneNum) {
                                    presenter.changePhone(randomCode, vCode);
                                } else {
                                    presenter.bindPhone(randomCode, vCode);
                                }
                            }
                        });
                vCodeHelper.setTitle(hasBind ? getString(R.string.change_mobile) : getString(R.string.bind_mobile));
                vCodeHelper.buildContentRes(R.string.change_binding_tip);
                vCodeHelper.show();
            }
        });
    }

    void setTbListener(final boolean hasBind) {
        tvStatusTb.setText(getString(hasBind ? R.string.account_unbind : R.string.account_bind));
        tvStatusTb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasBind) {
                    new TalkDialog.Builder(AccountsActivity.this)
                            .title(R.string.unbind_teambition_account)
                            .titleColorRes(R.color.white)
                            .titleBackgroundColorRes(R.color.colorPrimary)
                            .backgroundColorRes(R.color.white)
                            .content(R.string.unbind_teambition_account_content)
                            .negativeText(R.string.cancel)
                            .negativeColorRes(R.color.material_grey_700)
                            .positiveText(R.string.unbind)
                            .callback(new TalkDialog.ButtonCallback() {
                                @Override
                                public void onPositive(TalkDialog dialog, View v) {
                                    presenter.unbindTeambition();
                                }
                            })
                            .show();
                } else {
                    TransactionUtil.goToForResult(AccountsActivity.this,
                            UnionsActivity.class, REQUEST_TEAMBITION_CODE);
                }
            }
        });
    }
}
