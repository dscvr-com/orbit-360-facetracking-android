package com.iam360.views.record.engine;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.io.File;

/**
 * Created by Charlotte on 21.12.2016.
 */
public class ImageRecorder implements SurfaceProvider {
    public static final String FACEDETECTION_FILENAME = "faceDetection_%s.jpg";
    public static final String DIR_NAME = "FaceDetection";
    private static final String TAG = "ImageRecorder";
    private final Context context;

    //keep this because of garbage Collector
    private Surface surface;
    private ImageReader reader;

    public ImageRecorder(Context context) {
        this.context = context;
    }

    @Override
    public Size[] getOutputSizes(StreamConfigurationMap map) {
        return map.getOutputSizes(ImageReader.class);
    }

    @Override
    public Surface getSurface() {
        return surface;
    }

    @Override
    public void createSurface(Size size, SurfaceProviderCallback callback) {
        if(this.surface != null) {
            throw new IllegalStateException("Surface already created.");
        }
        if(callback == null) {
            throw new IllegalArgumentException("No callback given");
        }

        reader = ImageReader.newInstance(size.getWidth(), size.getHeight(), ImageFormat.JPEG, 1);
        surface = reader.getSurface();
        callback.SurfaceReady(this, surface, size);
    }

    @Override
    public void destroySurface(SurfaceProviderCallback callback) {
        if(this.surface == null) {
            throw new IllegalStateException("Surface not created.");
        }
        if(callback == null) {
            throw new IllegalArgumentException("No callback given");
        }

        reader.close();
        surface.release();

        reader = null;
        surface = null;
        callback.SurfaceDestroyed(this);
    }

    // TODO: THis does not belong here.
    public CaptureRequest.Builder createPictureRequest(CameraDevice device, File file, int rotation, Handler backgroundHandler) {
        try {
            final CaptureRequest.Builder captureBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(surface);
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotation);

            ImageReader.OnImageAvailableListener readerListener = new ImageListener(file, context);
            reader.setOnImageAvailableListener(readerListener, backgroundHandler);
            return captureBuilder;
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
}
