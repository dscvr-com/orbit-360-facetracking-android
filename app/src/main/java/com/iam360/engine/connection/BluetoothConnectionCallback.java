package com.iam360.engine.connection;

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


    private final ButtonValueListener bottomButton;
    private final ButtonValueListener topButton;
    private BluetoothConnector.BluetoothLoadingListener listener;

    private final Context context;

    public BluetoothConnectionCallback(Context context, BluetoothConnector.BluetoothLoadingListener listener, ButtonValueListener bottomButton, ButtonValueListener topButton) {
        this.topButton = topButton;
        this.bottomButton = bottomButton;
        this.listener = listener;
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
                try {
                    ((BluetoothCameraApplicationContext) context.getApplicationContext()).setBluetoothService(null);
                } catch (BluetoothEngineControlService.NoBluetoothConnectionException e) {
                    //nop
                }
                context.sendBroadcast(new Intent(BluetoothConnectionReciever.DISCONNECTED));
                break;
            default:
                Log.e("gattCallback", "STATE_OTHER");
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        try {
            if (((BluetoothCameraApplicationContext) context.getApplicationContext()).setBluetoothService(gatt)) {
                context.sendBroadcast(new Intent(BluetoothConnectionReciever.CONNECTED));
            } else {
                context.sendBroadcast(new Intent(BluetoothConnectionReciever.DISCONNECTED));
            }
        } catch (BluetoothEngineControlService.NoBluetoothConnectionException e) {
            context.sendBroadcast(new Intent(BluetoothConnectionReciever.DISCONNECTED));
        }
    }

    public interface ButtonValueListener {
        void buttomPressed();
    }
}