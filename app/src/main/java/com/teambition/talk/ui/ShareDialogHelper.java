package com.teambition.talk.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.talk.dialog.TalkDialog;
import com.teambition.talk.Constant;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.util.WeChatHelper;

/**
 * Created by nlmartian on 12/1/15.
 */
public class ShareDialogHelper implements View.OnClickListener {

    private Activity activity;
    private TalkDialog dialog;
    private String content;
    private WeChatHelper weChatHelper;

    public ShareDialogHelper(Activity activity, String content) {
        this.activity = activity;
        this.content = content;

        weChatHelper = WeChatHelper.getInstance(activity);
        final View customView = LayoutInflater.from(activity).inflate(R.layout.fragment_share_dialog, null);
        setUpSection(customView);
        dialog = new TalkDialog.Builder(activity)
                .title(R.string.share_team_invite)
                .customView(customView, false)
                .positiveText(R.string.cancel)
                .callback(new TalkDialog.ButtonCallback() {
                    @Override
                    public void onPositive(TalkDialog dialog, View v) {
                        super.onPositive(dialog, v);
                        dialog.dismiss();
                    }
                })
                .build();
    }

    public void showDialog() {
        dialog.show();
    }

    private void setUpSection(View baseView) {
        baseView.findViewById(R.id.wx_chat).setOnClickListener(this);
        baseView.findViewById(R.id.wx_moment).setOnClickListener(this);
        baseView.findViewById(R.id.copy).setOnClickListener(this);
        baseView.findViewById(R.id.more).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        dialog.dismiss();
        if (v.getId() == R.id.wx_chat) {
            clipContent();
            startWeChat();
            Toast.makeText(activity, R.string.copied_to_clipboard_and_gowx, Toast.LENGTH_SHORT).show();
        } else if (v.getId() == R.id.wx_moment) {
            weChatHelper.sendTextToTimeline(content);
        } else if (v.getId() == R.id.copy) {
            clipContent();
            Toast.makeText(activity, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
        } else if (v.getId() == R.id.more) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, content);
            if (sendIntent.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivity(sendIntent);
            }
        }
    }

    private void clipContent() {
        ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData data = ClipData.newPlainText("", content);
        cm.setPrimaryClip(data);
        MainApp.PREF_UTIL.putString(Constant.COPIED_INVITE_TOKEN, content);
    }

    private void startWeChat() {
        Intent intent = new Intent();
        ComponentName cmp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(cmp);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        } else {
            Toast.makeText(activity, R.string.wechat_not_installed, Toast.LENGTH_SHORT).show();
        }
    }
}
