package com.teambition.talk.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.teambition.talk.ui.activity.HomeActivity;

/**
 * Created by zeatual on 14-10-10.
 */
public class TransactionUtil {

    public static void goTo(Activity aty, Class clazz) {
        goTo(aty, clazz, false);
    }

    public static void goTo(Fragment fragment, Class clazz) {
        goTo(fragment.getActivity(), clazz, false);
    }

    public static void goTo(Activity aty, Class clazz, boolean isFinish) {
        goTo(aty, clazz, null, isFinish);
    }

    public static void goTo(Activity aty, Class clazz, Bundle bundle) {
        goTo(aty, clazz, bundle, false);
    }

    public static void goTo(Fragment fragment, Class clazz, boolean isFinish) {
        goTo(fragment.getActivity(), clazz, null, isFinish);
    }

    public static void goTo(Fragment fragment, Class clazz, Bundle bundle) {
        goTo(fragment.getActivity(), clazz, bundle, false);
    }

    public static void goTo(Activity aty, Class clazz, Bundle bundle, boolean isFinish) {
        Intent intent = new Intent(aty, clazz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        aty.startActivity(intent);
        if (isFinish) {
            aty.finish();
        }
    }

    public static void goToForResult(Activity aty, Class clazz, Bundle bundle, int requestCode) {
        Intent intent = new Intent(aty, clazz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        aty.startActivityForResult(intent, requestCode);
    }

    public static void goToForResult(Activity aty, Class clazz, int requestCode) {
        goToForResult(aty, clazz, null, requestCode);
    }

    public static void goAndRestartHome(Fragment f) {
        goAndRestartHome(f.getActivity());
    }

    public static void goAndRestartHome(Activity aty) {
        aty.finish();
        Intent intent = new Intent(aty, HomeActivity.class);
        intent.putExtra(HomeActivity.SHOW_PROGRESS_BAR, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        aty.startActivity(intent);
    }

}
