package com.iam360.motor.connection;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Class to manage the Bluetooth-Connection to the motor.
 * Created by Charlotte on 07.11.2016.
 */
public class MotorBluetoothConnectorView extends FrameLayout {
    public static final String TAG = "MotorBluetoothConnectorView";
    private final BluetoothDataAdapter dataAdapter;
    private BluetoothAdapter adapter;
    private ListView list;

    public MotorBluetoothConnectorView(Activity context) {
        super(context);
        list = new ListView(context);
        adapter = BluetoothAdapter.getDefaultAdapter();
        dataAdapter = new BluetoothDataAdapter(context, loadData());
        adapter.startDiscovery();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        ArrayList<ScanFilter> filters = new ArrayList<>();
        adapter.getBluetoothLeScanner().startScan(filters, settings, new BluetoothLeScanCallback());
        list.setAdapter(dataAdapter);
        addView(list);
    }

    private ArrayList<BluetoothDevice> loadData() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getContext().registerReceiver(new BluetoothBroadcastReceiver(), filter);
        if (adapter.getBondedDevices() != null) {
            return new ArrayList<>(adapter.getBondedDevices());
        } else {
            return new ArrayList<>();
        }


    }


    private final class BluetoothBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //TODO implement some message
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                dataAdapter.add(device);
            }
        }
    }

    private class BluetoothLeScanCallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            Log.i(TAG, String.format("Device Found %s %s", device.getName(), device.getAddress()));
            dataAdapter.add(device);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    }


}
