package com.teambition.talk.imageloader;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.teambition.talk.R;

/**
 * Created by ZZQ on 14-8-5.
 */
public class ImageLoaderConfig {

    public static final String PREFIX_DRAWABLE = "drawable://";
    public static final String PREFIX_FILE = "file://";

    public static DisplayImageOptions DEFAULT_OPTIONS = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.drawable_default_image)
            .showImageForEmptyUri(R.drawable.drawable_default_image)
            .showImageOnFail(R.drawable.drawable_default_image)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .build();

    public static DisplayImageOptions EMPTY_OPTIONS = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .build();

    public static DisplayImageOptions AVATAR_OPTIONS = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.ic_avatar_default)
            .showImageForEmptyUri(R.drawable.ic_avatar_default)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .build();

    public static DisplayImageOptions LINK_OPTIONS = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.ic_type_link)
            .showImageForEmptyUri(R.drawable.ic_type_link)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .build();

    public static DisplayImageOptions RTF_OPTIONS = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.ic_type_rtf)
            .showImageForEmptyUri(R.drawable.ic_type_rtf)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .build();

}
