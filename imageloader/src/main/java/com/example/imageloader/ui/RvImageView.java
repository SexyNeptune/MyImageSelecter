package com.example.imageloader.ui;

import android.content.Context;
import android.util.AttributeSet;

public class RvImageView extends android.support.v7.widget.AppCompatImageView {
    public RvImageView(Context context) {
        super(context);
    }

    public RvImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RvImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measureWidth = getMeasuredWidth();
        int measureHeight = getMeasuredHeight();
        if (measureHeight > measureWidth) {
            measureHeight = measureWidth;
        }else{
            measureWidth = measureHeight;
        }
        setMeasuredDimension(measureWidth,measureHeight);
    }
}
