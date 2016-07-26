package com.teambition.talk.util;

import android.graphics.Color;
import android.view.View;

import com.teambition.talk.ui.widget.MaterialRippleLayout;

/**
 * Created by zeatual on 14/10/27.
 */
public class RippleUtil {

    public static void makeRipple(int color, View... views) {
        for (View view : views) {
            MaterialRippleLayout.on(view)
                    .rippleColor(color)
                    .rippleAlpha(0.2f)
                    .rippleHover(true)
                    .create();
        }
    }

    public static void makeRippleWhite(View... views) {
        for (View view : views) {
            MaterialRippleLayout.on(view)
                    .rippleColor(Color.WHITE)
                    .rippleAlpha(0.2f)
                    .rippleHover(true)
                    .create();
        }
    }
}
