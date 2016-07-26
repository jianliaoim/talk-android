package com.teambition.talk.ui.activity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.talk.dialog.TalkDialog;
import com.teambition.talk.BizLogic;
import com.teambition.talk.FileDownloader;
import com.teambition.talk.MainApp;
import com.teambition.talk.MediaController;
import com.teambition.talk.R;
import com.teambition.talk.entity.File;

import org.parceler.Parcels;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.functions.Action1;
import uk.co.senab.photoview.PhotoView;

/**
 * Created by wlanjie on 16/1/11.
 */
public class FilePreViewActivity extends BaseActivity {

    public final static String FILE = "file";

    @InjectView(R.id.photo_view)
    PhotoView mPhotoView;

    @InjectView(R.id.progress_bar)
    View mProgressBar;

    @InjectView(R.id.file_scheme)
    TextView mFileScheme;

    private File mFile;

    private ProgressDialog proDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_preview);
        ButterKnife.inject(this);
        mFile = Parcels.unwrap(getIntent().getExtras().getParcelable(FILE));
        proDialog = new ProgressDialog(this);
        proDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        proDialog.setMessage(getResources().getString(R.string.wait));
        proDialog.setMax(100);
        if (BizLogic.isImg(mFile)) {
            mFileScheme.setVisibility(View.GONE);
            MainApp.IMAGE_LOADER.displayImage(mFile.getDownloadUrl(), mPhotoView, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    super.onLoadingComplete(imageUri, view, loadedImage);
                    mProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    super.onLoadingFailed(imageUri, view, failReason);
                    mProgressBar.setVisibility(View.GONE);
                }
            });
            mPhotoView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showDialog();
                    return false;
                }
            });
        } else {
            mPhotoView.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.GONE);
            mFileScheme.setVisibility(View.VISIBLE);
            GradientDrawable drawable = new GradientDrawable();
            final float radius = MainApp.CONTEXT.getResources().getDimension(R.dimen.story_chat_file_radius);
            drawable.setCornerRadii(new float[]{radius, radius, radius, radius, radius, radius, radius, radius});
            drawable.setColor(Color.parseColor(mFile.getSchemeColor(mFile.getFileType())));
            mFileScheme.setBackgroundDrawable(drawable);
            mFileScheme.setText(mFile.getFileType());
            mFileScheme.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showDialog();
                    return false;
                }
            });
        }
    }

    private void showDialog() {
        final CharSequence[] actions = new CharSequence[]{getString(R.string.save_to_local)};
        new TalkDialog.Builder(this)
                .items(actions)
                .itemsCallback(new TalkDialog.ListCallback() {
                    @Override
                    public void onSelection(TalkDialog dialog, View itemView, int which, CharSequence text) {
                        downloadFile(mFile.getDownloadUrl(), FileDownloader.getDownloadPath(mFile.getFileName()));
                    }
                }).show();
    }

    private void downloadFile(String downloadUrl, final String downloadPath) {
        FileDownloader.getInstance().startDownload(downloadUrl, downloadPath, new Action1<Integer>() {
            @Override
            public void call(Integer progress) {
                if (progress == FileDownloader.FINISH) {
                    proDialog.dismiss();
                    if (BizLogic.isImg(mFile)) {
                        MediaController.updateSystemGallery(downloadPath);
                    }
                    MainApp.showToastMsg(getString(R.string.save_finish_message, downloadPath));
                } else {
                    proDialog.setProgress(progress);
                    proDialog.show();
                }
            }
        }, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }
}
