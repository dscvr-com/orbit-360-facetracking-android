package com.iam360.facedetection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.camera2.CameraDevice;
import android.util.Log;
import com.iam360.views.record.RecorderPreviewView;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgproc.Imgproc;

import java.util.List;

/**
 * Created by Charlotte on 03.11.2016.
 */
public class FaceTrackingListener implements RecorderPreviewView.RecorderPreviewListener {
    public static final String TAG = "FaceTrackingListener";
    private FaceDetection faceDetection;

    public FaceTrackingListener(Context context){
        faceDetection = new FaceDetection(context);
    }

    @Override
    public void imageDataReady(byte[] data, int width, int height, Bitmap.Config colorFormat) {
        Mat rgba = new Mat(height, width, CvType.CV_8UC4);
        Mat grey = new Mat(height,width, CvType.CV_8UC1);
        rgba.put(0, 0, data);
        Imgproc.cvtColor(rgba, grey, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.equalizeHist(grey,grey);
        List<Rect> detectionResult = faceDetection.detect(grey, height, width);
        Log.i(TAG, String.format("Found %d faces\n", detectionResult.size()));
    }

    @Override
    public void cameraOpened(CameraDevice device) {

    }

    @Override
    public void cameraClosed(CameraDevice device) {

    }
}
