package com.iam360.facetracking;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.iam360.engine.connection.BluetoothConnectionReciever;
import com.iam360.engine.connection.BluetoothEngineControlService;
import com.iam360.engine.control.ButtomReciever;
import com.iam360.engine.control.RemoteButtonListener;
import com.iam360.facedetection.FaceDetection;
import com.iam360.facedetection.FaceTrackingListener;
import com.iam360.views.record.OverlayCanvasView;
import com.iam360.views.record.RecorderOverlayFragment;
import com.iam360.views.record.RecorderPreviewView;
import com.iam360.views.record.RotationFragment;
import com.iam360.views.record.engine.RecorderPreviewViewBase;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CameraActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, RecorderOverlayFragment.OnFragmentInteractionListener, RotationFragment.OnFragmentInteractionListener, GestureDetector.OnGestureListener {

    static {
        System.loadLibrary("opencv_java3");
    }


    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String TAG = "CameraActivity";

    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    private GestureDetector gestureDetector;


    private RecorderPreviewView recordPreview;
    private RecorderOverlayFragment overlayFragment;
    private boolean reactForTouchEvents = false;
    private RotationFragment splashFrag;
    private OverlayCanvasView overlayCanvas;
    private ButtomReciever buttomReciever;
    private IntentFilter filter;
    private Timer torchTimer = new Timer();

    @Override
    public void onResume() {
        Log.d(TAG, "Camera View onResume");
        super.onResume();
        if (recordPreview != null) {
            recordPreview.onResume();
        }
        this.registerReceiver(buttomReciever, filter);

    }

    @Override
    public void onPause() {
        unregisterReceiver(buttomReciever);
        Log.d(TAG, "Camera View onPause");
        super.onPause();
        if (recordPreview != null) {
            recordPreview.onPause();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (!((BluetoothCameraApplicationContext) this.getApplicationContext()).hasBluetoothConnection()) {
            startActivity(new Intent(this, BluetoothActivity.class));
        } else {
            requestCameraPermission();
        }
        if (((BluetoothCameraApplicationContext) getApplicationContext()).hasBluetoothConnection() && !((BluetoothCameraApplicationContext) getApplicationContext()).isInDemo()) {
            ((BluetoothCameraApplicationContext) getApplicationContext()).getBluetoothService().removeTrackingPoint();
        }
        filter = new IntentFilter();
        filter.addAction(RemoteButtonListener.UPPER_BUTTON_PRESSED);
        filter.addAction(RemoteButtonListener.LOWER_BUTTON_PRESSED);
        buttomReciever = new ButtomReciever(() -> remoteRecordingClicked(), () -> remoteTrackingClicked());
        gestureDetector = new GestureDetector(this, this);

    }

    private void remoteRecordingClicked() {
        overlayFragment.recordingClicked();
    }

    private void remoteTrackingClicked() {
        overlayFragment.onTrackingClicked();
    }


    private void requestCameraPermission() {
        boolean isCameraPermGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
        boolean isAudioPermGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
        boolean isWritePermGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
        if (!(isCameraPermGranted && isAudioPermGranted && isWritePermGranted)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                createCameraView();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CAMERA_PERMISSION);
            }
        } else {
            createCameraView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {

        requestCameraPermission();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    private void createCameraView() {
        overlayFragment = new RecorderOverlayFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.camera_overlay_fragment_container, overlayFragment).commit();

        createDrawView();
        createRecorderPreview(((BluetoothCameraApplicationContext) getApplicationContext()).isFrontCamera());
        BluetoothEngineControlService bluetoothService = ((BluetoothCameraApplicationContext) getApplicationContext()).getBluetoothService();
        if (((BluetoothCameraApplicationContext) getApplicationContext()).isTracking()) {
            bluetoothService.startTracking();
        } else {
            try {
                ((BluetoothCameraApplicationContext) getApplicationContext()).stopTracking();
            } catch (BluetoothEngineControlService.NoBluetoothConnectionException e) {
                if (!((BluetoothCameraApplicationContext) getApplicationContext()).isInDemo())
                    sendBroadcast(new Intent(BluetoothConnectionReciever.DISCONNECTED));
            }
        }

    }

    private void createDrawView() {
        overlayCanvas = new OverlayCanvasView(this, new Matrix());
        FrameLayout layout = (FrameLayout) findViewById(R.id.camera_fragment_canvas_container);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        layout.addView(overlayCanvas, params);

    }

    private void createRecorderPreview(boolean isFrontCamera) {
        // TODO.
        recordPreview = new RecorderPreviewView(this, isFrontCamera);

        int orientation = 0;

        switch (((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                orientation = 0;
                break;
            case Surface.ROTATION_90:
                orientation = 90;
                break;
            case Surface.ROTATION_270:
                orientation = 270;
                break;
        }


        recordPreview.setPreviewListener(
                new FaceTrackingListener(
                        this,
                        new FaceDetection.FaceDetectionResultListener[]{(rects, width, height) -> drawRects(rects, width, height)},
                        orientation));
        FrameLayout layout = (FrameLayout) findViewById(R.id.camera_fragment_container);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        layout.addView(recordPreview, params);
        overlayCanvas.bringToFront();
    }

    private void drawRects(List<RectF> rects, int width, int height) {
        if (overlayCanvas != null) {
            // We don't need display rotation here, since facetracker results are already rotated
            // int preview space.
            Matrix transform = RecorderPreviewViewBase.getTransformFitCenter(new Size(width, height), new Size(overlayCanvas.getWidth(), overlayCanvas.getHeight()));
            overlayCanvas.setTransform(transform);
            overlayCanvas.setRects(rects);
            runOnUiThread(() -> overlayCanvas.invalidate());
        }

    }

    @Override
    public void onSettingsClicked() {
        startActivity(new Intent(this, SettingsActivity.class));

    }

    @Override
    public void onTrackingPointsClicked(boolean b) {
        reactForTouchEvents = b;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        if (reactForTouchEvents) {
            ((BluetoothCameraApplicationContext) this.getApplicationContext()).getBluetoothService().setTrackingPoint(event.getX() / (float) overlayCanvas.getWidth(), event.getY() / (float) overlayCanvas.getHeight());
            overlayFragment.trackingPointsClicked();
        }
        return true;
    }

    @Override
    public void onTrackingClicked(boolean isTrackingNowOn) {
        if (isTrackingNowOn) {
            ((BluetoothCameraApplicationContext) getApplicationContext()).getBluetoothService().startTracking();
        } else {
            try {

                if (((BluetoothCameraApplicationContext) getApplicationContext()).getBluetoothService() != null)
                    ((BluetoothCameraApplicationContext) getApplicationContext()).getBluetoothService().stopTracking();
            } catch (BluetoothEngineControlService.NoBluetoothConnectionException e) {
                if (!((BluetoothCameraApplicationContext) getApplicationContext()).isInDemo())
                    sendBroadcast(new Intent(BluetoothConnectionReciever.DISCONNECTED));
            }
        }
    }

    private SharedPreferences getSharedPreferences() {
        return getPreferences(Context.MODE_PRIVATE);
    }

    @Override
    public void onCameraClicked() {
        boolean isFrontCamera = !((BluetoothCameraApplicationContext) getApplicationContext()).isFrontCamera();
        if (!isFrontCamera) {
            splashFrag = new RotationFragment();
            splashFrag.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.camera_fragment_container, splashFrag).commit();

            try {
                ((BluetoothCameraApplicationContext) getApplicationContext()).getBluetoothService().stopTracking();
            } catch (BluetoothEngineControlService.NoBluetoothConnectionException e) {
                if (!((BluetoothCameraApplicationContext) getApplicationContext()).isInDemo())
                    sendBroadcast(new Intent(BluetoothConnectionReciever.DISCONNECTED));
            }
            getSupportFragmentManager().beginTransaction().hide(overlayFragment).commit();
        }

        ((BluetoothCameraApplicationContext) getApplicationContext()).isFrontCamera(isFrontCamera);

        FrameLayout layout = (FrameLayout) findViewById(R.id.camera_fragment_container);
        layout.removeView(recordPreview);
        recordPreview.onPause();

        createRecorderPreview(isFrontCamera);
        recordPreview.onResume();
        if (splashFrag != null && splashFrag.isInLayout()) splashFrag.getView().bringToFront();
    }

    @Override
    public void onRecordingClicked(boolean shouldRecord, boolean startRecord) {
        if (shouldRecord) {
            Toast.makeText(getApplicationContext(), "Video will be available in the next version!", Toast.LENGTH_LONG).show();
            /*
            if (!startRecord) {
                Log.i(TAG, "stop video");
                recordPreview.stopVideo();
                overlayFragment.stopTimer();
            } else {
                Log.i(TAG, "start video");
                recordPreview.startVideo();
                overlayFragment.startTimer();
            }
            */
        } else {
            recordPreview.takePicture();
        }

    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        boolean result = false;
        float diffY = e2.getY() - e1.getY();
        float diffX = e2.getX() - e1.getX();
        if (Math.abs(diffX) > Math.abs(diffY)) {
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    Log.d(getClass().getSimpleName(), "swipe Right");
                    overlayFragment.onSwipeRight();
                } else {
                    Log.d(getClass().getSimpleName(), "swipe Left");
                    overlayFragment.onSwipeLeft();
                }
                result = true;
            }
        } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
            if (diffY > 0) {
                overlayFragment.onSwipeBottom();
            } else {
                overlayFragment.onSwipeTop();
            }
            result = true;
        }

        return result;
    }


    @Override
    public void onClosePressed() {
        getSupportFragmentManager().beginTransaction()
                .remove(splashFrag).commit();
        getSupportFragmentManager().beginTransaction().show(overlayFragment).commit();
    }

}

