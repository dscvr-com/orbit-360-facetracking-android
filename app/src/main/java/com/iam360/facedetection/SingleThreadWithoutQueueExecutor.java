package com.iam360.facedetection;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.iam360.engine.connection.BluetoothConnectionReciever;
import com.iam360.engine.connection.BluetoothEngineControlService;
import com.iam360.facetracking.BluetoothCameraApplicationContext;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * Created by Charlotte on 30.11.2016.
 */
public class SingleThreadWithoutQueueExecutor implements Executor {
    private final BluetoothEngineControlService motorControlService;
    private Thread current;
    private FaceDetection faceDetection;

    public SingleThreadWithoutQueueExecutor(Context context, FaceDetection.FaceDetectionResultListener[] otherListeners) {
        motorControlService = ((BluetoothCameraApplicationContext
                ) context.getApplicationContext()).getBluetoothService();

        faceDetection = new FaceDetection(context);
        faceDetection.addFaceDetectionResultListener(new FaceDetection.FaceDetectionResultListener() {
            @Override
            public void facesDetected(List<RectF> rects, int width, int height) {
                try {
                    if(motorControlService!= null) motorControlService.reactOnFaces(rects, width, height);
                } catch (BluetoothEngineControlService.NoBluetoothConnectionException e) {
                    if (!((BluetoothCameraApplicationContext) context.getApplicationContext()).isInDemo())
                        context.sendBroadcast(new Intent(BluetoothConnectionReciever.DISCONNECTED));
                }
            }
        });
        for (FaceDetection.FaceDetectionResultListener otherListener : otherListeners) {
            faceDetection.addFaceDetectionResultListener(otherListener);
        }

    }

    public FaceDetection getFaceDetection() {
        return faceDetection;
    }

    @Override
    public void execute(@NonNull Runnable command) {
        if (current == null || !current.isAlive()) {
            current = new Thread(command);
            current.start();
        }
    }

    public void addFaceDetection(byte[] data, final int width, final int height, final int orientation) {
        this.execute(new FaceDetectionRunnable(data, width, height, orientation));
    }

    private class FaceDetectionRunnable implements Runnable {

        private static final String TAG = "FaceDetectionRunnable";
        private final int height;
        private final int width;
        private final byte[] data;
        private final int orientation;

        public FaceDetectionRunnable(byte[] data, final int width, final int height, final int orientation) {
            this.data = data;
            this.width = width;
            this.height = height;
            this.orientation = orientation;
        }

        @Override
        public void run() {
            faceDetection.detect(data, width, height, orientation);
        }
    }
}
