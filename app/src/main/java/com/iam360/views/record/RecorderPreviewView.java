package com.iam360.views.record;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import com.iam360.facedetection.FaceDetection;
import com.iam360.facedetection.FaceTrackingListener;
import com.iam360.myapplication.BluetoothCameraApplicationContext;
import com.iam360.videorecording.ImageWrapper;
import com.iam360.videorecording.MediaRecorderWrapper;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by emi on 16/06/16.
 */
public class RecorderPreviewView extends AutoFitTextureView {

    private static final String TAG = "RecordPreviewView";
    private final static int START_DECODER = 0;
    private final static int FETCH_FRAME = 1;
    private final static int EXIT_DECODER = 2;
    private static final int DELAY_FOR_IMAGE = 3000;
    private final Activity activity;
    private AutoFitTextureView textureView;
    private CameraDevice cameraDevice;
    private CameraCaptureSession previewSession;
    private CaptureRequest.Builder previewBuilder;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private Semaphore cameraOpenCloseLock = new Semaphore(1);
    private int sensorOrientation;
    private Size previewSize;
    private Size wannabeVideoSize; // Video size we wish for. This is not the real video size.
    private CodecSurface surface;
    private HandlerThread decoderThread;
    private Handler decoderHandler;
    private ImageWrapper imageWrapper = null;
    private FaceTrackingListener dataListener;
    private MediaRecorderWrapper videoRecorder;
    private List<Rect> rects = new ArrayList<>();
    private Timer timer = new Timer("ImageTime");
    // Callbacks for cam opening - save camera ref and start preview
    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {


        @Override
        public void onOpened(CameraDevice cameraDevice) {
            RecorderPreviewView.this.cameraDevice = cameraDevice;
            startPreview();
            cameraOpenCloseLock.release();
            if (null != textureView) {
                configureTransform(previewSize.getWidth(), previewSize.getHeight());
            }
            if (null != dataListener) {
                dataListener.cameraOpened(cameraDevice);
            }
            if (null == videoRecorder) {
                try {
                    videoRecorder = new MediaRecorderWrapper(previewSize, activity, sensorOrientation);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
            imageWrapper = new ImageWrapper(getContext());
            CameraManager manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
            ((BluetoothCameraApplicationContext) getContext().getApplicationContext()).setFocalLengthInPx(manager, cameraDevice.getId());

        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            cameraOpenCloseLock.release();
            cameraDevice.close();
            RecorderPreviewView.this.cameraDevice = null;
            if (null != dataListener) {
                dataListener.cameraClosed(cameraDevice);
            }
            videoRecorder = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            cameraOpenCloseLock.release();
            cameraDevice.close();
            RecorderPreviewView.this.cameraDevice = null;
        }

    };
    // Callbacks for surface texture loading - open camera as soon as texture exists
    private TextureView.SurfaceTextureListener surfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(wannabeVideoSize.getWidth(), wannabeVideoSize.getHeight());

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

    public RecorderPreviewView(Activity ctx) {
        super(ctx);
        this.activity = ctx;
        this.textureView = this;
        this.wannabeVideoSize = new Size(1280, 960); //Size we want for stitcher input
        setWillNotDraw(false);
    }

    private static Size chooseOptimalPreviewSize(Size[] choices, int width, int height) {
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

    public void startVideo() {
        if (videoRecorder != null) {
            try {
                videoRecorder.startRecord();
                startPreview();
            } catch (CameraAccessException | IOException e) {
                Log.e(TAG, "Error starting record", e);
            }
        }
    }

    public void stopVideo() {
        if (videoRecorder != null) {
            closePreviewSession();
            videoRecorder.stopRecordingVideo();
            videoRecorder = null;
            //FIXME((BluetoothCameraApplicationContext) getContext().getApplicationContext()).getBluetoothService().moveBack();
            startPreview();
        }
    }

    // To be called from parent activity
    public void onResume() {
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera(textureView.getWidth(), textureView.getHeight());
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    public void setPreviewListener(FaceTrackingListener dataListener) {
        this.dataListener = dataListener;
        dataListener.getFaceDetection().addFaceDetectionResultListener(new FaceDetection.FaceDetectionResultListener(){
            @Override
            public void facesDetected(List<Rect> rects, int width, int height) {
                createRects(rects, width, height);
            }
        });
    }

    private void createRects(List<Rect> rects, int width, int height) {
        this.rects = rects;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }

/*
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //wannabeVideoSize - >previewSize
        float scaleX = (float) this.getWidth() / (float) wannabeVideoSize.getWidth();
        float scaleY = (float) this.getHeight() / (float) wannabeVideoSize.getHeight();


        for (Rect rect : rects) {
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(3);
            canvas.drawRect(rect.left * scaleX, rect.top * scaleY, rect.right * scaleX, rect.bottom * scaleY, paint);
            paint.setStrokeWidth(0);
        }

    }
    */

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());

        decoderThread = new HandlerThread("CameraDecoder");
        decoderThread.start();
        this.decoderHandler = new Handler(decoderThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == START_DECODER) {
                    createDecoderSurface();
                    // So I have no idea what we wait for. So we just wait.
                    try {
                        Thread.sleep(2500, 0);
                    } catch (InterruptedException e) {
                        Log.i(TAG, e.getMessage());
                    }
                } else if (msg.what == FETCH_FRAME) {
                    fetchFrame();
                } else if (msg.what == EXIT_DECODER) {
                    destroyDecoderSurface();
                }

            }

        };

        decoderHandler.obtainMessage(START_DECODER).sendToTarget();
    }

