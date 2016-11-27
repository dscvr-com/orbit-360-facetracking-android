package com.iam360.facedetection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.camera2.CameraDevice;
import android.util.Log;
import com.iam360.motor.connection.BluetoothMotorControlService;
import com.iam360.myapplication.BluetoothCameraApplicationContext;
import com.iam360.views.record.RecorderPreviewView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Charlotte on 03.11.2016.
 */
public class FaceTrackingListener implements RecorderPreviewView.RecorderPreviewListener {
    public static final String TAG = "FaceTrackingListener";
    private final BluetoothMotorControlService motorControlService;
    private FaceDetection faceDetection;
    private ExecutorService executor = Executors.newFixedThreadPool(10);

    public FaceTrackingListener(Context context){
        motorControlService = ((BluetoothCameraApplicationContext
                ) context.getApplicationContext()).getBluetoothService();
        faceDetection = new FaceDetection(context);
    }

    @Override
    public void imageDataReady(final byte[] data, final int width, final int height, Bitmap.Config colorFormat) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<Rect> detectionResult = faceDetection.detect(data, height, width);
                motorControlService.reactOnFaces(detectionResult, width, height);
                Log.d(TAG, String.format("Found %d faces\n", detectionResult.size()));
            }
        });
    }

    @Override
    public void cameraOpened(CameraDevice device) {

    }

    @Override
    public void cameraClosed(CameraDevice device) {

    }

}
