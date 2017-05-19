package com.iam360.facetracking;

import android.app.Application;
import android.bluetooth.BluetoothGatt;
import android.hardware.camera2.CameraCharacteristics;

import com.iam360.engine.connection.BluetoothConnector;
import com.iam360.engine.connection.BluetoothEngineControlService;

/**
 * New Application context which controls the bluetooth connection
 * Created by Charlotte on 15.11.2016.
 */
public class BluetoothCameraApplicationContext extends Application {
    private static final String TAG = "ApplicationContext";
    private BluetoothConnector connector;
    private float unitFocalLength;
    private boolean demoMode = false;
    private boolean isFrontCamera = true;
    private boolean isFilmMode = false;

    public BluetoothCameraApplicationContext() {
        super();
    }


    public void setBluetoothConnector(BluetoothConnector connector) {
        this.connector = connector;
        connector.getBluetoothService().setUnitFocalLength(unitFocalLength);
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

    public void setFocalLength(CameraCharacteristics cameraCharacteristics) {
        float[] focalLengths;

        focalLengths = cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
        //array because, if the camera has optical zoom, we get more than one result. This is very unlikely.
        float focalLength = focalLengths[0];
        float sensorWidth = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE).getWidth();
        this.unitFocalLength = focalLength / sensorWidth;
        connector.getBluetoothService().setUnitFocalLength(unitFocalLength);
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

    public void isFrontCamera(boolean isFrontCamera) {
        this.isFrontCamera = isFrontCamera;
    }

    public boolean isFrontCamera(){
        return isFrontCamera;
    }

    public boolean isTracking() {
        return getBluetoothService() != null && getBluetoothService().isTracking();
    }

    public boolean isFilmMode() {
        return isFilmMode;
    }

    public void setFilmMode(boolean filmMode) {
        this.isFilmMode = filmMode;
    }
}
