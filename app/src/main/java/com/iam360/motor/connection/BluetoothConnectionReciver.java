package com.iam360.motor.connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.iam360.myapplication.BluetoothConnectionActivity;
import com.iam360.myapplication.CameraActivity;

/**
 * Created by Charlotte on 15.11.2016.
 */
public class BluetoothConnectionReciver extends BroadcastReceiver {

    public static final String CONNECTED = "com.iam360.bluetooth.BLUETOOTH_CONNECTED";
    public static final String DISCONNECTED = "com.iam360.bluetooth.BLUETOOTH_DISCONNECTED";

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case CONNECTED:
                context.startActivity(new Intent(context, CameraActivity.class));

                break;
            case DISCONNECTED:
                context.startActivity(new Intent(context, BluetoothConnectionActivity.class));
                break;

        }
    }
}
