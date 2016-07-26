package com.teambition.talk.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by zeatual on 15/1/20.
 */
public class KeyBoardLayout extends RelativeLayout {

    public KeyBoardLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyBoardLayout(Context context) {
        super(context);
    }

    private OnSoftKeyboardListener onSoftKeyboardListener;

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        if (onSoftKeyboardListener != null) {
            final int newSpec = View.MeasureSpec.getSize(heightMeasureSpec);
            final int oldSpec = getMeasuredHeight();
            // If layout became smaller, that means something forced it to resize. Probably soft keyboard :)
            if (oldSpec > newSpec) {
                onSoftKeyboardListener.onShown();
            } else {
                onSoftKeyboardListener.onHidden();
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public final void setOnSoftKeyboardListener(final OnSoftKeyboardListener listener) {
        this.onSoftKeyboardListener = listener;
    }

    // Simplest possible listener :)
    public interface OnSoftKeyboardListener {
        void onShown();

        void onHidden();
    }
}
