package com.iam360.motor.control;

import java.nio.ByteBuffer;

/**
 * Created by Charlotte on 17.11.2016.
 */
public class MotorCommand {
    private static final byte[] EMPTY = new byte[0];
    private byte[] value = new byte[32];

    private MotorCommand() {

    }

    public static MotorCommand moveXY(MotorCommandPoint steps, MotorCommandPoint speed) {
        MotorCommand command = new MotorCommand();
        byte[] dataX = command.createDataWithoutFullStep((int) steps.getX(), (int) speed.getX());
        byte[] dataY = command.createData((int) steps.getY(), (int) speed.getY());
        byte[] data = command.mergeArrays(dataX, dataY);
        //func: 0x03 -> x + y motor
        command.createCommand((byte) 0x03, data);
        return command;
    }

    public static MotorCommand stop() {
        MotorCommand command = new MotorCommand();
        command.createCommand((byte) 0x04, EMPTY);
        return command;
    }

    private void createCommand(byte function, byte[] data) {
        value[0] = (byte) 0xFE;
        value[1] = (byte) data.length;
        value[2] = function;
        for (int i = 0; i < data.length; i++) {
            value[i + 3] = data[i];
        }
        int checksum = 0;
        for (int i = 0; i < data.length + 3; i++) {
            checksum += value[i];
        }
        value[data.length + 3] = (byte) (checksum & 0xFF);
    }

    private byte[] mergeArrays(byte[] dataX, byte[] dataY) {
        byte[] result = new byte[dataX.length + dataY.length];
        for (int i = 0; i < dataX.length; i++) {
            result[i] = dataX[i];
        }
        for (int i = dataX.length; i < result.length; i++) {
            result[i] = dataY[i - dataX.length];
        }
        return result;
    }


    private byte[] createData(int steps, int speed) {
        byte[] data = createDataWithoutFullStep(steps, speed);
        byte[] newData = new byte[data.length + 1];
        for (int i = 0; i < data.length; i++) {
            newData[i] = data[i];
        }
        //full stepps
        newData[data.length] = (byte) 0x00;
        return newData;
    }

    private byte[] createDataWithoutFullStep(int steps, int speed) {
        byte[] stepsAsArray = getByteArray(steps);
        byte[] data = new byte[stepsAsArray.length + 2];
        for (int i = 0; i < stepsAsArray.length; i++) {
            data[i] = stepsAsArray[i];
        }
        //add Speed
        byte[] speedInArray = getByteArray(speed);
        data[stepsAsArray.length] = speedInArray[2];
        data[stepsAsArray.length + 1] = speedInArray[3];
        return data;
    }

    private byte[] getByteArray(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    public byte[] getValue() {
        return value;
    }
}
