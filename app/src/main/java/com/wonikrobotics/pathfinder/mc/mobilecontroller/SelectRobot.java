package com.wonikrobotics.pathfinder.mc.mobilecontroller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wonikrobotics.pathfinder.mc.mobilecontroller.database.DataBases;
import com.wonikrobotics.pathfinder.mc.mobilecontroller.database.DbOpenHelper;

import java.util.ArrayList;


/**
 * Created by Notebook on 2016-07-26.
 */
public class SelectRobot extends Activity {
    private ListView availableRobotList;
    private ArrayList<RobotInformation> robotList;
    private DbOpenHelper mDbOpenHelper;
    private AvailableRobotListAdapter robotListAdapter;
    private TextView btnConnect, robotName, robotURL, robotInfo;
    private int selectedRobotPosition = -1;
    ListView.OnItemClickListener robotListListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (robotList.get(position).getMasterUri() != null) {
                robotName.setText(robotList.get(position).getRobotName());
                robotURL.setText(robotList.get(position).getUri_str());
                selectedRobotPosition = position;
            }
        }
    };
    private ImageView addRobot, delRobot, modRobot;
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.select_robot_add:
                    Intent addRobotActivity = new Intent(SelectRobot.this, AddRobotDialog.class);
                    startActivityForResult(addRobotActivity, 1);
                    break;
                case R.id.btnConnect:
                    if (selectedRobotPosition != -1 && robotList.get(selectedRobotPosition).getIdx() != -1) {
                        RobotInformation selected = robotList.get(selectedRobotPosition);
                        Intent controlActivity = new Intent(SelectRobot.this, RobotController.class);
                        controlActivity.putExtra("NAME", selected.getRobotName());
                        controlActivity.putExtra("URL", selected.getUri_str());
                        controlActivity.putExtra("MASTER", selected.getIsMaster());
                        controlActivity.putExtra("IDX", selected.getIdx());
                        startActivity(controlActivity);
                    } else {
                        Toast.makeText(SelectRobot.this, "Please select the robot", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.select_robot_del:
                    if (selectedRobotPosition != -1) {
                        AlertDialog.Builder build = new AlertDialog.Builder(SelectRobot.this);
                        build.setTitle(robotList.get(selectedRobotPosition).getRobotName());
                        build.setMessage("Do you want to delete this robot?");
                        build.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mDbOpenHelper == null) {
                                    mDbOpenHelper = new DbOpenHelper(SelectRobot.this);
                                }
                                mDbOpenHelper.open();
                                mDbOpenHelper.deleteColumn(Integer.toString(robotList.get(selectedRobotPosition).getIdx()));
                                mDbOpenHelper.close();
                                mDbOpenHelper = null;
                                robotListDataLoad();
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        build.create();
                        build.show();
                    } else {
                        Toast.makeText(SelectRobot.this, "Please select the robot", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.select_robot_mod:
                    if (selectedRobotPosition != -1) {
                        Intent robotModifyDialog = new Intent(SelectRobot.this, RobotModifyDialog.class);
                        RobotInformation info = robotList.get(selectedRobotPosition);
                        robotModifyDialog.putExtra("ROBOTNAME", info.getRobotName());
                        robotModifyDialog.putExtra("ROBOTURI", info.getUri_str());
                        robotModifyDialog.putExtra("ROBOTISMASTER", info.getIsMaster());
                        robotModifyDialog.putExtra("ROBOTIDX", info.getIdx());
                        startActivityForResult(robotModifyDialog, 2);
                    } else {
                        Toast.makeText(SelectRobot.this, "Please select the robot", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selectrobot);
        btnConnect = (TextView) findViewById(R.id.btnConnect);
        availableRobotList = (ListView) findViewById(R.id.availablerobotlist);

        addRobot = (ImageView) findViewById(R.id.select_robot_add);
        delRobot = (ImageView) findViewById(R.id.select_robot_del);
        modRobot = (ImageView) findViewById(R.id.select_robot_mod);
        robotName = (TextView) findViewById(R.id.robot_name);
        robotURL = (TextView) findViewById(R.id.robot_url);
        robotInfo = (TextView) findViewById(R.id.robot_info);

        addRobot.setOnClickListener(clickListener);
        delRobot.setOnClickListener(clickListener);
        modRobot.setOnClickListener(clickListener);
        availableRobotList.setOnItemClickListener(robotListListener);
        btnConnect.setOnClickListener(clickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        robotListDataLoad();
        availableRobotList.setSelection(selectedRobotPosition);
    }

    private void robotListDataLoad() {
        if (robotList == null) {
            robotList = new ArrayList<RobotInformation>();
        }
        robotList.clear();
        if (mDbOpenHelper == null) {
            mDbOpenHelper = new DbOpenHelper(SelectRobot.this);
        }
        mDbOpenHelper.open();
        Cursor c = mDbOpenHelper.getAllColumns();
        while (c.moveToNext()) {
            RobotInformation new_info = new RobotInformation(
                    c.getInt(c.getColumnIndex(DataBases.CreateDB.IDX)),
                    Uri.parse(c.getString(c.getColumnIndex(DataBases.CreateDB.URI)))
                    , c.getString(c.getColumnIndex(DataBases.CreateDB.NAME)),
                    c.getString(c.getColumnIndex(DataBases.CreateDB.URI))
                    , Boolean.valueOf(c.getString(c.getColumnIndex(DataBases.CreateDB.MASTER)))
                    , Float.parseFloat(c.getString(c.getColumnIndex(DataBases.CreateDB.VELOCITY)))
                    , Float.parseFloat(c.getString(c.getColumnIndex(DataBases.CreateDB.ANGULAR)))
                    , Integer.parseInt(c.getString(c.getColumnIndex(DataBases.CreateDB.CONTROLLER))));
            Log.e("list", Integer.toString(c.getInt(c.getColumnIndex(DataBases.CreateDB.IDX))));
            robotList.add(new_info);
        }
        c.close();
        mDbOpenHelper.close();
        mDbOpenHelper = null;
        if (robotList.isEmpty()) {
            robotList.add(new RobotInformation(-1, null, "no Robot!!", null, false));
        }
        robotListAdapter = new AvailableRobotListAdapter(SelectRobot.this, robotList);
        availableRobotList.setAdapter(robotListAdapter);
        robotName.setText("");
        robotURL.setText("");
        robotInfo.setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == 1) {
            if (mDbOpenHelper == null) {
                mDbOpenHelper = new DbOpenHelper(SelectRobot.this);
            }
            mDbOpenHelper.open();
            mDbOpenHelper.insertColumn(intent.getStringExtra("name"), intent.getStringExtra("uri"), intent.getStringExtra("master"), "1", "1.0", "1.0");
            Toast.makeText(SelectRobot.this, "New robot registering is success", Toast.LENGTH_SHORT).show();
            robotListDataLoad();
        } else if (resultCode == 2) {
            if (mDbOpenHelper == null) {
                mDbOpenHelper = new DbOpenHelper(SelectRobot.this);
            }
            mDbOpenHelper.open();
            mDbOpenHelper.updateCulumn(Integer.toString(intent.getIntExtra("idx", -1)), intent.getStringExtra("name"), intent.getStringExtra("uri"), intent.getStringExtra("master"));
            Toast.makeText(SelectRobot.this, "Update robot information is success", Toast.LENGTH_SHORT).show();
            robotListDataLoad();
        }
    }
}
