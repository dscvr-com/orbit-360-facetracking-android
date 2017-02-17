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
import com.iam360.facedetection.FaceTrackingListener;
import com.iam360.views.record.RecorderPreviewView;

public class CameraActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String TAG = "CameraActivity";

    static {
        System.loadLibrary("opencv_java3");
    }

    private RecorderPreviewView recordPreview;
    private boolean isFilming = false;

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
            ((BluetoothCameraApplicationContext) getApplicationContext()).setBluetoothService(null);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!((BluetoothCameraApplicationContext) this.getApplicationContext()).hasBluetoothConnection()) {
            startActivity(new Intent(this, BluetoothConnectionActivity.class));/**/
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
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createCameraView();
                } else {
                    Log.e(TAG, "No Camera permission");
                }
            }

        }
    }

    private void createCameraView() {
        recordPreview = new RecorderPreviewView(this);
        recordPreview.setPreviewListener(new FaceTrackingListener(this));
        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_camera);
        layout.addView(recordPreview);
        FloatingActionButton videoButton = (FloatingActionButton) findViewById(R.id.camera);
        videoButton.bringToFront();
        videoButton.setOnClickListener(v -> {
            if (isFilming) {
                Log.i(TAG, "stop video");
                recordPreview.stopVideo();
                isFilming = false;
            } else {
                Log.i(TAG, "start video");
                recordPreview.startVideo();
                isFilming = true;
            }
        });
        FloatingActionButton photoButton = (FloatingActionButton) findViewById(R.id.photo);
        photoButton.setOnClickListener(v -> recordPreview.takePicture());
        photoButton.bringToFront();
    }
}

