package com.wonikrobotics.mobilecontroller;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wonikrobotics.mobilecontroller.database.DataBases;
import com.wonikrobotics.mobilecontroller.database.DbOpenHelper;

import java.util.ArrayList;


/**
 * Created by Notebook on 2016-07-26.
 */
public class SelectRobot extends Activity {
    private ListView availableRobotList;
    private ArrayList<RobotInformation> robotList;
    private DbOpenHelper mDbOpenHelper;
    private AvailableRobotListAdapter robotListAdapter;
    private TextView btnConnect;
    private int selectedRobotPosition = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selectrobot);
        btnConnect = (TextView)findViewById(R.id.btnConnect);
        availableRobotList = (ListView)findViewById(R.id.availablerobotlist);
        availableRobotList.setOnItemClickListener(robotListListener);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(selectedRobotPosition != -1 && robotList.get(selectedRobotPosition).getIdx() != -1) {
//                    RobotInformation selected = robotList.get(selectedRobotPosition);
                    Intent controlActivity = new Intent(SelectRobot.this, RobotController.class);
//                    controlActivity.putExtra("NAME", selected.getRobotName());
//                    controlActivity.putExtra("URL", selected.getUri_str());
                    startActivity(controlActivity);
//                }else{
//                    Toast.makeText(SelectRobot.this,"Please select the robot",Toast.LENGTH_SHORT).show();
//                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        userOptionDataLoad();
        robotListDataLoad();
    }
    private void userOptionDataLoad(){

    }
    private void robotListDataLoad(){
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
            if(robotList.get(position).getMasterUri()!=null){
//                robot_name.setText(list_data.get(position).getRobotName());
//                robot_uri.setText(list_data.get(position).getUri_str());
                selectedRobotPosition = position;
            }
        }
    };
}
