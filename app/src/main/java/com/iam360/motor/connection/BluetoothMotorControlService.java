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
 * Class gto control Motor.
 * Can send MotorCommands to the motor
 * Created by Charlotte on 21.11.2016.
 */
public class BluetoothMotorControlService {

    public static final ParcelUuid SERVICE_UUID = ParcelUuid.fromString("00001000-0000-1000-8000-00805F9B34FB");
    public static final UUID CHARACTERISTIC_UUID = UUID.fromString("00001001-0000-1000-8000-00805F9B34FB");

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

    public void moveX(int steps) {
        MotorCommand command = new MotorCommand();
        command.moveX(steps);
        sendCommand(command);
    }

    private void sendCommand(MotorCommand command) {
        BluetoothGattCharacteristic characteristic = bluetoothService.getCharacteristic(CHARACTERISTIC_UUID);
        assert (((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) |
                (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) > 0);
        characteristic.setValue(command.getValue());
        gatt.writeCharacteristic(characteristic);

    }

    public void moveY(int steps) {
        MotorCommand command = new MotorCommand();
        command.moveY(steps);
        sendCommand(command);
    }

    public void moveXY(int stepsX, int stepsY) {
        MotorCommand command = new MotorCommand();
        command.moveXY(stepsX, stepsY);
        sendCommand(command);

    }
}
