package com.wonikrobotics.ros;

import org.ros.concurrent.CancellableLoop;
import org.ros.node.ConnectedNode;

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
