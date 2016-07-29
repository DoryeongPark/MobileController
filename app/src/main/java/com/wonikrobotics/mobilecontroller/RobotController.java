package com.wonikrobotics.mobilecontroller;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.wonikrobotics.controller.ControlLever;
import com.wonikrobotics.controller.ControlWheel;
import com.wonikrobotics.views.Velocity_Display;

/**
 * Created by Notebook on 2016-07-28.
 */
public class RobotController extends Activity {
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

    /** define views for display sensor data **/
    private ImageView camera;


    /** operated value for publish **/
    private float velocity;
    private float angular;


    /** base app views **/
    private TextView robotName;
    private ImageView userOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();
//        setLayout(RobotController.CONTROLLER_HORIZONTAL_STEER);
        setLayout(RobotController.CONTROLLER_VERTICAL_RTHETA);
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
                        Rect joystickArea;
                        if(joystickLayout.getWidth() > joystickLayout.getHeight()){
                            joystickArea = new Rect(joystickLayout.getWidth()/2-joystickLayout.getHeight()/2,0
                                    ,joystickLayout.getWidth()/2+joystickLayout.getHeight()/2,joystickLayout.getHeight());
                        }else{
                            joystickArea = new Rect(0,joystickLayout.getHeight()/2-joystickLayout.getWidth()/2
                                    ,joystickLayout.getWidth(),joystickLayout.getHeight()/2+joystickLayout.getWidth()/2);
                        }
                        if(flag == RobotController.CONTROLLER_VERTICAL_RTHETA){
                            /*
                            *
                            * *  create joysticView on joystickLayout. (R-Theta)
                            * *  joysticArea is Rect for joystick
                            * *  set Listeners for joystick
                            *
                             */
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
}
