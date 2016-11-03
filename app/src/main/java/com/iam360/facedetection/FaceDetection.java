package com.iam360.facedetection;

import android.content.Context;
import android.util.Log;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Charlotte on 03.11.2016.
 */
public class FaceDetection {
    public static final String TAG = "FaceDetection";
    private CascadeClassifier detector;
    private File cascadeFile;

    public FaceDetection(Context context) {
        File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
        cascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
        detector = new CascadeClassifier(cascadeFile.getAbsolutePath());
        if (detector.empty()) {
            Log.e(TAG, "Failed to load cascade classifier");
            detector = null;
        } else
            Log.i(TAG, "Loaded cascade classifier from " + cascadeFile.getAbsolutePath());
    }

    public List<android.graphics.Rect> detect(byte[] data, int height, int width) {
        Mat image = new Mat(height, width, CvType.CV_8UC3);
        image.put(0, 0, data);
        MatOfRect resultMatOfRect = new MatOfRect();
        detector.detectMultiScale(image, resultMatOfRect);
        List<android.graphics.Rect> resultList = new ArrayList<>();
        android.graphics.Rect resultRect;
        for (Rect rectInWrongFormat : resultMatOfRect.toList()) {
            resultRect = new android.graphics.Rect(rectInWrongFormat.x, rectInWrongFormat.y,
                    rectInWrongFormat.x + rectInWrongFormat.width, rectInWrongFormat.y + rectInWrongFormat.height);
            resultList.add(resultRect);
        }
        return resultList;
    }
}
