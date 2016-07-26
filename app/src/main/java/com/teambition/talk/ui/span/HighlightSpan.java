package com.teambition.talk.ui.span;

import android.content.res.Resources;
import android.text.TextPaint;
import android.text.style.CharacterStyle;

import com.teambition.talk.BizLogic;
import com.teambition.talk.Constant;
import com.teambition.talk.R;
import com.teambition.talk.util.ThemeUtil;

/**
 * Created by ZZQ on 3/30/15.
 */
public class HighlightSpan extends CharacterStyle {

    Resources res;

    public HighlightSpan(Resources res) {
        this.res = res;
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        int color;
        if ("ink".equals(BizLogic.getTeamColor())) {
            color = ThemeUtil.getThemeColor(res, Constant.DEFAULT_COLOR);
        } else {
//            color = ThemeUtil.getThemeColor(res, BizLogic.getTeamColor());
            color = res.getColor(R.color.colorPrimary);
        }
        tp.setColor(color);
    }
}
