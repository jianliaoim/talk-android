package com.teambition.talk.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.util.TypedValue;

import com.jni.bitmap_operations.JniBitmapHolder;
import com.teambition.talk.MainApp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by zeatual on 15/2/26.
 */
public class ImageUtil {
    public static final String TAG = ImageUtil.class.getSimpleName();

    private static final int COMPRESS_TARGET_SIZE = 1280;
    private static final float COMPRESS_TARGET_LENGTH = 100 * 1024;

    public static Bitmap createRoundedBitmap(Context context, Bitmap bitmap, int radiusDP) {
        Bitmap bmp;

        bmp = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        BitmapShader shader = new BitmapShader(bitmap,
                BitmapShader.TileMode.CLAMP,
                BitmapShader.TileMode.CLAMP);

        float radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radiusDP,
                context.getResources().getDisplayMetrics());
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(shader);

        RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        canvas.drawRoundRect(rect, radius, radius, paint);

        return bmp;
    }

    public static Bitmap createRoundedBitmap(Context context, Bitmap bitmap, int leftTop, int rightTop, int leftBottm, int rightBottom) {
        Bitmap bmp;

        bmp = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        BitmapShader shader = new BitmapShader(bitmap,
                BitmapShader.TileMode.CLAMP,
                BitmapShader.TileMode.CLAMP);

        float radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, leftTop,
                context.getResources().getDisplayMetrics());
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(shader);

        RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        canvas.drawRoundRect(rect, radius, radius, paint);
        return bmp;
    }

    public static Bitmap scaleBitmapToTargetSize(Bitmap bitmap, int targetSize) {
        Bitmap bmp;
        int size = DensityUtil.dip2px(MainApp.CONTEXT, targetSize);
        float scaleFactor;
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        if (width != 0 && height != 0) {
            bitmap = getImageCrop(bitmap);
            height = bitmap.getHeight();
            width = bitmap.getWidth();
            scaleFactor = (float) size / (float) height;
        } else {
            return bitmap;
        }

        Matrix matrix = new Matrix();
        matrix.postScale(scaleFactor, scaleFactor);
        bmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

        return bmp;
    }

    /**
     * 按正方形裁切图片
     */
    public static Bitmap getImageCrop(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int wh = w > h ? h : w;

        int retX = w > h ? (w - h) / 2 : 0;
        int retY = w > h ? 0 : (h - w) / 2;

        return Bitmap.createBitmap(bitmap, retX, retY, wh, wh, null, false);
    }

    public static Bitmap compressImage(String path) {
        if (StringUtil.isBlank(path)) return null;
        Bitmap image = Bitmap.createBitmap(BitmapFactory.decodeFile(path));
        if (image == null) return null;
        // 若长边大于1280，先将尺寸等比压缩至1280
        int height = image.getHeight();
        int width = image.getWidth();
        float scaleFactor = 1;
        Matrix matrix = new Matrix();
        if (height >= width && height > COMPRESS_TARGET_SIZE) {
            scaleFactor = COMPRESS_TARGET_SIZE / (float) height;
        } else if (width >= height && width > COMPRESS_TARGET_SIZE) {
            scaleFactor = COMPRESS_TARGET_SIZE / (float) width;
        }
        matrix.postScale(scaleFactor, scaleFactor);
        image = Bitmap.createBitmap(image, 0, 0, width, height, matrix, true);

        // 无论图片大小，先将精度压缩至70%
        int accuracy = 70;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, accuracy, out);

        // 若图片大小大于200K，则持续压缩至200K以下
        while (out.size() > COMPRESS_TARGET_LENGTH) {
            accuracy -= 10;
            out.reset();
            image.compress(Bitmap.CompressFormat.JPEG, accuracy, out);
        }

        return BitmapFactory.decodeStream(new ByteArrayInputStream(out
                .toByteArray()), null, null);
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Bitmap retVal;
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        retVal = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        return retVal;
    }

    public static void createRotateImageFile(String origin, String destination) {
        ExifInterface ei = null;
        int orientationType = ExifInterface.ORIENTATION_NORMAL;
        try {
            ei = new ExifInterface(origin);
            orientationType = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeFile(destination);
        try {
            JniBitmapHolder bitmapHolder = new JniBitmapHolder();
            bitmapHolder.storeBitmap(bitmap);
            switch (orientationType) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    bitmapHolder.rotateBitmapCw90();
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    bitmapHolder.rotateBitmap180();
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    bitmapHolder.rotateBitmapCcw90();
                    break;
            }
            bitmap = bitmapHolder.getBitmap();
            if (bitmap != null) {
                FileUtil.createCacheFileFromStream(bitmap, destination);
                bitmapHolder.freeBitmap();
            }
        } catch (Exception e) {
            Logger.e(TAG, "createRotateImageFile", e);
            try {
                FileUtil.copyFile(origin, destination);
            } catch (IOException ioe) {
            }
        }
    }
}
