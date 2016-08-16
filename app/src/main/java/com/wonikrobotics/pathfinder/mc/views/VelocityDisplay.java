package com.wonikrobotics.pathfinder.mc.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

/**
 * Created by Notebook on 2016-07-13.
 */
public class VelocityDisplay extends View {
    private int vel = 100;  // initiated value
    private Shader shader;  // shader for gradation
    private Paint grad;     // Paint for gradation
    private RectF rf;        // draw area

    public VelocityDisplay(Context c) {
        super(c);
    }


    // value update
    public void setVel(int pa) {
        this.vel = pa;
        invalidate();
    }

    public void setPaint(Paint p) {
        shader = p.getShader();
        grad = p;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if (shader == null) {
            /**
             * Define shader and paint.
             * It call once on first when shader has null value.
             */
            int[] colors = {Color.parseColor("#c7dcfe"), Color.parseColor("#7348e0"), Color.parseColor("#1b325f")};
            float[] positions = {0, canvas.getWidth() * 0.58f, canvas.getWidth()};
            shader = new LinearGradient(0, 0, canvas.getWidth(), 0, colors, positions, Shader.TileMode.CLAMP);
            grad = new Paint();
            grad.setShader(shader);
        }
        /**
         *  Draw 10 pixel of start point to user notice this view
         */
        rf = new RectF(10, 2, 20, canvas.getHeight() - 2);
        canvas.drawRect(rf, grad);
        /**
         *  Draw scaled horizontal progress
         */
        rf.set(20, 2, (canvas.getWidth() - 20) * (this.vel / 100f) + 10, canvas.getHeight() - 2);
        canvas.drawRect(rf, grad);
    }
}