package com.example.imageloader.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;

public class ZoomImageView extends android.support.v7.widget.AppCompatImageView implements ScaleGestureDetector.OnScaleGestureListener,
        View.OnTouchListener, ViewTreeObserver.OnGlobalLayoutListener

{
    private static final String TAG = ZoomImageView.class.getSimpleName();

    public static final float SCALE_MAX = 4.0f;
    /**
     * 初始化时的缩放比例，如果图片宽或高大于屏幕，此值将小于0
     */
    private float initScale = 1.0f;

    /**
     * 用于存放矩阵的9个值
     */
    private final float[] matrixValues = new float[9];

    private boolean once = true;

    /**
     * 缩放的手势检测
     */
    private ScaleGestureDetector mScaleGestureDetector;

    private final Matrix mScaleMatrix = new Matrix();
    private boolean isCheckLeftAndRight;
    private boolean isCheckTopAndBottom;
    private double mTouchSlop;
    private long startTime,endTime;

    public ZoomImageView(Context context) {
        this(context, null);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setScaleType(ScaleType.MATRIX);
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        setOnTouchListener(this);
    }


    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scale = getScale();
        float scaleFactor = detector.getScaleFactor();
        Log.e(TAG, "scaleFactor : " + scaleFactor + "scale:" + scale);
        if (getDrawable() == null)
            return true;
        /**
         * 缩放的范围控制
         */
        if ((scale < SCALE_MAX && scaleFactor > 1.0f) || (scale > initScale && scaleFactor < 1.0f)) {
            /**
             * 最大值最小值判断
             */
            if (scaleFactor * scale < initScale) {
                scaleFactor = initScale / scale;
            }
            if (scaleFactor * scale > SCALE_MAX) {
                scaleFactor = SCALE_MAX / scale;
            }
            /**
             * 设置缩放比例
             */
            mScaleMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(),
                    detector.getFocusY());
            checkBorderAndCenterWhenScale();
            setImageMatrix(mScaleMatrix);
        }
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }


    /**
     * 获得当前的缩放比例
     *
     * @return
     */
    public final float getScale() {
        mScaleMatrix.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X];
    }

    /**
     * 在缩放时，进行图片显示范围的控制
     */
    private void checkBorderAndCenterWhenScale() {
        RectF rect = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;

        int width = getWidth();
        int height = getHeight();

        // 如果宽或高大于屏幕，则控制范围
        if (rect.width() >= width) {
            if (rect.left > 0) {
                deltaX = -rect.left;
            }
            if (rect.right < width) {
                deltaX = width - rect.right;
            }
        }
        if (rect.height() >= height) {
            if (rect.top > 0) {
                deltaY = -rect.top;
            }
            if (rect.bottom < height) {
                deltaY = height - rect.bottom;
            }
        }
        // 如果宽或高小于屏幕，则让其居中
        if (rect.width() < width) {
            deltaX = width * 0.5f - rect.right + 0.5f * rect.width();
        }
        if (rect.height() < height) {
            deltaY = height * 0.5f - rect.bottom + 0.5f * rect.height();
        }
//        Log.e(TAG, "deltaX = " + deltaX + " , deltaY = " + deltaY);
        mScaleMatrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 根据当前图片的Matrix获得图片的范围
     *
     * @return
     */
    private RectF getMatrixRectF()
    {
        Matrix matrix = mScaleMatrix;
        RectF rect = new RectF();
        Drawable d = getDrawable();
        if (null != d) {
            rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            matrix.mapRect(rect);
        }
        return rect;
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }



    @SuppressWarnings("deprecation")
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeGlobalOnLayoutListener(this);
    }

    @SuppressLint("NewApi")
    @Override
    public void onGlobalLayout() {
//        if (once) {
        ViewTreeObserver observer = getViewTreeObserver();
        if (observer.isAlive()){
            observer.removeOnGlobalLayoutListener(this);
            Drawable d = getDrawable();
            if (d == null)
                return;
            int width = getMeasuredWidth();
        int height = getHeight();
        // 拿到图片的宽和高
        int dw = d.getIntrinsicWidth();
        int dh = d.getIntrinsicHeight();
        Log.e(TAG,  "宽: " + d.getIntrinsicWidth() + " , 高:" + d.getIntrinsicHeight() +  d.getCurrent());
//            // 如果图片的宽或者高大于屏幕，则缩放至屏幕的宽或者高
//            if (dw > width && dh <= height) {
//                scale = width * 1.0f / dw;
//            }
//            if (dh > height && dw <= width) {
//                scale = height * 1.0f / dh;
//            }
//            // 如果宽和高都大于屏幕，则让其按按比例适应屏幕大小
//            if (dw > width && dh > height) {
//                scale = Math.min(width * 1.0f / dw, height * 1.0f / dh);
//            }
            //直接让他适应屏幕大小
            float scale = Math.min(width * 1.0f / dw, height * 1.0f / dh);
            initScale = scale;
            // 图片移动至屏幕中心
            mScaleMatrix.postTranslate((width - dw) *1.0f / 2, (height - dh)*1.0f / 2);
            //缩放
            mScaleMatrix.postScale(scale, scale, getWidth() * 1.0f / 2, getHeight() * 1.0f / 2);
            setImageMatrix(mScaleMatrix);
//            once = false;
            Log.e(TAG,"onGlobalLayout");
        }
    }

    private float mLastX;
    private  float mLastY;
    private boolean isCanDrag;
    private int lastPointerCount;

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        Log.e(TAG , "onTouchEvent");
//        return super.onTouchEvent(event);
//    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        RectF rectF = getMatrixRectF();
        float x = 0, y = 0;
        // 拿到触摸点的个数
        final int pointerCount = event.getPointerCount();
        // 得到多个触摸点的x与y均值，因为在缩放的时候也会产生移动效果
        for (int i = 0; i < pointerCount; i++) {
            x += event.getX(i);
            y += event.getY(i);
        }
        x = x / pointerCount;
        y = y / pointerCount;

        /**
         * 每当触摸点发生变化时，重置mLasX , mLastY
         */
        if (pointerCount != lastPointerCount) {
            isCanDrag = false;
            mLastX = x;
            mLastY = y;
        }
        lastPointerCount = pointerCount;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startTime = System.currentTimeMillis();
                if (rectF.width() > getWidth() || rectF.height() > getHeight()) {
                    //当放大图片后请求反拦截
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
            case MotionEvent.ACTION_MOVE:
                Log.e(TAG, "ACTION_MOVE");
                float dx = x - mLastX;
                float dy = y - mLastY;

                if (!isCanDrag) {
                    isCanDrag = isCanDrag(dx, dy);
                }
                if (isCanDrag) {
//                    Log.e(TAG,"left :" + rectF.left +  "top:" + rectF.top + "right:" + rectF.right + "bottom:" + rectF.bottom );
                    if (getDrawable() != null) {
                        isCheckLeftAndRight = isCheckTopAndBottom = true;
                        // 如果宽度小于屏幕宽度，则禁止左右移动
                        if (rectF.width() < getWidth()) {
                            dx = 0;
                            isCheckLeftAndRight = false;
                        }
                        // 如果高度小于屏幕高度，则禁止上下移动
                        if (rectF.height() < getHeight()) {
                            dy = 0;
                            isCheckTopAndBottom = false;
                        }
                        //这样写不够严谨,因为滑动过快时，就无法滑动导致滑到边缘时会卡一下
//                        if (rectF.top + dy >= 0 || rectF.bottom + dy <= getHeight()){
//                            dy = 0;
//                        }
//                        if ( rectF.left + dx > 0 || rectF.right + dx < getWidth()){
//                            dx = 0;
//                        }
                        //移动相应距离
                        mScaleMatrix.postTranslate(dx, dy);
                        checkMatrixBounds();
                        setImageMatrix(mScaleMatrix);
                    }
                }
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                endTime = System.currentTimeMillis();
                if ((endTime - startTime)<200){
                    performClick();
                }
            case MotionEvent.ACTION_CANCEL:
                Log.e(TAG, "ACTION_UP");
                lastPointerCount = 0;
                break;
        }
        return true;
    }

    /**
     * 移动时，进行边界判断，主要判断宽或高大于屏幕的
     */
    private void checkMatrixBounds()
    {
        RectF rect = getMatrixRectF();

        float deltaX = 0, deltaY = 0;
        final float viewWidth = getWidth();
        final float viewHeight = getHeight();
        // 判断移动或缩放后，图片显示是否超出屏幕边界
        if (rect.top > 0 && isCheckTopAndBottom) {
            deltaY = -rect.top;
        }
        if (rect.bottom < viewHeight && isCheckTopAndBottom) {
            deltaY = viewHeight - rect.bottom;
        }
        if (rect.left > 0 && isCheckLeftAndRight) {
            deltaX = -rect.left;
        }
        if (rect.right < viewWidth && isCheckLeftAndRight) {
            deltaX = viewWidth - rect.right;
        }
        mScaleMatrix.postTranslate(deltaX, deltaY);
        if(deltaX != 0 || deltaY != 0){
            //只要有一个碰到边界就拦截
            getParent().requestDisallowInterceptTouchEvent(false);
        }
    }

    /**
     * 是否是推动行为
     *
     * @param dx
     * @param dy
     * @return
     */
    private boolean isCanDrag(float dx, float dy) {
        return Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
    }


}

