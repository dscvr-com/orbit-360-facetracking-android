package com.iam360.engine.connection;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;

/**
 * Created by Charlotte on 17.03.2017.
 */
public class BluetoothLeScanCallback extends ScanCallback {

    private final BluetoothDeviceListener listener;

    public BluetoothLeScanCallback(BluetoothDeviceListener listener) {
        this.listener = listener;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        BluetoothDevice device = result.getDevice();
        Log.i(this.getClass().getSimpleName(), String.format("Device Found %s %s", device.getName(), device.getAddress()));
        listener.deviceFound(device);
    }

    @Override
    public void onScanFailed(int errorCode) {
        Log.e("Scan Failed", "Error Code: " + errorCode);
    }

    public interface BluetoothDeviceListener {
        void deviceFound(BluetoothDevice device);
    }
}