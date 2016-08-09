package com.wonikrobotics.pathfinder.mc.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Felix on 2016-08-03.
 */
public class CameraView extends ImageView {

    public CameraView(Context c) {
        super(c);
    }

    public CameraView(Context c, AttributeSet set) {
        super(c, set);
    }

    public CameraView(Context c, AttributeSet set, int defaultStyle) {
        super(c, set, defaultStyle);
    }

    public void update(Bitmap bitmap) {
        this.setImageBitmap(bitmap);
    }
}
