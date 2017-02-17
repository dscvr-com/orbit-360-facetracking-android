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
import com.iam360.views.bluetooth.MotorBluetoothConnectorListView;

public class BluetoothConnectionActivity extends Activity {

    private static final int BLUETOOTH_REQUEST = 1;
    private static final String TAG = "BluetoothActivity";//only 23 chars are allowed
    private static final int BLUETOOTH__LOCATION_REQUEST = 2;
    private BluetoothAdapter adapter;
    private IntentFilter bluetoothBroadcastIntentFilter;
    private BluetoothConnectionReceiver bluetoothConnectionReceiver;
    private MotorBluetoothConnectorListView bluetoothConnectorView;


    @Override
    protected void onPause() {
        unregisterReceiver(bluetoothConnectionReceiver);
        if (bluetoothConnectorView != null) {
            bluetoothConnectorView.onStop();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (bluetoothConnectorView != null) {
            bluetoothConnectorView.onStop();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBluetooth();
        registerReceiver(bluetoothConnectionReceiver, bluetoothBroadcastIntentFilter);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadBluetooth();
        registerReceiver(bluetoothConnectionReceiver, bluetoothBroadcastIntentFilter);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bluetooth_connection);
        bluetoothBroadcastIntentFilter = new IntentFilter("com.iam360.bluetooth.BLUETOOTH_CONNECTED");
        bluetoothBroadcastIntentFilter.addAction("com.iam360.bluetooth.BLUETOOTH_DISCONNECTED");
        bluetoothConnectionReceiver = new BluetoothConnectionReceiver();
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            throw new IllegalStateException("No Bluetooth-adapter found");
        }
        if (!((BluetoothCameraApplicationContext) getApplicationContext()).hasBluetoothConnection()) {

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BLUETOOTH_REQUEST);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, BLUETOOTH__LOCATION_REQUEST);

        }

    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case BLUETOOTH_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "bluetooth Req");
                } else {
                    Log.e(TAG, "No Bluetooth permission");
                }
            }
            case BLUETOOTH__LOCATION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "bluetooth Req");
                } else {
                    Log.e(TAG, "No Bluetooth permission");
                }
            }
            adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null && adapter.isEnabled() && ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                loadBluetooth();
            }

        }
    }

    private void loadBluetooth() {
        bluetoothConnectorView = new MotorBluetoothConnectorListView(this);
        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_bluetooth_connection);
        layout.addView(bluetoothConnectorView);
    }

}
