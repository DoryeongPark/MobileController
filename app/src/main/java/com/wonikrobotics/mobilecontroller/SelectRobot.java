package com.wonikrobotics.mobilecontroller;

import android.os.Bundle;

import org.ros.android.RosActivity;
import org.ros.node.NodeMainExecutor;

/**
 * Created by Notebook on 2016-07-26.
 */
public class SelectRobot extends RosActivity {

    public SelectRobot(){

        super("A","A");

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }
    @Override
    public void init(NodeMainExecutor executor){

    }
}
