package com.teambition.talk.ui.span;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.style.LeadingMarginSpan;

import com.teambition.talk.MainApp;
import com.teambition.talk.util.DensityUtil;

/**
 * Created by zeatual on 15/8/27.
 */
public class QuoteSpan implements LeadingMarginSpan {

    private int mColor;
    private float width;

    public QuoteSpan() {
        mColor = Color.parseColor("#E6E6E6");
        width = DensityUtil.dip2px(MainApp.CONTEXT, 3);
    }

    @Override
    public int getLeadingMargin(boolean first) {
        return (int) width * 4;
    }

    @Override
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top,
                                  int baseline, int bottom, CharSequence text,
                                  int start, int end, boolean first, Layout layout) {
        Paint.Style style = p.getStyle();
        int color = p.getColor();

        p.setStyle(Paint.Style.FILL);
        p.setColor(mColor);

        c.drawRect(x, top, x + dir * width, bottom, p);

        p.setStyle(style);
        p.setColor(color);
    }
}
