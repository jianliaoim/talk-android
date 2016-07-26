package com.teambition.talk.imageloader;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecodingInfo;
import com.teambition.talk.entity.File;

import java.io.IOException;

/**
 * Created by nlmartian on 4/20/15.
 */
public class ThumbnailDecoder implements ImageDecoder {

    private ContentResolver contentResolver;
    private BaseImageDecoder baseImageDecoder;

    public ThumbnailDecoder(ContentResolver cr, BaseImageDecoder baseDecoder) {
        contentResolver = cr;
        baseImageDecoder = baseDecoder;
    }

    @Override
    public Bitmap decode(ImageDecodingInfo imageDecodingInfo) throws IOException {
        if (isThumbnailUri(Uri.parse(imageDecodingInfo.getOriginalImageUri()))) {
            String originUrl = imageDecodingInfo.getOriginalImageUri();
            String strId = originUrl.substring(16, originUrl.length());
            long imageId = Long.parseLong(strId);
            Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(
                    contentResolver, imageId, MediaStore.Images.Thumbnails.MINI_KIND, null);

            // check the orientation of image
            int rotation = 0;
            Cursor mediaCursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{"orientation"}, "_ID=" + imageId, null, null);
            if (mediaCursor != null && mediaCursor.getCount() != 0) {
                while (mediaCursor.moveToNext()) {
                    rotation = mediaCursor.getInt(0);
                    break;
                }
            }
            if (mediaCursor != null && !mediaCursor.isClosed()) {
                try {
                    mediaCursor.close();
                } catch (Exception e) {
                }
            }
            if (rotation != 0) {
                return rotateImage(bitmap, rotation);
            }
            return bitmap;
        } else {
            return baseImageDecoder.decode(imageDecodingInfo);
        }
    }

    private boolean isThumbnailUri(Uri uri) {
        return uri.toString().startsWith("content://thumb");
    }

    private static Bitmap rotateImage(Bitmap source, float angle) {
        Bitmap retVal;
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        retVal = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        return retVal;
    }
}
