package com.teambition.talk.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.UrlQuerySanitizer;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.teambition.talk.R;
import com.teambition.talk.client.ApiConfig;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by zeatual on 15/9/7.
 */
public class UnionsActivity extends BaseActivity {

    public static final String CODE = "code";

    @InjectView(R.id.webView)
    WebView webView;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    private static String url = ApiConfig.AUTHOR_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_view);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initWebView();
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

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith(ApiConfig.REDIRECT_URI)) {
                    List<UrlQuerySanitizer.ParameterValuePair> pvpList = new UrlQuerySanitizer(url)
                            .getParameterList();
                    for (UrlQuerySanitizer.ParameterValuePair pvp : pvpList) {
                        if ("code".equals(pvp.mParameter)) {
                            Intent data = new Intent();
                            data.putExtra(CODE, pvp.mValue);
                            setResult(RESULT_OK, data);
                            finish();
                            break;
                        }
                    }
                } else {
                    webView.loadUrl(url);
                }
                return true;
            }
        });

        webView.loadUrl(url);
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.anim_fade_transition_out);
    }
}
