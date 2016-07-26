package com.teambition.talk.ui;

import android.app.Activity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.CharacterStyle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.talk.dialog.TalkDialog;
import com.teambition.talk.Constant;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.adapter.CaptchaAdapter;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.data.ValidCaptchaResponseData;
import com.teambition.talk.entity.Captcha;
import com.teambition.talk.ui.span.HighlightSpan;

import java.util.Locale;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by nlmartian on 1/8/16.
 */
public class CaptchaDialogHelper {

    private Activity activity;
    private TalkDialog dialog;

    private TextView tvContent;
    private RecyclerView captchaList;
    private View progressBar;
    private CaptchaAdapter adapter;
    private OnValidSuccessListener validSuccessListener;
    private Captcha captcha;
    private boolean isFailed;

    public CaptchaDialogHelper(Activity activity, OnValidSuccessListener listener) {
        this.validSuccessListener = listener;
        this.activity = activity;
        final View customView = LayoutInflater.from(activity).inflate(R.layout.fragment_captcha_dialog, null);
        setupView(customView);
        dialog = new TalkDialog.Builder(activity)
                .title(R.string.title_captcha_dialog)
                .customView(customView, false)
                .positiveText(R.string.cancel)
                .negativeText(R.string.refresh)
                .autoDismiss(false)
                .callback(new TalkDialog.ButtonCallback() {
                    @Override
                    public void onPositive(TalkDialog dialog, View v) {
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegative(TalkDialog dialog) {
                        fetchCaptchas();
                    }
                })
                .build();
    }

    public void showDialog() {
        dialog.show();
        fetchCaptchas();
    }

    private void fetchCaptchas() {
        adapter.setCaptcha(null);
        progressBar.setVisibility(View.VISIBLE);
        String language = MainApp.PREF_UTIL.getString(Constant.LANGUAGE_PREF, null);
        if (language == null) {
            language = Locale.getDefault().getLanguage();
        }
        TalkClient.getInstance().getTbAuthApi()
                .setUpCaptcha(5, language)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Captcha>() {
                    @Override
                    public void call(Captcha captcha) {
                        captcha.setLang(TalkClient.getInstance().getLanguage());
                        CaptchaDialogHelper.this.captcha = captcha;
                        progressBar.setVisibility(View.GONE);
                        adapter.setCaptcha(captcha);
                        if (isFailed) {
                            String contentStr = activity.getString(R.string.content_captcha, captcha.getImageName())
                                    + activity.getString(R.string.valid_fail);
                            SpannableStringBuilder ssb = new SpannableStringBuilder(contentStr);
                            ssb.setSpan(CharacterStyle.wrap(new HighlightSpan(activity.getResources())),
                                    contentStr.length() - activity.getString(R.string.valid_fail).length(),
                                    contentStr.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                            tvContent.setText(ssb);
                        } else {
                            tvContent.setText(activity.getString(R.string.content_captcha, captcha.getImageName()));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void setupView(View view) {
        tvContent = (TextView) view.findViewById(R.id.content);
        captchaList = (RecyclerView) view.findViewById(R.id.captchas);
        progressBar = view.findViewById(R.id.progress_bar);
        captchaList.setLayoutManager(new GridLayoutManager(activity, 2));
        adapter = new CaptchaAdapter();
        captchaList.setAdapter(adapter);
        adapter.setOnItemClickListener(new CaptchaAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String uid, String value) {
                validCaptcha(uid, value);
            }
        });
    }

    private void validCaptcha(final String uid, String value) {
        TalkClient.getInstance().getTbAuthApi()
                .validCaptcha(uid, value)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ValidCaptchaResponseData>() {
                    @Override
                    public void call(ValidCaptchaResponseData data) {
                        if (data.isValid() && validSuccessListener != null) {
                            validSuccessListener.onValidSuccess(uid);
                        }
                        if (data.isValid()) {
                            dialog.dismiss();
                        } else {
                            isFailed = true;
                            String contentStr = activity.getString(R.string.content_captcha, captcha.getImageName())
                                    + activity.getString(R.string.valid_fail);
                            SpannableStringBuilder ssb = new SpannableStringBuilder(contentStr);
                            ssb.setSpan(CharacterStyle.wrap(new HighlightSpan(activity.getResources())),
                                    contentStr.length() - activity.getString(R.string.valid_fail).length(),
                                    contentStr.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                            tvContent.setText(ssb);
                            fetchCaptchas();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    public interface OnValidSuccessListener {
        public void onValidSuccess(String uid);
    }

}
