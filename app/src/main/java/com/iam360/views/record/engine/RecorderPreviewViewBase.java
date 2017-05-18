package com.iam360.views.record.engine;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import com.iam360.facetracking.BluetoothCameraApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by Emi on 15/05/2017.
 */

public abstract class RecorderPreviewViewBase extends AutoFitTextureView {

    private static final String TAG = "RecorderPreviewViewBase";

    protected CameraDevice cameraDevice;
    protected HandlerThread backgroundThread;
    protected Handler backgroundHandler;
    private Semaphore cameraOpenCloseLock = new Semaphore(1);
    private SurfaceProvider[] externalRenderTargets;
    private boolean[] externalRenderTargetReady;
    private Object syncRoot;
    private Surface previewSurface;
    protected CameraCaptureSession currentSession;
    protected Activity context;
    protected int sensorOrientation;
    protected Size previewSize;

    public Size getVideoSize() {
        return previewSize;
    }

    public RecorderPreviewViewBase(Activity context) {
        super(context);

        this.context = context;
        syncRoot = new Object();
    }

    protected void onCameraOpenend(CameraDevice device) { }
    protected void onCameraClosed(CameraDevice device) { }
    protected void onSessionCreated(CameraCaptureSession currentSession) { }
    protected void onSessionDestroying(CameraCaptureSession currentSession) { }

    protected abstract Size calculatePreviewSize(StreamConfigurationMap map, Size[] supportedPreviewSizes, Size viewSize);
    protected abstract boolean canUseCamera(CameraCharacteristics characteristics);
    protected abstract SurfaceProvider[] createSurfacesProviders();

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {

            synchronized (syncRoot) {
                Log.d(TAG, Thread.currentThread().getName());
                Log.d(TAG, "Camera device opened");
                // We can start the capture session here
                cameraOpenCloseLock.release();
                RecorderPreviewViewBase.this.cameraDevice = cameraDevice;
                onCameraOpenend(cameraDevice);
                if (allSurfacesReady() && cameraReady()) {
                    startSession();
                }
            }
        }

        @Override
        public void onDisconnected(@NonNull  CameraDevice cameraDevice) {
            // We close the camera device
            synchronized (syncRoot) {
                Log.d(TAG, Thread.currentThread().getName());
                Log.d(TAG, "Camera device disconnected");
                cameraOpenCloseLock.release();
                cameraDevice.close();
                RecorderPreviewViewBase.this.cameraDevice = null;
                onCameraClosed(cameraDevice);
            }
        }

