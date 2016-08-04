package com.wonikrobotics.views;

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
    final int FRONT_N_BEHIND = 1;
    float cX,cY;
    Paint red,green;
    float max_val = 5;
    //    back;
    float[] valuelist;

    public SonarSensorView(Context c, float[] values) {
        super(c);
        initPaint();
        this.valuelist = values;
    }

    public SonarSensorView(Context c, AttributeSet set) {
        super(c,set);
    }

    public SonarSensorView(Context c, AttributeSet set, int defaultStyle) {
        super(c,set,defaultStyle);
    }
    public void setMaxval(float val){
        this.max_val = val;
        invalidate();
    }
    private void initPaint(){
        red = new Paint();
        green = new Paint();
//        back = new Paint();
        red.setColor(Color.RED);
        red.setAlpha(80);
        green.setColor(Color.GREEN);
        green.setAlpha(80);
//        back.setColor(Color.WHITE);
//        back.setAlpha(70);
    }
    public void update(float[] list){
        this.valuelist = list;
        invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas){
        cX = canvas.getWidth()/2;
        cY = canvas.getHeight();
//        RectF rf = new RectF(cX*(1-(5/max_val)),cY*(1-(5/max_val)),cX*(1+(5/max_val)),cY*(1+(5/max_val)));
//        RectF wf = new RectF(cX-(cX*(2f/3f)),cY-(cY*(2f/3f)),cX+(cX*(2f/3f)),cY+(cY*(2f/3f)));

        for(int i = 0; i <valuelist.length;i++){
            float gapX = cX*(((valuelist[i]>5? 5:valuelist[i])/5)*(5/max_val));
            float gapY = cY*(((valuelist[i]>5? 5:valuelist[i])/5)*(5/max_val));
            RectF greenRect = new RectF(cX-gapX,cY-gapY,cX+gapX,cY+gapY);
//            canvas.drawArc(rf,190 + 20*i,20,true,red);
            canvas.drawArc(greenRect,190 + 20*i,20,true,green);
//            canvas.drawArc(wf,210 + 15*i,15,true,back);
        }
    }
}