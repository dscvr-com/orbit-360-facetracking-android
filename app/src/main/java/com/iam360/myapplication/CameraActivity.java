package com.iam360.myapplication;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.iam360.facedetection.FaceTrackingListener;
import com.iam360.views.record.RecorderOverlayFragment;
import com.iam360.views.record.RecorderPreviewView;

public class CameraActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, RecorderOverlayFragment.OnFragmentInteractionListener {

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String TAG = "CameraActivity";
    private boolean isFilmingMode = true;

    //FIXME, put this to a initial activity
    static {
        System.loadLibrary("opencv_java3");
    }

    private RecorderPreviewView recordPreview;
    private boolean isFilming = false;
    private RecorderOverlayFragment overlayFragment;

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

    }

    @Override
    public void onTrackingPointsClicked() {

    }

    @Override
    public void onTrackingClicked() {

    }

    @Override
    public void onCameraModeClicked() {

    }

    @Override
    public void onCameraClicked() {

    }

    @Override
    public void onRecordingClicked() {
        if (isFilmingMode) {
            if (isFilming) {
                Log.i(TAG, "stop video");
                recordPreview.stopVideo();
                isFilming = false;
            } else {
                Log.i(TAG, "start video");
                recordPreview.startVideo();
                isFilming = true;
            }
        } else {
            recordPreview.takePicture();
        }

    }
}

