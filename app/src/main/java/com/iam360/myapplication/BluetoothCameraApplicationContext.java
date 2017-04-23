package com.iam360.myapplication;

import android.app.Application;
import android.bluetooth.BluetoothGatt;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.util.Log;

import com.iam360.engine.connection.BluetoothEngineControlService;

/**
 * New Application context which controls the bluetooth connection
 * Created by Charlotte on 15.11.2016.
 */
public class BluetoothCameraApplicationContext extends Application {
    private static final String TAG = "ApplicationContext";
    private BluetoothEngineControlService bluetoothService = new BluetoothEngineControlService(true);

    public BluetoothCameraApplicationContext(){
        super();
    }


    public boolean setBluetoothService(BluetoothGatt gatt) {
        return bluetoothService.setBluetoothGatt(gatt);
    }

    public boolean hasBluetoothConnection() {
        return bluetoothService.hasBluetoothService();
    }

    public BluetoothEngineControlService getBluetoothService() {
        return bluetoothService;
    }

    public void setFocalLengthInPx(CameraManager cameraManager, String cameraId) {
        float[] focalLengths;
        try {
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
            focalLengths = cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
            //array because, if the camera has optical zoom, we get more than one result. This is very unlikely.
            float focalLength = focalLengths[0];
            float sensorWidth = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE).getWidth();
            bluetoothService.setFocalLengthInPx(focalLength / sensorWidth);
        } catch (CameraAccessException e) {
            Log.e(TAG, "error setting focalLength.", e);
        }
    }
}
