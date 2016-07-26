package com.teambition.talk.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.ImageMedia;
import com.teambition.talk.util.DensityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jgzhu on 10/8/14.
 */
public class ChooseImageAdapter extends RecyclerView.Adapter<ChooseImageAdapter.ViewHolder> {
    private static final int VIEW_TYPE_CAMERA = 0;
    private static final int VIEW_TYPE_IMG = 1;

    public interface OnItemClickListener {
        void onItemClick(int position, View view);
    }

    private ColorDrawable emptyDrawable = new ColorDrawable(Color.rgb(170, 170, 168));
    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(false)
            .showImageOnLoading(emptyDrawable)
            .showImageOnFail(emptyDrawable)
            .considerExifParams(true)
            .build();
    private OnItemClickListener listener;
    private LayoutInflater layoutInflater;
    private List<ImageMedia> mediaModelList = new ArrayList<>();
    final int size;

    private View oldSelectView;
    private View oldMaskView;
    private View oldItemView;
    private int selectPosition = 1;

    public ChooseImageAdapter(Context context, OnItemClickListener listener) {
        this.listener = listener;
        this.layoutInflater = LayoutInflater.from(context);
        int screenWidth = DensityUtil.screenWidthInPix(context);
        int dividerWidth = context.getResources().getDimensionPixelSize(R.dimen.image_story_divider);
        size = (screenWidth / 3) - dividerWidth;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_CAMERA:
                return new ViewHolder(layoutInflater.inflate(R.layout.grid_select_image_camera, parent, false));
            case VIEW_TYPE_IMG:
            default:
                return new ViewHolder(layoutInflater.inflate(R.layout.grid_select_image_media, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                listener.onItemClick(position, holder.itemView);
                if (position != 0) {
                    selectPosition = position;
                    holder.selectView.setVisibility(View.VISIBLE);
                    holder.mask.setVisibility(View.VISIBLE);
                    if (oldSelectView != null && oldSelectView != holder.selectView) {
                        oldSelectView.setVisibility(View.GONE);
                        oldMaskView.setVisibility(View.GONE);
                    }
                    oldMaskView = holder.mask;
                    oldSelectView = holder.selectView;

                    v.animate()
                            .scaleX(0.9f)
                            .scaleY(0.9f)
                            .setInterpolator(new FastOutSlowInInterpolator())
                            .setDuration(100)
                            .start();
                    if (oldItemView != null && oldItemView != v) {
                        oldItemView.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setInterpolator(new FastOutSlowInInterpolator())
                                .setDuration(100)
                                .start();
                    }
                    oldItemView = v;
                }
            }
        });
        if (position == 0) {
            setViewSize(holder.itemView, size, size);
        } else {
            if (position == selectPosition) {
                holder.selectView.setVisibility(View.VISIBLE);
                holder.mask.setVisibility(View.VISIBLE);
                holder.itemView.setScaleX(0.9f);
                holder.itemView.setScaleY(0.9f);
                oldMaskView = holder.mask;
                oldSelectView = holder.selectView;
                oldItemView = holder.itemView;
            } else {
                holder.itemView.setScaleX(1.0f);
                holder.itemView.setScaleY(1.0f);
                holder.mask.setVisibility(View.GONE);
                holder.selectView.setVisibility(View.GONE);
            }
            setViewSize(holder.itemView, size, size);
            ImageMedia mediaModel = (ImageMedia) getItem(position);
            String url = "content://thumb/" + mediaModel.id;
            MainApp.IMAGE_LOADER.displayImage(url, holder.imageView, displayImageOptions);
        }
    }

    @Override
    public int getItemCount() {
        return mediaModelList.size() + 1;
    }


    public Object getItem(int i) {
        if (i == 0) {
            return null;
        } else {
            return mediaModelList.get(i - 1);
        }
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

    private void setViewSize(View view, int height, int width) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = height;
        layoutParams.width = width;
        view.setLayoutParams(layoutParams);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        View selectView;
        View mask;

        public ViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.image);
            selectView = view.findViewById(R.id.select_item);
            mask = view.findViewById(R.id.mask);
        }
    }
}
