package com.iam360.motor.control;

import java.nio.ByteBuffer;

/**
 * Created by Charlotte on 17.11.2016.
 */
public class MotorCommand {
    private static final byte[] EMTY = new byte[0];
    private byte[] value = new byte[32];

    private MotorCommand() {

    }

    public static MotorCommand moveX(int steps) {
        MotorCommand command = new MotorCommand();
        //func: 0x01 -> x motor
        command.createCommand((byte) 0x01, command.createData(steps));
        return command;

    }

    public static MotorCommand moveY(int steps) {
        MotorCommand command = new MotorCommand();
        //func: 0x02 -> y motor
        command.createCommand((byte) 0x02, command.createData(steps));
        return command;
    }

    public static MotorCommand moveXY(int stepsX, int stepsY) {
        MotorCommand command = new MotorCommand();
        byte[] dataX = command.createData(stepsX);
        byte[] dataY = command.createData(stepsY);
        byte[] data = command.mergeArrays(dataX, dataY);
        //func: 0x03 -> x + y motor
        command.createCommand((byte) 0x03, data);
        return command;
    }

    public static MotorCommand stop() {
        MotorCommand command = new MotorCommand();
        command.createCommand((byte) 0x04, EMTY);
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


    private byte[] createData(int steps) {
        byte[] stepsAsArray = getByteArray(steps);
        byte[] data = new byte[stepsAsArray.length + 3];
        for (int i = 0; i < stepsAsArray.length; i++) {
            data[i] = stepsAsArray[i];
        }
        //add Speed
        data[stepsAsArray.length] = (byte) 0x03;
        data[stepsAsArray.length + 1] = (byte) 0xE8;
        //full stepps
        data[stepsAsArray.length + 2] = (byte) 0x01;
        return data;
    }

    // 00x02 -> y motor
    // 00x03 -> beide
    // x commando ganz zusammen, y commando ganz zusammen


    private byte[] getByteArray(int steps) {
        return ByteBuffer.allocate(4).putInt(steps).array();
    }

    public byte[] getValue() {
        return value;
    }
}
