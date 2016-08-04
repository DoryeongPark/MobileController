package com.wonikrobotics.mobilecontroller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.wonikrobotics.controller.ControlLever;
import com.wonikrobotics.controller.ControlWheel;
import com.wonikrobotics.controller.JogJoystick;
import com.wonikrobotics.controller.SteerTypeJoystick;
import com.wonikrobotics.mobilecontroller.database.DataBases;
import com.wonikrobotics.mobilecontroller.database.DbOpenHelper;
import com.wonikrobotics.ros.AndroidNode;
import com.wonikrobotics.ros.CustomPublisher;
import com.wonikrobotics.ros.CustomRosActivity;
import com.wonikrobotics.ros.CustomSubscriber;
import com.wonikrobotics.views.CameraView;
import com.wonikrobotics.views.LaserSensorView;
import com.wonikrobotics.views.SonarSensorView;
import com.wonikrobotics.views.VelocityDisplay;

import org.ros.address.InetAddressFactory;
import org.ros.android.BitmapFromCompressedImage;
import org.ros.internal.message.Message;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
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
    private VelocityDisplay velocityDisplayer;
    private JogJoystick jogJoystick;
    private SteerTypeJoystick steerTypeJoystick;
    private int currentSelectedController;
    private float velSensitive,angSensitive;

    /** define views for display sensor data **/
    private float[] sonarValues;
    private SonarSensorView sonarView;
    private LaserSensorView laserView;
    private CameraView cameraView;
    private ImageView connectionState;


    /** operated value for publish **/
    private float velocity;
    private float angular;

    /** base app views **/
    private TextView robotNameTxt;
    private ImageView userOption;
    private int idx = -1;
    private String robotNameStr;
    private View.OnClickListener optionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setPAUSE_STATE(PAUSE_WITHOUT_STOP);
            Intent userOptionDialog = new Intent(RobotController.this, UserOptionDialog.class);
            userOptionDialog.putExtra("IDX", idx);
            startActivityForResult(userOptionDialog, 1);
        }
    };

    /**
     * handler
     **/
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                /* ui change associated with connection */
                case -1:                    //state change
                    switch (msg.arg1) {
                        case 0:             //on connected node start
                            connectionState.setImageResource(R.drawable.connected);
                            break;
                        case 1:             //on error occured
                            connectionState.setImageResource(R.drawable.disconnecting);
                            this.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    connectionState.setImageResource(R.drawable.disconnected);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(RobotController.this);
                                    builder.setTitle("Connection Failed").setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            finish();
                                        }
                                    });
                                    builder.setMessage("Check the network state");
                                    builder.create();
                                    builder.show();
                                }
                            }, 2000);
                            break;
                    }
                    break;
                /* ui change associated with subscriber data
                    0 - camera, 1 - sonar, 2 - laser, 3 - map
                 */
                case 1:
                    if (msg.arg1 == 0) {
                        cameraView.update((Bitmap) msg.obj);
                    } else if (msg.arg1 == 1) {
                        sonarView.update(sonarValues);
                    } else if (msg.arg1 == 2) {
                        laserView.update((sensor_msgs.LaserScan) msg.obj);
                    }
                    break;
            }
        }
    };



    public RobotController(){ }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();

        initData();
        Intent instance = getIntent();
        if (instance.hasExtra("NAME"))
            robotNameStr = instance.getStringExtra("NAME");
        if (instance.hasExtra("IDX"))
            idx = instance.getIntExtra("IDX", -1);
        getUserOption(getIntent().getIntExtra("IDX", -1));
        setLayout(currentSelectedController);
        if (instance.hasExtra("URL") && instance.hasExtra("MASTER"))
            setURI(getIntent().getStringExtra("URL"), getIntent().getBooleanExtra("MASTER",false));
    }

    private void initData(){
        sonarValues = new float[8];
        for(int i = 0; i < 8; ++i)
            sonarValues[i] = 0.0f;
    }




    /**
     * method for change layout
     *
     * @param flag
     */
    private void setLayout(final int flag){
        innerScroll = new LinearLayout(RobotController.this);
        setPAUSE_STATE(PAUSE_WITHOUT_STOP);
        switch(flag){
            case RobotController.CONTROLLER_VERTICAL_RTHETA:
            case RobotController.CONTROLLER_VERTICAL_YTHETA:
                sonarView = new SonarSensorView(this, sonarValues);
                laserView = new LaserSensorView(this){
                    @Override
                    public void onMaxValChanged(float val) {}
                };
                cameraView = new CameraView(this);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                setContentView(R.layout.robotcontroller_vertical);
                robotNameTxt = (TextView) findViewById(R.id.controllerRobotName);
                userOption = (ImageView)findViewById(R.id.controllerUserOption);
                userOption.setOnClickListener(optionClickListener);
                connectionState = (ImageView) findViewById(R.id.connection_state);
                velocityDisplayLayout = (LinearLayout) findViewById(R.id.velocity_display_layout);//textview for display velocity
                joystickLayout = (LinearLayout)findViewById(R.id.robotController_joystickLayout);
                velocityDisplayer = new VelocityDisplay(RobotController.this);
                robotNameTxt.setText(robotNameStr);
                horizontalScroll = (HorizontalScrollView)findViewById(R.id.horizontalScroll);
                horizontalScroll.post(new Runnable() {
                    @Override
                    public void run() {
                        velocityDisplayLayout.removeAllViews();
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
                        if(cameraView.getParent() !=null)
                            ((ViewGroup)cameraView.getParent()).removeAllViews();
                        cameraView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        if(horizontalScroll.getWidth()>horizontalScroll.getHeight())
                              cameraView.setLayoutParams(new LinearLayout.LayoutParams(horizontalScroll.getHeight(),horizontalScroll.getHeight()));
                        else
                              cameraView.setLayoutParams(new LinearLayout.LayoutParams(horizontalScroll.getWidth(),horizontalScroll.getWidth()));
                        innerScroll.addView(cameraView);
                            sonarView.setLayoutParams(new LinearLayout.LayoutParams(horizontalScroll.getWidth(),horizontalScroll.getHeight()));
                        innerScroll.addView(sonarView);
                            laserView.setLayoutParams(new LinearLayout.LayoutParams(horizontalScroll.getWidth(),horizontalScroll.getHeight()));
                        innerScroll.addView(laserView);
                        horizontalScroll.removeAllViews();
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
                            jogJoystick = new JogJoystick(RobotController.this);
                            jogJoystick.setAreaMovable(joystickArea);
                            jogJoystick.setWeight(angSensitive, velSensitive);
                            joystickLayout.removeAllViews();
                            joystickLayout.addView(jogJoystick);
                            jogJoystick.setOnJoystickListener(new JogJoystick.JoystickListener() {
                                @Override
                                public void onMove(float dataAngular, float dataLinear) {
                                    RobotController.this.angular = dataAngular;
                                    RobotController.this.velocity = dataLinear;
                                    velocityDisplayer.setVel((int) Math.abs(dataLinear * 100.0f));
                                }
                            });

                        }else{

                            steerTypeJoystick = new SteerTypeJoystick(RobotController.this);
                            steerTypeJoystick.setAreaMovable(joystickArea);
                            steerTypeJoystick.setWeight(angSensitive, velSensitive);
                            joystickLayout.removeAllViews();
                            joystickLayout.addView(steerTypeJoystick);
                            steerTypeJoystick.setOnJoystickListener(new SteerTypeJoystick.JoystickListener() {
                                @Override
                                public void onMove(float dataAngular, float dataLinear) {
                                    RobotController.this.angular = dataAngular;
                                    RobotController.this.velocity = dataLinear;
                                    velocityDisplayer.setVel((int) Math.abs(dataLinear * 100.0f));
                                }
                            });
                        }
                    }

                });
                break;
            case RobotController.CONTROLLER_HORIZONTAL_STEER:
            case RobotController.CONTROLLER_HORIZONTAL_DOUBLELEVER:
                sonarView = new SonarSensorView(this, sonarValues);
                laserView = new LaserSensorView(this){
                    @Override
                    public void onMaxValChanged(float val) {}
                };
                cameraView = new CameraView(this);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                setContentView(R.layout.robotcontroller_horizontal);
                robotNameTxt = (TextView) findViewById(R.id.controllerRobotName);
                velocityDisplayLayout = (LinearLayout)findViewById(R.id.velocity_display_layout);
                leftCtrLayout = (LinearLayout)findViewById(R.id.left_control_layout);
                rightCtrLayout = (LinearLayout)findViewById(R.id.right_control_layout);
                robotNameTxt.setText(robotNameStr);
                userOption = (ImageView)findViewById(R.id.controllerUserOption);
                userOption.setOnClickListener(optionClickListener);
                connectionState = (ImageView) findViewById(R.id.connection_state);
                velocityDisplayer = new VelocityDisplay(RobotController.this);
                verticalScroll = (ScrollView)findViewById(R.id.verticalScroll);
                verticalScroll.post(new Runnable(){
                    @Override
                    public void run() {
                        velocityDisplayLayout.removeAllViews();
                        velocityDisplayLayout.addView(velocityDisplayer);
                        innerScroll = new LinearLayout(verticalScroll.getContext());
                        innerScroll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        if(cameraView.getParent() !=null)
                            ((ViewGroup)cameraView.getParent()).removeAllViews();
                        cameraView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        if(verticalScroll.getWidth()>verticalScroll.getHeight())
                            cameraView.setLayoutParams(new LinearLayout.LayoutParams(verticalScroll.getHeight(),verticalScroll.getHeight()));
                        else
                            cameraView.setLayoutParams(new LinearLayout.LayoutParams(verticalScroll.getWidth(),verticalScroll.getWidth()));
                        innerScroll.addView(cameraView);
                        sonarView.setLayoutParams(new LinearLayout.LayoutParams(verticalScroll.getWidth(),verticalScroll.getHeight()));
                        innerScroll.addView(sonarView);
                        laserView.setLayoutParams(new LinearLayout.LayoutParams(verticalScroll.getWidth(),verticalScroll.getHeight()));
                        innerScroll.addView(laserView);
                        verticalScroll.removeAllViews();
                        verticalScroll.addView(innerScroll);
                        leftCtrLayout.removeAllViews();
                        rightCtrLayout.removeAllViews();
                        leftCtrLayout.addView(new ControlLever(RobotController.this) {
                            @Override
                            public void onProgressChanged(int progress, boolean fromUser) {
                                velocity = progress == 0 ? 0 : -1 * progress / 100f;
                                velocityDisplayer.setVel(Math.abs(progress));
                            }
                        });

                        if(flag == RobotController.CONTROLLER_HORIZONTAL_STEER){
                            rightCtrLayout.addView(new ControlWheel(RobotController.this) {
                                @Override
                                public void onAngleChanged(int angle, boolean fromUser) {
                                    angular = angle == 0 ? 0 : angle / -180f;
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

    private void getUserOption(int idx) {
        DbOpenHelper mDbOpenHelper = new DbOpenHelper(RobotController.this);
        mDbOpenHelper.open();
        Cursor c = mDbOpenHelper.getAllColumns();
        if(c.getCount() != 0) {
            idx = getIntent().getIntExtra("IDX", -1);
            if (idx != -1) {
                while (c.moveToNext() && c.getInt(c.getColumnIndex(DataBases.CreateDB.IDX)) != idx) {
                }
                if (c != null) {
                    currentSelectedController = Integer.parseInt(c.getString(c.getColumnIndex(DataBases.CreateDB.CONTROLLER)));
                    velSensitive = Float.parseFloat(c.getString(c.getColumnIndex(DataBases.CreateDB.VELOCITY)));
                    angSensitive = Float.parseFloat(c.getString(c.getColumnIndex(DataBases.CreateDB.ANGULAR)));
                }
            } else {
                currentSelectedController = 1;
                velSensitive = 1.0f;
                angSensitive = 1.0f;
            }
        }else{
            currentSelectedController = 1;
            velSensitive = 1.0f;
            angSensitive = 1.0f;
        }
        mDbOpenHelper.close();
        mDbOpenHelper = null;

    }

    @Override
    protected void onStateChangeListener(int state) {
        switch (state) {
            case STATE_CONNECTED:
                connectionState.setImageResource(R.drawable.connected);
                break;
            case STATE_CONNECTING:
                connectionState.setImageResource(R.drawable.connecting);
                break;
            case STATE_DISCONNECTED:
                connectionState.setImageResource(R.drawable.disconnected);
                break;
            case STATE_UNREGISTERING:
                connectionState.setImageResource(R.drawable.disconnecting);
                break;
        }
    }
    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        setPAUSE_STATE(PAUSE_WITHOUT_STOP);
        NodeConfiguration nodeConfiguration =
                NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
        nodeConfiguration.setMasterUri(getMasterUri());

        AndroidNode androidNode = new AndroidNode() {
            @Override
            public void onError(Node node, Throwable throwable) {
                super.onError(node, throwable);
                Log.d("NodeExcutor", "onerror");
                android.os.Message msg = new android.os.Message();
                msg.what = -1;
                msg.arg1 = 1;
                msg.obj = "Error Occured";
                handler.sendMessage(msg);
                try {
                    node.shutdown();
                } catch (org.ros.internal.node.xmlrpc.XmlRpcTimeoutException e) {
                    Log.e("exception", "catch");
                }
            }

            @Override
            public void onStart(ConnectedNode connectedNode) {
                android.os.Message msg = new android.os.Message();
                msg.what = -1;
                msg.arg1 = 0;
                handler.sendMessage(msg);
                super.onStart(connectedNode);
            }
        };


        CustomPublisher velocityPublisher = new CustomPublisher("mobile_base/commands/velocity",
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
            public void onLoopClear(Publisher publisher, ConnectedNode connectedNode) {
                geometry_msgs.Twist veloPubData = connectedNode.
                        getTopicMessageFactory().newFromType(Twist._TYPE);
                veloPubData.getAngular().setZ(0.0f);
                veloPubData.getLinear().setX(0.0f);
                veloPubData.getLinear().setY(1.0f);
                publisher.publish(veloPubData);
            }
        };

        androidNode.addPublisher(velocityPublisher);

        for(int i = 1 ; i < 9; ++i) {
            final int in = i;
            CustomSubscriber sonarSubscriber = new CustomSubscriber("p1_sonar_" + String.valueOf(i),
                    sensor_msgs.Range._TYPE) {
                @Override
                public void subscribingRoutine(Message message) {
                    sensor_msgs.Range msg = (sensor_msgs.Range) message;
                    sonarValues[in-1] = msg.getRange();
                    android.os.Message hdmsg = new android.os.Message();
                    hdmsg.what = 1; hdmsg.arg1 = 1;
                    handler.sendMessage(hdmsg);
                }
            };
            androidNode.addSubscriber(sonarSubscriber);
        }

        CustomSubscriber laserSubscriber = new CustomSubscriber("scan", sensor_msgs.LaserScan._TYPE){
            @Override
            public void subscribingRoutine(Message message) {
                sensor_msgs.LaserScan msg = (sensor_msgs.LaserScan) message;
                android.os.Message hdmsg = new android.os.Message();
                hdmsg.what = 1; hdmsg.arg1 = 2; hdmsg.obj = msg;
                handler.sendMessage(hdmsg);
            }
        };
        androidNode.addSubscriber(laserSubscriber);

        CustomSubscriber cameraSubscriber = new CustomSubscriber("camera/rgb/image_raw/compressed", sensor_msgs.CompressedImage._TYPE){
            @Override
            public void subscribingRoutine(Message message) {
                BitmapFromCompressedImage bfci = new BitmapFromCompressedImage();
                Bitmap data = bfci.call((sensor_msgs.CompressedImage)message);
                android.os.Message hdmsg = new android.os.Message();
                hdmsg.what = 1; hdmsg.arg1 = 0; hdmsg.obj = data;
                handler.sendMessage(hdmsg);
            }
        };
        androidNode.addSubscriber(cameraSubscriber);

        nodeMainExecutor.execute(androidNode, nodeConfiguration);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == 1) {
            setPAUSE_STATE(PAUSE_WITHOUT_STOP);
        }
    }
}
