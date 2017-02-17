package com.iam360.motor.connection;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.graphics.Rect;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.util.Log;
import com.iam360.motor.control.MotorCommand;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Class to control Motor.
 * Can send MotorCommands to the motor
 * Created by Charlotte on 21.11.2016.
 */
public class BluetoothMotorControlService {

    public static final ParcelUuid SERVICE_UUID = ParcelUuid.fromString("69400001-B5A3-F393-E0A9-E50E24DCCA99");
    public static final UUID CHARACTERISTIC_UUID = UUID.fromString("69400002-B5A3-F393-E0A9-E50E24DCCA99");
    private static final String TAG = "MotorControl";
    private static final double STEPS_FOR_ONE_ROUND_X = 5111;
    private static final double STEPS_FOR_ONE_ROUND_Y = 15000;
    private static final float EPSILON_TO_MIDDLE = 0.1f;
    private static final int PERIOD = 500;


    private BluetoothGattService bluetoothService;
    private BluetoothGatt gatt;
    //this value has to be multiplied with the width because we don't have the width when we calc this value
    private float focalLengthInPx;
    private boolean isFinishedMoving = true;
    private Timer timer;


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
            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer();
            //stop timer if disconnected
            timer.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    isFinishedMoving = true;
                }
            }, PERIOD, PERIOD);
            return true;
        }
    }

    public boolean hasBluetoothService() {
        return bluetoothService != null;
    }

    public void moveX(int steps) {
        MotorCommand command = MotorCommand.moveX(steps);
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
        MotorCommand command = MotorCommand.moveY(steps);
        sendCommand(command);
    }

    public void moveXY(int stepsX, int stepsY) {
        MotorCommand command = MotorCommand.moveXY(stepsX, stepsY);
        sendCommand(command);

    }

    public void reactOnFaces(@NonNull List<Rect> detectionResult, int width, int height) {
        if (detectionResult.size() > 0) {
            Rect currentRelevantFace = detectionResult.get(0);
            int deltaX = (width / 2) - currentRelevantFace.centerX();
            int deltaY = (height / 2) - currentRelevantFace.centerY();
            double angX = Math.atan2(deltaX, focalLengthInPx * width);
            double angY = Math.atan2(deltaY, focalLengthInPx * width);

            int xSteps = (int) (STEPS_FOR_ONE_ROUND_X * angX / (2 * Math.PI)) * 30;
            int ySteps = (int) (STEPS_FOR_ONE_ROUND_Y * angY / (2 * Math.PI)) * 30;


            if (Math.abs(deltaX) < EPSILON_TO_MIDDLE * width) {
                isFinishedMoving = true;
                stop();
                return;
            }

            if (isFinishedMoving) {
                //Log.i(TAG,"xSteps: " + xSteps + " ySteps: " + ySteps);
                moveXY(xSteps, ySteps);
                isFinishedMoving = false;
            }
        } else {
            stop();
        }
    }

    private void stop() {
        sendCommand(MotorCommand.stop());
    }

    public void setFocalLengthInPx(float focalLengthInPx) {
        this.focalLengthInPx = focalLengthInPx;
    }
}
