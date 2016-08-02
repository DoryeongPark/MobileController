package com.wonikrobotics.mobilecontroller;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.common.base.Preconditions;
import com.wonikrobotics.controller.ControlLever;
import com.wonikrobotics.controller.ControlWheel;
import com.wonikrobotics.controller.Joystick;
import com.wonikrobotics.mobilecontroller.database.DataBases;
import com.wonikrobotics.mobilecontroller.database.DbOpenHelper;
import com.wonikrobotics.ros.CustomRosActivity;
import com.wonikrobotics.views.Velocity_Display;

import org.ros.node.NodeMainExecutor;

/**
 * Created by Notebook on 2016-07-28.
 */
public class RobotController extends CustomRosActivity {
   /**  define layout display mode **/
    static final int CONTROLLER_VERTICAL_RTHETA = 1;
    static final int CONTROLLER_VERTICAL_YTHETA = 2;
    static final int CONTROLLER_HORIZONTAL_STEER = 3;
    static final int CONTROLLER_HORIZONTAL_DOUBLELEVER = 4;
    /** define layout and views for scroll the sensor views **/
    private ScrollView verticalScroll;
    private HorizontalScrollView horizontalScroll;
    private LinearLayout innerScroll;

    /** define controller views **/
    private LinearLayout velocityDisplayLayout;
    private LinearLayout joystickLayout;
    private LinearLayout leftCtrLayout,rightCtrLayout;
    private Velocity_Display velocityDisplayer;
    private Joystick joystick;
    private int currentSelectedController;
    private float velSensitive,angSensitive;

    /** define views for display sensor data **/
    private ImageView camera;


    /** operated value for publish **/
    private float velocity;
    private float angular;


    /** base app views **/
    private TextView robotName;
    private ImageView userOption;

