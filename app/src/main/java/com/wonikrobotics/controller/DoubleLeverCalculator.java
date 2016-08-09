package com.wonikrobotics.controller;

/**
 * Created by Notebook on 2016-08-04.
 */
public abstract class DoubleLeverCalculator {
    private int leftWheel, rightWheel;
    private float velocity, angular;

    public DoubleLeverCalculator() {
        leftWheel = 0;
        rightWheel = 0;
        velocity = 0.0f;
        angular = 0.0f;
    }

    private void calculate() {

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
