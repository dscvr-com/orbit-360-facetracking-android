package com.iam360.myapplication;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.iam360.engine.connection.BluetoothConnectionReciever;
import com.iam360.engine.connection.BluetoothEngineControlService;
import com.iam360.facedetection.FaceTrackingListener;
import com.iam360.views.record.RecorderOverlayFragment;
import com.iam360.views.record.RecorderPreviewView;

public class CameraActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, RecorderOverlayFragment.OnFragmentInteractionListener, GestureDetector.OnGestureListener {

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String TAG = "CameraActivity";

    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    private GestureDetector gestureDetector;

    //FIXME, put this to a initial activity
    static {
        System.loadLibrary("opencv_java3");
    }

    private RecorderPreviewView recordPreview;
    private RecorderOverlayFragment overlayFragment;
    private boolean reactForTouchEvents = false;

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

        gestureDetector = new GestureDetector(this, (GestureDetector.OnGestureListener) this);

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

        recordPreview = new RecorderPreviewView(this);
        overlayFragment = new RecorderOverlayFragment();
        overlayFragment.setArguments(getIntent().getExtras());

        getSupportFragmentManager().beginTransaction()
                .add(R.id.camera_fragment_container, overlayFragment).commit();
        recordPreview.setPreviewListener(new FaceTrackingListener(this));

        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_camera);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        layout.addView(recordPreview, params);
        findViewById(R.id.camera_fragment_container).bringToFront();
    }

    @Override
    public void onSettingsClicked() {
        //TODO: settingsPage
    }

    @Override
    public void onTrackingPointsClicked(boolean b) {
        if (b) {
            reactForTouchEvents = true;
        } else {
            reactForTouchEvents = false;
        }
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
                sendBroadcast(new Intent(BluetoothConnectionReciever.DISCONNECTED));
            }
        }
    }

    @Override
    public void onCameraClicked(boolean isFrontCamera) {
        //ChangeCamera ....
    }

    @Override
    public void onRecordingClicked(boolean shouldRecord, boolean startRecord) {
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
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return result;
    }
}

