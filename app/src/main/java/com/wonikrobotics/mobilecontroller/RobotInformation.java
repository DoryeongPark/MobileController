package com.wonikrobotics.mobilecontroller;

import android.net.Uri;

/**
 * Created by Notebook on 2016-07-05.
 */
public class RobotInformation {
    private Uri masterUri;
    private String robotName;
    private String uri_str;
    private int idx;
    private boolean master;
    public RobotInformation(int idx, Uri masterUri, String robotName, String masterUri_str, boolean master){
        this.idx = idx;
        this.masterUri = masterUri;
        this.robotName = robotName;
        this.uri_str = masterUri_str;
        this.master = master;
    }
    public void setMasterUri(String str){this.uri_str = str;}
    public void setMasterUri(Uri input){
        this.masterUri = input;
    }
    public void setRobotName(String name){
        this.robotName = name;
    }
    public int getIdx(){return this.idx;}
    public Uri getMasterUri(){return this.masterUri;}
    public String getRobotName(){return this.robotName;}
    public String getUri_str(){return this.uri_str;}
    public boolean getIsMaster(){return this.master;}

}
