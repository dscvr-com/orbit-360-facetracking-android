package com.iam360.views.record;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
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
    private final Size wannabeVideoSize;
    private List<Rect> rects = new ArrayList();

    Paint paint = new Paint();

    public OverlayCanvasView(Context context, Size wannabeVideoSize) {
        super(context);
        setWillNotDraw(false);
        this.wannabeVideoSize = wannabeVideoSize;
    }


    public void setRects(List<Rect> rects) {
        this.rects = rects;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //wannabeVideoSize - >previewSize
        float scaleX = (float) this.getWidth() / (float) wannabeVideoSize.getWidth();
        float scaleY = (float) this.getHeight() / (float) wannabeVideoSize.getHeight();


        for (Rect rect : rects) {
            paint.setColor(getResources().getColor(R.color.orbitOrange));
            //used depreacted because not deprecated version needs api23 we use minversion 21
            paint.setStrokeWidth(3);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(rect.left * scaleX, rect.top * scaleY, rect.right * scaleX, rect.bottom * scaleY, paint);
        }
        super.onDraw(canvas);

    }


}
