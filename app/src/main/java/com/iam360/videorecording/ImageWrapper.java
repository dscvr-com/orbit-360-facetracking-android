package com.iam360.videorecording;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.*;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Charlotte on 21.12.2016.
 */
public class ImageWrapper {
    public static final String FACEDETECTION_FILENAME = "faceDetection_%s.jpg";
    public static final String DIR_NAME = "FaceDetection";
    private static final String TAG = "ImageWrapper";
    private final Context context;
    private final CameraManager manager;

    //keep this because of garbage Collector
    private Surface surface;
    private ImageReader reader;

    public ImageWrapper(Context context, Size size) {
        this.context = context;
        manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        reader = ImageReader.newInstance(size.getWidth(), size.getHeight(), ImageFormat.JPEG, 1);
        surface = reader.getSurface();
    }

    public Surface getSurface() {
        return surface;
    }


    public CaptureRequest createPictureRequest(CameraDevice device, File file, int rotation, Handler backgroundHandler) {
        try {
            final CaptureRequest.Builder captureBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(surface);
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotation);

            ImageReader.OnImageAvailableListener readerListener = new ImageListener(file, context);
            reader.setOnImageAvailableListener(readerListener, backgroundHandler);
            return captureBuilder.build();
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error with camera access.", e);
            return null;
        }
    }


    public static File getFile() {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), DIR_NAME);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return new File(dir, String.format(FACEDETECTION_FILENAME, System.currentTimeMillis()));
    }

    private Size getSize(CameraDevice device) throws CameraAccessException {
        CameraCharacteristics characteristics = manager.getCameraCharacteristics(device.getId());
        Size[] jpegSizes;
        jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
        int width = 640;
        int height = 480;
        if (jpegSizes != null && 0 < jpegSizes.length) {
            width = jpegSizes[0].getWidth();
            height = jpegSizes[0].getHeight();
        }
        return new Size(width, height);
    }
}
