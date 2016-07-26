package com.teambition.talk.ui.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.jni.bitmap_operations.JniBitmapHolder;
import com.mcxiaoke.packer.helper.PackerNg;
import com.teambition.talk.BizLogic;
import com.teambition.talk.BuildConfig;
import com.teambition.talk.Constant;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.util.ClipboardHelper;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.TransactionUtil;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;
import com.xiaomi.mipush.sdk.PushMessageHelper;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends Activity {

    @InjectView(R.id.view)
    RelativeLayout view;
    @InjectView(R.id.logo)
    View logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CrashReport.initCrashReport(getApplicationContext(), Constant.BUGLY_APP_ID,
                MainApp.BUILD_TYPE == MainApp.DEBUG);
        if (BizLogic.getUserInfo() != null) {
            CrashReport.putUserData(this, "userId", BizLogic.getUserInfo().get_id() + "\n");
            CrashReport.putUserData(this, "email", BizLogic.getUserInfo().getEmail() + "\n");
            CrashReport.putUserData(this, "phone", BizLogic.getUserInfo().getPhoneNumber());
        }

        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        // 渠道名
        final String market = PackerNg.getMarket(this);
        if (StringUtil.isNotBlank(market)) {
            AnalyticsConfig.setChannel(market);
        }
        SharedPreferences preferences = getSharedPreferences("shortcut", MODE_PRIVATE);
        if (!preferences.getBoolean("is_create_short", false))
            addShortCut();
        MobclickAgent.updateOnlineConfig(this);

        initData();
    }

    private void initData() {
        boolean isFirstOpenVersion3 = MainApp.PREF_UTIL.getBoolean(Constant.FIRST_OPEN_3_0, true);
        if (isFirstOpenVersion3) {
            startActivity(new Intent(this, GuideActivity.class));
            finish();
        } else {
            if (BizLogic.isLogin()) {

                // check the system clipboard for invite url or code
                if (ClipboardHelper.detectInviteCode(this)) {
                    finish();
                    return;
                }

                if (BizLogic.hasChosenTeam()) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(PushMessageHelper.KEY_MESSAGE, getIntent().getSerializableExtra(PushMessageHelper.KEY_MESSAGE));
                    TransactionUtil.goTo(this, HomeActivity.class, bundle, true);
                    overridePendingTransition(R.anim.anim_empty, R.anim.anim_empty);
                } else {
                    TransactionUtil.goTo(this, ChooseTeamActivity.class, true);
                    overridePendingTransition(R.anim.anim_empty, R.anim.anim_empty);
                }
            } else {
                TransactionUtil.goTo(this, Oauth2Activity.class, true);
                overridePendingTransition(R.anim.anim_empty, R.anim.anim_empty);
            }
        }
    }

    private void addShortCut() {
        Intent shortCutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        shortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
        shortCutIntent.putExtra("duplicate", false);
        Parcelable icon = Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher);
        shortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        ComponentName name = new ComponentName(getPackageName(), getClass().getName());
        shortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(Intent.ACTION_MAIN).setComponent(name));
        sendBroadcast(shortCutIntent);
        SharedPreferences preferences = getSharedPreferences("shortcut", MODE_PRIVATE);
        preferences.edit().putBoolean("is_create_short", true).apply();
    }

    /**
     * test for jni rotate image
     */
    private void testRotate() {
        JniBitmapHolder holder = new JniBitmapHolder();
        holder.storeBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        holder.rotateBitmap180();
        ImageView icon = new ImageView(this);
        icon.setImageBitmap(holder.getBitmapAndFree());
        Toast toast = new Toast(this);
        toast.setView(icon);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }

}
