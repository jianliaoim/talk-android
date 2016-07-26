package com.teambition.talk.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.teambition.talk.R;
import com.teambition.talk.util.SourceEditor;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by wlanjie on 15/8/3.
 */
public class CodePreviewActivity extends BaseActivity {

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.title)
    TextView mTitleView;

    @InjectView(R.id.webView)
    WebView mWebView;

    public static Intent startIntent(Context context, String title, String code, String codeType) {
        Intent intent = new Intent(context, CodePreviewActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("code", code);
        intent.putExtra("codeType", codeType);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_code_preview);
        ButterKnife.inject(this);

        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.code);

        Intent intent = getIntent();
        final String title = intent.getStringExtra("title");
        final String code = intent.getStringExtra("code");
        final String codeType = intent.getStringExtra("codeType");
        if (TextUtils.isEmpty(title)) {
            mTitleView.setVisibility(View.GONE);
        } else {
            mTitleView.setText(title);
        }
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        SourceEditor editor = new SourceEditor(mWebView);
        editor.setMarkdown(false).setSource(codeType, code, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}
