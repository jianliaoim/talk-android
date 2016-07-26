package com.teambition.talk.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by nlmartian on 4/30/15.
 */
public class AudioMessageView extends View {

    private float progressRatio = 0f;
    private int unreachedColor = Color.parseColor("#EBEDF7");
    private int reachedColor = Color.parseColor("#DCDFF0");
    private String strText = "";
    private int textColor = Color.parseColor("#212121");
    private boolean hasBorder = false;
    private int borderColor = Color.parseColor("#5C6BC0");
    private float borderWidth = 1f;
    private float textSize = 16f;
    private Drawable buttonDrawable;

    private Paint unreachedPaint;
    private Paint reachedPaint;
    private Paint textPaint;
    private Paint borderPaint;

    private RectF startRect;
    private RectF endRect;
    private RectF middleRect;

    public AudioMessageView(Context context) {
        this(context, null);
    }

    public AudioMessageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioMessageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        unreachedPaint = new Paint();
        unreachedPaint.setStyle(Paint.Style.FILL);
        unreachedPaint.setAntiAlias(true);

        reachedPaint = new Paint();
        reachedPaint.setStyle(Paint.Style.FILL);
        reachedPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(dip2px(textSize));

        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);
        borderPaint.setStrokeWidth(dip2px(borderWidth));

        startRect = new RectF();
        endRect = new RectF();
        middleRect = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width;
        int height;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(widthSize, getSuggestedMinimumWidth());
        } else {
            width = getSuggestedMinimumWidth();
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(heightSize, getSuggestedMinimumHeight());
        } else {
            height = getSuggestedMinimumHeight();
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        setPaintColor();

        int width = getWidth();
        int height = getHeight();
        int reachedWidth = (int) (width * progressRatio);

        startRect.set(0, 0, height, height);
        middleRect.set(height / 2, 0, width - height / 2, height);
        endRect.set(width - height, 0, width, height);

        if (reachedWidth <= width - height / 2) {
            canvas.drawRect(middleRect, unreachedPaint);
            canvas.drawArc(startRect, 90f, 180f, true, unreachedPaint);
            canvas.drawArc(endRect, -90f, 180f, true, unreachedPaint);

            Path reachedPath = new Path();
            int r = height / 2;
            int x = reachedWidth > r ? r : reachedWidth;
            double startAngle = Math.toDegrees(Math.asin((double) (r - x) / r));
            int y = Math.abs((int) ((r - x) / Math.tan(startAngle)));
            reachedPath.moveTo(x, r + y);
            reachedPath.addArc(startRect, (float) (90f + startAngle), (float) (180f - startAngle * 2));
            reachedPath.lineTo(x, r + y);
            reachedPath.close();
            canvas.drawPath(reachedPath, reachedPaint);
            if (reachedWidth > height / 2) {
                canvas.drawRect(height / 2, 0, reachedWidth, height, reachedPaint);
            }
        } else {
            canvas.drawRect(middleRect, reachedPaint);
            canvas.drawArc(startRect, 90f, 180f, true, reachedPaint);
            canvas.drawArc(endRect, -90f, 180f, true, reachedPaint);

            Path unreachedPath = new Path();
            int r = height / 2;
            int x = reachedWidth - (width - height / 2);
            double startAngle = Math.toDegrees(Math.asin((double)  x / r));
            int y = Math.abs((int) (x / Math.tan(startAngle)));
            unreachedPath.moveTo(reachedWidth, r - y);
            unreachedPath.addArc(endRect, (float) (startAngle - 90f), (float) (180f - startAngle * 2));
            unreachedPath.moveTo(reachedWidth, r - y);
            unreachedPath.close();
            canvas.drawPath(unreachedPath, unreachedPaint);
        }

        // draw text
        canvas.drawText(strText, width - textPaint.measureText(strText) - dip2px(12f),
                height / 2 + textSize, textPaint);

        // draw border
        if (hasBorder) {
            drawBorder(canvas);
        }

        // draw button
        if (buttonDrawable != null) {
            buttonDrawable.setBounds(dip2px(12f), (height - dip2px(20f)) / 2,  dip2px(32f),
                    (height + dip2px(20f)) / 2);
            buttonDrawable.draw(canvas);
        }
    }

    private void setPaintColor() {
        unreachedPaint.setColor(unreachedColor);
        reachedPaint.setColor(reachedColor);
        borderPaint.setColor(borderColor);
        textPaint.setColor(textColor);
    }

    private void drawBorder(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        int border = dip2px(borderWidth);
        Path borderPath = new Path();

        startRect.left += border / 2;
        startRect.top += border / 2;
        startRect.bottom -= border / 2;
        borderPath.addArc(startRect, 90f, 180f);
        borderPath.moveTo(height / 2, height - border / 2);
        borderPath.lineTo(width - height / 2, height - border / 2);

        endRect.bottom -= border / 2;
        endRect.right -= border / 2;
        endRect.top += border / 2;
        borderPath.addArc(endRect, -90f, 180f);
        borderPath.moveTo(width - height / 2, border / 2);
        borderPath.lineTo(height / 2, border / 2);
        borderPath.close();
        canvas.drawPath(borderPath, borderPaint);
    }

    public void setProgressRatio(float progressRatio) {
        this.progressRatio = progressRatio;
        invalidate();
    }

    public void setUnreachedColor(int unreachedColor) {
        this.unreachedColor = unreachedColor;
        invalidate();
    }

    public void setReachedColor(int reachedColor) {
        this.reachedColor = reachedColor;
        invalidate();
    }

    public void setText(String text) {
        this.strText = text;
        invalidate();
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        invalidate();
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
        invalidate();
    }

    public void setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
        invalidate();
    }

    public void setHasBorder(boolean hasBorder) {
        this.hasBorder = hasBorder;
        invalidate();
    }

    public void setButtonDrawable(Drawable buttonDrawable) {
        this.buttonDrawable = buttonDrawable;
        invalidate();
    }

    public void setButtonDrawable(int res) {
        this.buttonDrawable = getContext().getResources().getDrawable(res);
        invalidate();
    }

    private int dip2px(float dipValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
