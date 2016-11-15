package com.iam360.myapplication;

import android.app.Application;
import android.bluetooth.BluetoothGattService;

/**
 * Created by Charlotte on 15.11.2016.
 */
public class BluetoothApplicationContext extends Application {
    private BluetoothGattService bluetoothService = null;

    public BluetoothGattService getBluetoothService() {
        return bluetoothService;
    }

    public void setBluetoothService(BluetoothGattService bluetoothService) {
        this.bluetoothService = bluetoothService;
    }
}
