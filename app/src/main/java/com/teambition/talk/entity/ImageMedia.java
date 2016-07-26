package com.teambition.talk.entity;

import android.database.Cursor;
import android.provider.MediaStore;

/**
 * Created by jgzhu on 10/8/14.
 */
public class ImageMedia {
    public String url = null;
    public boolean status = false;
    public long id;

    public ImageMedia(long id, String url, boolean status) {
        this.id = id;
        this.url = url;
        this.status = status;
    }

    public static ImageMedia fromCursor(Cursor cursor) {
        String url = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
        return new ImageMedia(id, url, false);
    }
}
