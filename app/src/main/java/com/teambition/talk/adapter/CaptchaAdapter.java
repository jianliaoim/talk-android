package com.teambition.talk.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.client.ApiConfig;
import com.teambition.talk.entity.Captcha;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by nlmartian on 1/8/16.
 */
public class CaptchaAdapter extends RecyclerView.Adapter {

    private Captcha captcha;
    private OnItemClickListener onItemClickListener;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_captcha, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final String itemValue = captcha.getValues().get(position);
        ViewHolder itemHolder = (ViewHolder) holder;
        String url = String.format(ApiConfig.AUTH_SERVICE_URL + "/captcha/image?uid=%s&lang=%s&index=%d",
                captcha.getUid(), captcha.getLang(), position);
        MainApp.IMAGE_LOADER.displayImage(url, itemHolder.imgCaptcha);

        itemHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(captcha.getUid(), itemValue);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (captcha == null) {
            return 0;
        } else {
            return captcha.getValues().size();
        }
    }

    public void setCaptcha(Captcha captcha) {
        this.captcha = captcha;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        public void onItemClick(String uid, String value);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.captcha)
        ImageView imgCaptcha;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}
