package com.wonikrobotics.pathfinder.mc.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.wonikrobotics.pathfinder.mc.mobilecontroller.R;

/**
 * @author       : WeonWoo Joo
 * @date         : 28.07.2016
 * @description : Steering Wheel view
 * @wheel_image : {@drawable/steeringwheel}
 */
public abstract class ControlWheel extends View {
    private final double RAD = 57.2957795;                              // radian value
    private Handler handler;                                             // handler for rotation_zero
    private float centerX, centerY;                                    // center point coordination
    private int lastAngle = 0, lock_Angle = 0;                        // angle value for lock
    private boolean rc_run = false;                                    // is rotation_zero runing?  true : false
    private boolean rotation_lock = false;                            // is rotation locked?       true : false
    private boolean steer_wheel_touch_lock = true;                   // is touch locked?         false : true
    private int angle = 0;                                               // current angle
    private Matrix matrix;                                                // matrix for rotate view


    /**
     * Runnable instance to make controller heads default value
     */
    private Runnable rotation_zero = new Runnable() {
        @Override
        public void run() {
            if (rc_run) {
                if (lastAngle > 0)
                    lastAngle -= 10;
                else
                    lastAngle += 10;
                if (-10 < lastAngle && lastAngle < 10)
                    lastAngle = 0;
                if (lastAngle != 0)
                    handler.postDelayed(rotation_zero, 25);
                else
                    rc_run = false;
                setRotation(lastAngle);
            }
        }
    };


    public ControlWheel(Context c) {
        super(c);
        handler = new Handler();
        matrix = new Matrix();
    }


    /**
     * Listener of angle change
     *
     * @param angle
     * @param fromUser
     */
    public abstract void onAngleChanged(int angle, boolean fromUser);


    /**
     * calculate angle from two points, center and current touch point
     *
     * @param xPosition
     * @param yPosition
     * @return
     */
    private int getAngle(float xPosition, float yPosition) {

        if (xPosition > centerX) {
            if (yPosition < centerY) {//1사분면
                return (int) (Math.atan((int) (yPosition - centerY) / (xPosition - centerX)) * RAD + 90);
            } else if (yPosition > centerY) {//4사분면
                return (int) (Math.atan((int) (yPosition - centerY) / (xPosition - centerX)) * RAD + 90);
            } else {
                return lastAngle > 0 ? 180 : -180;
            }
        } else if (xPosition < centerX) {
            if (yPosition < centerY) {//2사분면
                return (int) (Math.atan((int) (yPosition - centerY) / (xPosition - centerX)) * RAD - 90);
            } else if (yPosition > centerY) {//3사분면
                return (int) (Math.atan((int) (yPosition - centerY) / (xPosition - centerX)) * RAD - 90);
            } else {
                return lastAngle > 0 ? 180 : -180;
            }
        } else {
            return lastAngle;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        centerX = canvas.getWidth() / 2;
        centerY = canvas.getHeight() / 2;

        /*
            draw steering wheel
         */
        Bitmap rawImage = BitmapFactory.decodeResource(getResources(), R.drawable.steeringwheel);
        float scale = canvas.getWidth() * 0.9f / rawImage.getWidth();
        Bitmap wheelImage = Bitmap.createScaledBitmap(rawImage, Math.round(rawImage.getWidth() * scale), Math.round(rawImage.getHeight() * scale), true);

        /*
            set rotation
         */
        matrix.reset();
        matrix.postTranslate(-(wheelImage.getWidth() / 2), -(wheelImage.getHeight() / 2));
        matrix.postRotate(angle);
        matrix.postTranslate(canvas.getWidth() / 2, canvas.getHeight() / 2);
        canvas.drawBitmap(wheelImage, matrix, null);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                 /*
                    if user thuch the thumb, vel_zero is stop
                 */
                rc_run = false;
                break;
            case MotionEvent.ACTION_MOVE:
                /*
                    calculate current angle
                 */
                int new_angle = getAngle(event.getX(), event.getY());

                /*
                    if the wheel rotate over limit, rotation will be locked until it heads opposite way.
                 */
                if (rotation_lock) {
                    if (lastAngle < 0) {
                        if ((new_angle < 0 && (lock_Angle - new_angle) >= 0) || (new_angle > 0 && (lock_Angle - new_angle) <= 0))
                            lock_Angle = new_angle;
                        else {
                            if (new_angle > -180 && new_angle < -90)
                                rotation_lock = false;
                        }
                    } else {
                        if ((new_angle < 0 && (lock_Angle - new_angle) <= 0) || (new_angle > 0 && (lock_Angle - new_angle) <= 0)) {

                            lock_Angle = new_angle;
                        } else {
                            if (new_angle < 180 && new_angle > 90)
                                rotation_lock = false;
                        }
                    }
                } else {
                    /*
                        if rotation has to be locked, save the last angle and way
                     */
                    if (lastAngle > 90 && new_angle < -90) {
                        lastAngle = 180;
                        rotation_lock = true;
                        lock_Angle = -1 * lastAngle;
                    } else if (lastAngle < -90 && new_angle > 90) {
                        lastAngle = -180;
                        rotation_lock = true;
                        lock_Angle = lastAngle;
                    } else {
                        /*
                            there is no rotation lock, angle can increase of decrease in 10 degree per 0.1 sec
                         */
                        if (10 < lastAngle - new_angle)
                            lastAngle = lastAngle > 180 ? 180 : lastAngle - 10;
                        else if (lastAngle - new_angle < -10)
                            lastAngle = lastAngle < -180 ? -180 : lastAngle + 10;
                        else
                            lastAngle = new_angle;
                    }
                }
                setRotation(lastAngle);
                /*
                    touch lock.
                    it will be released after 0.1 sec.
                 */
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        steer_wheel_touch_lock = true;
                    }
                }, 100);
                steer_wheel_touch_lock = false;

                break;
            case MotionEvent.ACTION_UP:
                /*
                    if touch event is over, vel_zero start to run and release touch lock and rotation lock
                 */
                rotation_lock = false;
                steer_wheel_touch_lock = true;
                rc_run = true;
                handler.post(rotation_zero);
                break;
        }


        return steer_wheel_touch_lock;
    }

    private void setRotation(int angle) {
        /*
            save new angle and draw again.
         */
        this.angle = angle;
        Log.e("setRotation", String.valueOf(angle));
        invalidate();
        onAngleChanged(angle, !rc_run);
    }
}
