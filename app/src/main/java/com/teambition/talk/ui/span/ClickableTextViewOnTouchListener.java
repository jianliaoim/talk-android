package com.teambition.talk.ui.span;

import android.os.Handler;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teambition.talk.util.Logger;

public class ClickableTextViewOnTouchListener implements View.OnTouchListener {

    boolean isLongClickPost = false;
    private boolean find = false;
    private ClickableSpan matchedSpan = null;
    private TextView widget;
    final Handler handler = new Handler();
    Runnable longClickRunnable = new Runnable() {
        public void run() {
            isLongClickPost = true;
            ((ViewGroup) widget.getParent()).performLongClick();
        }
    };

    public ClickableTextViewOnTouchListener(TextView widget) {
        this.widget = widget;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Layout layout = ((TextView) v).getLayout();

        if (layout == null) {
            return false;
        }

        int x = (int) event.getX();
        int y = (int) event.getY();

        int line = layout.getLineForVertical(y);
        int offset = layout.getOffsetForHorizontal(line, x);

        final TextView tv = (TextView) v;
        SpannableString value = SpannableString.valueOf(tv.getText());
        ClickableSpan[] urlSpans = value.getSpans(0, value.length(), ClickableSpan.class);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                for (ClickableSpan urlSpan : urlSpans) {
                    int start = value.getSpanStart(urlSpan);
                    int end = value.getSpanEnd(urlSpan);
                    if (start <= offset && offset <= end) {
                        matchedSpan = urlSpan;
                        Logger.d("DOWN", find);
                        find = true;
                        break;
                    }
                }
                float lineWidth = layout.getLineWidth(line);
                find &= (lineWidth >= x);
                if (find) {
                    handler.postDelayed(longClickRunnable, 1000);
                }
                return find;
            case MotionEvent.ACTION_UP:
                Logger.d("UP", find);
                if (find && !isLongClickPost) {
                    handler.removeCallbacks(longClickRunnable);
                    for (ClickableSpan urlSpan : urlSpans) {
                        int start = value.getSpanStart(urlSpan);
                        int end = value.getSpanEnd(urlSpan);
                        if (start <= offset && offset <= end) {
                            if (urlSpan.equals(matchedSpan)) {
                                urlSpan.onClick(widget);
                                find = false;
                            }
                            break;
                        }
                    }
                }
                isLongClickPost = false;
                return false;
            default:
                return false;
        }
    }
}
