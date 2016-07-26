package com.teambition.talk.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebView;

import com.teambition.talk.R;
import com.teambition.talk.util.StringUtil;
import com.umeng.analytics.MobclickAgent;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by zeatual on 15/2/26.
 */
public class RichContentActivity extends BaseActivity {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.webView)
    WebView webView;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rich_content);
        ButterKnife.inject(this);

        String data = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" +
                getIntent().getStringExtra("text");
        String title = getIntent().getStringExtra("title");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(StringUtil.isBlank(title) ? "" : title);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDefaultTextEncodingName("UTF -8");
        webView.loadDataWithBaseURL("file:///android_asset/", data, "text/html; charset=UTF-8", "UTF-8", null);
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

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
