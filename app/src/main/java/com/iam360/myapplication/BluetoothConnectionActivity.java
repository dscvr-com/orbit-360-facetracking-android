package com.iam360.myapplication;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.ViewGroup;
import com.iam360.motor.connection.BluetoothConnectionReceiver;
import com.iam360.motor.connection.MotorBluetoothConnectorView;

public class BluetoothConnectionActivity extends Activity {

    private static final int BLUETOOTH_REQUEST = 1;
    private static final String TAG = "BluetoothConnectionActivity";
    private static final int BLUETOOTH_LOCATION_REQUEST = 2;
    private BluetoothAdapter adapter;
    private boolean btOn = false;
    private boolean btLocationOn = false;
    private IntentFilter bluetoothBroadcastIntentFilter;
    private BluetoothConnectionReceiver bluetoothConnectionResiver;


    @Override
    protected void onPause() {
        unregisterReceiver(bluetoothConnectionResiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(bluetoothConnectionResiver);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(bluetoothConnectionResiver, bluetoothBroadcastIntentFilter);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        registerReceiver(bluetoothConnectionResiver, bluetoothBroadcastIntentFilter);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connection);
        bluetoothBroadcastIntentFilter = new IntentFilter("com.iam360.bluetooth.BLUETOOTH_CONNECTED");
        bluetoothBroadcastIntentFilter.addAction("com.iam360.bluetooth.BLUETOOTH_DISCONNECTED");
        bluetoothConnectionResiver = new BluetoothConnectionReceiver();
        registerReceiver(bluetoothConnectionResiver, bluetoothBroadcastIntentFilter);
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            throw new IllegalStateException("No Bluetooth-adapter found");
        }
        if (((BluetoothApplicationContext) getApplicationContext()).getBluetoothService() == null) {
            btLocationOn = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            btOn = adapter.isEnabled();
            if (!btLocationOn) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        BLUETOOTH_LOCATION_REQUEST);
            }
            if (!adapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, BLUETOOTH_REQUEST);
            }
            if (btLocationOn && btOn) {
                loadBluetooth();
            }
        }

    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case BLUETOOTH_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    btOn = true;
                } else {
                    Log.e(TAG, "No Bluetooth permission");
                }
            }
            case BLUETOOTH_LOCATION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    btLocationOn = true;
                } else {
                    Log.e(TAG, "No Bluetooth Location permission");
                }
            }

        }
        if (btOn && btLocationOn) {
            loadBluetooth();
        }
    }

    private void loadBluetooth() {
        MotorBluetoothConnectorView bluetoothConnectorView = new MotorBluetoothConnectorView(this);
        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_bluetooth_connection);
        layout.addView(bluetoothConnectorView);
    }

}
