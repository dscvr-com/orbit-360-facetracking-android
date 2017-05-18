package com.iam360.views.record;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.util.Size;
import android.view.View;

import com.iam360.facetracking.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lotti on 5/11/2017.
 */

public class OverlayCanvasView extends View {
    private Matrix transform;
    private List<RectF> rects = new ArrayList();

    Paint paint = new Paint();

    public OverlayCanvasView(Context context, Matrix transform) {
        super(context);
        setWillNotDraw(false);
        this.transform = transform;
    }


    public void setRects(List<RectF> rects) {
        this.rects = rects;
    }

    public void setTransform(Matrix transform) {
        this.transform = transform;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        for (RectF rect : rects) {
            paint.setColor(getResources().getColor(R.color.orbitOrange));
            //used depreacted because not deprecated version needs api23 we use minversion 21
            paint.setStrokeWidth(3);
            paint.setStyle(Paint.Style.STROKE);

            RectF res = new RectF();
            transform.mapRect(res, rect);
            Log.d("RECT1", "x: " + rect.centerX() + " y: " + rect.centerY());;
            Log.d("RECT2", "x: " + res.centerX() + " y: " + res.centerY());
            canvas.drawRect(rect, paint);
        }
        super.onDraw(canvas);
    }


}