package com.wonikrobotics.controller;

/**
 * Created by Notebook on 2016-08-04.
 */
public abstract class DoubleLeverCalculator {
    private final float distance = 0.566f; //distance between wheel
    private final float radius = 0.1225f;  //radius of wheel
    private int leftWheel, rightWheel;
    private float velocity, angular;
    private int max;
    private float velocityScale, angularScale;

    public DoubleLeverCalculator() {
        leftWheel = 0;
        rightWheel = 0;
        velocity = 0.0f;
        angular = 0.0f;
        max = 100;
        velocityScale = 1f / (max * radius);
        angularScale = distance / (2 * max * radius);
    }

    private void calculate() {
        velocity = (radius / 2) * (leftWheel + rightWheel) * velocityScale;
        angular = (radius / distance) * (leftWheel - rightWheel) * angularScale;
    }

    public void setLeftWheelVel(int leftWheel) {
        this.leftWheel = leftWheel;
        calculate();
        valueChangeListener(velocity, angular);
    }

    public void setRightWheelVel(int rightWheel) {
        this.rightWheel = rightWheel;
        calculate();
        valueChangeListener(velocity, angular);
    }

    public abstract void valueChangeListener(float velocity, float angular);

}
