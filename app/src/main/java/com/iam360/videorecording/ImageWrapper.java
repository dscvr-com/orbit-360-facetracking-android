package com.iam360.videorecording;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.*;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.util.Size;
import android.view.Surface;

import java.io.File;
import java.util.List;
import java.util.Timer;

/**
 * Created by Charlotte on 21.12.2016.
 */
public class ImageWrapper {

    public static final String FACEDETECTION_FILENAME = "facedetection_%s.jpg";
    public static final String DIR_NAME = "Facedetection";
    private final Context context;
    private final CameraManager manager;
    private Timer timer = new Timer("PictureTimer");

    public ImageWrapper(Context context) {
        this.context = context;
        manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }


    public void takePicture(CameraDevice device, List<Surface> surfaces, int rotation, CameraCaptureSession.CaptureCallback callback, Handler backgroundHandler) {
        try {
            Size size = getSize(device);
            ImageReader reader = ImageReader.newInstance(size.getWidth(), size.getHeight(), ImageFormat.JPEG, 1);
            final CaptureRequest.Builder captureBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            surfaces.add(reader.getSurface());
            // Orientation

            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotation);
            final File file = getFile();
            ImageReader.OnImageAvailableListener readerListener = new ImageListener(file);
            reader.setOnImageAvailableListener(readerListener, backgroundHandler);
            device.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), callback, backgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private File getFile() {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), DIR_NAME);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return new File(dir, String.format(FACEDETECTION_FILENAME, System.currentTimeMillis()));
    }

    private Size getSize(CameraDevice device) throws CameraAccessException {
        CameraCharacteristics characteristics = manager.getCameraCharacteristics(device.getId());
        Size[] jpegSizes = null;
        if (characteristics != null) {
            jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
        }
        int width = 640;
        int height = 480;
        if (jpegSizes != null && 0 < jpegSizes.length) {
            width = jpegSizes[0].getWidth();
            height = jpegSizes[0].getHeight();
        }
        return new Size(width, height);
    }
}
