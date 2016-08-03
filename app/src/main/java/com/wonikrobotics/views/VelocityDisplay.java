package com.wonikrobotics.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.Log;
import android.view.View;

/**
 * Created by Notebook on 2016-07-13.
 */
public class VelocityDisplay extends View {
    int vel = 100;
    Shader shader;
    Paint grad;
    RectF rf;
    public VelocityDisplay(Context c){
        super(c);
    }
    public void setVel(int pa){
        this.vel = pa;
        invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas){
        if(shader == null) {
            int[] colors = {Color.parseColor("#c7dcfe"),Color.parseColor("#7348e0"),Color.parseColor("#1b325f")};
            float[] positions = {0,canvas.getWidth()*0.58f,canvas.getWidth()};
            shader = new LinearGradient(0, 0, canvas.getWidth(), 0,colors ,positions, Shader.TileMode.CLAMP);
            grad = new Paint();
            grad.setShader(shader);
        }
        Log.e("onDraw",String.valueOf(canvas.getWidth())+","+String.valueOf(canvas.getHeight()));
        rf = new RectF(10,2,20,canvas.getHeight()-2);
        canvas.drawRect(rf,grad);
        rf.set(20,2,(canvas.getWidth()-20)*(this.vel/100f)+10,canvas.getHeight()-2);
        canvas.drawRect(rf,grad);
    }
}