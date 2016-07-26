package com.teambition.talk.imageloader;

import android.content.Context;

import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * Created by nlmartian on 1/18/16.
 */
public class OkHttpImageDownloader extends BaseImageDownloader {
    public static final int CONNECTION_TIMEOUT = 5;
    public static final int READ_TIMEOUT = 20;


    private OkHttpClient client;

    public OkHttpImageDownloader(Context context, OkHttpClient client) {
        super(context);
        this.client = client;
        this.client.setConnectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS);
        this.client.setReadTimeout(READ_TIMEOUT, TimeUnit.SECONDS);
    }

    @Override
    protected InputStream getStreamFromNetwork(String imageUri, Object extra) throws IOException {
        Request request = new Request.Builder().url(imageUri).build();
        return client.newCall(request).execute().body().byteStream();
    }
}
