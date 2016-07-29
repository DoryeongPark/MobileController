package com.wonikrobotics.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.wonikrobotics.mobilecontroller.R;

/**
 * Created by Notebook on 2016-07-28.
 */
public abstract class ControlWheel extends View {
    private final double RAD = 57.2957795;
    private Handler handler;
    private float centerX,centerY;
    private int lastAngle = 0,lock_Angle = 0;
    private boolean rc_run = false,rotation_lock = false , steer_wheel_touch_lock = true;
    private int angle = 0;
    private Matrix matrix;

    public ControlWheel(Context c){
        super(c);
        handler = new Handler();
        matrix = new Matrix();
    }
    public abstract void onAngleChanged(int angle,boolean fromUser);
    private int getAngle(float xPosition,float yPosition) {

        if (xPosition > centerX) {
            if (yPosition < centerY) {//1사분면
                return (int) (Math.atan((int)(yPosition - centerY) / (xPosition - centerX)) * RAD + 90);
            } else if (yPosition > centerY) {//4사분면
                return (int) (Math.atan((int)(yPosition - centerY) / (xPosition - centerX)) * RAD + 90);
            } else {
                return lastAngle>0? 180:-180;
            }
        } else if (xPosition < centerX) {
            if (yPosition < centerY) {//2사분면
                return (int) (Math.atan((int)(yPosition - centerY) / (xPosition - centerX)) * RAD-90);
            } else if (yPosition > centerY) {//3사분면
                return (int) (Math.atan((int)(yPosition - centerY) / (xPosition - centerX)) * RAD-90);
            } else {
                return lastAngle>0?180:-180;
            }
        } else{
            return lastAngle;
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        centerX = canvas.getWidth()/2;
        centerY = canvas.getHeight()/2;
        Bitmap rawImage = BitmapFactory.decodeResource(getResources(), R.drawable.steeringwheel);
        float scale = canvas.getWidth()*0.9f/rawImage.getWidth();
        Bitmap wheelImage = Bitmap.createScaledBitmap(rawImage,Math.round(rawImage.getWidth()*scale),Math.round(rawImage.getHeight()*scale),true);
        matrix.reset();
        matrix.postTranslate(-(wheelImage.getWidth()/2), -(wheelImage.getHeight()/2));
        matrix.postRotate(angle);
        matrix.postTranslate(canvas.getWidth()/2,canvas.getHeight()/2);
        canvas.drawBitmap(wheelImage,matrix,null);

    }
    @Override
    public boolean onTouchEvent(MotionEvent event){
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                rc_run =false;
                break;
            case MotionEvent.ACTION_MOVE:
                int new_angle = getAngle(event.getX(), event.getY());
                if(rotation_lock){
                    if(lastAngle <0){
                        if((new_angle<0 && (lock_Angle-new_angle)>=0) || (new_angle>0 && (lock_Angle-new_angle)<=0))
                            lock_Angle = new_angle;
                        else{
                            if(new_angle>-180 && new_angle<-90)
                                rotation_lock = false;
                        }
                    }else{
                        if((new_angle<0 && (lock_Angle-new_angle)<=0) || (new_angle>0 && (lock_Angle-new_angle)<=0)) {

                            lock_Angle = new_angle;
                        }else{
                            if(new_angle<180 && new_angle>90)
                                rotation_lock = false;
                        }
                    }
                }else {
                    if (lastAngle > 90 && new_angle < -90) {
                        lastAngle = 180;
                        rotation_lock = true;
                        lock_Angle = -1*lastAngle;
                    } else if (lastAngle < -90 && new_angle > 90) {
                        lastAngle = -180;
                        rotation_lock = true;
                        lock_Angle = lastAngle;
                    } else {
                        if(10<lastAngle-new_angle)
                            lastAngle = lastAngle>180? 180:lastAngle-10;
                        else if(lastAngle-new_angle<-10)
                            lastAngle = lastAngle<-180? -180:lastAngle+10;
                        else
                            lastAngle = new_angle;
                    }
                }
                setRotation(lastAngle);
                handler.postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        steer_wheel_touch_lock = true;
                    }
                },100);
                steer_wheel_touch_lock = false;

                break;
            case MotionEvent.ACTION_UP:
                rotation_lock = false;
                steer_wheel_touch_lock = true;
                rc_run = true;
                handler.post(rotation_zero);
                break;
        }


        return steer_wheel_touch_lock;
    }
    private void setRotation(int angle){
        this.angle = angle;
        Log.e("setRotation",String.valueOf(angle));
        invalidate();
        onAngleChanged(angle,!rc_run);
    }
    Runnable rotation_zero = new Runnable(){
        @Override
        public void run(){
            if(rc_run) {
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
}
