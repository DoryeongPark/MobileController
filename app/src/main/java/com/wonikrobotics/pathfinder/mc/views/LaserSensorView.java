package com.wonikrobotics.pathfinder.mc.views;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * LaserSensorView
 *
 * @author      Weonwoo Joo
 * @date        1. 8. 2016
 *
 * @description View visualizing laser sensor data
 */
public abstract class LaserSensorView extends View {
    /***********************************
     * View scope
     ****************************************/
    public static final int AROUND_ROBOT = 1;
    public static final int FRONT_OF_ROBOT = 2;
    /*********************************** Display mode ****************************************/
    public static final int FILL_INSIDE = 3;
    public static final int FILL_OUTSIDE = 4;
    public static final int POINT_CLOUD = 5;
    public static final int POINT_CLOUD_FILL_INSIDE = 6;
    public static final int POINT_CLOUD_FILL_OUTSIDE = 7;

    /******************************* message from sensor *************************************/
    private sensor_msgs.LaserScan scan_msg;

    /**************************** Paint instances for drawing **********************************/
    private Paint paint = new Paint();
    private Paint line = new Paint();
    private Paint laser = new Paint();
    private Paint point = new Paint();

    /***********************************
     * private values
     *******************************************/
    private float max_val = 0;                      // max range to display
    private float width, height, radius;          // canvas' width,canvas' height, radius for draw range
    private boolean mode = false;                  // zoomin,zoomout pinch mode
    private float old_dist = 1f;                    // value for calculate distance gap during zoomin,zoomout

    private PointF near = null, far = null;         // two points for drawing line
    private OnAutoResizeChangeListener resizeChangeListener = null;

    /*************************** scope & display mode & auto resizing *****************************/
    private boolean autoResizing = true;
    private int currentRange = 1;
    private int currentDisplay = 3;

    public LaserSensorView(Context c) {
        super(c);
        /*
            Paint initiate
         */
        paint.setColor(Color.BLACK);
        paint.setTextSize(40f);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(2);
        line.setColor(Color.BLACK);
        line.setAlpha(50);
        laser.setColor(Color.RED);
        laser.setAlpha(70);
        laser.setStrokeWidth(3);
        point.setColor(Color.BLUE);
        point.setAlpha(70);
        point.setStrokeWidth(10);
    }

    public void setAutoResizing(boolean tf) {
        this.autoResizing = tf;
    }

