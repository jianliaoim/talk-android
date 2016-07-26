package com.teambition.talk.ui.activity;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.adapter.ChooseImageAdapter;
import com.teambition.talk.entity.ImageMedia;
import com.teambition.talk.util.FileUtil;
import com.teambition.talk.util.ImageUtil;
import com.teambition.talk.util.StringUtil;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by jgzhu on 10/8/14.
 */
public class SelectImageActivity extends BaseActivity {

    public static final int SELECT_IMAGES = 8858;
    public static final String IMAGE_PATH = "image_path";
    protected static final int CAMERA_WITH_DATA = 3023;
    protected static final int CAMERA_PERMISSION = 0;
    private static final int WRITE_SDCARD_PERMISSION = 1;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.gridView)
    RecyclerView gridView;
    @InjectView(R.id.image)
    ImageView image;
    @InjectView(R.id.checkbox_source)
    CheckBox cbSource;

    private ChooseImageAdapter adapter;
    private Uri uriTemp;
    private File uploadFile;

    private String path;
    private MenuItem actionDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_image_story);
        ButterKnife.inject(this);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_select_image);
        adapter = new ChooseImageAdapter(this, new ChooseImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                if (position == 0) {
                    if (ContextCompat.checkSelfPermission(SelectImageActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(SelectImageActivity.this, Manifest.permission.CAMERA)) {
                            MainApp.showToastMsg(R.string.camera_permission_denied);
                        } else {
                            ActivityCompat.requestPermissions(SelectImageActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
                        }
                    } else {
                        initTempFile();
                        doTakePhoto();
                    }
                } else {
                    actionDone.setVisible(true);
                    ImageMedia mediaModel = (ImageMedia) adapter.getItem(position);
                    path = mediaModel.url;
                    MainApp.IMAGE_LOADER.displayImage(ImageLoaderConfig.PREFIX_FILE + path, image,
                            ImageLoaderConfig.EMPTY_OPTIONS);
                }
            }
        });
        gridView.setLayoutManager(new GridLayoutManager(this, 3));
        gridView.addItemDecoration(new ItemDecorationAlbumColumns(getResources().getDimensionPixelSize(R.dimen.image_story_divider), 3));
        gridView.setAdapter(adapter);

        if (savedInstanceState != null) {
            path = savedInstanceState.getString("path");
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                MainApp.showToastMsg(R.string.camera_permission_denied);
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE },
                        WRITE_SDCARD_PERMISSION);
            }
        } else {
            loadMediaData();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initTempFile();
                    doTakePhoto();
                }
                break;
            case WRITE_SDCARD_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadMediaData();
                }
                break;
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        actionDone = menu.findItem(R.id.action_done);
        actionDone.setVisible(!TextUtils.isEmpty(path));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_done:
                String result;
                if (!cbSource.isChecked()) {
                    result = FileUtil.createCompressedImagePath(path);
                    if (!FileUtil.isFileExist(result)) {
                        Bitmap bitmap = ImageUtil.compressImage(path);
                        if (bitmap != null) {
                            FileUtil.createCacheFileFromStream(bitmap, result);
                        }
                    }
                } else {
                    result = path;
                }
                ImageUtil.createRotateImageFile(path, result);

                Intent intent = new Intent();
                intent.putExtra(IMAGE_PATH, result);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_WITH_DATA && resultCode == RESULT_OK) {
            if (actionDone != null) {
                actionDone.setVisible(true);
            }
            MainApp.IMAGE_LOADER.displayImage(ImageLoaderConfig.PREFIX_FILE + path, image,
                    ImageLoaderConfig.EMPTY_OPTIONS);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("path", path);
    }

    private void initTempFile() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File photoDir = this.getExternalCacheDir();
            if (photoDir == null) {
                photoDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                        "/talk/cache/");
                if (!photoDir.exists()) {
                    photoDir.mkdirs();
                }
            }
            uploadFile = new File(photoDir, System.currentTimeMillis() + ".jpg");
            uriTemp = Uri.fromFile(uploadFile);
            path = uriTemp.getPath();
        } else {
            Toast.makeText(this, "No SDCard found", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMediaData() {
        Observable.create(new Observable.OnSubscribe<List<ImageMedia>>() {
            @Override
            public void call(Subscriber<? super List<ImageMedia>> subscriber) {
                try {
                    subscriber.onNext(queryImageMediaModels());
                    subscriber.onCompleted();
                } catch (Exception e) {

                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<ImageMedia>>() {
                    @Override
                    public void call(List<ImageMedia> mediaModels) {
                        adapter.replaceAll(mediaModels);
                        if (StringUtil.isBlank(path)) {
                            if (mediaModels != null && !mediaModels.isEmpty()) {
                                if (actionDone != null) {
                                    actionDone.setVisible(true);
                                }
                                path = mediaModels.get(0).url;
                                String url = "content://thumb/" + mediaModels.get(0).id;
                                MainApp.IMAGE_LOADER.displayImage(url, image, ImageLoaderConfig.EMPTY_OPTIONS);
                            }
                        } else {
                            MainApp.IMAGE_LOADER.displayImage(ImageLoaderConfig.PREFIX_FILE + path,
                                    image, ImageLoaderConfig.EMPTY_OPTIONS);
                        }
                    }
                });
    }

    private List<ImageMedia> queryImageMediaModels() {
        final String orderBy = MediaStore.Images.Media.DATE_TAKEN + " desc";
        final String[] columns = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media._ID};
        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columns, null, null, orderBy);
        ArrayList<ImageMedia> mediaModelList = new ArrayList<>();
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            mediaModelList.add(ImageMedia.fromCursor(cursor));
        }
        return mediaModelList;
    }

    protected void doTakePhoto() {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uriTemp);
            intent.putExtra("return-data", false);
            startActivityForResult(intent, CAMERA_WITH_DATA);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "There is on camera application found in device.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public class ItemDecorationAlbumColumns extends RecyclerView.ItemDecoration {

        private int mSizeGridSpacingPx;
        private int mGridSize;

        private boolean mNeedLeftSpacing = false;

        public ItemDecorationAlbumColumns(int gridSpacingPx, int gridSize) {
            mSizeGridSpacingPx = gridSpacingPx;
            mGridSize = gridSize;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int frameWidth = (int) ((parent.getWidth() - (float) mSizeGridSpacingPx * (mGridSize - 1)) / mGridSize);
            int padding = parent.getWidth() / mGridSize - frameWidth;
            int itemPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
            if (itemPosition < mGridSize) {
                outRect.top = 0;
            } else {
                outRect.top = mSizeGridSpacingPx;
            }
            if (itemPosition % mGridSize == 0) {
                outRect.left = 0;
                outRect.right = padding;
                mNeedLeftSpacing = true;
            } else if ((itemPosition + 1) % mGridSize == 0) {
                mNeedLeftSpacing = false;
                outRect.right = 0;
                outRect.left = padding;
            } else if (mNeedLeftSpacing) {
                mNeedLeftSpacing = false;
                outRect.left = mSizeGridSpacingPx - padding;
                if ((itemPosition + 2) % mGridSize == 0) {
                    outRect.right = mSizeGridSpacingPx - padding;
                } else {
                    outRect.right = mSizeGridSpacingPx / 2;
                }
            } else if ((itemPosition + 2) % mGridSize == 0) {
                mNeedLeftSpacing = false;
                outRect.left = mSizeGridSpacingPx / 2;
                outRect.right = mSizeGridSpacingPx - padding;
            } else {
                mNeedLeftSpacing = false;
                outRect.left = mSizeGridSpacingPx / 2;
                outRect.right = mSizeGridSpacingPx / 2;
            }
            outRect.bottom = 0;
        }
    }
}
