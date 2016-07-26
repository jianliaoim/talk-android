package com.teambition.talk.ui.span;

import android.content.Intent;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.teambition.talk.ui.activity.WebContainerActivity;

/**
 * Created by zeatual on 15/2/27.
 */
public class AutoLinkMeSpan extends ClickableSpan {

    private final String mURL;

    public AutoLinkMeSpan(String mURL) {
        this.mURL = mURL;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(Color.WHITE);
        ds.setUnderlineText(true);
    }

    @Override
    public void onClick(View widget) {
        Intent intent = WebContainerActivity.newIntent(widget.getContext(), mURL, null);
        widget.getContext().startActivity(intent);
    }
}
