package com.iam360.views.record;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

/**
 * Created by emi on 16/06/16.
 * Taken from google's samples.
 */
public class AutoFitTextureView extends TextureView {
    private static final String TAG = "AutoFitTextureView";
    private int mRatioWidth = 0;
    private int mRatioHeight = 0;
    public AutoFitTextureView(Context context) {
        this(context, null);
    }
    public AutoFitTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public AutoFitTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }
}