package com.iam360.videorecording;

import android.app.Activity;
import android.hardware.camera2.*;
import android.hardware.camera2.CameraCaptureSession.StateCallback;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Charlotte on 22.11.2016.
 */
public class MediaRecorderWrapper {
    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();
    private static final String TAG = "MediaRecorderWrapper";
    private static final String FORMAT = "facedetection-%s";

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    private MediaRecorder recorder;
    //    private SurfaceTexture texture;
    private Size size;
    private CameraDevice device;
    private Surface surfaceForPreview;
    private CaptureRequest.Builder previewBuilder;
    private CameraCaptureSession previewSession;
    private Activity activity;
    private Handler backgroundHandler;
    private String nextVideoAbsolutePath;
    private int sensorOrientation;
    private HandlerThread backgroundThread;

    public MediaRecorderWrapper(CameraDevice device, Size size, Surface surface, Activity activity, int sensorOrientation) {
        this.size = size;
        this.device = device;
        this.surfaceForPreview = surface;
        this.activity = activity;
        this.sensorOrientation = sensorOrientation;
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    public void onResume() {
        startBackgroundThread();
    }

    public void onPause() {
        stopBackgroundThread();
    }

    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            Log.e(TAG, "error while stop camera background thread.", e);
        }
    }


    public void startRecord() throws CameraAccessException, IOException {
        setUpMediaRecorder();
//        assert texture != null;
//        texture.setDefaultBufferSize(size.getWidth(), size.getHeight());
        previewBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
        List<Surface> surfaces = new ArrayList<>();

        surfaces.add(surfaceForPreview);
        previewBuilder.addTarget(surfaceForPreview);

        device.createCaptureSession(surfaces, new StateCallback() {

            @Override
            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                previewSession = cameraCaptureSession;
                try {
                    updatePreview();
                } catch (CameraAccessException e) {
                    Log.e(TAG, "error while updatePreview", e);
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recorder.start();
                    }
                });
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                if (null != activity) {
                    Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }, backgroundHandler);
    }

    public void stopRecordingVideo() {
        // Stop recording
        recorder.stop();
        recorder.reset();
        if (null != activity) {
            Toast.makeText(activity, "Video saved: " + nextVideoAbsolutePath,
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Video saved: " + nextVideoAbsolutePath);
        }
    }


    private void updatePreview() throws CameraAccessException {

        setUpCaptureRequestBuilder(previewBuilder);
        HandlerThread thread = new HandlerThread("CameraPreview");
        thread.start();
        previewSession.setRepeatingRequest(previewBuilder.build(), null, backgroundHandler);

    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }


    private void setUpMediaRecorder() throws IOException {
        if (null == activity) {
            return;
        }
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        if (nextVideoAbsolutePath == null || nextVideoAbsolutePath.isEmpty()) {
            nextVideoAbsolutePath = getVideoAbsolutePath();
        }
        recorder.setOutputFile(nextVideoAbsolutePath);
        recorder.setVideoEncodingBitRate(10000000);
        recorder.setVideoFrameRate(30);
        recorder.setVideoSize(size.getWidth(), size.getHeight());
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (sensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                recorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                recorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                break;
        }
        recorder.prepare();

    }

    //only the dir: fileName? how do I call my files?
    public String getVideoAbsolutePath() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        File resultFile = new File(dir, String.format(FORMAT, System.currentTimeMillis()));
        return resultFile.getAbsolutePath().replaceAll(" ", "_");

    }
}
