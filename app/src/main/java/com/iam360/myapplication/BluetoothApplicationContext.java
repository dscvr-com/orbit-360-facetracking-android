package com.iam360.myapplication;

import android.app.Application;
import android.bluetooth.BluetoothGatt;
import com.iam360.motor.connection.BluetoothMotorControlService;

/**
 * Created by Charlotte on 15.11.2016.
 */
public class BluetoothApplicationContext extends Application {
    private BluetoothMotorControlService bluetoothService = new BluetoothMotorControlService();


    public boolean setBluetoothService(BluetoothGatt gatt) {
        return bluetoothService.setBluetoothGatt(gatt);

    }

    public boolean hasBluetoothConnection() {
        return bluetoothService.hasBluetoothService();
    }

    public BluetoothMotorControlService getBluetoothService() {
        return bluetoothService;
    }
}
