package com.wonikrobotics.pathfinder.mc.mobilecontroller;

import android.os.Handler;

/**
 * Created by Felix on 2016-08-09.
 */
public abstract class ConnectionTimer extends Thread {

    public abstract void onTimerFinished();

    private int maxPermissionTime = 10;
    private int timer;

    private boolean stateShutDown = false;
    private boolean statePause = false;


    public ConnectionTimer(int maxPermissionTime){

        this.maxPermissionTime = maxPermissionTime;
        this.timer = maxPermissionTime;

    }

    public void run(){

        while(stateShutDown == false){

            --timer;
            System.out.println(this);

            try{ this.sleep(1000); } catch (Exception e) { e.printStackTrace(); }

            if (timer == 0) {
                shutDown();
                onTimerFinished();
            }

            if (timer == -1 || statePause == true)
                ++timer;

            System.out.println("Timer - " + timer);

        }

        System.out.println("Timer - Shutdown successful");

    }


    public void shutDown(){

        stateShutDown = true;

    }

    public void pause(boolean flag){

        statePause = flag;

    }

    public void getHeartBeat() {

        timer = maxPermissionTime;

    }

}
