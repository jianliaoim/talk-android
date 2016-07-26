package com.teambition.talk.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.talk.dialog.TalkDialog;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.data.ErrorResponseData;
import com.teambition.talk.client.data.RandomCodeData;
import com.teambition.talk.ui.activity.PickCountryCodeActivity;
import com.teambition.talk.ui.widget.CodeInputView;

import retrofit.RetrofitError;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zeatual on 15/9/15.
 */
public class VCodeDialogHelper {

    public static final int REQUEST_COUNTRY_CODE = 2048;
    private final static int RESTORATION_TIME = 1000;
    private static int TIMER = 60;

    private Activity context;
    private TalkDialog dialog;
    private VCodeDialogCallback callback;

    private String title;
    private CodeInputView codeInputView;
    private EditText phoneNumberEdit;
    private EditText emailAddressEdit;
    private TextView countryCodeView;
    private View lineView;
    private OnDismissListener onDismissListener;

    private String vCodeAction;

    private CaptchaDialogHelper captchaHelper;

    static Handler timerHandler = new Handler();
    static TimerRunnable timerRunnable = new TimerRunnable();

    public interface VCodeDialogCallback {
        void onPassThrough(String randomCode, String vCode);
    }

    public interface OnDismissListener {
        void onDismiss(TalkDialog dialog);
    }

    public void setOnDismissListener(OnDismissListener l) {
        onDismissListener = l;
    }

    public VCodeDialogHelper(Activity aty, String title, VCodeDialogCallback callback) {
        this(true, aty, title, callback);
    }

    public VCodeDialogHelper(boolean isPhone, Activity aty, String title, VCodeDialogCallback callback) {
        if (isPhone) {
            initForPhone(aty, title, callback);
        } else {
            initForEmail(aty, title, callback);
        }
    }

