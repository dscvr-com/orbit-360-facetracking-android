package com.iam360.facedetection;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Environment;
import android.util.Log;
import com.iam360.facetracking.R;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * starts faceDetection and informs listener about the result.
 * Created by Charlotte on 03.11.2016.
 */
public class FaceDetection {
    public static final String TAG = "FaceDetection";
    private final int SIZE_OF_SCALED_IMAGE = 120;
    private CascadeClassifier detector;
    private ArrayList<FaceDetectionResultListener> resultListeners = new ArrayList<>();
    private ArrayList<NonPermanentFaceDetectionResultListener> onlyOnceCalledListener = new ArrayList<>();

    public FaceDetection(Context context) {
        try {
            InputStream input = context.getResources().openRawResource(R.raw.haarcascade_frontalface_default);
            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            File cascadeFile = new File(cascadeDir, "haarcascade_frontalface_default.xml");
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

    public void addFaceDetectionResultListener(FaceDetectionResultListener listener) {
        resultListeners.add(listener);
    }

    public void addFaceDetectionResultListenerForNonPerm(NonPermanentFaceDetectionResultListener listener) {
        onlyOnceCalledListener.add(listener);
    }


    public boolean removeFaceDetectionResultListener(FaceDetectionResultListener listener) {
        return resultListeners.remove(listener);
    }

    public void detect(byte[] data, int width, int height, int orientation) {

        //Log.d("GET DATA", "w: " + width + ", h: " + height);
        Mat grey = getGreyMat(data, width, height, orientation);


        int scale = makeSmaller(grey);

        MatOfRect resultMatOfRect = new MatOfRect();
        detector.detectMultiScale(grey, resultMatOfRect);
        informListeners(resizeAndReformatFaces(resultMatOfRect.toList(), scale, scale), grey.width() * scale, grey.height() * scale);

    }

    private void informListeners(List<android.graphics.RectF> rects, int width, int height) {
        for (FaceDetectionResultListener listener : resultListeners) {
            listener.facesDetected(rects, width, height);
        }
        if (rects.size() >= 1) {
            for (NonPermanentFaceDetectionResultListener listener : onlyOnceCalledListener) {
                listener.facesDetected(rects, width, height);
            }
            onlyOnceCalledListener = new ArrayList<>();

        }
    }

    private List<android.graphics.RectF> resizeAndReformatFaces(List<Rect> rects, float sx, float sy) {
        List<android.graphics.RectF> resultsResized = new ArrayList<>(rects.size());
        for (Rect face : rects) {
            RectF res = new RectF(face.x * sx, face.y * sy, (face.x + face.width) * sx, (face.y + face.height) * sy);

            resultsResized.add(res);
        }
        return resultsResized;
    }

    private int makeSmaller(Mat input) {
        int scale = 1;
        while (input.cols() > SIZE_OF_SCALED_IMAGE && input.rows() > SIZE_OF_SCALED_IMAGE) {
            Imgproc.pyrDown(input, input);
            scale *= 2;
        }
        return scale;
    }

    private Mat getGreyMat(byte[] data, int width, int height, int orientation) {
        Mat rgba = new Mat(height, width, CvType.CV_8UC4);
        Mat grey = new Mat(height, width, CvType.CV_8UC1);

        rgba.put(0, 0, data);
        Imgproc.cvtColor(rgba, grey, Imgproc.COLOR_RGBA2GRAY);

        if (orientation == 270) {
            Core.transpose(grey, grey);
            Core.flip(grey, grey, 1);
        }
        if (orientation == 90) {
            Core.transpose(grey, grey);
            Core.flip(grey, grey, 0);
        }


        // Writes debug images:
        /*Imgcodecs.imwrite(
                new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString(),
                        System.currentTimeMillis() + ".jpg").getAbsolutePath(),
                grey
        );*/

        // For this project, we don't do histogram equalization, as the camera runs on full auto anyway.
        //Imgproc.equalizeHist(grey, grey);
        return grey;
    }

    public interface FaceDetectionResultListener {
        void facesDetected(List<android.graphics.RectF> rects, int width, int height);
    }

    public interface NonPermanentFaceDetectionResultListener extends FaceDetectionResultListener {

    }
}
