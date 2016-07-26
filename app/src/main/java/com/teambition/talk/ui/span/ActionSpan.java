package com.teambition.talk.ui.span;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;

/**
 * Created by zeatual on 15/7/22.
 */
public class ActionSpan extends ReplacementSpan {

    private String source;
    private String action;
    private String data;
    private String display;

    public ActionSpan(String source, String action, String data, String display) {
        this.source = source;
        this.action = action;
        this.data = data;
        this.display = display;
    }

    public String getSource() {
        return source;
    }

    public String getAction() {
        return action;
    }

    public String getData() {
        return data;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return (int) paint.measureText(display, 0, display.length());
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        canvas.drawText(display, x, y, paint);
    }
}
