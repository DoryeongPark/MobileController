package com.wonikrobotics.pathfinder.mc.mobilecontroller;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * AvailableRobotListAdapter
 *
 * @author Weonwoo Joo
 * @date 27. 7. 2016
 *
 * @description Adapter for robot ListView
 */
public class AvailableRobotListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater inflater;
    private ArrayList<RobotInformation> robotList;


    // robot list have to be declared in constructor
    public AvailableRobotListAdapter(Context c, ArrayList<RobotInformation> robotList) {
        this.mContext = c;
        this.robotList = robotList;
        this.inflater = LayoutInflater.from(c);
    }


    // return value decide number of list rows.
    @Override
    public int getCount() {
        return robotList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RobotInformation getItem(int position) {
        return robotList.get(position);
    }


    // Row views in ListView reused by getView method.
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.availablerobotlistitem, parent, false);
        }
        ((TextView) convertView.findViewById(R.id.availablerobotlist_robotname)).setText(robotList.get(position).getRobotName());

        // listener for option button clicked
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
