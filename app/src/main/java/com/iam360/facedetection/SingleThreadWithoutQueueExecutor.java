package com.iam360.facedetection;

import android.content.Context;
import android.support.annotation.NonNull;
import com.iam360.engine.connection.BluetoothEngineControlService;
import com.iam360.myapplication.BluetoothCameraApplicationContext;

import java.util.concurrent.Executor;

/**
 * Created by Charlotte on 30.11.2016.
 */
public class SingleThreadWithoutQueueExecutor implements Executor {
    private final BluetoothEngineControlService motorControlService;
    private Thread current;
    private FaceDetection faceDetection;

    public SingleThreadWithoutQueueExecutor(Context context) {
        motorControlService = ((BluetoothCameraApplicationContext
                ) context.getApplicationContext()).getBluetoothService();

        faceDetection = new FaceDetection(context);
        faceDetection.addFaceDetectionResultListener((rects, width, height) -> motorControlService.reactOnFaces(rects, width, height));
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

    public void addFaceDetection(byte[] data, final int width, final int height) {
        this.execute(new FaceDetectionRunnable(data, width, height));
    }

    private class FaceDetectionRunnable implements Runnable {

        private static final String TAG = "FaceDetectionRunnable";
        private final int height;
        private final int width;
        private final byte[] data;

        public FaceDetectionRunnable(byte[] data, final int width, final int height) {
            this.data = data;
            this.width = width;
            this.height = height;
        }

        @Override
        public void run() {
            faceDetection.detect(data, height, width);
        }
    }
}
