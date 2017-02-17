package com.iam360.motor.control;

/**
 * Created by Charlotte on 17.02.2017.
 */
public class MotorCommandPoint {

    private final float p;
    private final float x;
    private final float y;

    public MotorCommandPoint(float p, float x, float y) {
        this.p = p;
        this.x = x;
        this.y = y;
    }

    public MotorCommandPoint(float x, float y) {
        this.p = 1;
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x * p;
    }

    public float getY() {
        return y * p;
    }

    public MotorCommandPoint add(MotorCommandPoint b) {
        return new MotorCommandPoint(p, x + b.x, y + b.y);
    }

    public MotorCommandPoint sub(MotorCommandPoint b) {
        return new MotorCommandPoint(p, x - b.x, y - b.y);
    }

    public MotorCommandPoint mul(MotorCommandPoint b) {
        return new MotorCommandPoint(p, x * b.x, y * b.y);
    }

    public MotorCommandPoint mul(float b) {
        return new MotorCommandPoint(p, x * b, y * b);
    }


    public MotorCommandPoint div(MotorCommandPoint b) {
        return new MotorCommandPoint(p, x / b.x, y / b.y);
    }

    public MotorCommandPoint div(float b) {
        return new MotorCommandPoint(p, x / b, y / b);
    }

    public MotorCommandPoint abs() {
        return new MotorCommandPoint(p, Math.abs(x), Math.abs(y));
    }

    public MotorCommandPoint min(MotorCommandPoint b) {
        return new MotorCommandPoint(p, Math.min(x, b.x), Math.min(y, b.y));
    }

    public MotorCommandPoint max(MotorCommandPoint b) {
        return new MotorCommandPoint(p, Math.max(x, b.x), Math.max(y, b.y));
    }


}