    public RobotController(){ }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserOption();
        setLayout(currentSelectedController);
        Preconditions.checkNotNull(getIntent().getStringExtra("NAME"));
        Preconditions.checkNotNull(getIntent().getStringExtra("URL"));
        Preconditions.checkNotNull(getIntent().getBooleanExtra("MASTER",false));
        setURI(getIntent().getStringExtra("URL"),getIntent().getBooleanExtra("MASTER",false));
    }

    /**
     * method for change layout
     *
     * @param flag
     */
    private void setLayout(final int flag){
        innerScroll = new LinearLayout(RobotController.this);
        switch(flag){
            case RobotController.CONTROLLER_VERTICAL_RTHETA:
            case RobotController.CONTROLLER_VERTICAL_YTHETA:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                setContentView(R.layout.robotcontroller_vertical);
                robotName = (TextView)findViewById(R.id.controllerRobotName);
                userOption = (ImageView)findViewById(R.id.controllerUserOption);
                userOption.setOnClickListener(optionClickListener);
                velocityDisplayLayout = (LinearLayout) findViewById(R.id.velocity_display_layout);//textview for display velocity
                joystickLayout = (LinearLayout)findViewById(R.id.robotController_joystickLayout);
                velocityDisplayer = new Velocity_Display(RobotController.this);
                horizontalScroll = (HorizontalScrollView)findViewById(R.id.horizontalScroll);
                horizontalScroll.post(new Runnable() {
                    @Override
                    public void run() {
                        velocityDisplayLayout.addView(velocityDisplayer);
                        innerScroll = new LinearLayout(horizontalScroll.getContext());
                        innerScroll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        /****
                         *
                         * * add Sensor Views on innerScroll but have to set measure of views
                         *
                         *
                         * sample code for add camera view to innerScroll (camera view's scale is square)
                         *
                         *  camera = new ImageView(innerScroll.getContext());
                         *  camera.setScaleType(ImageView.ScaleType.FIT_CENTER);
                         *  if(horizontalScroll.getWidth()>horizontalScroll.getHeight())
                         *      camera.setLayoutParams(new LinearLayout.LayoutParams(horizontalScroll.getHeight(),horizontalScroll.getHeight()));
                         *  eles
                         *      camera.setLayoutParams(new LinearLayout.LayoutParams(horizontalScroll.getWidth(),horizontalScroll.getWidth()));
                         *  innerScroll.addView(camera);
                         *
                         *
                         ****/
                        horizontalScroll.addView(innerScroll);
                        Rect joystickArea = new Rect();
                        joystickLayout.getGlobalVisibleRect(joystickArea);
                        if(joystickLayout.getWidth() > joystickLayout.getHeight()){
                            joystickArea = new Rect(joystickLayout.getWidth()/2-joystickLayout.getHeight()/2,joystickArea.top
                                    ,joystickLayout.getWidth()/2+joystickLayout.getHeight()/2,joystickLayout.getHeight()+joystickArea.top);
                        }else{
                            joystickArea = new Rect(0,(joystickLayout.getHeight()/2-joystickLayout.getWidth()/2) + joystickArea.top
                                    ,joystickLayout.getWidth(),(joystickLayout.getHeight()/2+joystickLayout.getWidth()/2)+joystickArea.top);
                        }
                        if(flag == RobotController.CONTROLLER_VERTICAL_RTHETA){

                            joystick = new Joystick(RobotController.this);
                            joystick.setAreaMovable(joystickArea);
                            joystickLayout.addView(joystick);

                        }else{
                            /*
                            *
                            * *  create joysticView on joystickLayout. (Y-Theta)
                            * *  joysticArea is Rect for joystick
                            * *  set Listeners for joystick
                            *
                             */
                        }
                    }
                });
                break;
            case RobotController.CONTROLLER_HORIZONTAL_STEER:
            case RobotController.CONTROLLER_HORIZONTAL_DOUBLELEVER:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                setContentView(R.layout.robotcontroller_horizontal);
                robotName = (TextView)findViewById(R.id.controllerRobotName);
                velocityDisplayLayout = (LinearLayout)findViewById(R.id.velocity_display_layout);
                leftCtrLayout = (LinearLayout)findViewById(R.id.left_control_layout);
                rightCtrLayout = (LinearLayout)findViewById(R.id.right_control_layout);
                userOption = (ImageView)findViewById(R.id.controllerUserOption);
                userOption.setOnClickListener(optionClickListener);
                velocityDisplayer = new Velocity_Display(RobotController.this);
                verticalScroll = (ScrollView)findViewById(R.id.verticalScroll);
                verticalScroll.post(new Runnable(){
                    @Override
                    public void run() {
                        velocityDisplayLayout.addView(velocityDisplayer);
                        innerScroll = new LinearLayout(verticalScroll.getContext());
                        innerScroll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        verticalScroll.addView(innerScroll);
                        leftCtrLayout.addView(new ControlLever(RobotController.this) {
                            @Override
                            public void onProgressChanged(int progress, boolean fromUser) {

                                velocity = progress;
                                velocityDisplayer.setVel(Math.abs(progress));
                            }
                        });
                        if(flag == RobotController.CONTROLLER_HORIZONTAL_STEER){
                            rightCtrLayout.addView(new ControlWheel(RobotController.this) {
                                @Override
                                public void onAngleChanged(int angle, boolean fromUser) {
                                    angular = angle;
                                }
                            });
                        }else{
                            rightCtrLayout.addView(new ControlLever(RobotController.this) {
                                @Override
                                public void onProgressChanged(int progress, boolean fromUser) {

                                }
                            });
                        }

                    }
                });
                break;
            default:
                break;
        }
    }
    private View.OnClickListener optionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent userOpsionDialog = new Intent(RobotController.this,UserOptionDialog.class);
            startActivityForResult(userOpsionDialog,0);
        }
    };
    private void getUserOption(){
        DbOpenHelper mDbOpenHelper = new DbOpenHelper(RobotController.this);
        mDbOpenHelper.open();
        Cursor c = mDbOpenHelper.getAllColumnsFromOption();
        if(c.getCount() != 0) {
            c.moveToNext();
            currentSelectedController = Integer.parseInt(c.getString(c.getColumnIndex(DataBases.CreateDB.CONTROLLER)));
            velSensitive = Float.parseFloat(c.getString(c.getColumnIndex(DataBases.CreateDB.VELOCITY)));
            angSensitive = Float.parseFloat(c.getString(c.getColumnIndex(DataBases.CreateDB.ANGULAR)));

        }else{
            currentSelectedController = 1;
            velSensitive = 1.0f;
            angSensitive = 1.0f;
        }
        mDbOpenHelper.close();
        mDbOpenHelper = null;

    }
    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {

    }

}
