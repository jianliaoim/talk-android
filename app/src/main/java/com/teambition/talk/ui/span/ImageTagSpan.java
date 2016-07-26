package com.teambition.talk.ui.span;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;

import com.teambition.talk.MainApp;
import com.teambition.talk.R;

/**
 * for replace <img/> in TextView
 * Created by zeatual on 15/7/22.
 */
public class ImageTagSpan extends ReplacementSpan {

    String picture = MainApp.CONTEXT.getString(R.string.picture);

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return (int) paint.measureText(picture, 0, picture.length());
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        canvas.drawText(picture, x, y, paint);
    }
}
