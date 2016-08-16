package com.wonikrobotics.pathfinder.mc.mobilecontroller;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wonikrobotics.pathfinder.mc.mobilecontroller.database.DataBases;
import com.wonikrobotics.pathfinder.mc.mobilecontroller.database.DbOpenHelper;

/**
 * UserOptionDialog
 *
 * @author      Weonwoo Joo
 * @date        1. 8. 2016
 *
 * @description Dialog for enabling users to change control mode & apply user sensitivity
 */
public class UserOptionDialog extends Activity {

    private TextView dblever, leverwheel, rtheta, ytheta, angulartxt, veltxt;
    private SeekBar angularbar, velbar;
    private int ctrSelected = 1;
    private float velSensitive = 1.0f, angSensitive = 1.0f;
    private DbOpenHelper mDbOpenHelper = null;
    private int idx = -1;                                           // robot Index of DB

    /**
     * Listeners
     */

    // angular sensitivity SeekBar listener
    private SeekBar.OnSeekBarChangeListener angChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            angSensitive = progress / 100f;
            angulartxt.setText(String.valueOf(angSensitive));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    // Controller select listener
    private View.OnClickListener ctr_selector = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.useropt_ctr_doublelever: // 4  - Double lever
                    ctrSelected = RobotController.CONTROLLER_HORIZONTAL_DOUBLELEVER;
                    break;
                case R.id.useropt_ctr_joystick1: // 1   - Jog Controller
                    ctrSelected = RobotController.CONTROLLER_VERTICAL_JOG;
                    break;
                case R.id.useropt_ctr_joystick2: // 2   - Joystick
                    ctrSelected = RobotController.CONTROLLER_VERTICAL_JOYSTICK;
                    break;
                case R.id.useropt_ctr_leverwheel: // 3  - SteeringWheel
                    ctrSelected = RobotController.CONTROLLER_HORIZONTAL_STEER;
                    break;
            }
            ctrSelectChangeListener();
        }
    };

    // velocity sensitivity SeekBar Listener
    private SeekBar.OnSeekBarChangeListener velChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            velSensitive = progress / 100f;
            veltxt.setText(String.valueOf(velSensitive));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.useroption);
        dblever = (TextView) findViewById(R.id.useropt_ctr_doublelever);
        leverwheel = (TextView) findViewById(R.id.useropt_ctr_leverwheel);
        rtheta = (TextView) findViewById(R.id.useropt_ctr_joystick1);
        ytheta = (TextView) findViewById(R.id.useropt_ctr_joystick2);
        angulartxt = (TextView) findViewById(R.id.useropt_sst_angulartxt);
        veltxt = (TextView) findViewById(R.id.useropt_sst_veltxt);
        angularbar = (SeekBar) findViewById(R.id.useropt_sst_angularbar);
        velbar = (SeekBar) findViewById(R.id.useropt_sst_velbar);
        dblever.setOnClickListener(ctr_selector);
        leverwheel.setOnClickListener(ctr_selector);
        rtheta.setOnClickListener(ctr_selector);
        ytheta.setOnClickListener(ctr_selector);
        velbar.setOnSeekBarChangeListener(velChangeListener);
        angularbar.setOnSeekBarChangeListener(angChangeListener);
        final BitmapDrawable thumb = (BitmapDrawable) getResources().getDrawable(R.drawable.ctr_thumb);

        // make SeekBar Thumb small
        ViewTreeObserver vto = velbar.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                int h = Math.round(velbar.getMeasuredHeight() * 0.8f);
                Bitmap scale = Bitmap.createScaledBitmap(thumb.getBitmap(), h, h, true);
                Drawable newThumb = new BitmapDrawable(getResources(), scale);
                newThumb.setBounds(0, 0, newThumb.getIntrinsicWidth(), newThumb.getIntrinsicHeight());
                velbar.setThumb(newThumb);
                velbar.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });
        // make SeekBar Thumb small
        vto = angularbar.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                int h = Math.round(angularbar.getMeasuredHeight() * 0.8f);
                Bitmap scale = Bitmap.createScaledBitmap(thumb.getBitmap(), h, h, true);
                Drawable newThumb = new BitmapDrawable(getResources(), scale);
                newThumb.setBounds(0, 0, newThumb.getIntrinsicWidth(), newThumb.getIntrinsicHeight());
                angularbar.setThumb(newThumb);
                angularbar.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         *  load option data from DB
         */
        if (mDbOpenHelper == null) {
            mDbOpenHelper = new DbOpenHelper(UserOptionDialog.this);
        }
        Log.e("db", "before open");
        mDbOpenHelper.open();
        Log.e("db", "after open");
        Cursor c = mDbOpenHelper.getAllColumns();
        if (c.getCount() != 0) {
            idx = getIntent().getIntExtra("IDX", -1);
            if (idx != -1) {
                while (c.moveToNext() && c.getInt(c.getColumnIndex(DataBases.CreateDB.IDX)) != idx) {
                }
                if (c != null) {
                    ctrSelected = Integer.parseInt(c.getString(c.getColumnIndex(DataBases.CreateDB.CONTROLLER)));
                    velSensitive = Float.parseFloat(c.getString(c.getColumnIndex(DataBases.CreateDB.VELOCITY)));
                    angSensitive = Float.parseFloat(c.getString(c.getColumnIndex(DataBases.CreateDB.ANGULAR)));

                    veltxt.setText(Float.toString(velSensitive));
                    angulartxt.setText(Float.toString(angSensitive));
                    velbar.setProgress((int) (velSensitive * 100));
                    angularbar.setProgress((int) (angSensitive * 100));
                    ctrSelectChangeListener();
                }
            } else {
                ctrSelected = 1;
                veltxt.setText("1.0");
                angulartxt.setText("1.0");
                velbar.setProgress(100);
                angularbar.setProgress(100);
                ctrSelectChangeListener();
            }
        } else {
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

    /**
     * change background of selected option
     */
    private void ctrSelectChangeListener() {
        dblever.setBackground(getResources().getDrawable(R.drawable.lefttopwhite));
        dblever.setTextColor(Color.BLACK);
        leverwheel.setBackground(getResources().getDrawable(R.drawable.leftbottomwhite));
        leverwheel.setTextColor(Color.BLACK);
        rtheta.setBackground(getResources().getDrawable(R.drawable.righttopwhite));
        rtheta.setTextColor(Color.BLACK);
        ytheta.setBackground(getResources().getDrawable(R.drawable.rightbottomwhite));
        ytheta.setTextColor(Color.BLACK);
        switch (ctrSelected) {
            case RobotController.CONTROLLER_HORIZONTAL_DOUBLELEVER:
                dblever.setBackground(getResources().getDrawable(R.drawable.lefttopviolet));
                dblever.setTextColor(Color.WHITE);
                break;
            case RobotController.CONTROLLER_HORIZONTAL_STEER:
                leverwheel.setBackground(getResources().getDrawable(R.drawable.leftbottomviolet));
                leverwheel.setTextColor(Color.WHITE);
                break;
            case RobotController.CONTROLLER_VERTICAL_JOG:
                rtheta.setBackground(getResources().getDrawable(R.drawable.righttopviolet));
                rtheta.setTextColor(Color.WHITE);
                break;
            case RobotController.CONTROLLER_VERTICAL_JOYSTICK:
                ytheta.setBackground(getResources().getDrawable(R.drawable.rightbottomviolet));
                ytheta.setTextColor(Color.WHITE);
                break;
        }
    }

    // update DataBase on finish
    @Override
    protected void onPause() {
        if (mDbOpenHelper == null) {
            mDbOpenHelper = new DbOpenHelper(UserOptionDialog.this);
            mDbOpenHelper.open();
        }
        if (idx != -1) {
            mDbOpenHelper.updateOption(Integer.toString(idx), Integer.toString(ctrSelected), Float.toString(velSensitive), Float.toString(angSensitive));
        }
        mDbOpenHelper.close();
        mDbOpenHelper = null;
        super.onPause();
    }
}
