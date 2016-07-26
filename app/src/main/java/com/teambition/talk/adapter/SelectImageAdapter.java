package com.teambition.talk.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.ImageMedia;
import com.teambition.talk.util.DensityUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by jgzhu on 10/8/14.
 */
public class SelectImageAdapter extends BaseAdapter {
    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_CAMERA = 0;
    private static final int VIEW_TYPE_IMG = 1;

    private ColorDrawable emptyDrawable = new ColorDrawable(Color.rgb(170, 170, 168));
    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(false)
            .showImageOnLoading(emptyDrawable)
            .showImageOnFail(emptyDrawable)
            .build();
    private Context context;
    private LayoutInflater layoutInflater;
    private List<ImageMedia> mediaModelList = new ArrayList<>();
    private HashSet<String> selectedModels = new HashSet<>();
    private boolean isSingleChoice;

    public SelectImageAdapter(Context context, boolean isSingleChoice) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.isSingleChoice = isSingleChoice;
    }

    private ViewHolder getHolder(final View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        if (holder == null) {
            holder = new ViewHolder(view);
            view.setTag(holder);
        }
        return holder;
    }

    @Override
    public int getCount() {
        return mediaModelList.size() + 1;
    }

    @Override
    public Object getItem(int i) {
        if (i == 0) {
            return null;
        } else {
            return mediaModelList.get(i - 1);
        }
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        int screenWidth = DensityUtil.screenWidthInPix(context);
        if (getItemViewType(position) == VIEW_TYPE_CAMERA) {
            convertView = layoutInflater.inflate(R.layout.grid_select_image_camera, parent, false);
            View view = convertView.findViewById(R.id.frame);
            setViewSize(view, screenWidth / 3, screenWidth / 3);
            return convertView;
        } else {
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.grid_select_image_media, parent, false);
            }
        }
        final ViewHolder holder = getHolder(convertView);
        if (isSingleChoice) {
            holder.cb.setVisibility(View.GONE);
            holder.imageView.setClickable(false);
        } else {
            holder.imageView.setClickable(true);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.cb.performClick();
                }
            });
        }
        holder.cb.setOnCheckedChangeListener(null);

        setViewSize(holder.imageView, screenWidth / 3, screenWidth / 3);
        ImageMedia mediaModel = (ImageMedia) getItem(position);
        String url = "content://thumb/" + mediaModel.id;
        MainApp.IMAGE_LOADER.displayImage(url, holder.imageView, displayImageOptions);
        holder.cb.setChecked(mediaModel.status);
        holder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeStatus(position);
            }
        });
        return convertView;
    }


    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_CAMERA;
        } else {
            return VIEW_TYPE_IMG;
        }
    }

    public void replaceAll(List<ImageMedia> mediaModels) {
        mediaModelList.clear();
        mediaModelList.addAll(mediaModels);
        notifyDataSetChanged();
    }

    public void changeStatus(int position) {
        ImageMedia mediaModel = (ImageMedia) getItem(position);
        mediaModel.status = !mediaModel.status;
        mediaModelList.remove(position - 1);
        mediaModelList.add(position - 1, mediaModel);
        if (mediaModel.status) {
            selectedModels.add(mediaModel.url);
        } else {
            selectedModels.remove(mediaModel.url);
        }
    }

    public HashSet<String> getSelectedModels() {
        return selectedModels;
    }

    private void setViewSize(View view, int height, int width) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = height;
        layoutParams.width = width;
        view.setLayoutParams(layoutParams);
    }

    static class ViewHolder {
        ImageView imageView;
        CheckBox cb;

        public ViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.image);
            cb = (CheckBox) view.findViewById(R.id.checkbox);
        }
    }
}
