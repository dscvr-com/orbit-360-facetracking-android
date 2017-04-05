package com.iam360.engine.connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.iam360.myapplication.BluetoothActivity;
import com.iam360.myapplication.CameraActivity;

/**
 * class to handle Bluetooth Broadcasts
 *
 * Created by Charlotte on 15.11.2016.
 */
public class BluetoothConnectionReceiver extends BroadcastReceiver {

    public static final String CONNECTED = "com.iam360.bluetooth.BLUETOOTH_CONNECTED";
    public static final String DISCONNECTED = "com.iam360.bluetooth.BLUETOOTH_DISCONNECTED";
    private static final String TAG = "BluetoothConnectReceive";

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case CONNECTED:
                context.startActivity(new Intent(context, CameraActivity.class));
                Log.i(TAG, "connected to device");
                break;
            case DISCONNECTED:
                context.startActivity(new Intent(context, BluetoothActivity.class));
                Log.i(TAG, "lost connection to device");
                break;

        }
    }
}
