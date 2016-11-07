package com.iam360.motor.connection;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

/**
 * Class to manage the Bluetooth-Connection to the motor.
 * Created by Charlotte on 07.11.2016.
 */
public class MotorBluetoothConnection {
    public static final String TAG = "MotorBluetoothConnection";

    public MotorBluetoothConnection(Context context) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            throw new IllegalStateException("No Bluetooth-adapter found");
        }

        if (!adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivity(enableBtIntent);
        }

    }

}
