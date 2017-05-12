package com.iam360.engine.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.ParcelUuid;

import com.iam360.facetracking.BluetoothCameraApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.iam360.engine.connection.BluetoothConnectionReciever.CONNECTED;
import static com.iam360.engine.connection.BluetoothConnectionReciever.DISCONNECTED;

/**
 * Connects the application to a bluetoothdevice
 * Created by Charlotte on 17.03.2017.
 */
public class BluetoothConnector extends BroadcastReceiver {

    private static final long SCAN_PERIOD = 10000000;//very long time
    private final BluetoothAdapter adapter;
    private final Context context;
    private final Handler stopScanHandler = new Handler();
    private BluetoothLoadingListenerWithStartConnect listener;
    private final BluetoothConnectionCallback.ButtonValueListener upperButtomListener;
    private final BluetoothConnectionCallback.ButtonValueListener lowerButtonListener;
    private List<BluetoothDevice> nextDevice = new ArrayList<>();
    private boolean currentlyConnecting = false;
    private BluetoothEngineControlService controlService = new BluetoothEngineControlService(true);
    private BluetoothLeScanCallback scanCallback;


    public BluetoothConnector(BluetoothAdapter adapter, Context context, BluetoothLoadingListenerWithStartConnect listener, BluetoothConnectionCallback.ButtonValueListener upperButtomListener, BluetoothConnectionCallback.ButtonValueListener lowerButtonListener) {
        this.adapter = adapter;
        this.context = context;
        this.listener = listener;
        this.upperButtomListener = upperButtomListener;
        this.lowerButtonListener = lowerButtonListener;
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

        scanCallback = new BluetoothLeScanCallback((device -> addDeviceFromScan(device)));
        stopScanHandler.postDelayed(() -> adapter.getBluetoothLeScanner().stopScan(scanCallback), SCAN_PERIOD);
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        ArrayList<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(BluetoothEngineControlService.SERVICE_UUID).build());
        adapter.getBluetoothLeScanner().startScan(filters, settings, scanCallback);
    }

    private void addDeviceFromScan(BluetoothDevice device) {
        nextDevice.add(device);
        if (!currentlyConnecting) {
            if (nextDevice.size() > 0) {
                connect(nextDevice.get(0));
                nextDevice.remove(0);
            }
        }
    }

    private List<BluetoothDevice> searchBondedDevices() {
        Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
        List<BluetoothDevice> contactableList = new ArrayList<>();
        if (bondedDevices.isEmpty()) {
            return new ArrayList<>();
        }
        for (BluetoothDevice device : bondedDevices) {
            if (device.getUuids() == null) {
                continue;
            }
            for (ParcelUuid uuid : device.getUuids()) {
                if (uuid.equals(BluetoothEngineControlService.SERVICE_UUID)) {
                    contactableList.add(device);
                    continue;
                }
            }

        }
        return contactableList;
    }

    private void connect(BluetoothDevice device) {
        currentlyConnecting = true;
        listener.startedToConnect();
        device.connectGatt(context, true, new BluetoothConnectionCallback(context, gatt -> afterConnecting(gatt), upperButtomListener, lowerButtonListener));

    }

    private void afterConnecting(BluetoothGatt gatt) {
        try {
            controlService.setBluetoothGatt(gatt);
        } catch (BluetoothEngineControlService.NoBluetoothConnectionException e) {
            if (!((BluetoothCameraApplicationContext) context.getApplicationContext()).isInDemo())
                context.sendBroadcast(new Intent(BluetoothConnectionReciever.DISCONNECTED));
        }
        listener.endLoading(gatt);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case CONNECTED:
                break;
            case DISCONNECTED:
                currentlyConnecting = false;
                if (nextDevice.size() > 0) {
                    connect(nextDevice.get(0));
                    nextDevice.remove(0);
                } else {
                    findLeDevice();
                }
                break;

        }
    }

    public boolean hasDevices() {
        return !(nextDevice.size() == 0 && !currentlyConnecting);
    }

    public boolean isConnected() {
        return controlService.hasBluetoothService();
    }

    public BluetoothEngineControlService getBluetoothService() {
        return controlService;
    }

    public void stop() {
        adapter.getBluetoothLeScanner().stopScan(scanCallback);
    }


    public interface BluetoothLoadingListener {
        void endLoading(BluetoothGatt gatt);
    }

    public interface BluetoothLoadingListenerWithStartConnect extends BluetoothLoadingListener {

        void startedToConnect();
    }
}
