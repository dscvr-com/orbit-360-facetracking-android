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

    private static final String TAG = "RecordPreviewView";

    protected CameraDevice cameraDevice;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private Semaphore cameraOpenCloseLock = new Semaphore(1);
    private SurfaceProvider[] externalRenderTargets;
    private boolean[] externalRenderTargetReady;
    private Object syncRoot;
    private Surface previewSurface;
    protected CameraCaptureSession currentSession;
    private Activity context;


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
                Log.d(TAG, "Camera device opened");
                // We can start the capture session here
                cameraOpenCloseLock.release();
                RecorderPreviewViewBase.this.cameraDevice = cameraDevice;
                onCameraOpenend(cameraDevice);
                if (allSurfacesReady() && cameraReady()) {
                    startSession();
                }

                // TODO Lotti: This needs to be refeactored via some listener or a getter.
                //((BluetoothCameraApplicationContext) getContext().getApplicationContext()).setFocalLengthInPx(manager, cameraDevice.getId());
            }
        }

        @Override
        public void onDisconnected(@NonNull  CameraDevice cameraDevice) {
            // We close the camera device
            // and end.
            synchronized (syncRoot) {
                Log.d(TAG, "Camera device disconnected");
                cameraOpenCloseLock.release();
                cameraDevice.close();
                RecorderPreviewViewBase.this.cameraDevice = null;
                onCameraClosed(cameraDevice);
            }
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            synchronized (syncRoot) {
                // :(
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
                    Log.d(TAG, "Session configured");
                    currentSession = session;
                    onSessionCreated(currentSession);
                    startPreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
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
        startBackgroundThread();
        if (this.isAvailable()) {
            getSupportedSizesAndStartCamera(this.getWidth(), this.getHeight());
        } else {
            this.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    // To be called from parent activity
    public void onPause() {
        closeSession();
        closeExternalSurfaces();
        closeCamera();
        stopBackgroundThread();
    }

    protected void closeExternalSurfaces() {
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
                Log.d(TAG, "Closing camera");
                cameraDevice.close();
                cameraDevice = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            cameraOpenCloseLock.release();
        }
    }

    private void closeSession() {
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
        Log.d(TAG, "Starting background thread");
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    protected void startPreview() {
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
        Log.d(TAG, "Setting repeating capture request");
        currentSession.stopRepeating();
        currentSession.setRepeatingRequest(builder.build(), null, backgroundHandler);
    }

    protected void setCaptureRequest(CaptureRequest.Builder builder, CameraCaptureSession.CaptureCallback callback) throws CameraAccessException {
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
            Log.d(TAG, "Preview surface available.");
            getSupportedSizesAndStartCamera(width, height);

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
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
                    // TODO: Configure transform
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

    public static Size chooseOptimalPreviewSize(Size[] choices, int width, int height) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();

        // hack hack
        int w = Math.max(width, height);
        int h = Math.min(width, height);
        for (Size option : choices) {
            Log.d(TAG, String.format("Choice: %d x %d", option.getWidth(), option.getHeight())) ;
            if (option.getWidth() >= w && option.getHeight() >= h) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
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

    public static Matrix getTransform(Size sourceSize, Size targetSize, int displayRotation) {
        int height = sourceSize.getWidth();
        int width = sourceSize.getHeight();

        int viewWidth = targetSize.getWidth();
        int viewHeight = targetSize.getHeight();
        int rotation = displayRotation;

        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, width, height);

        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();

        bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
        matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);

        float[] vals = new float[9];
        matrix.getValues(vals);

        float scale = Math.max(
                (float) viewHeight / height,
                (float) viewWidth  / width);

        matrix.postScale(scale, scale, centerX, centerY);

        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }

        return matrix;
        //textureView.setTransform(matrix);
    }


}
