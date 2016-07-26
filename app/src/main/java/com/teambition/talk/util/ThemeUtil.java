package com.teambition.talk.util;

import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.graphics.drawable.DrawableCompat;

import com.teambition.talk.R;

import java.util.Random;

/**
 * Created by ZZQ on 4/7/15.
 */
public class ThemeUtil {

    enum Theme {
        GRAPE("grape"),
        BLUEBERRY("blueberry"),
        OCEAN("ocean"),
        MINT("mint"),
        TEA("tea"),
        INK("ink"),
        ACCOUNT("blue");

        private String value;

        Theme(String value) {
            this.value = value;
        }

        public static Theme getEnum(String value) {
            for (Theme v : values()) {
                if (v.value.equalsIgnoreCase(value)) {
                    return v;
                }
            }
            return OCEAN;
        }
    }

    public enum TopicColor {
        PURPLE("purple"),
        INDIGO("indigo"),
        BLUE("blue"),
        CYAN("cyan"),
        GRASS("grass"),
        YELLOW("yellow");

        private static Random random = new Random(47);
        private String value;

        TopicColor(String value) {
            this.value = value;
        }

        public static TopicColor getEnum(String value) {
            for (TopicColor v : values()) {
                if (v.value.equalsIgnoreCase(value)) {
                    return v;
                }
            }
            return BLUE;
        }

        public static String random() {
            return values()[random.nextInt(values().length)].value;
        }
    }

    public static int getTheme(String color) {
        switch (Theme.getEnum(color)) {
            case GRAPE:
                return R.style.Theme_Talk_Grape;
            case BLUEBERRY:
                return R.style.Theme_Talk_BlueBerry;
            case OCEAN:
                return R.style.Theme_Talk_Ocean;
            case MINT:
                return R.style.Theme_Talk_Mint;
            case TEA:
                return R.style.Theme_Talk_Tea;
            case INK:
                return R.style.Theme_Talk_Ink;
            default:
                return R.style.Theme_Talk_Account;
        }
    }

    public static int getThemeColor(Resources res, String color) {
        switch (Theme.getEnum(color)) {
            case GRAPE:
                return res.getColor(R.color.talk_grape);
            case BLUEBERRY:
                return res.getColor(R.color.talk_blueberry);
            case OCEAN:
                return res.getColor(R.color.talk_ocean);
            case MINT:
                return res.getColor(R.color.talk_mint);
            case TEA:
                return res.getColor(R.color.talk_tea);
            case INK:
                return res.getColor(R.color.talk_ink);
            default:
                return res.getColor(R.color.talk_blue);
        }
    }

    public static int getThemeColorRes(String color) {
        switch (Theme.getEnum(color)) {
            case GRAPE:
                return R.color.talk_grape;
            case BLUEBERRY:
                return R.color.talk_blueberry;
            case OCEAN:
                return R.color.talk_ocean;
            case MINT:
                return R.color.talk_mint;
            case TEA:
                return R.color.talk_tea;
            case INK:
                return R.color.talk_ink;
            default:
                return R.color.talk_blue;
        }
    }

    public static int getThemeColorDarkRes(String color) {
        switch (Theme.getEnum(color)) {
            case GRAPE:
                return R.color.talk_grape_dark;
            case BLUEBERRY:
                return R.color.talk_blueberry_dark;
            case OCEAN:
                return R.color.talk_ocean_dark;
            case MINT:
                return R.color.talk_mint_dark;
            case TEA:
                return R.color.talk_tea_dark;
            case INK:
                return R.color.talk_ink_dark;
            default:
                return R.color.talk_blue_dark;
        }
    }

    public static int getThemeColorLightRes(String color) {
        switch (Theme.getEnum(color)) {
            case GRAPE:
                return R.color.talk_grape_light;
            case BLUEBERRY:
                return R.color.talk_blueberry_light;
            case OCEAN:
                return R.color.talk_ocean_light;
            case MINT:
                return R.color.talk_mint_light;
            case TEA:
                return R.color.talk_tea_light;
            case INK:
                return R.color.talk_ink_light;
            default:
                return R.color.talk_ocean_light;
        }
    }

