package com.wonikrobotics.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.wonikrobotics.mobilecontroller.R;

import java.util.Vector;

public class JogJoystick extends ImageView{

    public static final float LEFT = 1.0f;
    public static final float CENTER = 0.0f;
    public static final float RIGHT = -1.0f;

    public static final float FORWARD = 1.0f;
    public static final float STOPPED = 0.0f;
    public static final float BACKWARD = -1.0f;

    private final int WIDTH = 200;
    private final int HEIGHT = 200;

    private int color = Color.rgb(254, 196, 54);

    private int centerX;
    private int centerY;

    private Rect areaMovable;

    private float angularWeight = 0.75f;
    private float linearWeight = 1.0f;

    private float angle = 0.0f;
    private float angleDir = CENTER;

    private float acc = 0.0f;
    private float accDir = STOPPED;

    private JoystickListener jl;

    //-- Data to be sent

    private float dataAngular;
    private float dataLinear;

    public interface JoystickListener{

        public void onMove(float dataAngular, float dataLinear);

    }

    public JogJoystick(Context context){

        super(context);
        initSettings(context);

    }

    public JogJoystick(Context context, AttributeSet attrs) {

        super(context, attrs);
        initSettings(context);

    }

    public JogJoystick(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
        initSettings(context);

    }

    public void setAreaMovable(Rect area){

        this.areaMovable = area;

        centerX = area.centerX();
        centerY = area.centerY();
    }

    public void changeColor(int color){

        this.color = color;
        invalidate();

    }

    protected void onDraw(Canvas c){

        super.onDraw(c);

        Paint p = new Paint();
        p.setColor(color);
        p.setAntiAlias(true);

        c.drawCircle(WIDTH /2, HEIGHT /2, 100, p);

    }

