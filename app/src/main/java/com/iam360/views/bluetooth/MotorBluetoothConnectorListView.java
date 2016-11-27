package com.iam360.views.bluetooth;

import android.app.Activity;
import android.bluetooth.*;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import com.iam360.motor.connection.BluetoothConnectionReceiver;
import com.iam360.motor.connection.BluetoothDataAdapter;
import com.iam360.motor.connection.BluetoothMotorControlService;
import com.iam360.myapplication.BluetoothCameraApplicationContext;

import java.util.ArrayList;
/**
 * Class to manage the Bluetooth-Connection to the motor.
 * Created by Charlotte on 07.11.2016.
 */
public class MotorBluetoothConnectorListView extends FrameLayout {
    public static final String TAG = "BluetoothConnectorView";//because of 23 charcters
    private static final long SCAN_PERIOD = 10000000;//very long time
    private final BluetoothDataAdapter dataAdapter;
    private final Handler stopScanHandler = new Handler();
    private BluetoothAdapter adapter;
    private ListView list;

    public MotorBluetoothConnectorListView(Activity context) {
        super(context);
        list = new ListView(context);
        adapter = BluetoothAdapter.getDefaultAdapter();
        dataAdapter = new BluetoothDataAdapter(context, loadData());
        final BluetoothLeScanCallback scanCallback = new BluetoothLeScanCallback();
        stopScanHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.getBluetoothLeScanner().stopScan(scanCallback);
            }
        }, SCAN_PERIOD);
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        ArrayList<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(BluetoothMotorControlService.SERVICE_UUID).build());
        adapter.getBluetoothLeScanner().startScan(filters, settings, scanCallback);
        list.setAdapter(dataAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice device = dataAdapter.getItem(i);
                adapter.getBluetoothLeScanner().stopScan(scanCallback);
                connectToDevice(device);
            }
        });
        addView(list);
    }

    public void onStop() {
        adapter.getBluetoothLeScanner().stopScan(new BluetoothLeScanCallback());
    }

    private void connectToDevice(BluetoothDevice device) {
        device.connectGatt(getContext(), true, new GattCallback());
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
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    }

    private final class GattCallback extends BluetoothGattCallback {
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
                    ((BluetoothCameraApplicationContext) getContext().getApplicationContext()).setBluetoothService(null);
                    getContext().sendBroadcast(new Intent(BluetoothConnectionReceiver.DISCONNECTED));
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (((BluetoothCameraApplicationContext) getContext().getApplicationContext()).setBluetoothService(gatt)) {
                getContext().sendBroadcast(new Intent(BluetoothConnectionReceiver.CONNECTED));
            } else {
                getContext().sendBroadcast(new Intent(BluetoothConnectionReceiver.DISCONNECTED));
            }

        }
    }


}
