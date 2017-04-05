package com.iam360.motor.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.iam360.motor.connection.BluetoothConnectionReceiver.CONNECTED;
import static com.iam360.motor.connection.BluetoothConnectionReceiver.DISCONNECTED;

/**
 * Connects the application to a bluetoothdevice
 * Created by Charlotte on 17.03.2017.
 */
public class BluetoothConnector extends BroadcastReceiver {

    private static final long SCAN_PERIOD = 10000000;//very long time
    private final BluetoothAdapter adapter;
    private final Context context;
    private final Handler stopScanHandler = new Handler();
    private BluetoothLoadingListener listener;
    private List<BluetoothDevice> nextDevice;

    public BluetoothConnector(BluetoothAdapter adapter, Context context) {
        this.adapter = adapter;
        this.context = context;
    }

    public void connect() {
        List<BluetoothDevice> bluetoothDevices = searchBondedDevices();
        if (bluetoothDevices.size() > 0) {
            connect(bluetoothDevices.get(0));
            bluetoothDevices.remove(0);
            nextDevice.addAll(bluetoothDevices);
        } else {
            findLeDevice();
        }

    }

    private void findLeDevice() {

        final BluetoothLeScanCallback scanCallback = new BluetoothLeScanCallback((device -> addDeviceFromScan(device)));
        stopScanHandler.postDelayed(() -> adapter.getBluetoothLeScanner().stopScan(scanCallback), SCAN_PERIOD);
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        ArrayList<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(BluetoothMotorControlService.SERVICE_UUID).build());
        adapter.getBluetoothLeScanner().startScan(filters, settings, scanCallback);
    }

    private void addDeviceFromScan(BluetoothDevice device) {
        nextDevice.add(device);
    }

    public void setListener(BluetoothLoadingListener listener) {
        this.listener = listener;
    }

    private List<BluetoothDevice> searchBondedDevices() {
        Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
        List<BluetoothDevice> contactableList = new ArrayList<>();
        for (BluetoothDevice device : bondedDevices) {
            for (ParcelUuid uuid : device.getUuids()) {
                if (uuid.equals(BluetoothMotorControlService.SERVICE_UUID)) {
                    contactableList.add(device);
                    continue;
                }
            }

        }
        return contactableList;
    }

    private void connect(BluetoothDevice device) {
        device.connectGatt(context, true, new BluetoothConnectionCallback(context));

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case CONNECTED:
                listener.endLoading();
                break;
            case DISCONNECTED:
                listener.endLoading();
                if (nextDevice.size() > 0) {
                    connect(nextDevice.get(0));
                    nextDevice.remove(0);
                } else {
                    findLeDevice();
                }
                break;

        }
    }

    public interface BluetoothLoadingListener {
        void endLoading();
    }
}
