package com.wonikrobotics.mobilecontroller;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.wonikrobotics.mobilecontroller.database.DataBases;
import com.wonikrobotics.mobilecontroller.database.DbOpenHelper;

import java.util.ArrayList;


/**
 * Created by Notebook on 2016-07-26.
 */
public class SelectRobot extends Activity {
    ListView availableRobotList;
    ArrayList<RobotInformation> robotList;
    DbOpenHelper mDbOpenHelper;
    AvailableRobotListAdapter robotListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selectrobot);
        availableRobotList = (ListView)findViewById(R.id.availablerobotlist);
        availableRobotList.setOnItemClickListener(robotListListener);
        dataLoad();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void dataLoad(){
        if(robotList == null){
            robotList = new ArrayList<RobotInformation>();
        }
        robotList.clear();
        if(mDbOpenHelper == null){
            mDbOpenHelper = new DbOpenHelper(SelectRobot.this);
        }
        mDbOpenHelper.open();
        Cursor c = mDbOpenHelper.getAllColumns();
        while(c.moveToNext()){
            RobotInformation new_info =new RobotInformation(
                    c.getInt(c.getColumnIndex(DataBases.CreateDB.IDX)),
                    Uri.parse(c.getString(c.getColumnIndex(DataBases.CreateDB.URI)))
                    ,c.getString(c.getColumnIndex(DataBases.CreateDB.NAME)),
                    c.getString(c.getColumnIndex(DataBases.CreateDB.URI))
                    ,Boolean.valueOf(c.getString(c.getColumnIndex(DataBases.CreateDB.MASTER))));
            robotList.add(new_info);
        }
        c.close();
        mDbOpenHelper.close();
        mDbOpenHelper = null;
        if(robotList.isEmpty() ){
            robotList.add(new RobotInformation(-1,null,"no Robot!!",null,false));
        }
        robotListAdapter = new AvailableRobotListAdapter(SelectRobot.this,robotList);
        availableRobotList.setAdapter(robotListAdapter);
    }
    ListView.OnItemClickListener robotListListener = new ListView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
//            if(list_data.get(position).getMasterUri()!=null){
//                robot_name.setText(list_data.get(position).getRobotName());
//                robot_uri.setText(list_data.get(position).getUri_str());
//                list_sel_position = list_data.get(position).getIdx();
//                list_item_position = position;
//            }
        }
    };
}
