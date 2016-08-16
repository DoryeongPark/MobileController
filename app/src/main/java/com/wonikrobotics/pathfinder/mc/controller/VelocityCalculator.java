package com.wonikrobotics.pathfinder.mc.controller;

/**
 * @author : WeonWoo Joo
 * @date : 04.08.2016
 * @description : Calculate linear velocity and angular velocity with velocities of two wheels
 */
public abstract class VelocityCalculator {
    private final float distance = 0.566f;          // distance between wheel
    private final float radius = 0.1225f;           // radius of wheel
    private int leftWheel, rightWheel;             // velocities of two wheels
    private float velocity, angular;               // result of calculation
    private int max;                                 // wheel velocity max value
    private float velocityScale, angularScale;   // scale to make value into scale
    // -1.0 <=  velocity,angular <= 1.0

    public VelocityCalculator() {
        /*
            initiate values
         */
        leftWheel = 0;
        rightWheel = 0;
        velocity = 0.0f;
        angular = 0.0f;
        max = 100;
        velocityScale = 1f / (max * radius);
        angularScale = distance / (2 * max * radius);
    }

    private void calculate() {
        /*
         *  left,right = 1/R * velocity +,- D/2R * angular
         */
        velocity = (radius / 2) * (leftWheel + rightWheel) * velocityScale;
        angular = (radius / distance) * (leftWheel - rightWheel) * angularScale;
    }

    /**
     * Update left wheel's velocity
     *
     * @param leftWheel
     */
    public void setLeftWheelVel(int leftWheel) {
        this.leftWheel = leftWheel;
        calculate();
        valueChangeListener(velocity, angular);
    }

    /**
     * Update right wheel's velocity
     *
     * @param rightWheel
     */
    public void setRightWheelVel(int rightWheel) {
        this.rightWheel = rightWheel;
        calculate();
        valueChangeListener(velocity, angular);
    }

    /**
     * when calculated value changed, this listener is work
     *
     * @param velocity
     * @param angular
     */
    public abstract void valueChangeListener(float velocity, float angular);

}
