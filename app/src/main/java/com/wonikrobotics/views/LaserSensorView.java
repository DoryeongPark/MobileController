package com.wonikrobotics.views;

/**
 * Created by Felix on 2016-08-03.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Notebook on 2016-07-13.
 */
public abstract class LaserSensorView extends View {
    private sensor_msgs.LaserScan scan_msg;
    private PointF near = null;
    private float max_val = 0;
    private float width, height, radius;
    private boolean mode = false;
    private float old_dist = 1f;
    private Paint paint = new Paint();
    private Paint line = new Paint();
    private Paint red = new Paint();
    private OnAutoResizeChangeListener resizeChangeListener = null;
    private boolean autoResizing = true;

    public LaserSensorView(Context c) {
        super(c);
        paint.setColor(Color.BLACK);
        paint.setTextSize(40f);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(2);
        line.setColor(Color.BLACK);
        line.setAlpha(50);
        red.setColor(Color.RED);
    }

    public void setAutoResizing(boolean tf) {
        this.autoResizing = tf;
    }
    public void update(sensor_msgs.LaserScan nm){
        if(nm!=null){
            this.scan_msg = nm;
            if(max_val == 0)
                max_val = scan_msg.getRangeMax();
            if (autoResizing) {
                float newMax = 0f;
                for (float range : nm.getRanges()) {
                    if (newMax < range)
                        newMax = range;
                }
                max_val = newMax + 1;
            }
            invalidate();
        }
    }
    @Override
    public void onDraw(Canvas canvas){
        if(this.scan_msg != null) {
            if(width ==0 && height == 0){
                width = canvas.getWidth();
                height=canvas.getHeight();
                if (width > height)
                    radius = height / 2f;
                else
                    radius = width / 2f;
            }
            canvas.drawCircle(width / 2f, height / 2f, radius, line);
            canvas.drawCircle(width / 2f, height / 2f, radius * 0.8f, line);
            canvas.drawCircle(width / 2f, height / 2f, radius * 0.6f, line);
            canvas.drawCircle(width / 2f, height / 2f, radius * 0.4f, line);
            canvas.drawCircle(width / 2f, height / 2f, radius * 0.2f, line);
            canvas.drawText(Integer.toString(Math.round(max_val)), width / 2f - (radius * 1.0f), height / 2f, paint);
            canvas.drawText(Integer.toString(Math.round(max_val * 0.8f)), width / 2f - (radius * 0.8f), height / 2f, paint);
            canvas.drawText(Integer.toString(Math.round(max_val * 0.6f)), width / 2f - (radius * 0.6f), height / 2f, paint);
            canvas.drawText(Integer.toString(Math.round(max_val * 0.4f)), width / 2f - (radius * 0.4f), height / 2f, paint);
            canvas.drawText(Integer.toString(Math.round(max_val * 0.2f)), width / 2f - (radius * 0.2f), height / 2f, paint);

            float[] lineEndPoints = new float[scan_msg.getRanges().length * 4];
            int numEndPoints = 0;
            float angle = scan_msg.getAngleMin();
            for (float range : scan_msg.getRanges()) {
                // Only process ranges which are in the valid range.
                if (scan_msg.getRangeMin() <= range && range <= scan_msg.getRangeMax()) {
                    if(near == null){
                        near = new PointF(width / 2f, height / 2f);
                    }
                    PointF far = new PointF((width / 2f) - (float) Math.sin(angle) * (radius) * (range / max_val),
                            height / 2f - (float) Math.cos(angle) * (radius) * (range / max_val));
                    lineEndPoints[numEndPoints++] = near.x;
                    lineEndPoints[numEndPoints++] = near.y;
                    lineEndPoints[numEndPoints++] = far.x;
                    lineEndPoints[numEndPoints++] = far.y;
                }
                angle += scan_msg.getAngleIncrement();
            }
            canvas.drawLines(lineEndPoints, 0, numEndPoints, red);
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent e){
        switch(e.getAction()& MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:    //첫번째 손가락 터치
                break;
            case MotionEvent.ACTION_MOVE:   // 드래그 중이면, 이미지의 X,Y값을 변환시키면서 위치 이동.
                if (mode == true && e.getPointerCount() >= 2) {    // 핀치줌 중이면, 이미지의 거리를 계산해서 확대를 한다.
                    float dist = spacing(e);
                    if (dist - old_dist > 20) {  // zoom in
                        max_val = max_val*0.9f;
                        this.onMaxValChanged(max_val);
                    } else if(old_dist - dist > 20) {  // zoom out
                        max_val = max_val*1.1f;
                        this.onMaxValChanged(max_val);
                    }
                    old_dist = dist;
                }
                break;
            case MotionEvent.ACTION_UP:    // 첫번째 손가락을 떼었을 경우
            case MotionEvent.ACTION_POINTER_UP:  // 두번째 손가락을 떼었을 경우
                mode = false;
                break;
            case MotionEvent.ACTION_POINTER_DOWN: //두번째 손가락을 터치한 경우
                mode = true;
                autoResizing = false;
                if (resizeChangeListener != null)
                    resizeChangeListener.onChange(autoResizing);
                old_dist = spacing(e);
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.i("touch","cancel");
            default :
                break;

        }
        return true;
    }

    public void setOnAutoResizeChangeListener(OnAutoResizeChangeListener listener) {
        this.resizeChangeListener = listener;
    }

    public void clearOnAutoResizeChangeListner() {
        this.resizeChangeListener = null;
    }
    public abstract void onMaxValChanged(float val);
    private float spacing(MotionEvent event) {
        if(event.getPointerCount() >= 2) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);

            return (float) Math.sqrt(x * x + y * y);
        }
        return 0;

    }

    public static abstract class OnAutoResizeChangeListener {
        public abstract void onChange(boolean onOff);
    }

}