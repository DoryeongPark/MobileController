package com.wonikrobotics.mobilecontroller;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wonikrobotics.mobilecontroller.database.DataBases;
import com.wonikrobotics.mobilecontroller.database.DbOpenHelper;

/**
 * Created by Notebook on 2016-08-01.
 */
public class UserOptionDialog extends Activity {
    private TextView dblever,leverwheel,rtheta,ytheta , angulartxt,veltxt;
    private SeekBar angularbar,velbar;
    private int ctrSelected = 1;
    private float velSensitive=1.0f,angSensitive=1.0f;
    private DbOpenHelper mDbOpenHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.useroption);
        dblever = (TextView)findViewById(R.id.useropt_ctr_doublelever);
        leverwheel = (TextView)findViewById(R.id.useropt_ctr_leverwheel);
        rtheta = (TextView)findViewById(R.id.useropt_ctr_joystick1);
        ytheta = (TextView)findViewById(R.id.useropt_ctr_joystick2);
        angulartxt = (TextView)findViewById(R.id.useropt_sst_angulartxt);
        veltxt= (TextView)findViewById(R.id.useropt_sst_veltxt);
        angularbar = (SeekBar)findViewById(R.id.useropt_sst_angularbar);
        velbar = (SeekBar)findViewById(R.id.useropt_sst_velbar);
        dblever.setOnClickListener(ctr_selector);
        leverwheel.setOnClickListener(ctr_selector);
        rtheta.setOnClickListener(ctr_selector);
        ytheta.setOnClickListener(ctr_selector);
        velbar.setOnSeekBarChangeListener(velChangeListener);
        angularbar.setOnSeekBarChangeListener(angChangeListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mDbOpenHelper == null) {
            mDbOpenHelper = new DbOpenHelper(UserOptionDialog.this);
        }
        Log.e("db","before open");
        mDbOpenHelper.open();
        Log.e("db","after open");
        Cursor c = mDbOpenHelper.getAllColumnsFromOption();
        if(c.getCount() != 0) {
            c.moveToNext();
            ctrSelected = Integer.parseInt(c.getString(c.getColumnIndex(DataBases.CreateDB.CONTROLLER)));
            velSensitive = Float.parseFloat(c.getString(c.getColumnIndex(DataBases.CreateDB.VELOCITY)));
            angSensitive = Float.parseFloat(c.getString(c.getColumnIndex(DataBases.CreateDB.ANGULAR)));

            veltxt.setText(Float.toString(velSensitive));
            angulartxt.setText(Float.toString(angSensitive));
            velbar.setProgress((int) (velSensitive * 100));
            angularbar.setProgress((int) (angSensitive * 100));
            ctrSelectChangeListener();
        }else{
            ctrSelected = 1;
            veltxt.setText("1.0");
            angulartxt.setText("1.0");
            velbar.setProgress(100);
            angularbar.setProgress(100);
            ctrSelectChangeListener();
        }
        mDbOpenHelper.close();
        mDbOpenHelper = null;
    }

    SeekBar.OnSeekBarChangeListener velChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            velSensitive=progress/100f;
            veltxt.setText(String.valueOf(velSensitive));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
    SeekBar.OnSeekBarChangeListener angChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            angSensitive = progress/100f;
            angulartxt.setText(String.valueOf(angSensitive));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
    View.OnClickListener ctr_selector = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.useropt_ctr_doublelever:
                    ctrSelected = RobotController.CONTROLLER_HORIZONTAL_DOUBLELEVER;
                    break;
                case R.id.useropt_ctr_joystick1:
                    ctrSelected = RobotController.CONTROLLER_VERTICAL_RTHETA;
                    break;
                case R.id.useropt_ctr_joystick2:
                    ctrSelected = RobotController.CONTROLLER_VERTICAL_YTHETA;
                    break;
                case R.id.useropt_ctr_leverwheel:
                    ctrSelected = RobotController.CONTROLLER_HORIZONTAL_STEER;
                    break;
            }
            ctrSelectChangeListener();
        }
    };
    private void ctrSelectChangeListener(){
        dblever.setBackground(getResources().getDrawable(R.drawable.lefttopwhite));
        dblever.setTextColor(Color.BLACK);
        leverwheel.setBackground(getResources().getDrawable(R.drawable.leftbottomwhite));
        leverwheel.setTextColor(Color.BLACK);
        rtheta.setBackground(getResources().getDrawable(R.drawable.righttopwhite));
        rtheta.setTextColor(Color.BLACK);
        ytheta.setBackground(getResources().getDrawable(R.drawable.rightbottomwhite));
        ytheta.setTextColor(Color.BLACK);
        switch(ctrSelected){
            case RobotController.CONTROLLER_HORIZONTAL_DOUBLELEVER:
                dblever.setBackground(getResources().getDrawable(R.drawable.lefttopviolet));
                dblever.setTextColor(Color.WHITE);
                break;
            case RobotController.CONTROLLER_HORIZONTAL_STEER:
                leverwheel.setBackground(getResources().getDrawable(R.drawable.leftbottomviolet));
                leverwheel.setTextColor(Color.WHITE);
                break;
            case RobotController.CONTROLLER_VERTICAL_RTHETA:
                rtheta.setBackground(getResources().getDrawable(R.drawable.righttopviolet));
                rtheta.setTextColor(Color.WHITE);
                break;
            case RobotController.CONTROLLER_VERTICAL_YTHETA:
                ytheta.setBackground(getResources().getDrawable(R.drawable.rightbottomviolet));
                ytheta.setTextColor(Color.WHITE);
                break;
        }
    }

    @Override
    protected void onPause() {
        if(mDbOpenHelper == null) {
            mDbOpenHelper = new DbOpenHelper(UserOptionDialog.this);
            mDbOpenHelper.open();
        }
        mDbOpenHelper.insertOption(Integer.toString(ctrSelected),Float.toString(velSensitive),Float.toString(angSensitive));
        mDbOpenHelper.close();
        mDbOpenHelper = null;
        super.onPause();
    }
}
