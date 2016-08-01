package com.wonikrobotics.ros;

import org.ros.concurrent.CancellableLoop;
import org.ros.node.ConnectedNode;

/**
 * Created by Felix on 2016-07-29.
 */
public abstract class PreCancellableLoop extends CancellableLoop {

    protected abstract void executeFinally();

    @Override
    public void cancel() {

        executeFinally();
        super.cancel();

    }
}