    private void initSettings(Context context) {

        //Layout size setting
        this.setLayoutParams(new ViewGroup.LayoutParams(WIDTH, HEIGHT));

        this.setMinimumWidth(WIDTH);
        this.setMinimumHeight(HEIGHT);

        this.setMaxWidth(WIDTH);
        this.setMaxHeight(HEIGHT);

        //Touch event setting
        this.setOnTouchListener(new OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent e){

                final int X = (int)e.getRawX();
                final int Y = (int)e.getRawY();

                switch(e.getAction() & MotionEvent.ACTION_MASK){

                    case MotionEvent.ACTION_DOWN:
                        break;

                    case MotionEvent.ACTION_MOVE:

                        float translateX = X - centerX;
                        float translateY = Y - centerY;

                        if(isInside(X, Y)) {

                            JogJoystick.this.setTranslationX(translateX);
                            JogJoystick.this.setTranslationY(translateY);

                            //Calculate angle
                            float angleCalculated = calculateAngle(X, Y);
                            angle = parseAngle(translateX, translateY, angleCalculated);

                            angleDir = determineAngleDir(translateX);
                            accDir = determineAccDir(translateY);

                            //-- Set data to be sent
                            setData(angle, angleDir, acc, accDir);

                            if(jl != null)
                                jl.onMove(dataAngular, dataLinear);

                        }else{

                            Vector<Double> maximumPoints = returnMaximumPoint(X, Y);

                            float maxTranslateX = (float)(maximumPoints.elementAt(0) - centerX);
                            float maxTranslateY = (float)(maximumPoints.elementAt(1) - centerY);

                            //Calculate angle
                            float angleCalculated = calculateAngle(X, Y);
                            angle = parseAngle(translateX, translateY, angleCalculated);

                            angleDir = determineAngleDir(maxTranslateX);
                            accDir = determineAccDir(maxTranslateY);

                            acc = 100.0f;

                            JogJoystick.this.setTranslationX(maxTranslateX);
                            JogJoystick.this.setTranslationY(maxTranslateY);

                            //-- Set data to be sent
                            setData(angle, angleDir, acc, accDir);

                            if(jl != null)
                                jl.onMove(dataAngular, dataLinear);

                        }

                        break;

                    case MotionEvent.ACTION_UP:

                        JogJoystick.this.setTranslationX(0);
                        JogJoystick.this.setTranslationY(0);

                        angleDir = CENTER;
                        angle = 0.0f;

                        accDir = STOPPED;
                        acc = 0.0f;

                        setData(angle, angleDir, acc, accDir);

                        if(jl != null)
                            jl.onMove(dataAngular, dataLinear);

                        break;

                    default:
                        break;

                }

                return true;

            }
        });

    }

    private float calculateAngle(int pX, int pY){

        int dx, dy;
        int ax, ay;

        float t;

        dx = centerX - pX;
        ax = Math.abs(dx);

        dy = centerY - pY;
        ay = Math.abs(dy);

        t = (ax + ay == 0) ? 0 : (float)dy / (ax + ay);

        if (dx < 0)
            t = 2 - t;

        else if (dy < 0)
            t = 4 + t;

        t = t * 90.0f;

        return t;

    }

    private float parseAngle(float translateX, float translateY, float angle){

        if(translateY < 0 && translateX < 0)
            angle = 90 - angle;

        else if(translateY < 0 && translateX > 0)
            angle = angle - 90;

        else if(translateY > 0 && translateX > 0)
            angle= 270 - angle;

        else if(translateY > 0 && translateX < 0)
            angle = angle - 270;

        else
            angle = 0.0f;

        return angle;

    }

    private float determineAccDir(float translateY){

        //Update information of Y
        if(translateY < 0)
            return FORWARD;

        else if(translateY > 0)
            return BACKWARD;

        else
            return STOPPED;

    }

    private float determineAngleDir(float translateX){

        //Update information of X
        if(translateX < 0)
            return LEFT;

        else if(translateX > 0)
            return RIGHT;

        else
            return CENTER;

    }

    private boolean isInside(int pX, int pY){

        double dx = pX - centerX;
        double dy = pY - centerY;

        double distance = Math.sqrt(Math.pow(dx, 2.0d) + Math.pow(dy, 2.0d));
        double radius = (areaMovable.height()/2 - HEIGHT /2);

        acc = (float)(distance / radius * 100);

        if(distance < areaMovable.height()/2 - HEIGHT /2)
            return true;

        else
            return false;

    }

    private Vector<Double> returnMaximumPoint(int x, int y){

        Vector<Double> result = new Vector<Double>();

        double dx = x - centerX;
        double dy = y - centerY;

        double radian = Math.atan2(dy, dx);
        double radius = (areaMovable.height()/2 - HEIGHT /2);

        result.add(centerX + Math.cos(radian) * radius);
        result.add(centerY + Math.sin(radian) * radius);

        return result;

    }

    private void parseData(){

        float parsedDir = angleDir * accDir;
        float parsedAWeight = (angularWeight * 0.5f) + 0.5f;
        float parsedLWeight = (linearWeight * 0.5f) + 0.5f;

        dataAngular = (angle / 90.0f) * parsedDir * parsedAWeight;
        dataLinear = (acc / 100.0f) * accDir * parsedLWeight;


    }

    public void setData(float vX, float dir0, float vY, float dir1){

        this.angle = vX;
        this.angleDir = dir0;

        this.acc = vY;
        this.accDir = dir1;

        parseData();

    }

    public void setWeight(float angularWeight, float linearWeight){

        this.angularWeight = angularWeight;
        this.linearWeight = linearWeight;

    }

    public void setOnJoystickListener(JoystickListener joystickListener){

        jl = joystickListener;

    }

    public float getDataAngular(){

        return dataAngular * angularWeight;
    }

    public float getDataLinear(){

        return dataLinear * linearWeight;
    }

    public String toString(){

        return angle + " " +
                angleDir + " " +
                acc + " " +
                accDir + " ";

    }

}

