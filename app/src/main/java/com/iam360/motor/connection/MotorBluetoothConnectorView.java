package com.iam360.motor.connection;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Handler;
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
    private static final long SCAN_PERIOD = 10000;
    private final BluetoothDataAdapter dataAdapter;
    private final Handler stopScanhandler = new Handler();
    private BluetoothAdapter adapter;
    private ListView list;


    public MotorBluetoothConnectorView(Activity context) {
        super(context);
        list = new ListView(context);
        adapter = BluetoothAdapter.getDefaultAdapter();
        dataAdapter = new BluetoothDataAdapter(context, loadData());
        BluetoothLeScanCallback scanCallback = new BluetoothLeScanCallback();
        stopScanhandler.postDelayed(() -> adapter.getBluetoothLeScanner().stopScan(scanCallback), SCAN_PERIOD);
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        ArrayList<ScanFilter> filters = new ArrayList<>();
        adapter.getBluetoothLeScanner().startScan(filters, settings, scanCallback);
        list.setAdapter(dataAdapter);
        addView(list);
    }

    private ArrayList<BluetoothDevice> loadData() {
        if (adapter.getBondedDevices() != null) {
            return new ArrayList<>(adapter.getBondedDevices());
        } else {
            return new ArrayList<>();
        }


    }

    private class BluetoothLeScanCallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            Log.i(TAG, String.format("Device Found %s %s", device.getName(), device.getAddress()));
            dataAdapter.add(device);
            dataAdapter.notifyDataSetChanged();
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    }


}
