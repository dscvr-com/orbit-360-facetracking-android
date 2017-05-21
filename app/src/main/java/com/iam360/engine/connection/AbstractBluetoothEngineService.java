package com.iam360.engine.connection;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.ParcelUuid;
import android.util.Log;

import com.iam360.engine.control.EngineCommand;

import java.util.List;
import java.util.UUID;

/**
 * Created by Lotti on 5/21/2017.
 */

public abstract class AbstractBluetoothEngineService {

    public static final UUID RESPONSE_UUID = UUID.fromString("69400003-B5A3-F393-E0A9-E50E24DCCA99");
    protected static final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID CHARACTERISTIC_UUID = UUID.fromString("69400002-B5A3-F393-E0A9-E50E24DCCA99");
    public static final ParcelUuid SERVICE_UUID = ParcelUuid.fromString("69400001-B5A3-F393-E0A9-E50E24DCCA99");

    private BluetoothGattService bluetoothService;
    private BluetoothGatt gatt;


    public boolean setBluetoothGatt(BluetoothGatt gatt) throws NoBluetoothConnectionException {
        if (gatt == null && this.hasBluetoothService()) {
            stop();
        }

        if (gatt == null) {
            bluetoothService = null;
            this.gatt = null;
            return false;
        }

        // set: bluetoothService
        List<BluetoothGattService> services = gatt.getServices();
        Log.i("onServicesDiscovered: ", services.toString());
        BluetoothGattService correctService = null;
        for (BluetoothGattService service : services) {
            if (service.getUuid().equals(SERVICE_UUID.getUuid())) {
                correctService = service;
                break;
            }
        }
        if (correctService == null) {
            return false;
        } else {
            this.gatt = gatt;
            this.bluetoothService = correctService;
            BluetoothGattCharacteristic characteristic = bluetoothService.getCharacteristic(RESPONSE_UUID);
            gatt.setCharacteristicNotification(characteristic, true);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
            return true;
        }
    }

    public boolean hasBluetoothService() {
        return bluetoothService != null;
    }


    protected void sendCommand(EngineCommand command) throws BluetoothEngineControlService.NoBluetoothConnectionException {
        if (bluetoothService == null) {
            throw new BluetoothEngineControlService.NoBluetoothConnectionException();
        }
        BluetoothGattCharacteristic characteristic = bluetoothService.getCharacteristic(CHARACTERISTIC_UUID);
        assert (((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) |
                (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) > 0);
        characteristic.setValue(command.getValue());
        gatt.writeCharacteristic(characteristic);

    }

    protected void stop() throws NoBluetoothConnectionException {
        sendCommand(EngineCommand.stop());
    }

    public class NoBluetoothConnectionException extends Exception {

    }
}
