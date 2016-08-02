package com.wonikrobotics.mobilecontroller;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
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
    private TextView btnConnect,robotName,robotURL,robotInfo;
    private int selectedRobotPosition = -1;
    private ImageView btnOption,addRobot;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selectrobot);
        btnConnect = (TextView)findViewById(R.id.btnConnect);
        availableRobotList = (ListView)findViewById(R.id.availablerobotlist);
        btnOption = (ImageView)findViewById(R.id.select_robot_option);
        addRobot = (ImageView)findViewById(R.id.select_robot_add);
        robotName = (TextView)findViewById(R.id.robot_name);
        robotURL = (TextView)findViewById(R.id.robot_url);
        robotInfo = (TextView)findViewById(R.id.robot_info);
        btnOption.setOnClickListener(clickListener);
        addRobot.setOnClickListener(clickListener);
        availableRobotList.setOnItemClickListener(robotListListener);
        btnConnect.setOnClickListener(clickListener);
    }
    private View.OnClickListener clickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.select_robot_add:
                    Intent addRobotActivity = new Intent(SelectRobot.this,AddRobotDialog.class);
                    startActivityForResult(addRobotActivity,1);
                    break;
                case R.id.select_robot_option:
                    Intent optionActivity = new Intent(SelectRobot.this,UserOptionDialog.class);
                    startActivityForResult(optionActivity,0);
                    break;
                case R.id.btnConnect:
                    if(selectedRobotPosition != -1 && robotList.get(selectedRobotPosition).getIdx() != -1) {
                        RobotInformation selected = robotList.get(selectedRobotPosition);
                        Intent controlActivity = new Intent(SelectRobot.this, RobotController.class);
                        controlActivity.putExtra("NAME", selected.getRobotName());
                        controlActivity.putExtra("URL", selected.getUri_str());
                        controlActivity.putExtra("MASTER", selected.getIsMaster());
                        startActivity(controlActivity);
                    }else{
                        Toast.makeText(SelectRobot.this,"Please select the robot",Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };
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
                robotName.setText(robotList.get(position).getRobotName());
                robotURL.setText(robotList.get(position).getUri_str());
                selectedRobotPosition = position;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        if(resultCode ==1){
            if(mDbOpenHelper == null){
                mDbOpenHelper = new DbOpenHelper(SelectRobot.this);
            }
            mDbOpenHelper.open();
            mDbOpenHelper.insertColumn(intent.getStringExtra("name"),intent.getStringExtra("uri"),intent.getStringExtra("master"));
            Toast.makeText(SelectRobot.this,"New robot registering is success",Toast.LENGTH_SHORT).show();
            robotListDataLoad();
        }else if(resultCode == 0){

        }
    }
}
