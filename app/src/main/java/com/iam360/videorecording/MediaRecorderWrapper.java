package com.iam360.videorecording;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * Class to manage the MediaRecorder, to record videos.
 * Created by Charlotte on 22.11.2016.
 */
public class MediaRecorderWrapper {
    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();
    private static final String TAG = "MediaRecorderWrapper";
    private static final String FORMAT = "faceDetection-%s.mp4";

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    private MediaRecorder recorder;
    private Size size;
    private Activity activity;
    private File nextVideoFile;
    private int sensorOrientation;

    public MediaRecorderWrapper(Size size, Activity activity, int sensorOrientation) throws IOException {
        this.size = size;
        this.activity = activity;
        this.sensorOrientation = sensorOrientation;
        setUpMediaRecorder();
    }

    public static int getOrientation(int sensorOrientation, int rotation) {
        switch (sensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                return INVERSE_ORIENTATIONS.get(rotation);
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                return DEFAULT_ORIENTATIONS.get(rotation);
        }
        return rotation;
    }

    public static void addImageToGallery(final File file, Context context) {
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
    }

    public Surface getSurface() {
        return recorder.getSurface();
    }

    public void startRecord() throws CameraAccessException, IOException {
        recorder.start();
    }

    public void stopRecordingVideo() {
        // Stop recording
        recorder.stop();
        recorder.reset();
        addImageToGallery(nextVideoFile, activity);
        if (null != activity) {
            Toast.makeText(activity, "Video saved: " + nextVideoFile.getAbsolutePath(),
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Video saved: " + nextVideoFile.getAbsolutePath());
        }
    }

    private void setUpMediaRecorder() throws IOException {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        if (nextVideoFile == null) {
            nextVideoFile = getVideoAbsolutePath();
        }
        recorder.setOutputFile(nextVideoFile.getAbsolutePath());
        recorder.setVideoEncodingBitRate(10000000);
        recorder.setVideoFrameRate(30);
        recorder.setVideoSize(size.getHeight(), size.getWidth());
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int orientation = getOrientation(sensorOrientation, activity.getWindowManager().getDefaultDisplay().getRotation());
        recorder.setOrientationHint(orientation);
        recorder.prepare();

    }

    public File getVideoAbsolutePath() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        return new File(dir, String.format(FORMAT, System.currentTimeMillis()));

    }
}
