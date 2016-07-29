package com.wonikrobotics.controller;

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

import com.wonikrobotics.mobilecontroller.R;

/**
 * Created by Notebook on 2016-07-28.
 */
public abstract class ControlLever extends View {
    private boolean ac_run = false;
    private int progress = 0;
    private Paint gray;
    private Bitmap scaled_image=null;
    private float max=0,min=0;
    private Handler handler;

    public ControlLever(Context c){
        super(c);
        gray = new Paint();
        gray.setColor(Color.parseColor("#ddadaaaa"));
        handler = new Handler();
    }
    public void setProgress(int degree){
        degree = degree > 100 ? 100:degree;
        degree = degree < -100? -100:degree;
        if(this.progress != degree) {
            this.progress = degree;
            onProgressChanged(this.progress,!ac_run);
            invalidate();
        }
    }
    public int getProgress(){return this.progress;}
    public abstract void onProgressChanged(int progress,boolean fromUser);
    @Override
    protected void onDraw(Canvas canvas){
        if(max ==0 && min == 0){
            max =canvas.getHeight()*0.1f;
            min = canvas.getHeight()*0.9f;
        }
        RectF roundRect = new RectF((canvas.getWidth()/2)-25,max,(canvas.getWidth()/2)+25,min);
        canvas.drawRoundRect(roundRect,10f,10f,gray);
        if(scaled_image == null){
            Bitmap raw_image = BitmapFactory.decodeResource(getResources(), R.drawable.ctr_thumb);
            float scale = canvas.getHeight()*0.1f/raw_image.getHeight();
            scaled_image = Bitmap.createScaledBitmap(raw_image,Math.round(raw_image.getWidth()*scale),Math.round(raw_image.getHeight()*scale),true);
            raw_image.recycle();
        }
        canvas.drawBitmap(scaled_image,(canvas.getWidth()-scaled_image.getWidth())/2f,canvas.getHeight()*0.5f+(canvas.getHeight()*0.4f*(this.progress/100f))-scaled_image.getHeight()/2f,null);

    }
    @Override
    public boolean onTouchEvent(MotionEvent event){
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                ac_run = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if(!ac_run) {
                    int new_progress = Math.round((event.getY() - ((max + min) / 2) ) / ((min - max) / 200));
                    if (new_progress - this.progress > 5)
                        this.setProgress(this.progress + 5);
                    else if (new_progress - this.progress < -5)
                        this.setProgress(this.progress - 5);
                    else
                        this.setProgress(new_progress);
                }
                break;
            case MotionEvent.ACTION_UP:
                ac_run = true;
                handler.post(vel_zero);
                break;
        }
        return true;
    }

    Runnable vel_zero = new Runnable(){
        @Override
        public void run(){
            if(ac_run) {
                int gage = ControlLever.this.getProgress();
                if (gage > 0)
                    gage -= 5;
                else
                    gage += 5;
                if (-5 < gage && gage < 5)
                    gage = 0;
//            velocity.setText(String.valueOf((gage-100)) + "%");
                if (gage != 0)
                    handler.postDelayed(vel_zero, 25);
                else
                    ac_run = false;
                ControlLever.this.setProgress(gage);
            }
        }
    };
}
