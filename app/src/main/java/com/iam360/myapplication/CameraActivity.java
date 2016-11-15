package com.iam360.myapplication;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.ViewGroup;
import com.iam360.facedetection.FaceTrackingListener;
import com.iam360.motor.connection.BluetoothConnectionReciver;
import com.iam360.motor.connection.MotorBluetoothConnectorView;
import com.iam360.views.record.RecorderPreviewView;

import java.util.Set;

public class CameraActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String TAG = "CameraActivity";

    static {
        System.loadLibrary("opencv_java3");
    }

    private RecorderPreviewView recordPreview;
    private MotorBluetoothConnectorView motorBluetoothConnectorView;
    private BroadcastReceiver bluetoothConnectionResiver;
    private IntentFilter bluetoothBroadcastIntentFilter;

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(bluetoothConnectionResiver, bluetoothBroadcastIntentFilter);
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
        unregisterReceiver(bluetoothConnectionResiver);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bluetoothBroadcastIntentFilter = new IntentFilter("com.iam360.bluetooth.BLUETOOTH_CONNECTED");
        bluetoothBroadcastIntentFilter.addAction("com.iam360.bluetooth.BLUETOOTH_DISCONNECTED");
        bluetoothConnectionResiver = new BluetoothConnectionReciver();
        requestCameraPermission();
        BluetoothDevice device;
        //1. Test if we can autoconnect
        Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        Log.i(TAG, String.format("%d bonded devices found.", bondedDevices.size()));
        //2. otherwise open Motor BluetoothConnectorView
        startActivity(new Intent(this, BluetoothConnectionActivity.class));

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
    }
}

