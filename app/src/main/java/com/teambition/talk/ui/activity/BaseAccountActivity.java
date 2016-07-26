package com.teambition.talk.ui.activity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import com.teambition.talk.R;

/**
 * Created by zeatual on 15/9/7.
 */
public class BaseAccountActivity extends BaseActivity {

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
        setTheme(R.style.Theme_Talk_Account);
    }

}
