package com.wonikrobotics.pathfinder.mc.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import com.wonikrobotics.pathfinder.mc.mobilecontroller.R;


/**
 * @author       : WeonWoo Joo
 * @date         : 28.07.2016
 * @description : Controll lever view
 * @thumb_image : {@drawable/ctr_thumb}
 */
public abstract class ControlLever extends View {

    private boolean ac_run = false;         // is vel_zero running? true : false
    private int progress = 0;                // current progress
    private Paint gray;                       // paint for background
    private Bitmap scaled_image = null;     // thumb image
    private float max = 0, min = 0;          // min, max value
    private Handler handler;                  // handler for vel_zero

    /**
     * Runnable instance to make controller heads default value
     */
    private Runnable vel_zero = new Runnable() {
        @Override
        public void run() {
            if (ac_run) {
                int gage = ControlLever.this.getProgress();
                if (gage > 0)
                    gage -= 5;
                else
                    gage += 5;
                if (-5 < gage && gage < 5)
                    gage = 0;
                if (gage != 0)
                    handler.postDelayed(vel_zero, 25);
                else
                    ac_run = false;
                ControlLever.this.setProgress(gage);
            }
        }
    };

    public ControlLever(Context c) {
        super(c);
        gray = new Paint();
        gray.setColor(Color.parseColor("#ddadaaaa"));
        handler = new Handler();
    }

    public int getProgress() {
        return this.progress;
    }

    public void setProgress(int degree) {
        /*
            set limit of progress
         */
        degree = degree > 100 ? 100 : degree;
        degree = degree < -100 ? -100 : degree;
        if (this.progress != degree) {
            this.progress = degree;
            onProgressChanged(this.progress, !ac_run);
            invalidate();
        }
    }

    /**
     * Progress change listener.
     * fromUser will be true if thumb is moved by user
     * and will be false if thumb is moved by vel_zero
     *
     * @param progress
     * @param fromUser
     */
    public abstract void onProgressChanged(int progress, boolean fromUser);

    @Override
    protected void onDraw(Canvas canvas) {
        if (max == 0 && min == 0) {
            max = canvas.getHeight() * 0.1f;
            min = canvas.getHeight() * 0.9f;
        }
        /*
            draw background bar
         */
        RectF roundRect = new RectF((canvas.getWidth() / 2) - 25, max, (canvas.getWidth() / 2) + 25, min);
        canvas.drawRoundRect(roundRect, 10f, 10f, gray);
        /*
            draw thumb
            scale is 0.1 * canvas' height
         */
        if (scaled_image == null) {
            Bitmap raw_image = BitmapFactory.decodeResource(getResources(), R.drawable.ctr_thumb);
            float scale = canvas.getHeight() * 0.1f / raw_image.getHeight();
            scaled_image = Bitmap.createScaledBitmap(raw_image, Math.round(raw_image.getWidth() * scale), Math.round(raw_image.getHeight() * scale), true);
            raw_image.recycle();
        }
        canvas.drawBitmap(scaled_image, (canvas.getWidth() - scaled_image.getWidth()) / 2f, canvas.getHeight() * 0.5f + (canvas.getHeight() * 0.4f * (this.progress / 100f)) - scaled_image.getHeight() / 2f, null);

    }

    /**
     * Touch event for handle user control
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                /*
                    if user thuch the thumb, vel_zero is stop
                 */
                ac_run = false;
                break;
            case MotionEvent.ACTION_MOVE:
                /*
                    this acts on not only vel_zero but also user control
                    it should be distinguished
                 */
                if (!ac_run) {
                    int new_progress = Math.round((event.getY() - ((max + min) / 2)) / ((min - max) / 200));
                    if (new_progress - this.progress > 5)
                        this.setProgress(this.progress + 5);
                    else if (new_progress - this.progress < -5)
                        this.setProgress(this.progress - 5);
                    else
                        this.setProgress(new_progress);
                }
                break;
            case MotionEvent.ACTION_UP:
                /*
                    if touch event is over, vel_zero start to run
                 */
                ac_run = true;
                handler.post(vel_zero);
                break;
        }
        return true;
    }
}
