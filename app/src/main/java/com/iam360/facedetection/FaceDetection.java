package com.iam360.facedetection;

import android.content.Context;
import android.util.Log;
import com.iam360.myapplication.R;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Charlotte on 03.11.2016.
 */
public class FaceDetection {
    public static final String TAG = "FaceDetection";
    private final int SIZE_OF_SCALLED_IMAGE = 240;
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

    public List<android.graphics.Rect> detect(byte[] data, int height, int width) {
        Log.d(TAG, "Started FaceDetection");
        Mat grey = getGreyMat(data, height, width);
        int scale = makeSmaller(grey);

        MatOfRect resultMatOfRect = new MatOfRect();
        detector.detectMultiScale(grey, resultMatOfRect);
        return resizeAndReformatFaces(resultMatOfRect.toList(), scale);

    }

    private List<android.graphics.Rect> resizeAndReformatFaces(List<Rect> rects, int scale) {
        List<android.graphics.Rect> resultsResized = new ArrayList<>(rects.size());
        android.graphics.Rect result;
        for (Rect face : rects) {
            result = new android.graphics.Rect(face.x * scale, face.y * scale,
                    (face.x + face.width) * scale, (face.y + face.height) * scale);
            resultsResized.add(result);
        }
        return resultsResized;
    }

    private int makeSmaller(Mat input) {
        int scale = 1;
        while (input.cols() > 240 && input.rows() > SIZE_OF_SCALLED_IMAGE) {
            Imgproc.pyrDown(input, input);
            scale *= 2;
        }
        return scale;
    }

    private Mat getGreyMat(byte[] data, int height, int width) {
        Mat rgba = new Mat(height, width, CvType.CV_8UC4);
        Mat grey = new Mat(height, width, CvType.CV_8UC1);
        rgba.put(0, 0, data);
        Imgproc.cvtColor(rgba, grey, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.equalizeHist(grey, grey);
        return grey;
    }
}
