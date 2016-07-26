package com.teambition.talk.ui.widget;

import android.app.Dialog;
import android.content.Context;

/**
 * Created by zeatual on 14/10/30.
 */
public class BottomMenu extends Dialog {


    public BottomMenu(Context context) {
        super(context);
    }

    public BottomMenu(Context context, int theme) {
        super(context, theme);
    }

    protected BottomMenu(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


}
