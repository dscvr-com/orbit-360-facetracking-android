package com.iam360.facedetection;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraDevice;
import com.iam360.views.record.RecorderPreviewListener;

/**
 * Created by Charlotte on 03.11.2016.
 */
public class FaceTrackingListener implements RecorderPreviewListener {
    public static final String TAG = "FaceTrackingListener";
    private SingleThreadWithoutQueueExecutor executor;

    public FaceTrackingListener(Context context){
        executor = new SingleThreadWithoutQueueExecutor(context);
    }

    public FaceDetection getFaceDetection() {
        return executor.getFaceDetection();
    }

    @Override
    public void imageDataReady(final byte[] data, final int width, final int height, Bitmap.Config colorFormat) {
        executor.addFaceDetection(data, width, height);
    }

    @Override
    public void cameraOpened(CameraDevice device) {

    }

    @Override
    public void cameraClosed(CameraDevice device) {

    }

}
