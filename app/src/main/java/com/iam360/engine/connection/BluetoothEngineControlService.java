package com.iam360.engine.connection;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.graphics.Rect;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.util.Log;
import com.iam360.engine.control.MotorCommand;
import com.iam360.engine.control.MotorCommandPoint;

import java.util.List;
import java.util.UUID;

/**
 * Class to control Motor.
 * Can send MotorCommands to the motor
 * Created by Charlotte on 21.11.2016.
 */
public class BluetoothEngineControlService {

    public static final ParcelUuid SERVICE_UUID = ParcelUuid.fromString("69400001-B5A3-F393-E0A9-E50E24DCCA99");
    public static final UUID CHARACTERISTIC_UUID = UUID.fromString("69400002-B5A3-F393-E0A9-E50E24DCCA99");
    private static final String TAG = "MotorControl";
    private static final double STEPS_FOR_ONE_ROUND_X = 5111;
    private static final double STEPS_FOR_ONE_ROUND_Y = 15000;
    private static final float EPSILON_X_Steps = 10;
    private static final float EPSILON_Y_Steps = 10;
    private static final float P = 0.5f;
    private static final MotorCommandPoint SPEED_FACTOR = new MotorCommandPoint(0.5f, 0.5f);//FIXME Android
    private static final MotorCommandPoint MOVE_BACK_SPEED = new MotorCommandPoint(800f, 800f);


    private BluetoothGattService bluetoothService;
    private BluetoothGatt gatt;
    //this value has to be multiplied with the width because we don't have the width when we calc this value
    private float focalLengthInPx;
    private long lastTimeInMillis;
    private boolean firstRun = true;
    private MotorCommandPoint movedSteps = new MotorCommandPoint(0, 0);


    public boolean setBluetoothGatt(BluetoothGatt gatt) {
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
            return true;
        }
    }

    public boolean hasBluetoothService() {
        return bluetoothService != null;
    }

    private void sendCommand(MotorCommand command) {
        BluetoothGattCharacteristic characteristic = bluetoothService.getCharacteristic(CHARACTERISTIC_UUID);
        assert (((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) |
                (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) > 0);
        characteristic.setValue(command.getValue());
        gatt.writeCharacteristic(characteristic);

    }


    public void moveXY(MotorCommandPoint steps, MotorCommandPoint speed) {
        MotorCommand command = MotorCommand.moveXY(steps, speed);
        movedSteps.add(steps);
        sendCommand(command);

    }

    public void reactOnFaces(@NonNull List<Rect> detectionResult, int width, int height) {
        if (firstRun) {
            firstRun = false;
            return;
        }

        if (detectionResult.size() > 0) {
            MotorCommandPoint pointOfFace = MotorCommandPoint.CreateMiddel(detectionResult);

            MotorCommandPoint steps = getSteps(width, height, pointOfFace);
            long currentTime = System.nanoTime();

            long deltaTime = currentTime - lastTimeInMillis;
            lastTimeInMillis = currentTime;
            MotorCommandPoint speed = steps.div(deltaTime).abs();
            speed = speed.mul(SPEED_FACTOR);
            speed = speed.min(new MotorCommandPoint(1000f, 1000f));
            speed = speed.max(new MotorCommandPoint(250f, 250f));


            MotorCommandPoint stepsAbs = steps.abs();
            if (stepsAbs.getX() > EPSILON_X_Steps || stepsAbs.getY() > EPSILON_Y_Steps)
                moveXY(steps, speed);
            else
                stop();
        } else {
            stop();
        }
    }

    private MotorCommandPoint getSteps(int width, int height, MotorCommandPoint pointOfFace) {
        float deltaX = (width / 2) - pointOfFace.getX();
        float deltaY = (height / 3) - pointOfFace.getY();
        MotorCommandPoint steps = new MotorCommandPoint(getStepsX(width, deltaX), getStepsY(height, deltaY));
        return steps.mul(P).mul(0.5f).mul(-1);
    }

    private void stop() {
        sendCommand(MotorCommand.stop());
    }

    public void setFocalLengthInPx(float focalLengthInPx) {
        this.focalLengthInPx = focalLengthInPx;
    }

    public int getStepsX(int width, float deltaX) {
        double angX = Math.atan2(deltaX, focalLengthInPx * width);
        return (int) (STEPS_FOR_ONE_ROUND_X * angX / (2 * Math.PI));

    }

    public int getStepsY(int height, float deltaY) {
        double angY = Math.atan2(deltaY, focalLengthInPx * height);
        return (int) (STEPS_FOR_ONE_ROUND_Y * angY / (2 * Math.PI));

    }


    public void moveBack() {
        moveXY(movedSteps.mul(-1), MOVE_BACK_SPEED);
        resetSteps();
    }

    public void resetSteps() {
        movedSteps = new MotorCommandPoint(0, 0);
    }
}
