package com.iam360.myapplication;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.ViewGroup;
import com.iam360.facedetection.FaceTrackingListener;
import com.iam360.views.record.RecorderPreviewView;

public class CameraActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String TAG = "CameraActivity";

    static {
        System.loadLibrary("opencv_java3");
    }

    private RecorderPreviewView recordPreview;

    @Override
    public void onResume() {
        super.onResume();
        if(recordPreview!= null){
            recordPreview.onResume();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if(recordPreview!= null) {
            recordPreview.onPause();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestCameraPermission();
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                createCameraView();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION);
            }
        } else {
            createCameraView();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createCameraView();
                } else {
                    Log.e(TAG, "No Camera permission");
                }
                return;
            }

        }
    }

    private void createCameraView() {
        recordPreview = new RecorderPreviewView(this);
        recordPreview.setPreviewListener(new FaceTrackingListener(this));
        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_camera);
        layout.addView(recordPreview);
    }
}

