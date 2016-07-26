package com.teambition.talk.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.talk.dialog.TalkDialog;
import com.teambition.talk.Constant;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.rx.ApiErrorAction;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.data.ErrorResponseData;
import com.teambition.talk.client.data.FileUploadResponseData;
import com.teambition.talk.client.data.StrikerTokenResponseData;
import com.teambition.talk.client.data.UserUpdateData;
import com.teambition.talk.entity.CountingTypedFile;
import com.teambition.talk.entity.CountryModel;
import com.teambition.talk.entity.User;
import com.teambition.talk.ui.VCodeDialogHelper;
import com.teambition.talk.util.AnalyticsHelper;
import com.teambition.talk.util.Logger;
import com.teambition.talk.util.NotificationUtil;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.TransactionUtil;

import org.parceler.Parcels;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.RetrofitError;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zeatual on 15/5/27.
 */
public class Oauth2Activity extends BaseAccountActivity {

    private final static int REQUEST_TEAMBITION_CODE = 0;

    @InjectView(R.id.btn_signup)
    Button btnBegin;
    @InjectView(R.id.btn_register)
    Button btnBeginWithTb;

    private VCodeDialogHelper vCodeHelper;
    private TalkDialog userDialog;
    private View userView;
    private ImageView dialogAvatar;
    private EditText dialogName;
    private View layoutAvatar;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_oauth2);
        ButterKnife.inject(this);

        vCodeHelper = new VCodeDialogHelper(this, getString(R.string.oauth_login_phone),
                new VCodeDialogHelper.VCodeDialogCallback() {
                    @Override
                    public void onPassThrough(String randomCode, String vCode) {
                        TalkClient.getInstance().getAccountApi().mobileSignIn(randomCode, vCode)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<User>() {
                                    @Override
                                    public void call(User user) {
                                        if (StringUtil.isNotBlank(user.getAccountToken())) {
                                            vCodeHelper.dismiss();
                                            MainApp.PREF_UTIL.putString(Constant.ACCESS_TOKEN, user.getAccountToken());
                                            TalkClient.getInstance().setAccessToken(user.getAccountToken());
                                            TalkClient.getInstance().getTalkApi().subscribeUser(MainApp.PREF_UTIL
                                                    .getString(Constant.SOCKET_ID)).subscribe();
                                            if (user.getPreference() == null) {
                                                User u = (User) MainApp.PREF_UTIL.getObject(Constant.USER, User.class);
                                                if (u != null) {
                                                    user.setPreference(u.getPreference());
                                                }
                                            }
                                            MainApp.PREF_UTIL.putObject(Constant.USER, user);
                                            NotificationUtil.startPush(Oauth2Activity.this);
                                            checkIsNew(user);
                                        }
                                    }
                                }, new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        if (throwable instanceof RetrofitError) {
                                            try {
                                                ErrorResponseData error = (ErrorResponseData) ((RetrofitError) throwable)
                                                        .getBodyAs(ErrorResponseData.class);
                                                vCodeHelper.buildVCodeError(error.message);
                                            } catch (Exception e) {
                                                vCodeHelper.buildVCodeError(getString(R.string.network_failed));
                                            }
                                        }
                                    }
                                });
                    }
                });

        userView = LayoutInflater.from(this).inflate(R.layout.dialog_user_prefs, null);
        dialogAvatar = (ImageView) userView.findViewById(R.id.img_avatar);
        dialogName = (EditText) userView.findViewById(R.id.et_name);
        layoutAvatar = userView.findViewById(R.id.layout_avatar);
        layoutAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Oauth2Activity.this, SelectImageActivity.class);
                startActivityForResult(intent, SelectImageActivity.SELECT_IMAGES);
            }
        });
        userDialog = new TalkDialog.Builder(this)
                .title(R.string.complete_user_info)
                .titleColorRes(R.color.white)
                .titleBackgroundColorRes(R.color.talk_blue)
                .autoDismiss(false)
                .backgroundColorRes(R.color.white)
                .customView(userView, false)
                .negativeText(R.string.cancel)
                .negativeColorRes(R.color.material_grey_700)
                .positiveText(R.string.action_done)
                .positiveColorRes(R.color.talk_blue)
                .callback(new TalkDialog.ButtonCallback() {
                    @Override
                    public void onNegative(TalkDialog dialog) {
                        dialog.dismiss();
                    }

                    @Override
                    public void onPositive(TalkDialog dialog, View v) {
                        updateUser(dialogName.getText().toString(), null);
                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        TransactionUtil.goTo(Oauth2Activity.this, ChooseTeamActivity.class, true);
                    }
                })
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case VCodeDialogHelper.REQUEST_COUNTRY_CODE:
                    if (data != null) {
                        CountryModel model = Parcels.unwrap(data.getParcelableExtra(PickCountryCodeActivity.COUNTRY_CODE_DATA));
                        vCodeHelper.setCountryCode("+ " + String.valueOf(model.callingCode));
                    }
                    break;
                case REQUEST_TEAMBITION_CODE:
                    AnalyticsHelper.getInstance().sendEvent(AnalyticsHelper.Category.login, "login succ", "with teambition");

                    String code = data.getStringExtra(UnionsActivity.CODE);
                    TalkClient.getInstance().getAccountApi()
                            .signInByTeambition(code)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<User>() {
                                @Override
                                public void call(User user) {
                                    if (StringUtil.isNotBlank(user.getAccountToken())) {
                                        MainApp.PREF_UTIL.putString(Constant.ACCESS_TOKEN, user.getAccountToken());
                                        TalkClient.getInstance().setAccessToken(user.getAccountToken());
                                        TalkClient.getInstance().getTalkApi().subscribeUser(MainApp.PREF_UTIL
                                                .getString(Constant.SOCKET_ID)).subscribe();
                                        NotificationUtil.startPush(Oauth2Activity.this);
                                        checkIsNew(user);
                                    }
                                }
                            }, new ApiErrorAction());
                    break;
                case SelectImageActivity.SELECT_IMAGES:
                    String path = data.getStringExtra(SelectImageActivity.IMAGE_PATH);
                    if (StringUtil.isNotBlank(path)) {
                        File file = new File(path);
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
                                    public void call(FileUploadResponseData data) {
                                        MainApp.IMAGE_LOADER.displayImage(data.getThumbnailUrl(),
                                                dialogAvatar, ImageLoaderConfig.AVATAR_OPTIONS);
                                        updateUser(null, data.getThumbnailUrl());
                                    }
                                }, new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        MainApp.showToastMsg(R.string.network_failed);
                                    }
                                });
                    }
                    break;
            }
        }
    }

    @OnClick({R.id.btn_signup, R.id.btn_register, R.id.tv_tb_login})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_signup:
                startActivity(new Intent(this, LoginActivity.class));
                break;
            case R.id.btn_register:
                startActivity(new Intent(this, RegisterActivity.class));
                break;
            case R.id.tv_tb_login:
                TransactionUtil.goToForResult(this, UnionsActivity.class, REQUEST_TEAMBITION_CODE);
                overridePendingTransition(R.anim.anim_fade_transition_in, 0);

                AnalyticsHelper.getInstance().sendEvent(AnalyticsHelper.Category.login, "login ready", "with teambition");
                break;
        }
    }

    private void checkIsNew(User user) {
        if (user.wasNew()) {
            this.user = user;
            MainApp.IMAGE_LOADER.displayImage(user.getAvatarUrl(), dialogAvatar,
                    ImageLoaderConfig.AVATAR_OPTIONS);
            TalkClient.getInstance().getTalkApi().getStrikerToken().observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<StrikerTokenResponseData>() {
                        @Override
                        public void call(StrikerTokenResponseData responseData) {
                            if (responseData != null) {
                                MainApp.PREF_UTIL.putString(Constant.STRIKER_TOKEN, responseData.getToken());
                                userDialog.show();
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Logger.e("Oauth2Activity", "fail to get file auth key", throwable);
                        }
                    });
        } else {
            TransactionUtil.goTo(this, ChooseTeamActivity.class, true);
        }
    }

    private void updateUser(final String name, String avatar) {
        String n = StringUtil.isNotBlank(name) ? name : user.getName();
        String a = StringUtil.isNotBlank(avatar) ? avatar : user.getAvatarUrl();
        if (user != null) {
            TalkClient.getInstance().getTalkApi()
                    .updateUser(user.get_id(), new UserUpdateData(n, user.getEmail(), a))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<User>() {
                        @Override
                        public void call(User user) {
                            Oauth2Activity.this.user = user;
                            MainApp.PREF_UTIL.putObject(Constant.USER, user);
                            if (StringUtil.isNotBlank(name)) {
                                userDialog.dismiss();
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            MainApp.showToastMsg(R.string.network_failed);
                        }
                    });
        }
    }
}
