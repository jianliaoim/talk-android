package com.teambition.talk.ui.widget;

import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.Button;

import com.teambition.talk.Constant;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.Team;
import com.teambition.talk.util.DensityUtil;
import com.teambition.talk.util.ThemeUtil;

/**
 * Created by nlmartian on 4/15/15.
 */
public class ThemeButton extends Button {

    public ThemeButton(Context context) {
        this(context, null);
    }

    public ThemeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            setThemeBackground();
        }
    }

    public void setThemeBackground() {
        Team team = (Team) MainApp.PREF_UTIL.getObject(Constant.TEAM, Team.class);
        String colorName = (team == null) ? Constant.DEFAULT_COLOR : team.getColor();
        int radius = DensityUtil.dip2px(getContext(), 2);
        float[] radii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};


        StateListDrawable states = new StateListDrawable();

        RoundRectShape disableShape = new RoundRectShape(radii, null, null);
        ShapeDrawable disableDrawable = new ShapeDrawable(disableShape);
        disableDrawable.getPaint().setColor(getResources().getColor(R.color.material_grey_300));
        states.addState(new int[]{-android.R.attr.state_enabled}, disableDrawable);

        int themeDarkColor = getResources().getColor(ThemeUtil.getThemeColorDarkRes(colorName));
        RoundRectShape pressedShape = new RoundRectShape(radii, null, null);
        ShapeDrawable pressedDrawable = new ShapeDrawable(pressedShape);
        pressedDrawable.getPaint().setColor(themeDarkColor);
        states.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);

        RoundRectShape normalShape = new RoundRectShape(radii, null, null);
        ShapeDrawable normalDrawable = new ShapeDrawable(normalShape);
        int themeColor = ThemeUtil.getThemeColor(getResources(), colorName);
        normalDrawable.getPaint().setColor(themeColor);
        states.addState(new int[]{-android.R.attr.state_pressed}, normalDrawable);

        setBackgroundDrawable(states);
    }

    public void setThemeBackground(String colorName) {
        setThemeBackground(ThemeUtil.getThemeColor(getResources(), colorName),
                getResources().getColor(ThemeUtil.getThemeColorDarkRes(colorName)));
    }

    public void setThemeBackground(int color, int colorPressed) {
        int radius = DensityUtil.dip2px(getContext(), 2);
        float[] radii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        StateListDrawable states = new StateListDrawable();

        RoundRectShape disableShape = new RoundRectShape(radii, null, null);
        ShapeDrawable disableDrawable = new ShapeDrawable(disableShape);
        disableDrawable.getPaint().setColor(getResources().getColor(R.color.material_grey_300));
        states.addState(new int[]{-android.R.attr.state_enabled}, disableDrawable);

        RoundRectShape pressedShape = new RoundRectShape(radii, null, null);
        ShapeDrawable pressedDrawable = new ShapeDrawable(pressedShape);
        pressedDrawable.getPaint().setColor(colorPressed);
        states.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);

        RoundRectShape normalShape = new RoundRectShape(radii, null, null);
        ShapeDrawable normalDrawable = new ShapeDrawable(normalShape);
        normalDrawable.getPaint().setColor(color);
        states.addState(new int[]{-android.R.attr.state_pressed}, normalDrawable);

        setBackgroundDrawable(states);
    }
}
