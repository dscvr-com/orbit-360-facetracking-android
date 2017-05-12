package com.iam360.facetracking;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.iam360.engine.connection.BluetoothConnectionReciever;
import com.iam360.engine.connection.BluetoothEngineControlService;
import com.iam360.facedetection.FaceDetection;
import com.iam360.facedetection.FaceTrackingListener;
import com.iam360.views.record.OverlayCanvasView;
import com.iam360.views.record.RecorderOverlayFragment;
import com.iam360.views.record.RecorderPreviewView;
import com.iam360.views.record.RotationFragment;

import java.util.List;

public class CameraActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, RecorderOverlayFragment.OnFragmentInteractionListener, RotationFragment.OnFragmentInteractionListener, GestureDetector.OnGestureListener {

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String TAG = "CameraActivity";

    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    private GestureDetector gestureDetector;
    private static final String KEY_CAMERA_IS_FRONT = "isFrontCamera";


    private RecorderPreviewView recordPreview;
    private RecorderOverlayFragment overlayFragment;
    private boolean reactForTouchEvents = false;
    private RotationFragment splashFrag;
    private OverlayCanvasView overlayCanvas;

    @Override
    public void onResume() {
        super.onResume();
        if (recordPreview != null) {
            recordPreview.onResume();

        }

    }

    @Override
    public void onPause() {
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
        if (((BluetoothCameraApplicationContext) getApplicationContext()).hasBluetoothConnection()) {
            ((BluetoothCameraApplicationContext) getApplicationContext()).getBluetoothService().removeTrackingPoint();
        }
        gestureDetector = new GestureDetector(this, this);

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

    private void createCameraView() {


        overlayFragment = new RecorderOverlayFragment();
        overlayFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.camera_fragment_container, overlayFragment).commit();
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        createDrawView();
        createRecorderPreview(sharedPref.getBoolean(KEY_CAMERA_IS_FRONT, true));

    }

    private void createDrawView() {
        overlayCanvas = new OverlayCanvasView(this, new Size(1280, 960));
        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_camera);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        layout.addView(overlayCanvas, params);

    }

    private void createRecorderPreview(boolean isFrontCamera) {
        RecorderPreviewView oldView = recordPreview;
        recordPreview = new RecorderPreviewView(this, isFrontCamera);
        recordPreview.setPreviewListener(new FaceTrackingListener(this, new FaceDetection.FaceDetectionResultListener[]{(rects, width, height) -> drawRects(rects, width, height)}));

        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_camera);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        if (oldView != null) {
            oldView.closeCamera();
            layout.removeView(oldView);
            oldView.onPause();
        }
        layout.addView(recordPreview, params);
        recordPreview.onResume();

        overlayCanvas.bringToFront();
        findViewById(R.id.camera_fragment_container).bringToFront();
    }

    private void drawRects(List<Rect> rects, int width, int height) {
        if (overlayCanvas != null) {
            overlayCanvas.setRects(rects);
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
            ((BluetoothCameraApplicationContext) this.getApplicationContext()).getBluetoothService().setTrackingPoint(event.getX(), event.getY());
        }
        return true;
    }

    @Override
    public void onTrackingClicked(boolean isTrackingNowOn) {
        if (isTrackingNowOn) {
            ((BluetoothCameraApplicationContext) getApplicationContext()).getBluetoothService().startTracking();
        } else {
            try {
                ((BluetoothCameraApplicationContext) getApplicationContext()).getBluetoothService().stopTracking();
            } catch (BluetoothEngineControlService.NoBluetoothConnectionException e) {
                if (!((BluetoothCameraApplicationContext) getApplicationContext()).isInDemo())
                    sendBroadcast(new Intent(BluetoothConnectionReciever.DISCONNECTED));
                }
            }
        }

        @Override
        public void onCameraClicked ( boolean isFrontCamera){
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
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            sharedPref.edit().putBoolean(KEY_CAMERA_IS_FRONT, isFrontCamera).apply();
            createRecorderPreview(isFrontCamera);
        }

        @Override
        public void onRecordingClicked ( boolean shouldRecord, boolean startRecord){
            if (shouldRecord) {
                if (!startRecord) {
                    Log.i(TAG, "stop video");
                    recordPreview.stopVideo();
                    overlayFragment.stopTimer();
                } else {
                    Log.i(TAG, "start video");
                    recordPreview.startVideo();
                    overlayFragment.startTimer();
                }
            } else {
                recordPreview.takePicture();
            }

        }

        @Override
        public boolean onDown (MotionEvent e){
            return false;
        }

        @Override
        public void onShowPress (MotionEvent e){

        }

        @Override
        public boolean onSingleTapUp (MotionEvent e){
            return false;
        }

        @Override
        public boolean onScroll (MotionEvent e1, MotionEvent e2,float distanceX, float distanceY){
            return false;
        }

        @Override
        public void onLongPress (MotionEvent e){

        }

        @Override
        public boolean onFling (MotionEvent e1, MotionEvent e2,float velocityX, float velocityY){
            boolean result = false;
            try {
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
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }


        @Override
        public void onClosePressed () {
            getSupportFragmentManager().beginTransaction()
                    .remove(splashFrag).commit();
            getSupportFragmentManager().beginTransaction().show(overlayFragment).commit();
        }
    }

