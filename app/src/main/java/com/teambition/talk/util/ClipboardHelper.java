package com.teambition.talk.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.teambition.talk.Constant;
import com.teambition.talk.MainApp;
import com.teambition.talk.ui.activity.ChooseTeamActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nlmartian on 12/1/15.
 */
public class ClipboardHelper {
    private static final String BALLOON = "\\uD83C\\uDF88";
    private static final String PATTERN = BALLOON + "(.+?)" + BALLOON;
    private static final Pattern pattern = Pattern.compile(PATTERN);

    public static boolean detectInviteCode(Context context) {
        String clipText = getClipText(context);
        if (MainApp.PREF_UTIL.getString(Constant.COPIED_INVITE_TOKEN, "").equals(clipText)) {
            return false;
        }

        String command = matchCommand(context, clipText);
        if (clipText != null && command != null) {
            String url = "tb-talk://team_invite?inviteCode=" + command;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.setClass(context, ChooseTeamActivity.class);
            context.startActivity(intent);
            return true;
        }
        return false;
    }

    private static String matchCommand(Context context, String str) {
        Matcher matcher = pattern.matcher(str);
        String command = null;
        if (matcher.find()) {
            command = matcher.group(1);
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData data = ClipData.newPlainText("", "");
            cm.setPrimaryClip(data);
        }
        return command;
    }

    private static String getClipText(Context context) {
        String text = "";
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = cm.getPrimaryClip();
        if (clipData != null) {
            if (clipData.getItemCount() > 0) {
                ClipData.Item item = clipData.getItemAt(0);
                text = item.getText().toString();
            }
        }

        return text;
    }
}
