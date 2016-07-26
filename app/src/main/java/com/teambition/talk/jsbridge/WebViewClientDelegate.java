package com.teambition.talk.jsbridge;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

public interface WebViewClientDelegate {

		public boolean shouldOverrideUrlLoading(WebView view, String url);

		public void onPageStarted(WebView view, String url, Bitmap favicon);

		public void onPageFinished(WebView view, String url);

		public void onLoadResource(WebView view, String url);

		public WebResourceResponse shouldInterceptRequest(WebView view, String url);

		public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request);

		public void onTooManyRedirects(WebView view, android.os.Message cancelMsg, android.os.Message continueMsg);

		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl);

		public void onFormResubmission(WebView view, android.os.Message dontResend, android.os.Message resend);

		public void doUpdateVisitedHistory(WebView view, String url, boolean isReload);

		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error);

		public void onReceivedClientCertRequest(WebView view, ClientCertRequest request);

		public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm);

		public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event);

		public void onUnhandledKeyEvent(WebView view, KeyEvent event);

		public void onUnhandledInputEvent(WebView view, InputEvent event);

		public void onScaleChanged(WebView view, float oldScale, float newScale);

		public void onReceivedLoginRequest(WebView view, String realm, String account, String args);
}