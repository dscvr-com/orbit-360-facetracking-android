package com.iam360.facedetection;

import android.content.Context;
import android.util.Log;
import com.iam360.myapplication.R;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.objdetect.CascadeClassifier;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Charlotte on 03.11.2016.
 */
public class FaceDetection {
    public static final String TAG = "FaceDetection";
    private CascadeClassifier detector;

    public FaceDetection(Context context) {
        try {
            InputStream input = context.getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            File cascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream out = new FileOutputStream(cascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            input.close();
            out.close();
            detector = new CascadeClassifier(cascadeFile.getAbsolutePath());
            detector.load(cascadeFile.getAbsolutePath());
            if (detector.empty()) {
                Log.e(TAG, "Failed to load cascade classifier");
                detector = null;
            } else
                Log.i(TAG, "Loaded cascade classifier from " + cascadeFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Filed to load cascade classifier", e);
        }
    }

    public List<android.graphics.Rect> detect(Mat data, int height, int width) {
        Log.d(TAG, "Started FaceDetection");
        MatOfRect resultMatOfRect = new MatOfRect();
        detector.detectMultiScale(data, resultMatOfRect);
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
