/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.teambition.talk.util;

import android.os.Build;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;


/**
 * Utilities for displaying source code in a {@link WebView}
 */
public class SourceEditor {

    private static final String CHARSET_UTF8 = "UTF-8";

    private static final String BASE64 = "base64";

    private static final String URL_PAGE = "file:///android_asset/source-editor.html";

    private final WebView view;

    private boolean wrap;

    private String name;

    private String content;

    private boolean encoded;

    private boolean markdown;

    /**
     * Create source editor using given web view
     *
     * @param view
     */
    public SourceEditor(final WebView view) {
        WebViewClient client = new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (URL_PAGE.equals(url)) {
                    view.loadUrl(url);
                    return false;
                } else {
                    return true;
                }
            }
        };
        view.setWebViewClient(client);

        WebSettings settings = view.getSettings();
        settings.setJavaScriptEnabled(true);
        view.addJavascriptInterface(this, "SourceEditor");
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setBuiltInZoomControls(true);
            settings.setUseWideViewPort(true);
        }

        this.view = view;
    }

    /**
     * @return name
     */
    @JavascriptInterface
    public String getName() {
        return name;
    }

    /**
     * @return content
     */
    @JavascriptInterface
    public String getRawContent() {
        return content;
    }

    /**
     * @return content
     */
    @JavascriptInterface
    public String getContent() {
        if (encoded)
            try {
                return new String(android.util.Base64.decode(content, android.util.Base64.DEFAULT), CHARSET_UTF8);
            } catch (UnsupportedEncodingException e) {
                return getRawContent();
            }
        else
            return getRawContent();
    }

    /**
     * @return wrap
     */
    @JavascriptInterface
    public boolean getWrap() {
        return wrap;
    }

    /**
     * @return markdown
     */
    public boolean isMarkdown() {
        return markdown;
    }

    /**
     * Set whether lines should wrap
     *
     * @param wrap
     * @return this editor
     */
    public SourceEditor setWrap(final boolean wrap) {
        this.wrap = wrap;
        loadSource();
        return this;
    }

    /**
     * Sets whether the content is a markdown file
     *
     * @param markdown
     * @return this editor
     */
    public SourceEditor setMarkdown(final boolean markdown) {
        this.markdown = markdown;
        return this;
    }

    /**
     * Bind content to current {@link WebView}
     *
     * @param name
     * @param content
     * @param encoded
     * @return this editor
     */
    public SourceEditor setSource(String name, final String content, final boolean encoded) {
        this.name = name;
        this.content = content;
        this.encoded = encoded;
        loadSource();

        return this;
    }

    private void loadSource() {
        if (name != null && content != null)
            if (markdown)
                view.loadDataWithBaseURL(null, content, "text/html", CHARSET_UTF8, null);
            else
                view.loadUrl(URL_PAGE);
    }

    /**
     * Toggle line wrap
     *
     * @return this editor
     */
    public SourceEditor toggleWrap() {
        return setWrap(!wrap);
    }

    /**
     * Toggle markdown file rendering
     *
     * @return this editor
     */
    public SourceEditor toggleMarkdown() {
        return setMarkdown(!markdown);
    }
}
