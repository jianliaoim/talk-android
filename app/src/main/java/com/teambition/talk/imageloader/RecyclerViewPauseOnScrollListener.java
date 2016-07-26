package com.teambition.talk.imageloader;

import android.support.v7.widget.RecyclerView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.teambition.talk.MainApp;

/**
 * Created by wlanjie on 15/8/25.
 */
public class RecyclerViewPauseOnScrollListener extends RecyclerView.OnScrollListener {

    final RecyclerView.OnScrollListener mListener;
    final boolean mPauseOnScroll;
    final boolean mPauseOnSettling;
    final ImageLoader mImageLoader;

    public RecyclerViewPauseOnScrollListener() {
        this(null);
    }

    public RecyclerViewPauseOnScrollListener(RecyclerView.OnScrollListener l) {
        mListener = l;
        mPauseOnSettling = true;
        mPauseOnScroll = true;
        mImageLoader = MainApp.IMAGE_LOADER;
    }

    public RecyclerViewPauseOnScrollListener(ImageLoader imageLoader, boolean pauseOnScroll, boolean pauseOnSettling, RecyclerView.OnScrollListener l) {
        mImageLoader = imageLoader;
        mPauseOnScroll = pauseOnScroll;
        mPauseOnSettling = pauseOnSettling;
        mListener = l;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        switch (newState) {
            case RecyclerView.SCROLL_STATE_IDLE:
                mImageLoader.resume();
                break;
            case RecyclerView.SCROLL_STATE_DRAGGING:
                if (mPauseOnScroll) {
                    mImageLoader.pause();
                }
                break;
            case RecyclerView.SCROLL_STATE_SETTLING:
                if (mPauseOnSettling) {
                    mImageLoader.pause();
                }
                break;
        }

        if (mListener != null) {
            mListener.onScrollStateChanged(recyclerView, newState);
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (mListener != null) {
            mListener.onScrolled(recyclerView, dx, dy);
        }
    }
}