    /**
     *  Store new message from sensor.
     *  This method have to be called on listener of subscriber.
     *
     * @param nm
     */
    public void update(sensor_msgs.LaserScan nm) {
        if (nm != null) {
            this.scan_msg = nm;
            /*
                Update max_val.
                If auto resizing mode is on, find max value from float array automatically.
             */
            if (max_val == 0)
                max_val = scan_msg.getRangeMax();
            if (autoResizing) {
                float newMax = 0f;
                for (float range : nm.getRanges()) {
                    if (newMax < range)
                        newMax = range;
                }
                max_val = newMax + 1;
            }
            this.onMaxValChanged(max_val);
            invalidate();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (this.scan_msg != null) {
            width = canvas.getWidth();
            height = canvas.getHeight();
            /*
                Initiate radius of max range.
             */
            if (currentRange == AROUND_ROBOT) {
                if (width > height)
                    radius = height / 2f;
                else
                    radius = width / 2f;
            } else if (currentRange == FRONT_OF_ROBOT) {
                if (width / 2f > height)
                    radius = height;
                else
                    radius = width / 2f;
            }

            /**
             *  Draw circles on background to user can guess the value of displayed lines.
             */
            if (currentRange == AROUND_ROBOT) {
                canvas.drawCircle(width / 2f, height / 2f, radius * 1.0f, line);
                canvas.drawCircle(width / 2f, height / 2f, radius * 0.8f, line);
                canvas.drawCircle(width / 2f, height / 2f, radius * 0.6f, line);
                canvas.drawCircle(width / 2f, height / 2f, radius * 0.4f, line);
                canvas.drawCircle(width / 2f, height / 2f, radius * 0.2f, line);
                canvas.drawText(Float.toString(Math.round(max_val * 10 * 1.0f) / 10f), width / 2f - (radius * 1.0f) / 1.414f, height / 2f - (radius * 1.0f) / 1.414f, paint);
                canvas.drawText(Float.toString(Math.round(max_val * 10 * 0.8f) / 10f), width / 2f - (radius * 0.8f) / 1.414f, height / 2f - (radius * 0.8f) / 1.414f, paint);
                canvas.drawText(Float.toString(Math.round(max_val * 10 * 0.6f) / 10f), width / 2f - (radius * 0.6f) / 1.414f, height / 2f - (radius * 0.6f) / 1.414f, paint);
                canvas.drawText(Float.toString(Math.round(max_val * 10 * 0.4f) / 10f), width / 2f - (radius * 0.4f) / 1.414f, height / 2f - (radius * 0.4f) / 1.414f, paint);
                canvas.drawText(Float.toString(Math.round(max_val * 10 * 0.2f) / 10f), width / 2f - (radius * 0.2f) / 1.414f, height / 2f - (radius * 0.2f) / 1.414f, paint);
            } else if (currentRange == FRONT_OF_ROBOT) {
                canvas.drawCircle(width / 2f, height, radius * 1.0f, line);
                canvas.drawCircle(width / 2f, height, radius * 0.8f, line);
                canvas.drawCircle(width / 2f, height, radius * 0.6f, line);
                canvas.drawCircle(width / 2f, height, radius * 0.4f, line);
                canvas.drawCircle(width / 2f, height, radius * 0.2f, line);
                canvas.drawText(Float.toString(Math.round(max_val * 10 * 1.0f) / 10f), width / 2f - (radius * 1.0f) / 1.414f, height - (radius * 1.0f) / 1.414f, paint);
                canvas.drawText(Float.toString(Math.round(max_val * 10 * 0.8f) / 10f), width / 2f - (radius * 0.8f) / 1.414f, height - (radius * 0.8f) / 1.414f, paint);
                canvas.drawText(Float.toString(Math.round(max_val * 10 * 0.6f) / 10f), width / 2f - (radius * 0.6f) / 1.414f, height - (radius * 0.6f) / 1.414f, paint);
                canvas.drawText(Float.toString(Math.round(max_val * 10 * 0.4f) / 10f), width / 2f - (radius * 0.4f) / 1.414f, height - (radius * 0.4f) / 1.414f, paint);
                canvas.drawText(Float.toString(Math.round(max_val * 10 * 0.2f) / 10f), width / 2f - (radius * 0.2f) / 1.414f, height - (radius * 0.2f) / 1.414f, paint);
            }


            float angle = scan_msg.getAngleMin();
            switch (currentDisplay) {
                case FILL_INSIDE:
                    float[] lineEndPoints = new float[scan_msg.getRanges().length * 4];
                    int numEndPoints = 0;
                    for (float range : scan_msg.getRanges()) {
                        // Only process ranges which are in the valid range.
                        // Draw lines from center(near) to scaled point(far) of range value
                        if (scan_msg.getRangeMin() <= range && range <= scan_msg.getRangeMax()) {
                            if (currentRange == AROUND_ROBOT) {
                                near = new PointF(width / 2f, height / 2f);
                            } else if (currentRange == FRONT_OF_ROBOT) {
                                near = new PointF(width / 2f, height);
                            }

                            far = null;
                            if (currentRange == AROUND_ROBOT) {
                                far = new PointF((width / 2f) - (float) Math.sin(angle) * (radius) * (range / max_val),
                                        height / 2f - (float) Math.cos(angle) * (radius) * (range / max_val));
                            } else if (currentRange == FRONT_OF_ROBOT) {
                                far = new PointF((width / 2f) - (float) Math.sin(angle) * (radius) * (range / max_val),
                                        height - (float) Math.cos(angle) * (radius) * (range / max_val));
                            }
                            if (far != null) {
                                lineEndPoints[numEndPoints++] = near.x;
                                lineEndPoints[numEndPoints++] = near.y;
                                lineEndPoints[numEndPoints++] = far.x;
                                lineEndPoints[numEndPoints++] = far.y;
                            }
                        }
                        angle += scan_msg.getAngleIncrement();
                    }
                    canvas.drawLines(lineEndPoints, 0, numEndPoints, laser);
                    break;
                case FILL_OUTSIDE:
                    float[] lineEndPoints2 = new float[scan_msg.getRanges().length * 4];
                    int numEndPoints2 = 0;
                    for (float range : scan_msg.getRanges()) {
                        // Only process ranges which are in the valid range.
                        // Draw lines from scaled point(near) of range value to scaled point(far) of max value
                        if (scan_msg.getRangeMin() <= range && range <= scan_msg.getRangeMax()) {
                            far = null;
                            near = null;
                            if (currentRange == AROUND_ROBOT) {
                                far = new PointF((width / 2f) - (float) Math.sin(angle) * (radius),
                                        height / 2f - (float) Math.cos(angle) * (radius));
                                near = new PointF((width / 2f) - (float) Math.sin(angle) * (radius) * (range / max_val),
                                        height / 2f - (float) Math.cos(angle) * (radius) * (range / max_val));
                            } else if (currentRange == FRONT_OF_ROBOT) {
                                far = new PointF((width / 2f) - (float) Math.sin(angle) * (radius),
                                        height - (float) Math.cos(angle) * (radius));
                                near = new PointF((width / 2f) - (float) Math.sin(angle) * (radius) * (range / max_val),
                                        height - (float) Math.cos(angle) * (radius) * (range / max_val));
                            }
                            if (near != null) {
                                lineEndPoints2[numEndPoints2++] = near.x;
                                lineEndPoints2[numEndPoints2++] = near.y;
                                lineEndPoints2[numEndPoints2++] = far.x;
                                lineEndPoints2[numEndPoints2++] = far.y;
                            }
                        }
                        angle += scan_msg.getAngleIncrement();
                    }
                    canvas.drawLines(lineEndPoints2, 0, numEndPoints2, laser);
                    break;
                case POINT_CLOUD:
                    float[] lineEndPoints3 = new float[scan_msg.getRanges().length * 2];
                    int numEndPoints3 = 0;
                    for (float range : scan_msg.getRanges()) {
                        // Only process ranges which are in the valid range.
                        // Draw points at scaled point(near) of range value
                        if (scan_msg.getRangeMin() <= range && range <= scan_msg.getRangeMax()) {
                            near = null;
                            if (currentRange == AROUND_ROBOT) {
                                near = new PointF((width / 2f) - (float) Math.sin(angle) * (radius) * (range / max_val),
                                        height / 2f - (float) Math.cos(angle) * (radius) * (range / max_val));
                            } else if (currentRange == FRONT_OF_ROBOT) {
                                near = new PointF((width / 2f) - (float) Math.sin(angle) * (radius) * (range / max_val),
                                        height - (float) Math.cos(angle) * (radius) * (range / max_val));
                            }
                            if (near != null) {
                                lineEndPoints3[numEndPoints3++] = near.x;
                                lineEndPoints3[numEndPoints3++] = near.y;
                            }
                        }
                        angle += scan_msg.getAngleIncrement();
                    }
                    canvas.drawPoints(lineEndPoints3, 0, numEndPoints3, point);
                    break;
                case POINT_CLOUD_FILL_INSIDE:
                    int size = scan_msg.getRanges().length;
                    float[] lineEndPoints4 = new float[size * 4];
                    float[] cloudEndPoints = new float[size * 2];
                    int numLinePoints = 0;
                    int numCloudPoints = 0;
                    for (float range : scan_msg.getRanges()) {
                        // Only process ranges which are in the valid range.
                        // Draw points at scaled point(far) of range value and draw lines from center(near) to scaled point(far) of range value
                        if (scan_msg.getRangeMin() <= range && range <= scan_msg.getRangeMax()) {
                            if (currentRange == AROUND_ROBOT) {
                                near = new PointF(width / 2f, height / 2f);
                            } else if (currentRange == FRONT_OF_ROBOT) {
                                near = new PointF(width / 2f, height);
                            }

                            far = null;
                            if (currentRange == AROUND_ROBOT) {
                                far = new PointF((width / 2f) - (float) Math.sin(angle) * (radius) * (range / max_val),
                                        height / 2f - (float) Math.cos(angle) * (radius) * (range / max_val));
                            } else if (currentRange == FRONT_OF_ROBOT) {
                                far = new PointF((width / 2f) - (float) Math.sin(angle) * (radius) * (range / max_val),
                                        height - (float) Math.cos(angle) * (radius) * (range / max_val));
                            }
                            if (far != null) {
                                lineEndPoints4[numLinePoints++] = near.x;
                                lineEndPoints4[numLinePoints++] = near.y;
                                lineEndPoints4[numLinePoints++] = far.x;
                                lineEndPoints4[numLinePoints++] = far.y;
                                cloudEndPoints[numCloudPoints++] = far.x;
                                cloudEndPoints[numCloudPoints++] = far.y;
                            }
                        }
                        angle += scan_msg.getAngleIncrement();
                    }
                    canvas.drawLines(lineEndPoints4, 0, numLinePoints, laser);
                    canvas.drawPoints(cloudEndPoints, 0, numCloudPoints, point);
                    break;
                case POINT_CLOUD_FILL_OUTSIDE:
                    int size2 = scan_msg.getRanges().length;
                    float[] lineEndPoints5 = new float[size2 * 4];
                    float[] cloudEndPoints2 = new float[size2 * 2];
                    int numLinePoints2 = 0;
                    int numCloudPoints2 = 0;
                    for (float range : scan_msg.getRanges()) {
                        // Only process ranges which are in the valid range.
                        // Draw points at scaled point(near) of range value and draw lines from scaled point(near) of range value to scaled point(far) of max value
                        if (scan_msg.getRangeMin() <= range && range <= scan_msg.getRangeMax()) {
                            far = null;
                            near = null;
                            if (currentRange == AROUND_ROBOT) {
                                far = new PointF((width / 2f) - (float) Math.sin(angle) * (radius),
                                        height / 2f - (float) Math.cos(angle) * (radius));
                                near = new PointF((width / 2f) - (float) Math.sin(angle) * (radius) * (range / max_val),
                                        height / 2f - (float) Math.cos(angle) * (radius) * (range / max_val));
                            } else if (currentRange == FRONT_OF_ROBOT) {
                                far = new PointF((width / 2f) - (float) Math.sin(angle) * (radius),
                                        height - (float) Math.cos(angle) * (radius));
                                near = new PointF((width / 2f) - (float) Math.sin(angle) * (radius) * (range / max_val),
                                        height - (float) Math.cos(angle) * (radius) * (range / max_val));
                            }
                            if (far != null) {
                                lineEndPoints5[numLinePoints2++] = near.x;
                                lineEndPoints5[numLinePoints2++] = near.y;
                                lineEndPoints5[numLinePoints2++] = far.x;
                                lineEndPoints5[numLinePoints2++] = far.y;
                                cloudEndPoints2[numCloudPoints2++] = near.x;
                                cloudEndPoints2[numCloudPoints2++] = near.y;
                            }
                        }
                        angle += scan_msg.getAngleIncrement();
                    }
                    canvas.drawLines(lineEndPoints5, 0, numLinePoints2, laser);
                    canvas.drawPoints(cloudEndPoints2, 0, numCloudPoints2, point);
                    break;
            }
        }
    }

    public void setDisplayRangeMode(int mode) {
        this.currentRange = mode;
        invalidate();
    }

    public void setDiplayMode(int mode) {
        this.currentDisplay = mode;
        invalidate();
    }

    public void setLaserPaint(Paint p) {
        this.laser = p;
        invalidate();
    }

    public void setPointPaint(Paint p) {
        this.point = p;
        invalidate();
    }


    public void zoomIn() {
        max_val = max_val * 0.9f;
        this.onMaxValChanged(max_val);
        /*
            Auto resize mode should be off
         */
        autoResizing = false;
        if (resizeChangeListener != null)
            resizeChangeListener.onChange(autoResizing);
    }

    public void zoomOut() {
        max_val = max_val * 1.1f;
        this.onMaxValChanged(max_val);
        /*
            Auto resize mode should be off
         */
        autoResizing = false;
        if (resizeChangeListener != null)
            resizeChangeListener.onChange(autoResizing);
    }
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:    //첫번째 손가락 터치
                break;
            case MotionEvent.ACTION_MOVE:   // 드래그 중이면, 이미지의 X,Y값을 변환시키면서 위치 이동.
                if (mode == true && e.getPointerCount() >= 2) {    // 핀치줌 중이면, 이미지의 거리를 계산해서 확대를 한다.
                    float dist = spacing(e);
                    if (dist - old_dist > 20) {  // zoom in
                        zoomIn();
                    } else if (old_dist - dist > 20) {  // zoom out
                        zoomOut();
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
                old_dist = spacing(e);
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.i("touch", "cancel");
            default:
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


    /**
     * Calculate distance between two touch points
     *
     * @param event
     * @return
     */
    private float spacing(MotionEvent event) {
        if (event.getPointerCount() >= 2) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);

            return (float) Math.sqrt(x * x + y * y);
        }
        return 0;

    }

    public interface OnAutoResizeChangeListener {
        void onChange(boolean onOff);
    }

}