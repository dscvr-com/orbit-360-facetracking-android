package com.iam360.motor.connection;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.ParcelUuid;
import android.util.Log;
import com.iam360.motor.control.MotorCommand;

import java.util.List;
import java.util.UUID;

/**
 * Created by Charlotte on 21.11.2016.
 */
public class BluetoothMotorControlService {

    public static final ParcelUuid SERVICE_UUID = ParcelUuid.fromString("00001000-0000-1000-8000-00805F9B34FB");
    public static final UUID CHARACTERISITC_UUID = UUID.fromString("00001001-0000-1000-8000-00805F9B34FB");

    private BluetoothGattService bluetoothService;
    private BluetoothGatt gatt;

    public boolean setBluetoothGatt(BluetoothGatt gatt) {

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
            return true;
        }
    }

    public boolean hasBluetoothService() {
        return bluetoothService != null;
    }

    public boolean moveX(int steps) {
        BluetoothGattCharacteristic characteristic = bluetoothService.getCharacteristic(CHARACTERISITC_UUID);
        assert (((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) |
                (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) > 0);
        MotorCommand command = new MotorCommand();
        command.moveX(steps);
        characteristic.setValue(command.getValue());
        gatt.writeCharacteristic(characteristic);
        return true;
    }
}
