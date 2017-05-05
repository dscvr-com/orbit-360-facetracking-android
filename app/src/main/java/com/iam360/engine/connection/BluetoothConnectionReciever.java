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
public class BluetoothConnectionReciever extends BroadcastReceiver {

    public static final String CONNECTED = "com.iam360.bluetooth.BLUETOOTH_CONNECTED";
    public static final String DISCONNECTED = "com.iam360.bluetooth.BLUETOOTH_DISCONNECTED";
    private static final String TAG = "BluetoothConnectReceive";

    @Override
    public void onReceive(Context context, Intent i) {

        Intent intent;
        switch (i.getAction()) {
            case CONNECTED:
                intent = new Intent(context, CameraActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                Log.i(TAG, "connected to device");
                break;
            case DISCONNECTED:
                intent = new Intent(context, BluetoothActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                Log.i(TAG, "lost connection to device");
                break;

        }
    }
}
