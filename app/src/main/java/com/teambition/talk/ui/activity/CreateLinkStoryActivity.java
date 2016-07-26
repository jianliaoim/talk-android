package com.teambition.talk.ui.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.teambition.talk.BizLogic;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.rx.ApiErrorAction;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.data.CreateStoryRequestData;
import com.teambition.talk.entity.Link;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Story;
import com.teambition.talk.realm.StoryDataProcess;
import com.teambition.talk.ui.widget.ThemeButton;
import com.teambition.talk.util.AnalyticsHelper;
import com.teambition.talk.util.DensityUtil;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.TransactionUtil;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zeatual on 15/10/21.
 */
public class CreateLinkStoryActivity extends BaseActivity {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.title_input_layout)
    TextInputLayout titleInputLayout;
    @InjectView(R.id.content_input_layout)
    TextInputLayout contentInputLayout;
    @InjectView(R.id.link_input_layout)
    TextInputLayout linkInputLayout;
    @InjectView(R.id.link_content)
    EditText contentEdit;
    @InjectView(R.id.link_title)
    EditText titleEdit;
    @InjectView(R.id.image)
    ImageView image;
    @InjectView(R.id.link)
    EditText etLink;
    @InjectView(R.id.clear_link)
    View clearLinkView;
    @InjectView(R.id.crawl)
    ThemeButton crawlView;
    @InjectView(R.id.link_content_layout)
    View linkContentView;
    @InjectView(R.id.progress_bar)
    View progressBar;

    private MenuItem actionNext;
    private final Link link = new Link();
    private int imageWidth;
    private String originLink = "";

    public Link getLink() {
        return link;
    }

    public static CreateLinkStoryActivity getInstance() {
        return new CreateLinkStoryActivity();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_link_story);
        ButterKnife.inject(this);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_link_story);

        crawlView.setThemeBackground(getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorPrimaryDark));
        etLink.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String url = ((EditText) v).getText().toString();
                if (StringUtil.isNotBlank(url) && actionId == EditorInfo.IME_ACTION_DONE) {
                    progressBar.setVisibility(View.VISIBLE);
                    getMetaData(((EditText) v).getText().toString());
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        clearLinkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linkInputLayout.setErrorEnabled(false);
                etLink.setText("");
                etLink.requestFocus();
            }
        });

        crawlView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                originLink = etLink.getText().toString();
                progressBar.setVisibility(View.VISIBLE);
                linkInputLayout.setErrorEnabled(false);
                String url = etLink.getText().toString();
                if (!TextUtils.isEmpty(url)) {
                    link.setUrl(url);
                    getMetaData(url);
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });

        etLink.addTextChangedListener(linkTextWatcher);

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = clipboard.getPrimaryClip();
        if (clip != null && clip.getItemCount() > 0) {
            final Spannable url = new SpannableString(clip.getItemAt(0).coerceToText(this));
            if (Linkify.addLinks(url, Linkify.WEB_URLS)) {
                etLink.setText(url.toString());
                etLink.setSelection(url.toString().length());
                getMetaData(url.toString());
            }
        }
        final int screenWidth = getResources().getDisplayMetrics().widthPixels;
        imageWidth = screenWidth - (DensityUtil.dip2px(this, 16) * 2);
    }

    TextWatcher linkTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!originLink.equals(s.toString())) {
                linkInputLayout.setErrorEnabled(false);
            }
            final boolean isNotEmpty = !TextUtils.isEmpty(s.toString());
            clearLinkView.setEnabled(isNotEmpty);
            crawlView.setEnabled(isNotEmpty);
            if (actionNext != null && !isNotEmpty) {
                actionNext.setVisible(false);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        etLink.removeTextChangedListener(linkTextWatcher);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ChooseMemberActivity.REQUEST_CHOOSE_MEMBER:
                    List<Member> members = Parcels.unwrap(data.getParcelableExtra(ChooseMemberActivity.MEMBERS));
                    List<String> memberIds = new ArrayList<>(members.size());
                    for (Member member : members) {
                        if (member != null) {
                            memberIds.add(member.get_id());
                        }
                    }
                    final String meId = BizLogic.getUserInfo().get_id();
                    if (!memberIds.contains(meId)) {
                        memberIds.add(0, meId);
                    }
                    if (!TextUtils.isEmpty(titleEdit.getText().toString())) {
                        this.link.setTitle(titleEdit.getText().toString());
                    }
                    if (!TextUtils.isEmpty(contentEdit.getText().toString())) {
                        this.link.setText(contentEdit.getText().toString());
                    }
                    CreateStoryRequestData requestData = new CreateStoryRequestData(BizLogic.getTeamId(),
                            StoryDataProcess.Category.LINK.value, this.link, memberIds);
                    TalkClient.getInstance().getTalkApi().createStory(requestData)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Story>() {
                                   @Override
                                   public void call(Story story) {
                                       Bundle bundle = new Bundle();
                                       bundle.putParcelable(ChatActivity.EXTRA_STORY, Parcels.wrap(story));
                                       TransactionUtil.goTo(CreateLinkStoryActivity.this,
                                               ChatActivity.class, bundle, true);

                            }
                        }, new ApiErrorAction());
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_next, menu);
        actionNext = menu.findItem(R.id.action_next);
        actionNext.setVisible(!TextUtils.isEmpty(etLink.getText().toString()));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_next:
                startActivityForResult(new Intent(this, ChooseMemberActivity.class),
                        ChooseMemberActivity.REQUEST_CHOOSE_MEMBER);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getMetaData(final String url) {
        TalkClient.getInstance().getTalkApi()
                .getUrlMeta(url)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Link>() {
                    @Override
                    public void call(Link link) {
                        if (actionNext != null) {
                            actionNext.setVisible(true);
                        }
                        CreateLinkStoryActivity.this.link.setUrl(url);
                        progressBar.setVisibility(View.GONE);
                        setMetaData(link);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (actionNext != null) {
                            actionNext.setVisible(true);
                        }
                        link.setUrl(url);
                        progressBar.setVisibility(View.GONE);
                        linkInputLayout.setErrorEnabled(true);
                        linkInputLayout.setError(getResources().getString(R.string.carw_error));
                    }
                });
    }

    private void setMetaData(Link link) {
        titleEdit.setText(link.getTitle());
        if (StringUtil.isNotBlank(link.getText())) {
            contentInputLayout.setVisibility(View.VISIBLE);
            contentEdit.setText(link.getText());
        } else {
            contentInputLayout.setVisibility(View.GONE);
        }
        if (StringUtil.isNotBlank(link.getImageUrl())) {
            image.setVisibility(View.VISIBLE);
            MainApp.IMAGE_LOADER.loadImage(link.getImageUrl(), ImageLoaderConfig.DEFAULT_OPTIONS, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    if (loadedImage == null) return;
                    if (loadedImage.getWidth() > imageWidth) {
                        final float scaleWidth = ((float) imageWidth) / loadedImage.getWidth();
                        Matrix matrix = new Matrix();
                        matrix.postScale(scaleWidth, scaleWidth);
                        Bitmap bitmap = Bitmap.createBitmap(loadedImage, 0, 0, loadedImage.getWidth(), loadedImage.getHeight(), matrix, true);
                        image.setImageBitmap(bitmap);
                    } else {
                        image.setImageBitmap(loadedImage);
                    }
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });
        } else {
            image.setVisibility(View.GONE);
        }
    }
}
