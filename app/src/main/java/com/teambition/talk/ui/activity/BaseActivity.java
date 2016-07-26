package com.teambition.talk.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import com.teambition.talk.Constant;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.service.MessageService;
import com.teambition.talk.util.ClipboardHelper;
import com.teambition.talk.util.Logger;
import com.teambition.talk.view.BaseView;

import java.util.Locale;

/**
 * Created by zeatual on 14/10/27.
 */
public class BaseActivity extends AppCompatActivity implements BaseView {

    protected ProgressDialog progressDialog;

    protected View progressBar;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String language = MainApp.PREF_UTIL.getString(Constant.LANGUAGE_PREF, null);
        switchLanguage( language == null ? Locale.getDefault().getLanguage() : language);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
        setTheme(R.style.Theme_Talk_Ocean);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ClipboardHelper.detectInviteCode(this)) {
            finish();
            return;
        }

        // if activityCount is 1, means that app is coming foreground from background
        if (((MainApp) getApplicationContext()).getActivityCount() == 1) {
            Logger.d("Wakeup", this);
            startService(MessageService.readHistoryIntent(this));
        }
    }

    @Override
    public void showProgressDialog(int message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setMessage(getString(message));
        progressDialog.show();
    }

    @Override
    public void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private long showTime;

    @Override
    public void showProgressBar() {
        if (progressBar == null)
            return;
        progressBar.setVisibility(View.VISIBLE);
        showTime = System.currentTimeMillis();
    }

    @Override
    public void dismissProgressBar() {
        if (progressBar == null)
            return;
        if (System.currentTimeMillis() - showTime > 1500) {
            progressBar.setVisibility(View.GONE);
        } else {
            progressBar.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                }
            }, 1200);
        }

    }

    protected void switchLanguage(String language) {
        if (!"zh".equals(language) && !"en".equals(language)) {
            return;
        } else {
            MainApp.PREF_UTIL.putString(Constant.LANGUAGE_PREF, language);
        }
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();
        if (language.equals("en")) {
            config.locale = Locale.ENGLISH;
        } else {
            config.locale = Locale.CHINESE;
        }
        resources.updateConfiguration(config, dm);
    }


}
