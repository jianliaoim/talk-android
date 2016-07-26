package com.teambition.talk.util;

/**
 * Created by wlanjie on 15/9/12.
 */
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.view.View;

/**
 * Created by jgzhu on 5/23/14.
 */
public class BlurBuilder {

    public static void RsBlur(RenderScript rs, Bitmap original) {
        // use this constructor for best performance, because it uses USAGE_SHARED mode which
        // reuses memory
        final Allocation input = Allocation.createFromBitmap(rs, original);
        final Allocation output = Allocation.createTyped(rs, input.getType());
        final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setRadius(8f);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(original);
        rs.destroy();
    }

    public static void RsBlur(RenderScript rs, Bitmap original, float radius) {
        // use this constructor for best performance, because it uses USAGE_SHARED mode which
        // reuses memory
        final Allocation input = Allocation.createFromBitmap(rs, original);
        final Allocation output = Allocation.createTyped(rs, input.getType());
        final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setRadius(radius);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(original);
        rs.destroy();
    }

    public static Bitmap getScreenShot(View v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.draw(c);
        return b;
    }

}