        @Override
        public void onClosed(@NonNull CameraDevice camera) {
            super.onClosed(camera);
            Log.d(TAG, Thread.currentThread().getName());
            Log.d(TAG, "Camera device closed");
            cameraOpenCloseLock.release();
            RecorderPreviewViewBase.this.cameraDevice = null;
            onCameraClosed(cameraDevice);
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            synchronized (syncRoot) {
                // :(
                Log.d(TAG, Thread.currentThread().getName());
                Log.e(TAG, "Camera device error: " + error);
                cameraOpenCloseLock.release();
                cameraDevice.close();
                RecorderPreviewViewBase.this.cameraDevice = null;
            }
        }
    };

    // Callbacks for surface external surface loading - open camera as soon as texture exists
    private SurfaceProvider.SurfaceProviderCallback externalSurfaceCallback = new SurfaceProvider.SurfaceProviderCallback() {
        @Override
        public void SurfaceReady(SurfaceProvider sender, Surface surface, Size size) {
            Log.d(TAG, Thread.currentThread().getName());
            Log.d(TAG, "Surface Ready: " + sender.getClass().getName());
            synchronized (syncRoot) {
                externalRenderTargetReady[Arrays.asList(externalRenderTargets).indexOf(sender)] = true;
                if (allSurfacesReady() && cameraReady()) {
                    startSession();
                }
            }
        }

        @Override
        public void SurfaceDestroyed(SurfaceProvider sender) {
            Log.d(TAG, Thread.currentThread().getName());
            Log.d(TAG, "Surface destroyed: " + sender.getClass().getName());
            synchronized (syncRoot) {
                externalRenderTargetReady[Arrays.asList(externalRenderTargets).indexOf(sender)] = false;
            }
        }

        @Override
        public void Error(String what) {
            Log.e(TAG, "External surface provider error: " + what);
        }
    };

    private void startSession() {

        Log.d(TAG, Thread.currentThread().getName());
        Log.d(TAG, "Starting Session");
        if(!cameraReady() || !allSurfacesReady()) {
            throw new IllegalStateException("Not ready for initialization");
        }

        List<Surface> sessionSurfaces = new ArrayList<>();

        previewSurface = new Surface(this.getSurfaceTexture());
        sessionSurfaces.add(new Surface(this.getSurfaceTexture()));

        for(SurfaceProvider target : externalRenderTargets) {
            Surface surf = target.getSurface();
            if(surf == null) {
                throw new IllegalStateException("Surface passed was null");
            }
            sessionSurfaces.add(surf);
        }

        try {
            CameraCaptureSession.StateCallback callback = new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    Log.d(TAG, Thread.currentThread().getName());
                    Log.d(TAG, "Session configured");
                    currentSession = session;
                    onSessionCreated(currentSession);
                    startPreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.d(TAG, Thread.currentThread().getName());
                    Log.e(TAG, "Session config failed");
                }
            };
            createCaptureSession(sessionSurfaces, callback, cameraDevice, backgroundHandler);

        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    protected void createCaptureSession(List<Surface> sessionSurfaces, CameraCaptureSession.StateCallback callback, CameraDevice cameraDevice, Handler backgroundHandler) throws CameraAccessException {
        cameraDevice.createCaptureSession(sessionSurfaces, callback, backgroundHandler);
    }

    public void onResume() {

        Log.d(TAG, Thread.currentThread().getName());
        Log.d(TAG, "onResume");

        startBackgroundThread();
        if (this.isAvailable()) {
            getSupportedSizesAndStartCamera(this.getWidth(), this.getHeight());
        } else {
            this.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    // To be called from parent activity
    public void onPause() {

        Log.d(TAG, Thread.currentThread().getName());
        Log.d(TAG, "onPause");

        closeSession();
        closeExternalSurfaces();
        closeCamera();
        stopBackgroundThread();
    }

    protected void closeExternalSurfaces() {
        Log.d(TAG, Thread.currentThread().getName());
        Log.d(TAG, "closeExternalSurfaces");

        for(SurfaceProvider target : externalRenderTargets) {
            if(externalRenderTargetReady[Arrays.asList(externalRenderTargets).indexOf(target)]) {
                target.destroySurface(externalSurfaceCallback);
            }
        }
    }

    public void closeCamera() {
        try {
            cameraOpenCloseLock.acquire();
            if (null != cameraDevice) {
                Log.d(TAG, Thread.currentThread().getName());
                Log.d(TAG, "Closing camera");
                cameraDevice.close();
            } else {
                Log.d(TAG, "Tried to close cam, but it was not open.");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            cameraOpenCloseLock.release();
        }
    }

    private void closeSession() {
        Log.d(TAG, Thread.currentThread().getName());
        Log.d(TAG, "Closing session");
        if (currentSession == null) {
            throw new IllegalStateException("Session is not running.");
        }
        try {
            onSessionDestroying(currentSession);
            currentSession.stopRepeating();
            currentSession.abortCaptures();
            currentSession.close();
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error on closing Preview Session", e);
        } catch (IllegalStateException e) {
            Log.w(TAG, "Error on closing Preview Session", e);
        }
    }

    private void stopBackgroundThread() {
        Log.d(TAG, Thread.currentThread().getName());
        Log.d(TAG, "Stopping background thread");

        if (backgroundThread == null) {
            throw new IllegalStateException("Background thread is not running.");
        }
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() {
        Log.d(TAG, Thread.currentThread().getName());
        Log.d(TAG, "Starting background thread");
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    protected void startPreview() {
        Log.d(TAG, Thread.currentThread().getName());
        Log.d(TAG, "Starting preview");
        if(!cameraReady() || !allSurfacesReady() || !sessionReady()) {
            throw new IllegalStateException("Not ready");
        }

        try {
            CaptureRequest.Builder builder = setupPreviewSession(cameraDevice, previewSurface);
            setRepeatingRequest(builder);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Problem with the camera: ", e);
        }

    }

    protected void setRepeatingRequest(CaptureRequest.Builder builder) throws CameraAccessException {
        Log.d(TAG, Thread.currentThread().getName());
        Log.d(TAG, "Setting repeating capture request");
        currentSession.stopRepeating();
        currentSession.setRepeatingRequest(builder.build(), null, backgroundHandler);
    }

    protected void setCaptureRequest(CaptureRequest.Builder builder, CameraCaptureSession.CaptureCallback callback) throws CameraAccessException {
        Log.d(TAG, Thread.currentThread().getName());
        Log.d(TAG, "Setting non-repeating capture request");
        currentSession.stopRepeating();
        currentSession.capture(builder.build(), callback, backgroundHandler);
    }

    protected CaptureRequest.Builder setupPreviewSession(CameraDevice device, Surface previewSurface) throws CameraAccessException {
        CaptureRequest.Builder builder = device.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
        builder.addTarget(previewSurface);
        return builder;
    }

    private boolean sessionReady() {
        return currentSession != null;
    }

    private boolean cameraReady() {
        return cameraDevice != null;
    }

    private boolean allSurfacesReady() {
        for(boolean b : externalRenderTargetReady) if(!b) return false;
        return true;
    }


    // Callbacks for surface texture loading - open camera as soon as texture exists
    private TextureView.SurfaceTextureListener surfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.d(TAG, Thread.currentThread().getName());
            Log.d(TAG, "Preview surface available: width: " + width + " height: " + height);
            getSupportedSizesAndStartCamera(width, height);

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "Preview surface size changed: width: " + width + " height: " + height);
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }

    };

    private void getSupportedSizesAndStartCamera(int width, int height) {
        CameraManager manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            Log.d(TAG, Thread.currentThread().getName());
            Log.d(TAG, "tryAcquire");
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            for (String cameraId : manager.getCameraIdList()) {
                Log.d(TAG, "Checking camera " + cameraId);
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                if(characteristics != null && canUseCamera(characteristics)) {
                    Log.d(TAG, "Init'ing " + cameraId);
                    StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    Size[] validOutputSizes = map.getOutputSizes(SurfaceTexture.class);
                    Size optimalSize = calculatePreviewSize(map, validOutputSizes, new Size(width, height));
                    previewSize = optimalSize;
                    // TODO: Configure transform
                    sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

                    configureTransform(optimalSize.getWidth(), optimalSize.getHeight());
                    // TODO: Init surfaces and open the cam and if all is here, start the session.
                    externalRenderTargets = createSurfacesProviders();
                    externalRenderTargetReady = new boolean[externalRenderTargets.length];
                    Arrays.fill(externalRenderTargetReady, false);

                    for(SurfaceProvider target : externalRenderTargets) {
                        Log.d(TAG, "Creating external surface " + target.getClass().getName());
                        target.createSurface(optimalSize, externalSurfaceCallback);
                    }

                    // TODO: Do this somewhere else
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    // TODO Lotti: This needs to be refeactored via some listener or a getter.
                    ((BluetoothCameraApplicationContext) getContext().getApplicationContext()).setFocalLength(characteristics);

                    Log.d(TAG, "Opening camera...");
                    manager.openCamera(cameraId, stateCallback, null);

                    break;
                }
            }

        } catch (CameraAccessException | NullPointerException | InterruptedException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void configureTransform(int width, int height) {
        int rotation = context.getWindowManager().getDefaultDisplay().getRotation();
        Matrix m = getTransform(new Size(width, height), new Size(this.getWidth(), this.getHeight()), rotation);
        this.setTransform(m);
    }

    public static Size chooseOptimalPreviewSize(Size[] choices, int minWidth, int minHeight, float aspectW, float aspectH) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> aspectOk  = new ArrayList<>();

        float aspect = aspectW / aspectH;

        for (Size option : choices) {
            Log.d(TAG, String.format("Choice: %d x %d", option.getWidth(), option.getHeight())) ;
            if (Math.abs(option.getWidth() - option.getHeight() * aspect) < 1 &&
                    option.getWidth() > minWidth && option.getHeight() > minHeight) {
                aspectOk.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (aspectOk.size() > 0) {
            return Collections.min(aspectOk, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    public static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }
    public static class CompareSizesByAspect implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return (int)Math.signum((float) lhs.getHeight() / lhs.getWidth() - (float) rhs.getHeight() / rhs.getWidth());
        }
    }

    public static Matrix getTransform(Size sourceSize, Size targetSize, int displayRotation) {
        Log.d(TAG, "Formatting: video: " + sourceSize + ", view: " + targetSize + ", rotation: " + displayRotation);

        float viewWidth = targetSize.getWidth();
        float viewHeight = targetSize.getHeight();
        int rotation = displayRotation;

        Matrix matrix = new Matrix();
        matrix.setTranslate(-viewWidth / 2, -viewHeight / 2); // Translate to origin.
        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
            // We swap width/height because of the sensor orientation.
            float width = sourceSize.getHeight();
            float height = sourceSize.getWidth();

            float videoAspect = width / height;
            float viewAspect = viewWidth / viewHeight;

            // Just scale the aspect so it looks nice again
            matrix.postScale(Math.max(1.0f, videoAspect / viewAspect), Math.max(viewAspect / videoAspect, 1.0f));

            // If we need to, rotate
            if (Surface.ROTATION_180 == rotation) {
                matrix.postRotate(180, 0, 0);
            }
        } else {
            float height = sourceSize.getHeight();
            float width = sourceSize.getWidth();

            float videoAspect = width / height;
            float viewAspect = viewWidth / viewHeight;

            // Compensate swapped w/h in landscape mode
            matrix.postScale(height / width, width / height);
            // Fix the aspect and rotate
            matrix.postScale(Math.max(1.0f, videoAspect / viewAspect), Math.max(viewAspect / videoAspect, 1.0f));
            matrix.postRotate(90 * (rotation - 2), 0, 0);
        }
        matrix.postTranslate(viewWidth / 2, viewHeight / 2); // Translate to view coordinates.

        return matrix;
    }


}
