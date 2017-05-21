package com.iam360.engine.connection;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.graphics.RectF;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.util.Log;

import com.iam360.engine.control.EngineCommand;
import com.iam360.engine.control.EngineCommandPoint;

import java.util.List;
import java.util.UUID;

/**
 * Class to control Motor.
 * Can send MotorCommands to the motor
 * Created by Charlotte on 21.11.2016.
 */
public class BluetoothEngineControlService extends AbstractBluetoothEngineService{
    private static final String TAG = "MotorControl";
   public static final byte[] TOPBUTTON = new byte[]{(byte) 0xFE, 0x01, (byte) 0x08, (byte) 0x01, (byte) 0x08, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    public static final byte[] BOTTOMBUTTON = new byte[]{(byte) 0xFE, 0x01, 0x08, 0x00, 0x07, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    private static final double STEPS_FOR_ONE_ROUND_X = 5111;
    private static final double STEPS_FOR_ONE_ROUND_Y = 15000;
    private static final float EPSILON_X_Steps = 10;
    private static final float EPSILON_Y_Steps = 10;
    private static final float P = 0.5f;
    private static final EngineCommandPoint SPEED_FACTOR = new EngineCommandPoint(0.5f, 1.0f); // Speed factor to compaensate poort racking performance.
    //this value has to be multiplied with the width because we don't have the width when we calc this value
    private float unitFocalLength;
    private long lastTimeInMillis;
    private boolean firstRun = true;
    private EngineCommandPoint movedSteps = new EngineCommandPoint(0, 0);
    private boolean stopped = false;
    private static final EngineCommandPoint MAX_SPEED = new EngineCommandPoint(1000f, 1000f);
    private static final EngineCommandPoint MIN_SPEED = new EngineCommandPoint(50, 50f);
    private EngineCommandPoint trackingPoint = null;

    public BluetoothEngineControlService(boolean directStart) {
        stopped = !directStart;
    }


    public void moveXY(EngineCommandPoint steps, EngineCommandPoint speed) throws NoBluetoothConnectionException {
        EngineCommand command = EngineCommand.moveXY(steps, speed);
        movedSteps.add(steps);
        sendCommand(command);

    }

    public void stopTracking() throws NoBluetoothConnectionException {
        stopped = true;
        stop();
    }

    public void startTracking() {
        stopped = false;
    }

    public boolean isTracking() {
        return !stopped;
    }

    public void reactOnFaces(@NonNull List<RectF> detectionResult, int width, int height, boolean isFrontCamera) throws NoBluetoothConnectionException {
        if (stopped) {
            return;
        }
        if (firstRun) {
            firstRun = false;
            return;
        }
        if (detectionResult.size() > 0) {
            EngineCommandPoint pointOfFace = EngineCommandPoint.AveragePosition(detectionResult);
            EngineCommandPoint steps = getSteps(width, height, pointOfFace, isFrontCamera);
            long currentTime = System.currentTimeMillis();

            long deltaTime = currentTime - lastTimeInMillis;
            lastTimeInMillis = currentTime;
            EngineCommandPoint speed = steps.div(deltaTime / 1000f).abs();
            speed = speed.mul(SPEED_FACTOR);
            speed = speed.min(MAX_SPEED);
            speed = speed.max(MIN_SPEED);

            EngineCommandPoint stepsAbs = steps.abs();
            if (stepsAbs.getX() > EPSILON_X_Steps || stepsAbs.getY() > EPSILON_Y_Steps)
                moveXY(steps, speed);
            else
                stop();
        } else {
            stop();
        }
    }

    private EngineCommandPoint getSteps(int width, int height, EngineCommandPoint pointOfFace, boolean isFrontCamera) {
        float deltaX = getTrackingPoint(width, height).getX() - pointOfFace.getX();
        float deltaY = getTrackingPoint(width, height).getY() - pointOfFace.getY();
        Log.d(TAG, "deltax: " + deltaX + " deltay: " + deltaY);
        EngineCommandPoint steps = new EngineCommandPoint(getStepsX(width, deltaX, isFrontCamera), getStepsY(height, deltaY));
        return steps.mul(P).mul(P).mul(-1f);
    }

    public void setUnitFocalLength(float unitFocalLength) {
        this.unitFocalLength = unitFocalLength;
    }

    public int getStepsX(int width, float deltaX, boolean isFrontCamera) {
        double angX = Math.atan2(deltaX / width, unitFocalLength);
        Log.d(TAG, "angX: " + angX);
        return (int) ((STEPS_FOR_ONE_ROUND_X * angX / (2 * Math.PI)) * (isFrontCamera ? 1f : -1f));

    }

    public int getStepsY(int height, float deltaY) {
        double angY = Math.atan2(deltaY / height, unitFocalLength);
        Log.d(TAG, "angY: " + angY);
        return (int) (STEPS_FOR_ONE_ROUND_Y * angY / (2 * Math.PI));

    }

    private EngineCommandPoint getTrackingPoint(int width, int height) {
        if (trackingPoint != null) {
            return new EngineCommandPoint(trackingPoint.getX() * width, trackingPoint.getY() * height);
        } else {
            return new EngineCommandPoint(width / 2f, height / 3f);
        }
    }

    public void setTrackingPoint(float x, float y) {
        if (x < 0 || x > 1 || y < 0 || y > 1) {
            throw new IllegalArgumentException(x + ", " + y + " is not a valid tracking point");
        }
        Log.w(TAG, "Setting tracking point" + x + ", " + y);
        trackingPoint = new EngineCommandPoint(x, y);
    }

    public void removeTrackingPoint() {
        trackingPoint = null;
    }

}
