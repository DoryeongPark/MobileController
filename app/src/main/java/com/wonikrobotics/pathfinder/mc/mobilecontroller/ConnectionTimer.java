package com.wonikrobotics.pathfinder.mc.mobilecontroller;

/**
 * Created by Felix on 2016-08-09.
 *
 * Heartbeat time out thread class
 * onTimerFinished method will executed when timer is over
 */
public abstract class ConnectionTimer extends Thread {
    private int maxPermissionTime = 10;
    private long timer;
    private boolean stateShutDown = false;

    public ConnectionTimer(int maxPermissionTime) {
        //생성자에서 시간제한 설정 및 현재 시간 초기화
        this.maxPermissionTime = maxPermissionTime;
        this.timer = System.currentTimeMillis();
    }

    //타이머가 끝났을 때 동작 정의
    public abstract void onTimerFinished();

    public void run(){

        while(stateShutDown == false){
            //현재 시간과 timer에 저장된 시간을 비교해 시간제한과 비교
            long current = System.currentTimeMillis();
            if (current - timer >= maxPermissionTime * 1000) { // 단위는 milisecond
                onTimerFinished();
                shutDown();
            } else {
                System.out.println("Timer - alive " + Integer.toString(Math.round((maxPermissionTime * 1000 - (current - timer)) / 1000f)));
            }
            // 1초 단위로 반복
            try {
                sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        System.out.println("Timer - Shutdown successful");
    }


    public void shutDown(){
        // loop 종료
        stateShutDown = true;

    }
    public void getHeartBeat() {
        //현재 시간 갱신
        timer = System.currentTimeMillis();

    }

}
