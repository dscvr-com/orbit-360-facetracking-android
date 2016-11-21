package com.iam360.motor.control;

/**
 * Created by Charlotte on 17.11.2016.
 */
public class MotorCommand {
    private byte[] value = new byte[32];

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
}
