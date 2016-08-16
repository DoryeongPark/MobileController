package com.wonikrobotics.pathfinder.mc.controller;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.wonikrobotics.pathfinder.mc.mobilecontroller.R;

import java.util.Vector;

/**
 * JogJoystick
 *
 * @author      Doryeong Park
 * @date        16. 8. 2016
 *
 * @description Jog joystick controller
 */
public class JogJoystick extends ImageView {

    /* Definition of direction constants */

    public static final float LEFT = 1.0f;
    public static final float CENTER = 0.0f;
    public static final float RIGHT = -1.0f;

    public static final float FORWARD = 1.0f;
    public static final float STOPPED = 0.0f;
    public static final float BACKWARD = -1.0f;

    private final int DEFAULT_WIDTH = 200;
    private final int DEFAULT_HEIGHT = 200;

    /* Rectangle area with absolute coordinate */

    private int centerX;
    private int centerY;

    private Rect areaMovable;

    /* Sensitivity default value */

    private float angularWeight = 0.75f;
    private float linearWeight = 1.0f;

    /* Intermediate data */

    private float angle = 0.0f;
    private float angleDir = CENTER;

    private float acc = 0.0f;
    private float accDir = STOPPED;

    /* Joystick listener */

    private JoystickListener jl;

    /* Current parsed data to be published */

    private float dataAngular;
    private float dataLinear;

