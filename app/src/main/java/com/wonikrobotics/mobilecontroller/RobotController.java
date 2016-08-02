package com.wonikrobotics.mobilecontroller;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import com.wonikrobotics.ros.AndroidNode;
import com.wonikrobotics.ros.CustomPublisher;
import com.wonikrobotics.ros.CustomRosActivity;
import com.wonikrobotics.ros.CustomSubscriber;
import com.wonikrobotics.views.Velocity_Display;

import org.ros.address.InetAddressFactory;
import org.ros.internal.message.Message;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Publisher;

import geometry_msgs.Twist;

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
//        setLayout(RobotController.CONTROLLER_HORIZONTAL_STEER);
        setLayout(RobotController.CONTROLLER_VERTICAL_RTHETA);
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
                         *  else
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
                            joystick.setOnJoystickListener(new Joystick.JoystickListener() {
                                @Override
                                public void onMove(float angle, float angleDir, float acc, float accDir) {

                                    RobotController.this.angular = angle / 90.0f * angleDir * accDir;
                                    RobotController.this.velocity = acc / 100.0f * accDir;
                                    velocityDisplayer.setVel((int)Math.abs(acc));

                                }
                            });

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
            Intent userOpeionDialog = new Intent(RobotController.this,UserOptionDialog.class);
            startActivityForResult(userOpeionDialog,0);
        }
    };

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        NodeConfiguration nodeConfiguration =
                NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
        nodeConfiguration.setMasterUri(getMasterUri());

        CustomPublisher pSet = new CustomPublisher("mobile_base/commands/velocity",
                geometry_msgs.Twist._TYPE, 100) {
            @Override
            public void publishingRoutine(Publisher publisher, ConnectedNode connectedNode) {

                geometry_msgs.Twist veloPubData = connectedNode.
                        getTopicMessageFactory().newFromType(Twist._TYPE);

                veloPubData.getAngular().setZ(RobotController.this.angular);
                veloPubData.getLinear().setX(RobotController.this.velocity);

                veloPubData.getLinear().setY(2.0f);

                publisher.publish(veloPubData);

            }

            @Override
            public void onLoopClear(Publisher publisher, ConnectedNode connectedNode){

                geometry_msgs.Twist veloPubData = connectedNode.
                        getTopicMessageFactory().newFromType(Twist._TYPE);

                veloPubData.getAngular().setZ(0.0f);
                veloPubData.getLinear().setX(0.0f);

                veloPubData.getLinear().setY(1.0f);

                publisher.publish(veloPubData);

            }

        };

        CustomSubscriber sSet = new CustomSubscriber("p1_sonar_1", sensor_msgs.Range._TYPE){
            @Override
            public void subscribingRoutine(Message message) {

                sensor_msgs.Range msg = (sensor_msgs.Range)message;

            }
        };

        AndroidNode androidNode = new AndroidNode();

        androidNode.addSubscriber(sSet);
        androidNode.addPublisher(pSet);

        nodeMainExecutor.execute(androidNode, nodeConfiguration);

   }
}
