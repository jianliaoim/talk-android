package com.teambition.talk.ui;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.teambition.talk.R;

/**
 * Created by wlanjie on 15/9/17.
 */
public class GuideDialog extends AppCompatDialog {

    final Context mContext;
    final Builder mBuilder;

    protected GuideDialog(Context context, Builder builder) {
        this(context, 0, builder);
    }

    protected GuideDialog(Context context, int theme, Builder builder) {
        super(context, theme);
        this.mContext = context;
        this.mBuilder = builder;
        setContentView(getContentView());
    }

    protected GuideDialog(Context context, boolean cancelable, OnCancelListener cancelListener, Builder builder) {
        super(context, cancelable, cancelListener);
        this.mContext = context;
        this.mBuilder = builder;
        setContentView(getContentView());
    }

    public View getContentView() {
        final View view = getLayoutInflater().inflate(R.layout.dialog_match_background, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.dialog_img);
        imageView.setImageResource(mBuilder.contentImageRes);
        TextView title = (TextView) view.findViewById(R.id.dialog_title);
        title.setText(mBuilder.title);
        TextView content = (TextView) view.findViewById(R.id.dialog_content);
        content.setText(mBuilder.content);
        TextView aLaterDateView = (TextView) view.findViewById(R.id.a_later_date);
        if (!TextUtils.isEmpty(mBuilder.neutralText)) {
            aLaterDateView.setText(mBuilder.neutralText);
        }
        if (!mBuilder.neutralVisible) {
             aLaterDateView.setVisibility(View.GONE);
        }

        aLaterDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBuilder.autoDismiss) {
                    dismiss();
                }
                if (mBuilder.neutralClickListener != null) {
                    mBuilder.neutralClickListener.onNeutralClick(v);
                }
            }
        });
        TextView immediatelyUsingView = (TextView) view.findViewById(R.id.immediately_using);
        if (!mBuilder.positiveVisible) {
            immediatelyUsingView.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(mBuilder.positiveText)) {
            immediatelyUsingView.setText(mBuilder.positiveText);
        }
        immediatelyUsingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBuilder.autoDismiss) {
                    dismiss();
                }
                if (mBuilder.positiveClickListener != null) {
                    mBuilder.positiveClickListener.onPositiveClick(v);
                }
            }
        });
        return view;
    }

    public interface OnPositiveClickListener {
        void onPositiveClick(View view);
    }

    public interface OnNeutralClickListener {
        void onNeutralClick(View view);
    }

    public static class Builder {

        final Context context;
        final int theme;
        String title;
        String content;
        String positiveText;
        String neutralText;
        int contentImageRes;
        boolean autoDismiss = true;
        boolean neutralVisible = true;
        boolean positiveVisible = true;
        OnPositiveClickListener positiveClickListener;
        OnNeutralClickListener neutralClickListener;

        public Builder(Context context) {
            this(context, 0);
        }

        public Builder(Context context, int theme) {
            this.context = context;
            this.theme = theme;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setTitle(@StringRes int title) {
            setTitle(context.getString(title));
            return this;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setContent(@StringRes int content) {
            setContent(context.getString(content));
            return this;
        }

        public Builder setPositiveText(String positiveText) {
            this.positiveText = positiveText;
            return this;
        }

        public Builder setPositiveTextRes(@StringRes int positiveText) {
            setPositiveText(context.getString(positiveText));
            return this;
        }

        public Builder setNegativeText(String negativeText) {
            this.neutralText = negativeText;
            return this;
        }

        public Builder setNegativeTextRes(@StringRes int negativeText) {
            setNegativeText(context.getString(negativeText));
            return this;
        }

        public Builder setContentImageRes(@DrawableRes int contentImageRes) {
            this.contentImageRes = contentImageRes;
            return this;
        }

        public Builder setOnPositiveClickListener(OnPositiveClickListener l) {
            this.positiveClickListener = l;
            return this;
        }

        public Builder setOnNeutralClickListener(OnNeutralClickListener l) {
            this.neutralClickListener = l;
            return this;
        }

        public Builder setAutoDismiss(boolean autoDismiss) {
            this.autoDismiss = autoDismiss;
            return this;
        }

        public Builder setNegativeVisible(boolean negativeVisible) {
            this.neutralVisible = negativeVisible;
            return this;
        }

        public Builder setPositiveVisible(boolean positiveVisible) {
            this.positiveVisible = positiveVisible;
            return this;
        }

        public Builder show() {
            GuideDialog dialog = new GuideDialog(context, theme, this);
            dialog.show();
            return this;
        }
    }
}
