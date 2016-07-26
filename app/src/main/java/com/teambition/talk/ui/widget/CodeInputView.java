package com.teambition.talk.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.ColorRes;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import com.teambition.talk.R;

/**
 * Created by wlanjie on 15/9/1.
 */
public class CodeInputView extends EditText {

    private float mLineHeight;
    private float mLineInterval;
    private int mLineColor;
    private float mTextSize;
    private int mTextColor;
    private Paint mLinePaint;
    private Paint mTextPaint;
    private int mCodeSize;
    private float mMarginBottom;
    private TextChangeListener mListener;
    private OnDeleteKeyListener mDeleteListener;

    public CodeInputView(Context context) {
        super(context);
        initPaint();
    }

    public CodeInputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CodeInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttributeSet(context, attrs, defStyleAttr);
        initPaint();
    }

    void initAttributeSet(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CodeInputView);
        mLineHeight = a.getDimension(R.styleable.CodeInputView_lineHeight, 0);
        mLineInterval = a.getDimension(R.styleable.CodeInputView_lineInterval, 0);
        mLineColor = a.getColor(R.styleable.CodeInputView_lineColor, getResources().getColor(android.R.color.black));
        mTextSize = a.getDimension(R.styleable.CodeInputView_textSize, 0);
        mTextColor = a.getColor(R.styleable.CodeInputView_textColor, getResources().getColor(android.R.color.black));
        mCodeSize = a.getInteger(R.styleable.CodeInputView_codeSize, 0);
        mMarginBottom = a.getDimension(R.styleable.CodeInputView_marginBottom, 0);
        a.recycle();
    }

    void initPaint() {
        mLinePaint = new Paint();
        mLinePaint.setColor(mLineColor);
        mLinePaint.setStrokeWidth(mLineHeight);
        mTextPaint = new Paint();
        mTextPaint.setColor(mTextColor);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mTextSize);

        setFocusableInTouchMode(true);
        addTextChangedListener(textWatcher);
        setLongClickable(false);
    }

    @Override
    public boolean isSuggestionsEnabled() {
        return false;
    }

    public void setOnDeleteKeyListener(OnDeleteKeyListener listener) {
        mDeleteListener = listener;
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            final String text = getText().toString();
            if (start < mCodeSize) {
                if (count < before) {
                    if (mDeleteListener != null) {
                        mDeleteListener.onDeleteKey(text.length());
                    }
                } else {
                    if (mListener != null && text.length() == mCodeSize) {
                        mListener.onTextChange(text);
                    }
                }
                invalidate();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    public void setTextChangeListener(TextChangeListener listener) {
        removeTextChangedListener(textWatcher);
        addTextChangedListener(textWatcher);
        mListener = listener;
    }

    public int getCodeSize() {
        return mCodeSize;
    }

    public void setLineColor(@ColorRes int color) {
        mLinePaint.setColor(getResources().getColor(color));
        invalidate();
    }

    public void setLineHeight(int height) {
        mLineHeight = height;
        invalidate();
    }

    public void setLineInterval(int interval) {
        mLineInterval = interval;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < mCodeSize; i++) {
            float fromX = getMeasuredWidth() / mCodeSize * i;
            float toX = fromX + getMeasuredWidth() / mCodeSize;
            canvas.drawLine(fromX + mLineInterval, getMeasuredHeight(), toX, getMeasuredHeight(), mLinePaint);
            String text = getText().toString();
            if (!text.isEmpty() && text.length() > i) {
                drawText(fromX, toX, text.subSequence(i, i + 1).toString(), canvas);
            }
        }
    }

    private void drawText(float fromX, float toX, String text, Canvas canvas) {
        float actualWidth = toX - fromX;
        float centerWidth = actualWidth / 2;
        float centerX = fromX + centerWidth;
        canvas.drawText(text, centerX, mMarginBottom == 0 ? getMeasuredHeight() / 2 : getMeasuredHeight() - mMarginBottom, mTextPaint);
    }

    public interface TextChangeListener {
        void onTextChange(String s);
    }

    public interface OnDeleteKeyListener {
        void onDeleteKey(int length);
    }
}
