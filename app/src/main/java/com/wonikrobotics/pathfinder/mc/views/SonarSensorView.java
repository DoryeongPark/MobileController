package com.wonikrobotics.pathfinder.mc.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * SonarSensorView
 *
 * @author      Weonwoo Joo
 * @date        1. 8. 2016
 *
 * @description View visualizing sonar sensor data
 */
public class SonarSensorView extends View {
    /***********************************
     * View scope
     ****************************************/
    public static final int AROUND_ROBOT = 1;
    public static final int FRONT_OF_ROBOT = 2;
    public static final int BEHIND_OF_ROBOT = 3;

    /*********************************
     * private values
     **************************************/
    private int minAngle[] = null, drawAngle[] = null;        // start angles , drawing angles (right - 0 , left - 180 , bottom - 90 ,top  - 270)
    private float cX = 0, cY = 0, radius = 0;                   // center point and radius
    private Paint red, green, line, word;                       // paints
    private float max_val = 5;                                  // max value of ranges
    private float[] valuelist;                                  // ranges array

    /**************************************
     * scope
     *************************************/
    private int scale_mode = 1;                                 // current scope

    public SonarSensorView(Context c, float[] values, int[] min, int[] draw) {
        super(c);
        initPaint();
        /*
            These arrays must be initiate.
         */
        this.valuelist = values;
        this.minAngle = min;
        this.drawAngle = draw;
    }

    public SonarSensorView(Context c, AttributeSet set, float[] values, int[] min, int[] draw) {
        super(c, set);
        /*
            These arrays must be initiate.
         */
        this.valuelist = values;
        this.minAngle = min;
        this.drawAngle = draw;
    }

    public SonarSensorView(Context c, AttributeSet set, int defaultStyle, float[] values, int[] min, int[] draw) {
        super(c, set, defaultStyle);
        /*
            These arrays must be initiate.
         */
        this.valuelist = values;
        this.minAngle = min;
        this.drawAngle = draw;
    }


    // Change max value of ranges
    public void setMaxval(float val) {
        this.max_val = val;
        invalidate();
    }

    public void zoomIn() {
        max_val = max_val * 0.9f;
    }

    public void zoomOut() {
        max_val = max_val * 1.1f;
    }
    private void initPaint() {
        red = new Paint();
        green = new Paint();
        line = new Paint();
        word = new Paint();
        red.setColor(Color.RED);
        red.setAlpha(80);
        green.setColor(Color.GREEN);
        green.setAlpha(80);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(2);
        line.setColor(Color.BLACK);
        line.setAlpha(50);
        word.setColor(Color.BLACK);
        word.setTextSize(40f);
    }

    public void setScale(int mode) {
        this.scale_mode = mode;
        invalidate();
    }

    /**
     * Update array of range. draw angle is not change
     *
     * @param list
     */
    public void update(float[] list) {
        this.valuelist = list;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (minAngle != null) {
            /**
             *  Draw circles on background to user can guess the value of displayed lines.
             */
            switch (scale_mode) {
                case AROUND_ROBOT:
                    cX = canvas.getWidth() / 2;
                    cY = canvas.getHeight() / 2;
                    if (canvas.getWidth() > canvas.getHeight())
                        radius = canvas.getHeight() / 2f;
                    else
                        radius = canvas.getWidth() / 2f;

                    canvas.drawCircle(cX, cY, radius * 1.0f, line);
                    canvas.drawCircle(cX, cY, radius * 0.8f, line);
                    canvas.drawCircle(cX, cY, radius * 0.6f, line);
                    canvas.drawCircle(cX, cY, radius * 0.4f, line);
                    canvas.drawCircle(cX, cY, radius * 0.2f, line);
                    canvas.drawText(Float.toString(Math.round(max_val * 10 * 1.0f) / 10f), cX - (radius * 1.0f), cY, word);
                    canvas.drawText(Float.toString(Math.round(max_val * 10 * 0.8f) / 10f), cX - (radius * 0.8f), cY, word);
                    canvas.drawText(Float.toString(Math.round(max_val * 10 * 0.6f) / 10f), cX - (radius * 0.6f), cY, word);
                    canvas.drawText(Float.toString(Math.round(max_val * 10 * 0.4f) / 10f), cX - (radius * 0.4f), cY, word);
                    canvas.drawText(Float.toString(Math.round(max_val * 10 * 0.2f) / 10f), cX - (radius * 0.2f), cY, word);
                    break;
                case FRONT_OF_ROBOT:
                    cX = canvas.getWidth() / 2;
                    cY = canvas.getHeight();
                    radius = canvas.getWidth() / 2f;
                    canvas.drawCircle(cX, cY, radius * 1.0f, line);
                    canvas.drawCircle(cX, cY, radius * 0.8f, line);
                    canvas.drawCircle(cX, cY, radius * 0.6f, line);
                    canvas.drawCircle(cX, cY, radius * 0.4f, line);
                    canvas.drawCircle(cX, cY, radius * 0.2f, line);
                    canvas.drawText(Float.toString(Math.round(max_val * 10 * 1.0f) / 10f), cX, cY - (radius * 1.0f), word);
                    canvas.drawText(Float.toString(Math.round(max_val * 10 * 0.8f) / 10f), cX, cY - (radius * 0.8f), word);
                    canvas.drawText(Float.toString(Math.round(max_val * 10 * 0.6f) / 10f), cX, cY - (radius * 0.6f), word);
                    canvas.drawText(Float.toString(Math.round(max_val * 10 * 0.4f) / 10f), cX, cY - (radius * 0.4f), word);
                    canvas.drawText(Float.toString(Math.round(max_val * 10 * 0.2f) / 10f), cX, cY - (radius * 0.2f), word);
                    break;
                case BEHIND_OF_ROBOT:
                    cX = canvas.getWidth() / 2;
                    cY = canvas.getHeight();
                    radius = canvas.getWidth() / 2f;

                    canvas.drawCircle(cX, 0, radius * 1.0f, line);
                    canvas.drawCircle(cX, 0, radius * 0.8f, line);
                    canvas.drawCircle(cX, 0, radius * 0.6f, line);
                    canvas.drawCircle(cX, 0, radius * 0.4f, line);
                    canvas.drawCircle(cX, 0, radius * 0.2f, line);
                    canvas.drawText(Float.toString(Math.round(max_val * 10 * 1.0f) / 10f), cX, (radius * 1.0f), word);
                    canvas.drawText(Float.toString(Math.round(max_val * 10 * 0.8f) / 10f), cX, (radius * 0.8f), word);
                    canvas.drawText(Float.toString(Math.round(max_val * 10 * 0.6f) / 10f), cX, (radius * 0.6f), word);
                    canvas.drawText(Float.toString(Math.round(max_val * 10 * 0.4f) / 10f), cX, (radius * 0.4f), word);
                    canvas.drawText(Float.toString(Math.round(max_val * 10 * 0.2f) / 10f), cX, (radius * 0.2f), word);
                    break;
            }
            /**
             *  Draw scaled arc
             */
            for (int i = 0; i < minAngle.length; i++) {
                float gapX = cX * (valuelist[i] / max_val);
                float gapY = cY * (valuelist[i] / max_val);
                RectF aroundRect;
                if (scale_mode == BEHIND_OF_ROBOT) {
                    aroundRect = new RectF(cX - gapX, -gapY, cX + gapX, gapY);
                } else {
                    aroundRect = new RectF(cX - gapX, cY - gapY, cX + gapX, cY + gapY);
                }
                canvas.drawArc(aroundRect, minAngle[i], drawAngle[i], true, green);

            }
        }


    }
}