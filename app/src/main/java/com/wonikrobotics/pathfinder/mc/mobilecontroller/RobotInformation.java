package com.wonikrobotics.pathfinder.mc.mobilecontroller;

import android.net.Uri;

/**
 * RobotInformation
 *
 * @author      Weonwoo Joo
 * @date        5. 7. 2016
 *
 * @description Data set for robot information
 */
public class RobotInformation {
    private Uri masterUri;
    private String robotName;
    private String uri_str;
    private int idx;
    private boolean master;
    private float velSensitive, angSensitive;
    private int controller;

    public RobotInformation(int idx, Uri masterUri, String robotName, String masterUri_str, boolean master) {
        this.idx = idx;
        this.masterUri = masterUri;
        this.robotName = robotName;
        this.uri_str = masterUri_str;
        this.master = master;
    }

    public RobotInformation(int idx, Uri masterUri, String robotName, String masterUri_str, boolean master, float vel, float ang, int ctr) {
        this.idx = idx;
        this.masterUri = masterUri;
        this.robotName = robotName;
        this.uri_str = masterUri_str;
        this.master = master;
        this.controller = ctr;
        this.velSensitive = vel;
        this.angSensitive = ang;
    }

    public void setMasterUri(String str) {
        this.uri_str = str;
    }

    public float getVelSensitive() {
        return this.velSensitive;
    }

    public void setVelSensitive(float vel) {
        this.velSensitive = vel;
    }

    public float getAngSensitive() {
        return this.angSensitive;
    }

    public void setAngSensitive(float ang) {
        this.angSensitive = ang;
    }

    public int getCtr() {
        return this.controller;
    }

    public void setCtr(int ctr) {
        this.controller = ctr;
    }

    public int getIdx() {
        return this.idx;
    }

    public Uri getMasterUri() {
        return this.masterUri;
    }

    public void setMasterUri(Uri input) {
        this.masterUri = input;
    }

    public String getRobotName() {
        return this.robotName;
    }

    public void setRobotName(String name) {
        this.robotName = name;
    }

    public String getUri_str() {
        return this.uri_str;
    }

    public boolean getIsMaster() {
        return this.master;
    }

}
