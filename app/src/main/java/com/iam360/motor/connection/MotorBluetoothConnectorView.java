package com.iam360.motor.connection;

import android.app.Activity;
import android.bluetooth.*;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import com.iam360.myapplication.BluetoothApplicationContext;

import java.util.ArrayList;
import java.util.UUID;

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
        final BluetoothLeScanCallback scanCallback = new BluetoothLeScanCallback();
        stopScanhandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.getBluetoothLeScanner().stopScan(scanCallback);
            }
        }, SCAN_PERIOD);
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        ArrayList<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(UUID.fromString("1000"))).build());
        adapter.getBluetoothLeScanner().startScan(filters, settings, scanCallback);
        list.setAdapter(dataAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice device = dataAdapter.getItem(i);
                connectToDevice(device);
            }
        });
        addView(list);
    }

    private void connectToDevice(BluetoothDevice device) {
        //FIXME
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
            dataAdapter.notifyDataSetChanged();
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
                    BluetoothGattService service = gatt.getService(UUID.fromString("1000"));
                    ((BluetoothApplicationContext) getContext().getApplicationContext()).setBluetoothService(service);
                    getContext().sendBroadcast(new Intent(BluetoothConnectionReciver.CONNECTED));
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    ((BluetoothApplicationContext) getContext().getApplicationContext()).setBluetoothService(null);
                    getContext().sendBroadcast(new Intent(BluetoothConnectionReciver.DISCONNECTED));
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }
        }
    }


}
