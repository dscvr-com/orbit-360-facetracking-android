package com.iam360.motor.connection;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.iam360.myapplication.BluetoothCameraApplicationContext;

/**
 * Created by Charlotte on 17.03.2017.
 */
public class BluetoothConnectionCallback extends BluetoothGattCallback {


    private final Context context;

    public BluetoothConnectionCallback(Context context) {
        this.context = context;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Log.i("onConnectionStateChange", "Status: " + status);
        switch (newState) {
            case BluetoothProfile.STATE_CONNECTED:
                Log.i("gattCallback", "STATE_CONNECTED");
                gatt.discoverServices();
                break;
            case BluetoothProfile.STATE_DISCONNECTED:
                Log.e("gattCallback", "STATE_DISCONNECTED");
                ((BluetoothCameraApplicationContext) context.getApplicationContext()).setBluetoothService(null);
                context.sendBroadcast(new Intent(BluetoothConnectionReceiver.DISCONNECTED));
                break;
            default:
                Log.e("gattCallback", "STATE_OTHER");
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (((BluetoothCameraApplicationContext) context.getApplicationContext()).setBluetoothService(gatt)) {
            context.sendBroadcast(new Intent(BluetoothConnectionReceiver.CONNECTED));
        } else {
            context.sendBroadcast(new Intent(BluetoothConnectionReceiver.DISCONNECTED));
        }

    }
}