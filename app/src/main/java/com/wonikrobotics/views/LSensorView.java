package com.wonikrobotics.views;

/**
 * Created by Felix on 2016-08-03.
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Notebook on 2016-07-13.
 */
public abstract class LSensorView extends View{
    private boolean mode = false;
    private float old_dist = 1f;
    private Paint paint = new Paint();
    private Paint back = new Paint();
    private Paint line = new Paint();
    private Paint red = new Paint();
    sensor_msgs.LaserScan scan_msg;
    PointF near = null;
    RectF rf = null;
    float max_val = 0;
    float width,height;
    public LSensorView(Context c){
        super(c);
        paint.setColor(Color.BLACK);
        paint.setTextSize(40f);
        back.setColor(Color.WHITE);
        line.setColor(Color.BLACK);
        line.setAlpha(50);
        red.setColor(Color.RED);
    }

    public void update(sensor_msgs.LaserScan nm){
        if(nm!=null){
            this.scan_msg = nm;
            if(max_val == 0)
                max_val = scan_msg.getRangeMax();
            invalidate();
        }
    }
    @Override
    public void onDraw(Canvas canvas){
        if(this.scan_msg != null) {
            if(width ==0 && height == 0){
                width = canvas.getWidth();
                height=canvas.getHeight();
            }
            if(rf == null){
                rf = new RectF(0,0,width,height*2);
            }
            rf.set(0,0,width,height*2);


            float[] lineEndPoints = new float[scan_msg.getRanges().length * 4];
            int numEndPoints = 0;
            float angle = scan_msg.getAngleMin();
            for (float range : scan_msg.getRanges()) {
                // Only process ranges which are in the valid range.
                if (scan_msg.getRangeMin() <= range && range <= scan_msg.getRangeMax()) {
                    if(near == null){
                        near = new PointF(width/2f,height);
                    }
                    PointF far = new PointF((width/2f)-(float)Math.sin(angle)*(width/2f)*(range/max_val),
                            height - (float)Math.cos(angle)*(width/2)*(range/max_val));
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
            case MotionEvent.ACTION_POINTER_DOWN:
                mode = true;
                old_dist = spacing(e);
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.i("touch","cancel");
            default :
                break;

        }
        return true;
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

}