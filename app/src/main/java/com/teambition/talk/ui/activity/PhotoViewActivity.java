package com.teambition.talk.ui.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.teambition.talk.FileDownloader;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.File;
import com.teambition.talk.entity.Message;
import com.teambition.talk.realm.MessageDataProcess;
import com.teambition.talk.ui.widget.HackyViewPager;
import com.teambition.talk.util.FileUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rx.functions.Action1;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by zeatual on 15/5/7.
 */
public abstract class PhotoViewActivity extends BaseActivity {

    protected HackyViewPager mViewPager;
    protected PhotoPagerAdapter adapter;

    protected boolean isLoading;
    protected boolean canLoadLeft;
    protected boolean canLoadRight;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);
        View baseView = findViewById(R.id.container);
        progressBar = findViewById(R.id.progress_bar);
        mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
        setContentView(baseView);
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (!isLoading) {
                    if (position < 4 && canLoadLeft) {
                        loadLeft();
                    } else if (position > adapter.getCount() - 5 && canLoadRight) {
                        loadRight();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void setLoading(boolean isLoading) {
        this.isLoading = isLoading;
    }

    public void setCanLoadLeft(boolean canLoadLeft) {
        this.canLoadLeft = canLoadLeft;
    }

    public void setCanLoadRight(boolean canLoadRight) {
        this.canLoadRight = canLoadRight;
    }

    abstract void loadLeft();

    abstract void loadRight();

    protected void initPager(int position, List<Message> messages) {
        adapter = new PhotoPagerAdapter(this, removePSD(messages));
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(resetPosition(position, messages));
        if (canLoadLeft && position == 0) {
            loadLeft();
        }
    }

    protected void addToLeft(List<Message> messages) {
        List<Message> pureImg = removePSD(messages);
        if (adapter != null) {
            adapter.addToLeft(pureImg);
            mViewPager.setCurrentItem(pureImg.size() + mViewPager.getCurrentItem(), false);
        }
    }

    protected void addToRight(List<Message> messages) {
        if (adapter != null) {
            adapter.addToRight(removePSD(messages));
        }
    }

    static class PhotoPagerAdapter extends PagerAdapter {
        private static final String SCHEME_FILE = "file://";

        private PhotoViewAttacher.OnPhotoTapListener tapListener;
        private View.OnLongClickListener longClickListener;
        private List<Message> messages = new ArrayList<>();

        PhotoPagerAdapter(final PhotoViewActivity aty, List<Message> messages) {
            this.messages.addAll(messages);
            tapListener = new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float v, float v2) {
                    aty.finish();
                }
            };
        }

        PhotoPagerAdapter(final PhotoViewActivity aty, List<Message> messages, View.OnLongClickListener longClickListener) {
            this(aty, messages);
            this.longClickListener = longClickListener;
        }

        public void addToLeft(List<Message> messages) {
            this.messages.addAll(0, messages);
            notifyDataSetChanged();
        }

        public void addToRight(List<Message> messages) {
            this.messages.addAll(messages);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return messages.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            final View pagerView = LayoutInflater.from(container.getContext()).inflate(
                    R.layout.pager_view_photo, container, false);
            final View progressBar = pagerView.findViewById(R.id.progress_bar);
            final PhotoView photoView = (PhotoView) pagerView.findViewById(R.id.photo_view);

            File file = MessageDataProcess.getInstance().getImages(messages.get(position)).get(0);
            final String path = FileDownloader.getCachePath(file.getFileKey(), file.getFileType());

            final SimpleImageLoadingListener loadingListener = new SimpleImageLoadingListener() {
                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    progressBar.setVisibility(View.GONE);
                }
            };

            if (FileUtil.isFileExist(path, file.getFileSize())) {
                MainApp.IMAGE_LOADER.displayImage(SCHEME_FILE + path, photoView, loadingListener);
            } else {
                FileDownloader.getInstance().startDownload(file.getDownloadUrl(), path,
                        new Action1<Integer>() {
                            @Override
                            public void call(Integer integer) {
                                if (integer == FileDownloader.FINISH) {
                                    MainApp.IMAGE_LOADER.displayImage(SCHEME_FILE + path, photoView, loadingListener);
                                }
                            }
                        }, null);
            }

            photoView.setOnPhotoTapListener(tapListener);
            photoView.setOnLongClickListener(longClickListener);
            container.addView(pagerView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            return pagerView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public Message getItem(int position) {
            return messages.get(position);
        }

    }

    protected List<Message> removePSD(List<Message> messages) {
        if (messages == null) return new ArrayList<>();
        Iterator<Message> iterator = messages.iterator();
        while (iterator.hasNext()) {
            Message message = iterator.next();
            File file = MessageDataProcess.getInstance().getImages(message).get(0);
            if (file != null && "psd".equals(file.getFileType())) {
                iterator.remove();
            }
        }
        return messages;
    }

    protected int resetPosition(int position, List<Message> messages) {
        if (messages == null) return position;
        for (int i = 0; i < position; i++) {
            Message message = messages.get(i);
            List<File> files = MessageDataProcess.getInstance().getImages(message);
            if (files.isEmpty()) {
                continue;
            }
            File file = files.get(0);
            if (file != null && "psd".equals(file.getFileType())) {
                position--;
            }
        }
        return position;
    }

}
