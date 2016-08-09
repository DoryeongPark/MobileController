package com.wonikrobotics.pathfinder.mc.ros;

import org.ros.concurrent.CancellableLoop;

/**
 * Created by Felix on 2016-07-29.
 */
public abstract class CustomCancellableLoop extends CancellableLoop {

    protected abstract void onPreCancel();

    @Override
    public void cancel() {

        onPreCancel();
        super.cancel();

    }
}
