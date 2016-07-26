package com.teambition.talk.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.umeng.analytics.MobclickAgent;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by michael on 10/23/14.
 */
public class ImageReviewActivity extends BaseActivity {

    @InjectView(R.id.work_image)
    ImageView image;

    private PhotoViewAttacher attacher;

    public static Intent getIntent(Context context, String path) {
        Intent intent = new Intent(context, ImageReviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("path", path);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image_review);
        ButterKnife.inject(this);

        attacher = new PhotoViewAttacher(image);
        attacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float v, float v2) {
                finish();
            }
        });
        final String path = getIntent().getExtras().getString("path");
        displayImage("file://" + path, image);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        MobclickAgent.onPageStart(getClass().getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        MobclickAgent.onPageEnd(getClass().getName());
    }

    private void displayImage(String url, ImageView imageView) {
        MainApp.IMAGE_LOADER.displayImage(url, imageView, ImageLoaderConfig.EMPTY_OPTIONS,
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        attacher.update();
                    }
                });
    }
}
