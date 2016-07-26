package com.teambition.talk.ui.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.teambition.talk.util.Logger;

/**
 * Created by nlmartian on 1/28/16.
 * Workaround for a @see <a href="https://code.google.com/p/android/issues/detail?id=77846">bug</a>
 * in RecyclerView according to @see <a href="http://stackoverflow.com/a/33822747">stackoverflow</a>
 */
public class WrapContentLinearLayoutManager extends LinearLayoutManager {
    public static final String TAG = WrapContentLinearLayoutManager.class.getSimpleName();

    public WrapContentLinearLayoutManager(Context context) {
        super(context);
    }

    public WrapContentLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public WrapContentLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            Logger.e(TAG, "IOOBE in RecyclerView", e);
        }
    }
}
