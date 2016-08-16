package com.wonikrobotics.pathfinder.mc.ros;

import org.ros.concurrent.CancellableLoop;

/**
 * CustomCancellableLoop
 *
 * @author      Doryeong Park
 * @date        7. 29. 2016
 *
 * @description Loop interface as thread which has method to be executed when loop is finished
 */
public abstract class CustomCancellableLoop extends CancellableLoop {

    protected abstract void onPreCancel();//Abstract method to be executed when loop is finished

    @Override
    public void cancel() {

        onPreCancel();
        super.cancel();

    }
}