    private void initForEmail(Activity aty, String title, VCodeDialogCallback callback) {
        context = aty;
        this.title = title;
        this.callback = callback;
        final View customView = LayoutInflater.from(context).inflate(R.layout.dialog_email_login_custom_view, null);
        final View emailLayout = customView.findViewById(R.id.email_layout);
        emailAddressEdit = (EditText) customView.findViewById(R.id.email_address);
        lineView = customView.findViewById(R.id.line);
        lineView.setBackgroundResource(R.color.colorPrimary);
        codeInputView = (CodeInputView) customView.findViewById(R.id.code_input);
        codeInputView.setLineColor(R.color.colorPrimary);
        dialog = new TalkDialog.Builder(context)
                .title(title)
                .titleError(R.string.input_email_error)
                .titleColorRes(R.color.white)
                .contentError(R.string.input_email_error_content)
                .titleBackgroundColorRes(R.color.colorPrimary)
                .titleBackgroundErrorColorRes(R.color.talk_red)
                .content(R.string.login_tip)
                .autoDismiss(false)
                .contentColorRes(R.color.material_grey_700)
                .customView(customView, false)
                .negativeText(R.string.cancel)
                .negativeColorRes(R.color.material_grey_700)
                .positiveText(R.string.action_next)
                .positiveColorRes(R.color.colorPrimary)
                .showListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
                                .toggleSoftInputFromWindow(emailAddressEdit.getWindowToken(), 0, 0);
                    }
                })
                .callback(new TalkDialog.ButtonCallback() {
                    @Override
                    public void onPositive(final TalkDialog dialog, final View view) {
                        super.onPositive(dialog, view);
                        if (view instanceof TextView) {
                            final TextView textView = (TextView) view;
                            if (textView.getText().equals(context.getResources().getString(R.string.action_next))) {
                                final String emailAddress = emailAddressEdit.getText().toString().trim();
                                if (emailAddress.isEmpty()) {
                                    dialog.buildErrorResource();
                                    lineView.setBackgroundResource(R.color.talk_red);
                                    return;
                                }
                                if (emailAddress.endsWith("qq.com")) {
                                    new TalkDialog.Builder(context)
                                            .titleColorRes(R.color.white)
                                            .titleBackgroundColorRes(R.color.talk_red)
                                            .title(R.string.tips)
                                            .content(R.string.recommend_not_qqmail)
                                            .negativeText(R.string.modify)
                                            .positiveText(R.string.confirm)
                                            .callback(new TalkDialog.ButtonCallback() {
                                                @Override
                                                public void onPositive(TalkDialog dialog, View v) {
                                                    sendEmailRequest(emailAddress, emailLayout, textView, view);
                                                }
                                            })
                                            .build()
                                            .show();
                                } else {
                                    sendEmailRequest(emailAddress, emailLayout, textView, view);
                                }
                            } else if (textView.getText().equals(context.getResources().getString(R.string.resend))) {
                                view.setEnabled(false);
                                final String emailAddress = emailAddressEdit.getText().toString().trim();
                                TalkClient.getInstance().getAccountApi()
                                        .sendEmailVLink(emailAddress, vCodeAction)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Action1<RandomCodeData>() {
                                            @Override
                                            public void call(RandomCodeData randomCodeData) {
                                                timerRunnable.setTextView(textView);
                                                timerHandler.postDelayed(timerRunnable, RESTORATION_TIME);
                                                setInputViewListener(randomCodeData.getRandomCode());
                                            }
                                        }, new Action1<Throwable>() {
                                            @Override
                                            public void call(Throwable throwable) {
                                                view.setEnabled(true);
                                                MainApp.showToastMsg(R.string.network_failed);
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onNegative(TalkDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();
                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        timerHandler.removeCallbacks(timerRunnable);
                    }
                }).build();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface d) {
                if (onDismissListener != null) {
                    onDismissListener.onDismiss(dialog);
                }
                dialog.getBuilder()
                        .title(VCodeDialogHelper.this.title)
                        .contentError(R.string.input_phone_error_content)
                        .positiveColorRes(R.color.talk_blue)
                        .content(R.string.login_tip);
                timerHandler.removeCallbacks(timerRunnable);
                dialog.getPositiveButton().setEnabled(true);
                dialog.setPositiveText(R.string.action_next);
                emailLayout.setVisibility(View.VISIBLE);
                codeInputView.setLineColor(R.color.colorPrimary);
                codeInputView.setVisibility(View.GONE);
                dialog.buildResource();
                lineView.setBackgroundResource(R.color.colorPrimary);
                emailAddressEdit.setText("");
                codeInputView.setText("");
            }
        });
    }

    private void sendEmailRequest(final String emailAddress, final View emailLayout,
                                  final TextView textView, final View buttonView) {
        TalkClient.getInstance().getAccountApi().sendEmailVLink(emailAddress, vCodeAction)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RandomCodeData>() {
                    @Override
                    public void call(RandomCodeData randomCodeData) {
                        if (randomCodeData != null) {
                            emailLayout.setVisibility(View.GONE);
                            codeInputView.setVisibility(View.VISIBLE);
                            emailLayout.clearFocus();
                            codeInputView.requestFocus();

                            textView.setText(R.string.resend);
                            final String content = String.format(context.getResources().getString(R.string.send_code_to_phone), emailAddress);
                            dialog.getBuilder().title(R.string.input_v_code_title)
                                    .contentError(R.string.input_sms_code_error_content)
                                    .positiveColorRes(R.color.colorPrimary)
                                    .content(content);
                            dialog.buildResource();

                            timerRunnable.setTextView(textView);
                            timerHandler.postDelayed(timerRunnable, RESTORATION_TIME);
                            setInputViewListener(randomCodeData.getRandomCode());
                            buttonView.setEnabled(false);

                            setInputViewListener(randomCodeData.getRandomCode());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (throwable instanceof RetrofitError) {
                            try {
                                ErrorResponseData error = (ErrorResponseData) ((RetrofitError) throwable)
                                        .getBodyAs(ErrorResponseData.class);
                                if (error.code == 227) { // 发送过于频繁
                                    buildPhoneNumError(error.message);
                                } else {
                                    buildPhoneNumError(context.getString(R.string.input_email_error));
                                }
                            } catch (Exception e) {
                                MainApp.showToastMsg(R.string.network_failed);
                            }
                        }
                    }
                });
    }

    private void initForPhone(Activity aty, String title, VCodeDialogCallback callback) {
        context = aty;
        this.title = title;
        this.callback = callback;
        final View customView = LayoutInflater.from(context).inflate(R.layout.dialog_login_custom_view, null);
        final View phoneLayout = customView.findViewById(R.id.phone_layout);
        countryCodeView = (TextView) customView.findViewById(R.id.country_code);
        countryCodeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PickCountryCodeActivity.class);
                context.startActivityForResult(intent, REQUEST_COUNTRY_CODE);
                context.overridePendingTransition(R.anim.anim_fade_transition_in, 0);
            }
        });
        phoneNumberEdit = (EditText) customView.findViewById(R.id.phone_number);
        lineView = customView.findViewById(R.id.line);
        lineView.setBackgroundResource(R.color.colorPrimary);
        codeInputView = (CodeInputView) customView.findViewById(R.id.code_input);
        codeInputView.setLineColor(R.color.colorPrimary);
        dialog = new TalkDialog.Builder(context)
                .title(title)
                .titleError(R.string.input_phone_error)
                .titleColorRes(R.color.white)
                .contentError(R.string.input_phone_error_content)
                .titleBackgroundColorRes(R.color.colorPrimary)
                .titleBackgroundErrorColorRes(R.color.talk_red)
                .content(R.string.login_tip)
                .autoDismiss(false)
                .contentColorRes(R.color.material_grey_700)
                .customView(customView, false)
                .negativeText(R.string.cancel)
                .negativeColorRes(R.color.material_grey_700)
                .positiveText(R.string.action_next)
                .positiveColorRes(R.color.colorPrimary)
                .showListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
                                .toggleSoftInputFromWindow(phoneNumberEdit.getWindowToken(), 0, 0);
                    }
                })
                .callback(new TalkDialog.ButtonCallback() {
                    @Override
                    public void onPositive(final TalkDialog dialog, final View view) {
                        super.onPositive(dialog, view);
                        if (view instanceof TextView) {
                            final TextView textView = (TextView) view;
                            if (textView.getText().equals(context.getResources().getString(R.string.action_next))) {
                                final String phoneNumber = phoneNumberEdit.getText().toString().trim();
                                if (phoneNumber.isEmpty()) {
                                    dialog.buildErrorResource();
                                    lineView.setBackgroundResource(R.color.talk_red);
                                    return;
                                }
                                captchaHelper = new CaptchaDialogHelper(context, new CaptchaDialogHelper.OnValidSuccessListener() {
                                    @Override
                                    public void onValidSuccess(String uid) {
                                        TalkClient.getInstance().getAccountApi().sendVerifyCode(phoneNumber, uid)
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Action1<RandomCodeData>() {
                                                    @Override
                                                    public void call(RandomCodeData randomCodeData) {
                                                        if (randomCodeData != null) {
                                                            phoneLayout.setVisibility(View.GONE);
                                                            codeInputView.setVisibility(View.VISIBLE);
                                                            phoneLayout.clearFocus();
                                                            codeInputView.requestFocus();

                                                            textView.setText(R.string.resend);
                                                            final String countryCode = countryCodeView.getText().toString();
                                                            final String content = String.format(context.getResources().getString(R.string.send_code_to_phone), countryCode + " " + phoneNumber);
                                                            dialog.getBuilder().title(R.string.input_v_code_title)
                                                                    .contentError(R.string.input_sms_code_error_content)
                                                                    .positiveColorRes(R.color.colorPrimary)
                                                                    .content(content);
                                                            dialog.buildResource();

                                                            timerRunnable.setTextView(textView);
                                                            timerHandler.postDelayed(timerRunnable, RESTORATION_TIME);
                                                            setInputViewListener(randomCodeData.getRandomCode());
                                                            view.setEnabled(false);

                                                            setInputViewListener(randomCodeData.getRandomCode());
                                                        }
                                                    }
                                                }, new Action1<Throwable>() {
                                                    @Override
                                                    public void call(Throwable throwable) {
                                                        if (throwable instanceof RetrofitError) {
                                                            try {
                                                                ErrorResponseData error = (ErrorResponseData) ((RetrofitError) throwable)
                                                                        .getBodyAs(ErrorResponseData.class);
                                                                if (error.code == 227) { // 发送过于频繁
                                                                    buildPhoneNumError(error.message);
                                                                } else {
                                                                    buildPhoneNumError(context.getString(R.string.input_phone_error));
                                                                }
                                                            } catch (Exception e) {
                                                                MainApp.showToastMsg(R.string.network_failed);
                                                            }
                                                        }
                                                    }
                                                });
                                    }
                                });
                                captchaHelper.showDialog();
                            } else if (textView.getText().equals(context.getResources().getString(R.string.resend))) {
                                view.setEnabled(false);
                                final String phoneNumber = phoneNumberEdit.getText().toString().trim();
                                TalkClient.getInstance().getAccountApi()
                                        .sendVerifyCode(phoneNumber)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Action1<RandomCodeData>() {
                                            @Override
                                            public void call(RandomCodeData randomCodeData) {
                                                timerRunnable.setTextView(textView);
                                                timerHandler.postDelayed(timerRunnable, RESTORATION_TIME);
                                                setInputViewListener(randomCodeData.getRandomCode());
                                            }
                                        }, new Action1<Throwable>() {
                                            @Override
                                            public void call(Throwable throwable) {
                                                view.setEnabled(true);
                                                MainApp.showToastMsg(R.string.network_failed);
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onNegative(TalkDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();
                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        timerHandler.removeCallbacks(timerRunnable);
                    }
                }).build();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface d) {
                if (onDismissListener != null) {
                    onDismissListener.onDismiss(dialog);
                }
                dialog.getBuilder()
                        .title(VCodeDialogHelper.this.title)
                        .contentError(R.string.input_phone_error_content)
                        .positiveColorRes(R.color.talk_blue)
                        .content(R.string.login_tip);
                timerHandler.removeCallbacks(timerRunnable);
                dialog.getPositiveButton().setEnabled(true);
                dialog.setPositiveText(R.string.action_next);
                phoneLayout.setVisibility(View.VISIBLE);
                codeInputView.setLineColor(R.color.colorPrimary);
                codeInputView.setVisibility(View.GONE);
                dialog.buildResource();
                lineView.setBackgroundResource(R.color.colorPrimary);
                phoneNumberEdit.setText("");
                codeInputView.setText("");
            }
        });
    }

    private TextWatcher phoneNumWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            phoneNumberEdit.removeTextChangedListener(phoneNumWatcher);
            dialog.buildResource();
            if (lineView != null) {
                lineView.setBackgroundResource(R.color.colorPrimary);
            }
        }
    };

    private CodeInputView.OnDeleteKeyListener codeDeleteListener = new CodeInputView.OnDeleteKeyListener() {
        @Override
        public void onDeleteKey(int length) {
            if (length <= codeInputView.getCodeSize() && dialog.getContentView() != null
                    && dialog.getContentView().getText().toString()
                    .equals(context.getString(R.string.input_sms_code_error_content))) {
                codeInputView.setOnDeleteKeyListener(null);
                codeInputView.setLineColor(R.color.colorPrimary);
                dialog.buildResource();
            }
        }
    };

    void setInputViewListener(final String randomCode) {
        codeInputView.setTextChangeListener(new CodeInputView.TextChangeListener() {
            @Override
            public void onTextChange(String s) {
                if (s.length() == codeInputView.getCodeSize()) {
                    callback.onPassThrough(randomCode, s);
                }
            }
        });
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public void setTitle(String title) {
        this.title = title;
        dialog.setTitle(title);
    }

    public String getvCodeAction() {
        return vCodeAction;
    }

    public void setvCodeAction(String vCodeAction) {
        this.vCodeAction = vCodeAction;
    }

    public void setCountryCode(String code) {
        if (countryCodeView != null) {
            countryCodeView.setText("+ " + String.valueOf(code));
        }
    }

    public void buildPhoneNumError(String title) {
        dialog.getBuilder().titleError(title);
        dialog.buildErrorResource();
        lineView.setBackgroundResource(R.color.talk_red);
        phoneNumberEdit.addTextChangedListener(phoneNumWatcher);
    }

    public void buildVCodeError(String title) {
        dialog.getBuilder().titleError(title);
        dialog.buildErrorResource();
        codeInputView.setLineColor(R.color.talk_red);
        codeInputView.setOnDeleteKeyListener(codeDeleteListener);
    }

    public void buildContentRes(int contentRes) {
        if (dialog != null && dialog.getContentView() != null) {
            dialog.getContentView().setText(contentRes);
        }
    }

    static class TimerRunnable implements Runnable {

        TextView textView;

        @Override
        public void run() {
            if (textView == null) return;
            TIMER--;
            if (TIMER == 0) {
                TIMER = 60;
                textView.setEnabled(true);
                textView.setText(R.string.resend);
                textView.setTextColor(textView.getResources().getColor(R.color.colorPrimary));
                timerHandler.removeCallbacks(this);
            } else {
                textView.setEnabled(false);
                textView.setText(String.format(textView.getResources().getString(R.string.resend_countdown), TIMER));
                textView.setTextColor(textView.getResources().getColor(R.color.material_grey_500));
                timerHandler.postDelayed(this, RESTORATION_TIME);
            }
        }

        public void setTextView(TextView textView) {
            this.textView = textView;
        }
    }
}
