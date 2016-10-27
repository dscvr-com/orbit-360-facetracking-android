package com.iam360.views.record;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
/**
 * Created by emi on 16/06/16.
 * Taken from google's samples.
 */
public class AutoFitTextureView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "AutoFitTextureView";
    private int mRatioWidth = 0;
    private int mRatioHeight = 0;
    private boolean isAvailable;
    public AutoFitTextureView(Context context) {
        this(context, null);
    }
    public AutoFitTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public AutoFitTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getHolder().addCallback(this);
    }
    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that
     * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param width  Relative horizontal size
     * @param height Relative vertical size
     */
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        Log.w(TAG, "Re-layouting with aspect: " + mRatioWidth + "x" + mRatioHeight + ", Size: " + width + "x" + height);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width > (height * mRatioWidth) / mRatioHeight) {
                Log.w(TAG, "Re-layouting result: " + width + "x" + (width * mRatioHeight) / mRatioWidth);
                setMeasuredDimension(width, (width * mRatioHeight) / mRatioWidth);
            } else {
                Log.w(TAG, "Re-layouting result: " + (height * mRatioHeight) / mRatioWidth + "x" + height);
                setMeasuredDimension((height * mRatioHeight) / mRatioWidth, height);
            }
        }
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isAvailable = true;
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isAvailable = false;
    }
    public boolean isAvailable() {
        return this.isAvailable;
    }
}