    public static int getThemeColorLightPressedRes(String color) {
        switch (Theme.getEnum(color)) {
            case GRAPE:
                return R.color.talk_grape_light_pressed;
            case BLUEBERRY:
                return R.color.talk_blueberry_light_pressed;
            case OCEAN:
                return R.color.talk_ocean_light_pressed;
            case MINT:
                return R.color.talk_mint_light_pressed;
            case TEA:
                return R.color.talk_tea_light_pressed;
            case INK:
                return R.color.talk_ink_light_pressed;
            default:
                return R.color.talk_ocean_light_pressed;
        }
    }

    public static int getThemeColorItemBackground(String color) {
        switch (Theme.getEnum(color)) {
            case GRAPE:
                return R.drawable.selector_item_grape;
            case BLUEBERRY:
                return R.drawable.selector_item_blueberry;
            case OCEAN:
                return R.drawable.selector_item_ocean;
            case MINT:
                return R.drawable.selector_item_mint;
            case TEA:
                return R.drawable.selector_item_tea;
            case INK:
                return R.drawable.selector_item_ink;
            default:
                return R.drawable.selector_item_ocean;
        }
    }

    public static int getThemeCircleDrawableId(String color) {
        switch (Theme.getEnum(color)) {
            case GRAPE:
                return R.drawable.bg_circle_grape_border;
            case BLUEBERRY:
                return R.drawable.bg_circle_blueberry_border;
            case OCEAN:
                return R.drawable.bg_circle_ocean_border;
            case MINT:
                return R.drawable.bg_circle_mint_border;
            case TEA:
                return R.drawable.bg_circle_tea_border;
            case INK:
                return R.drawable.bg_circle_ink_border;
            default:
                return R.drawable.bg_circle_ocean_border;
        }
    }

    public static int getThemeRoundDrawableId(String color) {
        if (color == null) {
            return R.drawable.bg_circle_ocean;
        }
        switch (Theme.getEnum(color)) {
            case GRAPE:
                return R.drawable.bg_circle_grape;
            case BLUEBERRY:
                return R.drawable.bg_circle_blueberry;
            case OCEAN:
                return R.drawable.bg_circle_ocean;
            case MINT:
                return R.drawable.bg_circle_mint;
            case TEA:
                return R.drawable.bg_circle_tea;
            case INK:
                return R.drawable.bg_circle_ink;
            default:
                return R.drawable.bg_circle_ocean;
        }
    }

    public static int getTopicCircleDrawableId(String color) {
        switch (TopicColor.getEnum(color)) {
            case PURPLE:
                return R.drawable.bg_circle_purple_border;
            case INDIGO:
                return R.drawable.bg_circle_indigo_border;
            case BLUE:
                return R.drawable.bg_circle_blue_border;
            case CYAN:
                return R.drawable.bg_circle_cyan_border;
            case GRASS:
                return R.drawable.bg_circle_grass_border;
            case YELLOW:
                return R.drawable.bg_circle_yellow_border;
            default:
                return R.drawable.bg_circle_blue_border;
        }
    }

    public static int getTopicRoundDrawableId(String color) {
        switch (TopicColor.getEnum(color)) {
            case PURPLE:
                return R.drawable.bg_circle_purple;
            case INDIGO:
                return R.drawable.bg_circle_indigo;
            case BLUE:
                return R.drawable.bg_circle_blue;
            case CYAN:
                return R.drawable.bg_circle_cyan;
            case GRASS:
                return R.drawable.bg_circle_grass;
            case YELLOW:
                return R.drawable.bg_circle_yellow;
            default:
                return R.drawable.bg_circle_blue;
        }
    }

    public static Drawable getThemeDrawable(Resources res, @DrawableRes int drawableResId, String themeColor) {
        Drawable drawable = DrawableCompat.wrap(res.getDrawable(drawableResId).mutate());
        int color = res.getColor(getThemeColorRes(themeColor));
//        DrawableCompat.setTint(drawable, color);
        DrawableCompat.setTint(drawable, res.getColor(R.color.colorPrimary));
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
        return drawable;
    }

    public static Drawable getDrawableWithColor(Resources res, @DrawableRes int drawableResId, @ColorRes int color) {
        Drawable drawable = DrawableCompat.wrap(res.getDrawable(drawableResId).mutate());
        DrawableCompat.setTint(drawable, res.getColor(color));
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
        return drawable;
    }
}