    private void createDecoderSurface() {
        surface = new CodecSurface(wannabeVideoSize.getWidth(), wannabeVideoSize.getHeight());
        decoderHandler.obtainMessage(FETCH_FRAME).sendToTarget();
    }

    private void destroyDecoderSurface() {
        surface.release();
        surface = null;
    }

    private void fetchFrame() {
        if (surface == null) {
            return;
        }
        try {
            if (dataListener != null) {
                if (surface.awaitNewImage()) {
                    surface.drawImage(false);
                    dataListener.imageDataReady(surface.fetchPixels(), surface.mWidth, surface.mHeight, CodecSurface.colorFormat);
                }
            } else {
                Log.e(TAG, "Fetch frame failed");
                Thread.sleep(10, 0);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            // Do nothing
        }
        decoderHandler.obtainMessage(FETCH_FRAME).sendToTarget();
    }

    // To be called from parent activity
    public void onPause() {
        stopBackgroundThread();
        closeCamera();
    }

    public void stopPreviewFeed() {
        stopBackgroundThread();
        closeCamera();
    }

    private void stopBackgroundThread() {
        if (backgroundThread == null)
            return;
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
            decoderHandler.obtainMessage(EXIT_DECODER).sendToTarget();
            decoderThread.quitSafely();
            decoderThread.join();
            decoderThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        try {
            cameraOpenCloseLock.acquire();
            closePreviewSession();
            if (null != cameraDevice) {
                cameraDevice.close();
                cameraDevice = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            cameraOpenCloseLock.release();
        }
    }

    private void configureTransform(int width, int height) {
        int viewWidth = textureView.getWidth();
        int viewHeight = textureView.getHeight();

        Log.d(TAG, String.format("Layouting for View: %d x %d, Video: %d x %d.", viewWidth, viewHeight, width, height));

        Matrix scale = new Matrix();
        Matrix translate = new Matrix();

        // TODO - this is different for landscape and portrait.
        // This version holds for portrait. For landscape, you'll have to switch height/width

        float scaleX = (float)height / (float)viewWidth;
        float scaleY = (float)width / (float)viewHeight;

        float upscale = Math.min(scaleX, scaleY);
        scaleX = scaleX / upscale;
        scaleY = scaleY / upscale;

        scale.setScale(scaleX, scaleY);
        float translateX = (0.5f - scaleX / 2.f) * viewWidth;
        float translateY = (0.5f - scaleY / 2.f) * viewHeight;
        translate.setTranslate(translateX, translateY);

        Log.d(TAG, String.format("Layouting scale: %f, %f, Translate: %f, %f.", scaleX, scaleY, translateX, translateY));


        Matrix transform = new Matrix();
        transform.setConcat(translate, scale);
        textureView.setTransform(transform);
        // Do nothing for now, we are locked in portrait anyway.
    }

    private void openCamera(int width, int height) {
        CameraManager manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            Log.d(TAG, "tryAcquire");
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                if (characteristics != null) {
                    if (characteristics.get(CameraCharacteristics.LENS_FACING) != CameraCharacteristics.LENS_FACING_FRONT) {
                        continue;
                    }
                    StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    previewSize = chooseOptimalPreviewSize(map.getOutputSizes(SurfaceTexture.class), height, width);
                    sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                    //textureView.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
                    configureTransform(previewSize.getWidth(), previewSize.getHeight());
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    manager.openCamera(cameraId, stateCallback, null);
                    break;

                }
            }

        } catch (CameraAccessException | NullPointerException | InterruptedException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    // Starts the preview, if all necassary parts are there.
    private void startPreview() {
        if (null == cameraDevice || !textureView.isAvailable() || null == previewSize) {
            return;
        }
        try {
            closePreviewSession();

            previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);//Is This the Point?

            SurfaceTexture tex = textureView.getSurfaceTexture();
            tex.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            Surface previewSurface = new Surface(tex);

            List<Surface> surfaces = new ArrayList<>();

            surfaces.add(previewSurface);
            surfaces.add(surface.getSurface());

            if (videoRecorder != null) {
                surfaces.add(videoRecorder.getSurface());
            }

            for (Surface surface : surfaces) {
                previewBuilder.addTarget(surface);
            }
            cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    previewSession = cameraCaptureSession;
                    beginPreviewAfterStarting();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.e(TAG, "Camera configure failed.");
                }
            }, backgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Problem with the camera: ", e);
        }
    }

    private void beginPreviewAfterStarting() {
        if (null == cameraDevice) {
            return;
        }
        try {
            previewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            previewSession.stopRepeating();
            previewSession.setRepeatingRequest(previewBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Problem with the camera: ", e);
        }
    }

    public void lockExposure() {
        if (null == cameraDevice) {
            return;
        }
        try {
            Log.w(TAG, "Locking Exposure.");
            previewBuilder.set(CaptureRequest.CONTROL_AE_LOCK, true);
            previewBuilder.set(CaptureRequest.CONTROL_AWB_LOCK, true);
            previewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
            previewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
            previewSession.stopRepeating();
            previewSession.setRepeatingRequest(previewBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "camera Error: ", e);
        }
    }

    private void closePreviewSession() {
        if (previewSession != null) {
            try {
                previewSession.stopRepeating();
                previewSession.abortCaptures();
                previewSession.close();
            } catch (CameraAccessException e) {
                Log.e(TAG, "Error on closing Preview Session", e);
            } catch (IllegalStateException e) {
                //nop - then the session was closed
            }
            previewSession = null;
        }
    }

    public void takePicture() {
        if (null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null");
            return;
        }
        final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                Toast.makeText(getContext(), "Saved Image", Toast.LENGTH_SHORT).show();
                startPreview();
            }
        };
        int rotationOfWindow = activity.getWindowManager().getDefaultDisplay().getRotation();
        int rotation = MediaRecorderWrapper.getOrientation(sensorOrientation, rotationOfWindow);
        imageWrapper.takePicture(cameraDevice, surface.getSurface(), rotation, captureListener, backgroundHandler);
    }


    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }
}