package com.teambition.talk.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.teambition.talk.BizLogic;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.rx.ApiErrorAction;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.data.FileUploadResponseData;
import com.teambition.talk.client.data.UpdateStoryRequestData;
import com.teambition.talk.entity.CountingTypedFile;
import com.teambition.talk.entity.File;
import com.teambition.talk.realm.StoryDataProcess;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.TransactionUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zeatual on 15/10/29.
 */
public class EditFileStoryActivity extends EditStoryActivity {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.et_title)
    EditText etTitle;
    @InjectView(R.id.et_text)
    EditText etText;
    @InjectView(R.id.til_title)
    TextInputLayout tilTitle;
    @InjectView(R.id.til_text)
    TextInputLayout tilText;
    @InjectView(R.id.image)
    ImageView imageView;
    @InjectView(R.id.img_upload)
    View vUpload;
    @InjectView(R.id.layout_image)
    View layoutImage;
    @InjectView(R.id.file_scheme)
    TextView tvFileScheme;

    private File file;
    private FileUploadResponseData fileData;
    private String originFileName;
    private String originText;
    private String originFileKey;
    private MenuItem actionDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_file_story);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        file = GsonProvider.getGson().fromJson(story.getData(), File.class);

        originFileKey = file.getFileKey();
        originText = file.getText() == null ? "" : file.getText();
        originFileName = file.getFileName();

        etTitle.setText(originFileName);
        etText.setText(originText);
        etTitle.setSelection(file.getFileName().length());
        if (BizLogic.isImg(file)) {
            tvFileScheme.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            vUpload.setVisibility(View.VISIBLE);
            MainApp.IMAGE_LOADER.displayImage(file.getDownloadUrl(), imageView,
                    ImageLoaderConfig.EMPTY_OPTIONS);
        } else {
            tvFileScheme.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            vUpload.setVisibility(View.GONE);
            GradientDrawable drawable = new GradientDrawable();
            final float radius = MainApp.CONTEXT.getResources().getDimension(R.dimen.story_chat_file_radius);
            drawable.setCornerRadii(new float[]{radius, radius, radius, radius, radius, radius, radius, radius});
            drawable.setColor(Color.parseColor(file.getSchemeColor(file.getFileType())));
            tvFileScheme.setBackgroundDrawable(drawable);
            tvFileScheme.setText(file.getFileType());
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                tilTitle.setVisibility(View.VISIBLE);
                tilText.setVisibility(View.VISIBLE);
                layoutImage.setVisibility(View.VISIBLE);
            }
        }, 200);

        vUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransactionUtil.goToForResult(EditFileStoryActivity.this, SelectImageActivity.class,
                        SelectImageActivity.SELECT_IMAGES);
            }
        });
        etTitle.addTextChangedListener(watcher);
        etText.addTextChangedListener(watcher);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SelectImageActivity.SELECT_IMAGES:
                    String path = data.getStringExtra(SelectImageActivity.IMAGE_PATH);
                    if (StringUtil.isNotBlank(path)) {
                        final java.io.File file = new java.io.File(path);
                        TalkClient.getInstance().getUploadApi()
                                .uploadFile(file.getName(), "image/*", file.length(), new CountingTypedFile("image/*", file,
                                        new CountingTypedFile.ProgressListener() {
                                            @Override
                                            public void transferred(long bytes) {

                                            }
                                        }))
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<FileUploadResponseData>() {
                                    @Override
                                    public void call(FileUploadResponseData fileUploadResponseData) {
                                        fileData = fileUploadResponseData;
                                        MainApp.IMAGE_LOADER.displayImage(fileData.getDownloadUrl(),
                                                imageView, ImageLoaderConfig.EMPTY_OPTIONS);
                                        checkUpdate();
                                    }
                                }, new ApiErrorAction());
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            checkUpdate();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        etTitle.removeTextChangedListener(watcher);
        etText.removeTextChangedListener(watcher);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        actionDone = menu.findItem(R.id.action_done);
        actionDone.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(0, R.anim.anim_fade_transition_out);
                break;
            case R.id.action_done:
                UpdateStoryRequestData updateData = new UpdateStoryRequestData();
                updateData.category = StoryDataProcess.Category.FILE.value;
                if (fileData != null) {
                    if (!originFileName.equals(etTitle.getText().toString())) {
                        fileData.setFileName(etTitle.getText().toString());
                    } else {
                        fileData.setFileName(null);
                    }
                    if (!originText.equals(etText.getText().toString())) {
                        fileData.setText(etText.getText().toString());
                    } else {
                        fileData.setText(null);
                    }
                    updateData.data = fileData;
                } else {
                    if (!originFileName.equals(etTitle.getText().toString())) {
                        file.setFileName(etTitle.getText().toString());
                    } else {
                        file.setFileName(null);
                    }
                    if (!originText.equals(etText.getText().toString())) {
                        file.setText(etText.getText().toString());
                    } else {
                        file.setText(null);
                    }
                    updateData.data = file;
                }
                updateStory(story.get_id(), updateData);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void hideInputMethodManager() {
        super.hideInputMethodManager();
        inputMethodManager.hideSoftInputFromWindow(etText.getWindowToken(), 0);
    }

    private void checkUpdate() {
        actionDone.setVisible((fileData != null && !originFileKey.equals(fileData.getFileKey())) ||
                !originFileName.equals(etTitle.getText().toString()) ||
                !originText.equals(etText.getText().toString()));
    }
}
