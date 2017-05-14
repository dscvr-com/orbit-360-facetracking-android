package com.iam360.facetracking;

import android.app.Application;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.util.Log;

import com.iam360.engine.connection.BluetoothConnector;
import com.iam360.engine.connection.BluetoothEngineControlService;

/**
 * New Application context which controls the bluetooth connection
 * Created by Charlotte on 15.11.2016.
 */
public class BluetoothCameraApplicationContext extends Application {
    private static final String TAG = "ApplicationContext";
    private BluetoothConnector connector;
    private float focalLengthInPx;
    private boolean demoMode = false;

    public BluetoothCameraApplicationContext() {
        super();
    }


    public void setBluetoothConnector(BluetoothConnector connector) {
        this.connector = connector;
        connector.getBluetoothService().setFocalLengthInPx(focalLengthInPx);
    }

    public boolean setBluetoothService(BluetoothGatt gatt) throws BluetoothEngineControlService.NoBluetoothConnectionException {
        return connector != null && connector.getBluetoothService().setBluetoothGatt(gatt);
    }

    public boolean hasBluetoothConnection() {
        return demoMode || connector != null && connector.getBluetoothService().hasBluetoothService();
    }

    public BluetoothEngineControlService getBluetoothService() {
        return connector != null ? connector.getBluetoothService() : null;
    }

    public void setFocalLengthInPx(CameraManager cameraManager, String cameraId) {
        float[] focalLengths;
        try {
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
            focalLengths = cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
            //array because, if the camera has optical zoom, we get more than one result. This is very unlikely.
            float focalLength = focalLengths[0];
            float sensorWidth = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE).getWidth();
            this.focalLengthInPx = focalLength / sensorWidth;
        } catch (CameraAccessException e) {
            Log.e(TAG, "error setting focalLength.", e);
        }
    }

    public void setDemoMode() {
        this.demoMode = true;
        if (connector != null) {
            connector.stop();
        }

    }

    public void stopDemoMode() {
        this.demoMode = false;
    }

    public boolean isInDemo() {
        return demoMode;
    }
}
