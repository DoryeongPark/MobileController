package com.wonikrobotics.pathfinder.mc.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Felix on 2016-08-03.
 */
public class SonarSensorView extends View {
    public static final int AROUND_ROBOT = 1;
    public static final int FRONT_OF_ROBOT = 2;
    public static final int BEHIND_OF_ROBOT = 3;
    private int minAngle[] = null, drawAngle[] = null;
    private float cX = 0, cY = 0;
    private Paint red, green;
    private float max_val = 5;
    private float[] valuelist;
    private int scale_mode = 1;

    public SonarSensorView(Context c, float[] values, int[] min, int[] draw) {
        super(c);
        initPaint();
        this.valuelist = values;
        this.minAngle = min;
        this.drawAngle = draw;
    }

    public SonarSensorView(Context c, AttributeSet set) {
        super(c, set);
    }

    public SonarSensorView(Context c, AttributeSet set, int defaultStyle) {
        super(c, set, defaultStyle);
    }

    public void setMaxval(float val) {
        this.max_val = val;
        invalidate();
    }

    private void initPaint() {
        red = new Paint();
        green = new Paint();
        red.setColor(Color.RED);
        red.setAlpha(80);
        green.setColor(Color.GREEN);
        green.setAlpha(80);
    }

    public void setScale(int mode) {
        this.scale_mode = mode;
    }

    public void update(float[] list) {
        this.valuelist = list;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (minAngle != null) {
            for (int i = 0; i < minAngle.length; i++) {
                switch (scale_mode) {
                    case AROUND_ROBOT:
                        cX = canvas.getWidth() / 2;
                        cY = canvas.getHeight() / 2;
                        break;
                    case FRONT_OF_ROBOT:
                        cX = canvas.getWidth() / 2;
                        cY = canvas.getHeight();
                        break;
                    case BEHIND_OF_ROBOT:
                        cX = canvas.getWidth() / 2;
                        cY = canvas.getHeight();
                        break;
                }
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