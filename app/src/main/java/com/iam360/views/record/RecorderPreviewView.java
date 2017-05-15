package com.iam360.views.record;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import com.iam360.facedetection.FaceTrackingListener;
import com.iam360.views.record.engine.ImageRecorder;
import com.iam360.views.record.engine.InMemoryImageProvider;
import com.iam360.views.record.engine.RecorderPreviewViewBase;
import com.iam360.views.record.engine.SurfaceProvider;
import com.iam360.views.record.engine.VideoRecorder;

/**
 * Created by Emi on 15/05/2017.
 */

public class RecorderPreviewView extends RecorderPreviewViewBase {

    private static final String TAG = "RecorderPreviewView";
    private ImageRecorder imageRecorder;
    private VideoRecorder videoRecorder;
    private InMemoryImageProvider inMemoryRecorder;
    private FaceTrackingListener dataListener;
    private boolean isFrontCamera;

    public RecorderPreviewView(Activity context, boolean isFrontCamera) {
        super(context);

        imageRecorder = new ImageRecorder(context);
        videoRecorder = new VideoRecorder(context, 90);
        inMemoryRecorder = new InMemoryImageProvider();

        this.isFrontCamera = isFrontCamera;
    }

    @Override
    protected Size calculatePreviewSize(StreamConfigurationMap map, Size[] supportedPreviewSizes, Size viewSize) {
        // TODO
        return RecorderPreviewViewBase.chooseOptimalPreviewSize(supportedPreviewSizes, viewSize.getWidth(), viewSize.getHeight());
    }

    @Override
    protected boolean canUseCamera(CameraCharacteristics characteristics) {
        return isFrontCamera == (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT);
    }

    @Override
    protected SurfaceProvider[] createSurfacesProviders() {
        return new SurfaceProvider[] { imageRecorder, videoRecorder, inMemoryRecorder };
    }

    @Override
    public CaptureRequest.Builder setupPreviewSession(CameraDevice device, Surface previewSurface) throws CameraAccessException {
        CaptureRequest.Builder builder = super.setupPreviewSession(device, previewSurface);
        builder.addTarget(videoRecorder.getSurface());
        builder.addTarget(inMemoryRecorder.getSurface());
        return builder;
    }

    @Override
    protected void onSessionCreated(CameraCaptureSession currentSession) {
        super.onSessionCreated(currentSession);
        inMemoryRecorder.startFrameFetching(dataListener);
    }

    @Override
    protected void onSessionDestroying(CameraCaptureSession currentSession) {
        super.onSessionDestroying(currentSession);
        inMemoryRecorder.stopFrameFetching();
    }

    public void setPreviewListener(FaceTrackingListener dataListener) {
        this.dataListener = dataListener;
    }

    @Override
    protected void onCameraOpenend(CameraDevice device) {
        super.onCameraOpenend(device);
        if (null != dataListener) {
            dataListener.cameraOpened(cameraDevice);
        }
    }

    @Override
    protected void onCameraClosed(CameraDevice device) {
        super.onCameraClosed(device);
        if (null != dataListener) {
            dataListener.cameraClosed(cameraDevice);
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        inMemoryRecorder.startBackgroundThread();
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        try {
            inMemoryRecorder.stopBackgroundThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void takePicture() {

    }

    public void startVideo() {

    }

    public void stopVideo() {

    }
}
