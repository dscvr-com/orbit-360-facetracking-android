package com.iam360.videorecording;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
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
    private boolean prepared;

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
    private Context context;
    private Surface surface;

    private File currentFile;

    public MediaRecorderWrapper(Size size, Context context) {
        this.size = size;
        this.context = context;
        this.recorder = new MediaRecorder();
        this.prepared = false;
    }

    public static int sensorToMediaOrientation(int sensorOrientation, int rotation) {
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
        return surface;
    }

    public void stopRecording(File videoFile) {
        // Stop recording
        recorder.stop();
        recorder.reset();
        prepared = false;
        currentFile.renameTo(videoFile);
        addImageToGallery(videoFile, context);
        if (null != context) {
            Toast.makeText(context, "Video saved: " + videoFile.getAbsolutePath(),
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Video saved: " + videoFile.getAbsolutePath());
        }
    }

    public void startRecording(int orientation) throws IOException, CameraAccessException {
        if(!prepared) {
            setUpMediaRecorder(orientation);
        }
        recorder.start();
    }

    // Note(ej):
    // Create surface needs to configure the media recorder once, so the surface
    // has the correct size for the capture session.
    // In all other cases, the media recorder is configured whenever the video is started.
    public void createSurface(int initialOrientation) throws IOException {
        surface = MediaCodec.createPersistentInputSurface();
        setUpMediaRecorder(initialOrientation);
    }

    public void destroySurface() {
        recorder.reset();
        surface.release();

        surface = null;
    }

    private void setUpMediaRecorder(int orientation) throws IOException {
        recorder.setInputSurface(surface);
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setVideoEncodingBitRate(10000000);
        recorder.setVideoFrameRate(30);
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setOrientationHint(orientation);
        recorder.setVideoSize(size.getWidth(), size.getHeight());
        recorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
                Log.e(TAG, "Video Recorder Error: " + what + ", " + extra);
            }
        });
        currentFile = getTemporaryPath();

        Log.d(TAG, "Writing video: " + currentFile.getAbsolutePath());
        recorder.setOutputFile(currentFile.getAbsolutePath());
        recorder.prepare();
        recorder.start();
    }

    public static File getVideoAbsolutePath() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        return new File(dir, String.format(FORMAT, System.currentTimeMillis()));

    }
    private File getTemporaryPath() {
        File dir = context.getCacheDir();
        return new File(dir, String.format(FORMAT, System.currentTimeMillis()));
    }

    @Override
    public void finalize() {
        if(surface != null) {
            throw new Error("MediaRecorderWrapper moved out of scope, but surface was not destroyed.");
        }
    }
}
