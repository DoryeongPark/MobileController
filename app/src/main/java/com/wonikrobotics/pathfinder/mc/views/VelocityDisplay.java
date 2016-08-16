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
 * VelocityDisplay
 *
 * @author      Weonwoo Joo
 * @date        13. 7. 2016
 *
 * @description View displaying linear velocity
 */
public class VelocityDisplay extends View {
    int vel = 100;
    Shader shader;
    Paint grad;
    RectF rf;

    public VelocityDisplay(Context c) {
        super(c);
    }

    public void setVel(int pa) {
        this.vel = pa;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (shader == null) {
            int[] colors = {Color.parseColor("#c7dcfe"), Color.parseColor("#7348e0"), Color.parseColor("#1b325f")};
            float[] positions = {0, canvas.getWidth() * 0.58f, canvas.getWidth()};
            shader = new LinearGradient(0, 0, canvas.getWidth(), 0, colors, positions, Shader.TileMode.CLAMP);
            grad = new Paint();
            grad.setShader(shader);
        }
        rf = new RectF(10, 2, 20, canvas.getHeight() - 2);
        canvas.drawRect(rf, grad);
        rf.set(20, 2, (canvas.getWidth() - 20) * (this.vel / 100f) + 10, canvas.getHeight() - 2);
        canvas.drawRect(rf, grad);
    }
}