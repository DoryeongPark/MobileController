package com.wonikrobotics.mobilecontroller;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Notebook on 2016-07-27.
 */
public class AvailableRobotListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater inflater;
    private ArrayList<RobotInformation> robotList;

    public AvailableRobotListAdapter(Context c, ArrayList<RobotInformation> robotList){
        this.mContext = c;
        this.robotList = robotList;
        this.inflater = LayoutInflater.from(c);
    }
    @Override
    public int getCount() {
        return robotList.size();
    }

    @Override
    public long getItemId(int position){
        return position;
    }
    @Override
    public RobotInformation getItem(int position){
        return robotList.get(position);
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = inflater.inflate(R.layout.availablerobotlistitem,parent,false);
        }
        ((TextView)convertView.findViewById(R.id.availablerobotlist_robotname)).setText(robotList.get(position).getRobotName());
        if (robotList.get(position).getIdx() >= 0) {
            convertView.findViewById(R.id.availablerobotlist_option).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent optionDialog = new Intent(mContext, UserOptionDialog.class);
                    optionDialog.putExtra("IDX", robotList.get(position).getIdx());
                    mContext.startActivity(optionDialog);
                }
            });
        }
        return convertView;
    }
}
