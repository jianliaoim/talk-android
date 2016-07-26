package com.teambition.talk.util;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.teambition.talk.Constant;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

/**
 * Created by michael on 9/25/14.
 */
public class FileUtil {

    public static boolean createDirIfNotExisted(String path, boolean isNomedia) {
        File file = new File(path);
        File fileName = new File(path + "/.nomedia");
        try {
            if (!file.exists()) {
                file.mkdirs();
            }
            if (isNomedia && !fileName.exists()) {
                fileName.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean isFileExist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    public static boolean isFileExist(String filePath, int fileSize) {
        File file = new File(filePath);
        return file.exists() && file.length() == fileSize;
    }

    /**
     * 根据文件类型打开文件
     */
    public static void openFileByType(Context context, String type, File file) {
        Intent intent = null;

        if (StringUtil.isNotBlank(type) && file.isFile()) {
            if (checkEndsWithInFileType(type, getStringArray(context, R.array.file_type_text))) {
                intent = OpenFileIntent.getTextFileIntent(file);
            } else if (checkEndsWithInFileType(type, getStringArray(context, R.array.file_type_image))) {
                intent = OpenFileIntent.getImageFileIntent(file);
            } else if (checkEndsWithInFileType(type, getStringArray(context, R.array.file_type_pdf))) {
                intent = OpenFileIntent.getPdfFileIntent(file);
            } else if (checkEndsWithInFileType(type, getStringArray(context, R.array.file_type_html))) {
                intent = OpenFileIntent.getHtmlFileIntent(file);
            } else if (checkEndsWithInFileType(type, getStringArray(context, R.array.file_type_word))) {
                intent = OpenFileIntent.getWordFileIntent(file);
            } else if (checkEndsWithInFileType(type, getStringArray(context, R.array.file_type_excel))) {
                intent = OpenFileIntent.getExcelFileIntent(file);
            } else if (checkEndsWithInFileType(type, getStringArray(context, R.array.file_type_ppt))) {
                intent = OpenFileIntent.getPPTFileIntent(file);
            } else if (checkEndsWithInFileType(type, getStringArray(context, R.array.file_type_apk))) {
                intent = OpenFileIntent.getApkFileIntent(file);
            } else if (checkEndsWithInFileType(type, getStringArray(context, R.array.file_type_audio))) {
                intent = OpenFileIntent.getAudioFileIntent(file);
            } else if (checkEndsWithInFileType(type, getStringArray(context, R.array.file_type_video))) {
                intent = OpenFileIntent.getVideoFileIntent(file);
            } else if ((checkEndsWithInFileType(type, getStringArray(context, R.array.file_type_rar))) ||
                    checkEndsWithInFileType(type, getStringArray(context, R.array.file_type_zip))) {
                intent = OpenFileIntent.getZipFileIntent(file);
            }

            if (file.exists() && intent != null) {
                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    MainApp.showToastMsg("can't open this file, please install related app to open it.");
                }

            } else {
                MainApp.showToastMsg("can't open this file.");
            }
        } else {
            MainApp.showToastMsg("can't open this file.");
        }

    }

    /**
     * 判断文件类型
     */
    public static boolean checkEndsWithInFileType(String fileType, String[] fileEnds) {

        if (StringUtil.isBlank(fileType)) return false;

        for (String end : fileEnds) {
            if (fileType.trim().equals(end.trim()))
                return true;
        }
        return false;
    }

    private static String[] getStringArray(Context context, int id) {
        return context.getResources().getStringArray(id);
    }

    /**
     * Copy file using NOI
     *
     * @param source
     * @param dest
     */
    public static void copyFile(String source, String dest) throws IOException {
        File sourceFile = new File(source);
        File destFile = new File(dest);
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(sourceFile).getChannel();
            outputChannel = new FileOutputStream(destFile).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            try {
                inputChannel.close();
                outputChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String createCompressedImagePath(String path) {
        File file = new File(path);
        return Constant.FILE_DIR_COMPRESSED + "/" + file.getName();
    }

    public static void createCacheFileFromStream(Bitmap bitmap, String path) {
        File file = new File(path);
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getFilePath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if ("com.android.externalStorage.documents".equalsIgnoreCase(uri.getAuthority())) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type) || "content".equalsIgnoreCase(uri.getScheme())) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if ("com.android.providers.downloads.documents".equalsIgnoreCase(uri.getAuthority())) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if ("com.android.providers.media.documents".equalsIgnoreCase(uri.getAuthority())) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return "";
    }

    static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return "";
    }

    public static String getFileScheme(@NonNull final String filePath) {
        if (TextUtils.isEmpty(filePath)) return "";
        return filePath.substring(filePath.lastIndexOf(".") + 1);
    }

}
