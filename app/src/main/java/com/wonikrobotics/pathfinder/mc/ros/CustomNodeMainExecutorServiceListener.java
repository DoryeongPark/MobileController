package com.wonikrobotics.pathfinder.mc.ros;

/**
 * Created by Notebook on 2016-08-01.
 */
public interface CustomNodeMainExecutorServiceListener {

    /**
     * @param nodeMainExecutorService the {@link CustomNodeMainExecutorService} that was shut down
     */
    void onShutdown(CustomNodeMainExecutorService nodeMainExecutorService);
}