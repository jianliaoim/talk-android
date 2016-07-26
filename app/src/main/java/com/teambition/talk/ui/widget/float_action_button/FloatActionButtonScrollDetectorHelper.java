package com.teambition.talk.ui.widget.float_action_button;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.ListView;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.teambition.talk.MainApp;
import com.teambition.talk.util.DensityUtil;

/**
 * Created by zeatual on 15/3/2.
 */
public class FloatActionButtonScrollDetectorHelper {

    private static final int TRANSLATE_DURATION_MILLIS = 200;

    private View floatActionButton;
    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    private boolean mVisible = true;

    public FloatActionButtonScrollDetectorHelper(View floatActionButton, ListView listView) {
        this.floatActionButton = floatActionButton;
        attachToListView(listView, null, null);
    }

    public FloatActionButtonScrollDetectorHelper(View floatActionButton, RecyclerView recyclerView) {
        this.floatActionButton = floatActionButton;
        attachToRecyclerView(recyclerView, null, null);
    }

    public void show() {
        show(true);
    }

    public void hide() {
        hide(true);
    }

    public void show(boolean animate) {
        toggle(true, animate, false);
    }

    public void hide(boolean animate) {
        toggle(false, animate, false);
    }

    public void attachToListView(@NonNull AbsListView listView,
                                 ScrollDirectionListener scrollDirectionListener,
                                 AbsListView.OnScrollListener onScrollListener) {
        AbsListViewScrollDetectorImpl scrollDetector = new AbsListViewScrollDetectorImpl();
        scrollDetector.setScrollDirectionListener(scrollDirectionListener);
        scrollDetector.setOnScrollListener(onScrollListener);
        scrollDetector.setListView(listView);
        scrollDetector.setScrollThreshold(DensityUtil.dip2px(MainApp.CONTEXT, 4));
        listView.setOnScrollListener(scrollDetector);
    }

    public void attachToRecyclerView(@NonNull RecyclerView recyclerView,
                                     ScrollDirectionListener scrollDirectionlistener,
                                     RecyclerView.OnScrollListener onScrollListener) {
        RecyclerViewScrollDetectorImpl scrollDetector = new RecyclerViewScrollDetectorImpl();
        scrollDetector.setScrollDirectionListener(scrollDirectionlistener);
        scrollDetector.setOnScrollListener(onScrollListener);
        scrollDetector.setScrollThreshold(DensityUtil.dip2px(MainApp.CONTEXT, 4));
        recyclerView.setOnScrollListener(scrollDetector);
    }

    private void toggle(final boolean visible, final boolean animate, boolean force) {
        if (mVisible != visible || force) {
            mVisible = visible;
            int height = floatActionButton.getHeight();
            if (height == 0 && !force) {
                ViewTreeObserver vto = floatActionButton.getViewTreeObserver();
                if (vto.isAlive()) {
                    vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            ViewTreeObserver currentVto = floatActionButton.getViewTreeObserver();
                            if (currentVto.isAlive()) {
                                currentVto.removeOnPreDrawListener(this);
                            }
                            toggle(visible, animate, true);
                            return true;
                        }
                    });
                    return;
                }
            }
            int translationY = visible ? 0 : height + getMarginBottom();
            if (animate) {
                ViewPropertyAnimator.animate(floatActionButton).setInterpolator(mInterpolator)
                        .setDuration(TRANSLATE_DURATION_MILLIS)
                        .translationY(translationY);
            } else {
                ViewHelper.setTranslationY(floatActionButton, translationY);
            }
        }
    }

    private int getMarginBottom() {
        int marginBottom = 0;
        final ViewGroup.LayoutParams layoutParams = floatActionButton.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
        }
        return marginBottom;
    }

    private class AbsListViewScrollDetectorImpl extends AbsListViewScrollDetector {
        private ScrollDirectionListener mScrollDirectionListener;
        private AbsListView.OnScrollListener mOnScrollListener;

        private void setScrollDirectionListener(ScrollDirectionListener scrollDirectionListener) {
            mScrollDirectionListener = scrollDirectionListener;
        }

        public void setOnScrollListener(AbsListView.OnScrollListener onScrollListener) {
            mOnScrollListener = onScrollListener;
        }

        @Override
        public void onScrollDown() {
            show();
            if (mScrollDirectionListener != null) {
                mScrollDirectionListener.onScrollDown();
            }
        }

        @Override
        public void onScrollUp() {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    if (floatActionButton instanceof FloatingActionsMenu) {
                        if (((FloatingActionsMenu) floatActionButton).isExpanded()) {
                            ((FloatingActionsMenu) floatActionButton).collapse();
                        }
                    }
                }
            });
            hide();
            if (mScrollDirectionListener != null) {
                mScrollDirectionListener.onScrollUp();
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                             int totalItemCount) {
            if (mOnScrollListener != null) {
                mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }

            super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (mOnScrollListener != null) {
                mOnScrollListener.onScrollStateChanged(view, scrollState);
            }

            super.onScrollStateChanged(view, scrollState);
        }
    }

    private class RecyclerViewScrollDetectorImpl extends RecyclerViewScrollDetector {
        private ScrollDirectionListener mScrollDirectionListener;
        private RecyclerView.OnScrollListener mOnScrollListener;

        private void setScrollDirectionListener(ScrollDirectionListener scrollDirectionListener) {
            mScrollDirectionListener = scrollDirectionListener;
        }

        public void setOnScrollListener(RecyclerView.OnScrollListener onScrollListener) {
            mOnScrollListener = onScrollListener;
        }

        @Override
        public void onScrollDown() {
            show();
            if (mScrollDirectionListener != null) {
                mScrollDirectionListener.onScrollDown();
            }
        }

        @Override
        public void onScrollUp() {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    if (floatActionButton instanceof FloatingActionsMenu) {
                        if (((FloatingActionsMenu) floatActionButton).isExpanded()) {
                            ((FloatingActionsMenu) floatActionButton).collapse();
                        }
                    }
                }
            });
            hide();
            if (mScrollDirectionListener != null) {
                mScrollDirectionListener.onScrollUp();
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (mOnScrollListener != null) {
                mOnScrollListener.onScrolled(recyclerView, dx, dy);
            }

            super.onScrolled(recyclerView, dx, dy);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (mOnScrollListener != null) {
                mOnScrollListener.onScrollStateChanged(recyclerView, newState);
            }

            super.onScrollStateChanged(recyclerView, newState);
        }
    }
}