    public JogJoystick(Context context) {

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

    /**
     * setAreaMovable
     * @param area
     * @description Set rectangle area with absolute coordinate joystick can move
     */
    public void setAreaMovable(Rect area) {

        this.areaMovable = area;

        centerX = area.centerX();
        centerY = area.centerY();

        int widthResized = areaMovable.width() / 4;
        int heightResized = areaMovable.height() / 4;

        //Layout size setting
        this.setLayoutParams(new ViewGroup.LayoutParams(widthResized, heightResized));

        this.setMinimumWidth(widthResized);
        this.setMinimumHeight(heightResized);

        this.setMaxWidth(widthResized);
        this.setMaxHeight(heightResized);

        invalidate();

    }

    protected void onDraw(Canvas c) {

        super.onDraw(c);

        Drawable joystickImage = getContext().getResources().getDrawable(R.drawable.ctr_thumb);
        this.setImageDrawable(joystickImage);

    }

    /**
     * initSettings
     * @param context
     * @description Defines joystick's size & event which occurs when user moves joystick
     */
    private void initSettings(Context context) {

        //Layout size setting
        this.setLayoutParams(new ViewGroup.LayoutParams(DEFAULT_WIDTH, DEFAULT_HEIGHT));

        this.setMinimumWidth(DEFAULT_WIDTH);
        this.setMinimumHeight(DEFAULT_HEIGHT);

        this.setMaxWidth(DEFAULT_WIDTH);
        this.setMaxHeight(DEFAULT_HEIGHT);

        //Touch event setting
        this.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent e) {

                final int X = (int) e.getRawX();
                final int Y = (int) e.getRawY();

                switch (e.getAction() & MotionEvent.ACTION_MASK) {

                    case MotionEvent.ACTION_DOWN:
                        break;

                    case MotionEvent.ACTION_MOVE:

                        float translateX = X - centerX;
                        float translateY = Y - centerY;

                        if (isInside(X, Y)) {       //If user is moving joystick inside of area

                            JogJoystick.this.setTranslationX(translateX);
                            JogJoystick.this.setTranslationY(translateY);

                            //Calculate angle
                            float angleCalculated = calculateAngle(X, Y);
                            angle = parseAngle(translateX, translateY, angleCalculated);

                            angleDir = determineAngleDir(translateX);
                            accDir = determineAccDir(translateY);

                            parseData();

                            if (jl != null)
                                jl.onMove(dataAngular, dataLinear);

                        } else {        //If user is moving joystick outside of area

                            Vector<Double> maximumPoints = returnMaximumPoint(X, Y);

                            float maxTranslateX = (float) (maximumPoints.elementAt(0) - centerX);
                            float maxTranslateY = (float) (maximumPoints.elementAt(1) - centerY);

                            acc = 100.0f;

                            //Calculate angle
                            float angleCalculated = calculateAngle(X, Y);
                            angle = parseAngle(translateX, translateY, angleCalculated);

                            angleDir = determineAngleDir(maxTranslateX);
                            accDir = determineAccDir(maxTranslateY);

                            JogJoystick.this.setTranslationX(maxTranslateX);
                            JogJoystick.this.setTranslationY(maxTranslateY);

                            parseData();

                            if (jl != null)
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

                        parseData();

                        if (jl != null)
                            jl.onMove(dataAngular, dataLinear);

                        break;

                    default:
                        break;

                }

                return true;

            }
        });

    }

    /**
     * calculateAngle
     * @param pX Coordinate x of event
     * @param pY Coordinate y of event
     * @description From axis X of center to coordinate of event, calculate absolute angle(0 ~ 360)
     * @return Angle value(degree: 0 ~ 360)
     */
    private float calculateAngle(int pX, int pY) {

        int dx, dy;
        int ax, ay;

        float t;

        dx = centerX - pX;
        ax = Math.abs(dx);

        dy = centerY - pY;
        ay = Math.abs(dy);

        t = (ax + ay == 0) ? 0 : (float) dy / (ax + ay);

        if (dx < 0)
            t = 2 - t;

        else if (dy < 0)
            t = 4 + t;

        t = t * 90.0f;

        return t;

    }

    /**
     * parseAngle
     * @param translateX Coordinate x of event
     * @param translateY Coordinate y of event
     * @param angle Angle value(degree: 0 ~ 360)
     * @description Parse absolute angle to rotation angle (degree: 0 ~ 90)
     * @return Rotation angle (degree: 0 ~ 90)
     */
    private float parseAngle(float translateX, float translateY, float angle) {

        if (translateY < 0 && translateX < 0)
            angle = 90 - angle;

        else if (translateY < 0 && translateX > 0)
            angle = angle - 90;

        else if (translateY > 0 && translateX > 0)
            angle = 270 - angle;

        else if (translateY > 0 && translateX < 0)
            angle = angle - 270;

        else
            angle = 0.0f;

        return angle;

    }

    /**
     * determineAccDir
     * @param translateY Coordinate y of event
     * @return Defined constant of direction
     */
    private float determineAccDir(float translateY) {

        //Update information of Y
        if (translateY < 0)
            return FORWARD;

        else if (translateY > 0)
            return BACKWARD;

        else
            return STOPPED;

    }

    /**
     * determineAngleDir
     * @param translateX Coordinate x of event
     * @return Defined constant of direction
     */
    private float determineAngleDir(float translateX) {

        if (translateX < 0)
            return LEFT;

        else if (translateX > 0)
            return RIGHT;

        else
            return CENTER;

    }

    /**
     * isInside
     * @param pX Coordinate x of event
     * @param pY Coordinate y of event
     * @desrpition Determines whether current point is inside of area or not then calculate speed
     * @return Whether current point is inside of area or not
     */
    private boolean isInside(int pX, int pY) {

        double dx = pX - centerX;
        double dy = pY - centerY;

        double distance = Math.sqrt(Math.pow(dx, 2.0d) + Math.pow(dy, 2.0d));
        double radius = (areaMovable.height() / 2 - this.getHeight() / 2);

        if (distance < areaMovable.height() / 2 - this.getHeight() / 2) {

            acc = (float) (distance / radius * 100);

            return true;

        }else{

            return false;

        }

    }

    /**
     * ReturnMaximumPoint
     * @param x Coordinate x of event
     * @param y Coordinate y of event
     * @description Returns maximum point for current point in circle area joystick can move
     * @return Maximum coordinate x, y
     */
    private Vector<Double> returnMaximumPoint(int x, int y) {

        Vector<Double> result = new Vector<Double>();

        double dx = x - centerX;
        double dy = y - centerY;

        double radian = Math.atan2(dy, dx);
        double radius = (areaMovable.height() / 2 - DEFAULT_HEIGHT / 2);

        result.add(centerX + Math.cos(radian) * radius);
        result.add(centerY + Math.sin(radian) * radius);

        return result;

    }

    /**
     * parseData
     * @description Parse data from 4 values of angular speed, angular direction, linear speed, linear direction
     *              to 2 values of angular velocity, linear velocity applying sensitivity for ROS master
     */
    private void parseData() {

        float parsedDir = angleDir * accDir;

        dataAngular = (angle / 90.0f) * parsedDir * angularWeight;
        dataLinear = (acc / 100.0f) * accDir * linearWeight;

    }

    public void setWeight(float angularWeight, float linearWeight) {

        this.angularWeight = angularWeight;
        this.linearWeight = linearWeight;

    }

    public void setOnJoystickListener(JoystickListener joystickListener) {

        jl = joystickListener;

    }

    public String toString() {

        return angle + " " +
                angleDir + " " +
                acc + " " +
                accDir + " ";

    }

    /**
     * Listener for data variables to be published
     */
    public interface JoystickListener {

        void onMove(float dataAngular, float dataLinear);

    }

}
