package com.wonikrobotics.pathfinder.mc.mobilecontroller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.wonikrobotics.pathfinder.mc.controller.ControlLever;
import com.wonikrobotics.pathfinder.mc.controller.ControlWheel;
import com.wonikrobotics.pathfinder.mc.controller.DoubleLeverCalculator;
import com.wonikrobotics.pathfinder.mc.controller.JogJoystick;
import com.wonikrobotics.pathfinder.mc.controller.SteerTypeJoystick;
import com.wonikrobotics.pathfinder.mc.mobilecontroller.database.DataBases;
import com.wonikrobotics.pathfinder.mc.mobilecontroller.database.DbOpenHelper;
import com.wonikrobotics.pathfinder.mc.ros.AndroidNode;
import com.wonikrobotics.pathfinder.mc.ros.CustomPublisher;
import com.wonikrobotics.pathfinder.mc.ros.CustomRosActivity;
import com.wonikrobotics.pathfinder.mc.ros.CustomSubscriber;
import com.wonikrobotics.pathfinder.mc.views.CameraView;
import com.wonikrobotics.pathfinder.mc.views.LaserSensorView;
import com.wonikrobotics.pathfinder.mc.views.SonarSensorView;
import com.wonikrobotics.pathfinder.mc.views.VelocityDisplay;

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
    /**
     * define layout display mode
     **/
    static final int CONTROLLER_VERTICAL_RTHETA = 1;
    static final int CONTROLLER_VERTICAL_YTHETA = 2;
    static final int CONTROLLER_HORIZONTAL_STEER = 3;
    static final int CONTROLLER_HORIZONTAL_DOUBLELEVER = 4;
    private static ConnectionTimer cTimer = null;
    /**
     * define layout and views for scroll the sensor views
     **/
    private HorizontalScrollView horizontalScroll;
    private LinearLayout innerScroll;
    /**
     * define controller views
     **/
    private LinearLayout velocityDisplayLayout;
    private LinearLayout joystickLayout;
    private LinearLayout leftCtrLayout, rightCtrLayout;
    private VelocityDisplay velocityDisplayer;
    private JogJoystick jogJoystick;
    private SteerTypeJoystick steerTypeJoystick;
    private int currentSelectedController;
    private float velSensitive, angSensitive;
    /**
     * define views for display sensor data
     **/
    private float[] sonarValues;
    private int[] sonarMinAngle, sonarDrawAngle;
    private SonarSensorView sonarView;
    private CameraView cameraView;
    private LaserSensorView laserView;
    View.OnClickListener zoom = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.laser_zoomin:
                    laserView.zoomIn();
                    break;
                case R.id.laser_zoomout:
                    laserView.zoomOut();
                    break;
            }
        }
    };
    private ImageView connectionState;
    private ImageView horizontalLeftArrow;
    private ImageView horizontalRightArrow;
    private LinearLayout viewContents;
    /**
     * operated value for publish
     **/
    private float velocity;
    private float angular;
    /**
     * base app views
     **/
    private TextView robotNameTxt;
    private ImageView userOption;
    private int idx = -1;
    private boolean resumeDialog = false, resumeLayout = false;
    private String robotNameStr;
    /**
     *   listeners
     **/
    private View.OnClickListener optionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setPAUSE_STATE(PAUSE_WITHOUT_STOP);
            Intent userOptionDialog = new Intent(RobotController.this, UserOptionDialog.class);
            userOptionDialog.putExtra("IDX", idx);
            resumeDialog = true;
            startActivity(userOptionDialog);
        }
    };
    private AdapterView.OnItemSelectedListener sonar_range_Selected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (sonarView != null) {
                switch (position) {
                    case 0:
                        sonarView.setScale(SonarSensorView.AROUND_ROBOT);
                        break;
                    case 1:
                        sonarView.setScale(SonarSensorView.FRONT_OF_ROBOT);
                        break;
                    case 2:
                        sonarView.setScale(SonarSensorView.BEHIND_OF_ROBOT);
                        break;
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            if (sonarView != null)
                sonarView.setScale(SonarSensorView.AROUND_ROBOT);
        }
    };
    private AdapterView.OnItemSelectedListener laser_range_Selected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (laserView != null) {
                switch (position) {
                    case 0:
                        laserView.setDisplayRangeMode(LaserSensorView.AROUND_ROBOT);
                        break;
                    case 1:
                        laserView.setDisplayRangeMode(LaserSensorView.FRONT_OF_ROBOT);
                        break;
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            if (laserView != null)
                laserView.setDisplayRangeMode(LaserSensorView.AROUND_ROBOT);
        }
    };
    private AdapterView.OnItemSelectedListener laser_displayMode_Selected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (laserView != null) {
                switch (position) {
                    case 0:
                        laserView.setDiplayMode(LaserSensorView.FILL_INSIDE);
                        break;
                    case 1:
                        laserView.setDiplayMode(LaserSensorView.FILL_OUTSIDE);
                        break;
                    case 2:
                        laserView.setDiplayMode(LaserSensorView.POINT_CLOUD);
                        break;
                    case 3:
                        laserView.setDiplayMode(LaserSensorView.POINT_CLOUD_FILL_INSIDE);
                        break;
                    case 4:
                        laserView.setDiplayMode(LaserSensorView.POINT_CLOUD_FILL_OUTSIDE);
                        break;

                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

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
                            setStateConnect(STATE_CONNECTED);
                            break;
                        case 1:             //on error occured
                            connectionState.setImageResource(R.drawable.disconnecting);
                            setStateConnect(STATE_UNREGISTERING);
                            this.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Log.e("disconnted", "on error");
                                        connectionState.setImageResource(R.drawable.disconnected);
                                        setStateConnect(STATE_DISCONNECTED);
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
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, 2000);
                            break;
                    }
                    break;
                /*
                    ui change associated with subscriber data
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
    private View.OnTouchListener scrollEvent = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
//            return event.getPointerCount() > 1;
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("ONRESUME START", "PAUSE_STATE : " + Integer.toString(getPAUSE_STATE()) + ", dialog : " + Boolean.valueOf(resumeDialog));
        initData();
        Intent instance = getIntent();
        if (instance.hasExtra("NAME"))
            robotNameStr = instance.getStringExtra("NAME");
        if (instance.hasExtra("IDX"))
            idx = instance.getIntExtra("IDX", -1);
        getUserOption(getIntent().getIntExtra("IDX", -1));
        setLayout(currentSelectedController);
        if (!resumeDialog) {
            Log.e("FLAG", "ON");
            if (instance.hasExtra("URL") && instance.hasExtra("MASTER")) {
                if (instance.getBooleanExtra("MASTER", false)) {
                    Log.e("TAG", "IN");
                    NetworkInfo wifi = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    if (wifi.isAvailable()) {
                        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                        WifiInfo info = wifiManager.getConnectionInfo();
                        int ip = info.getIpAddress();
                        setURI("http://" + Formatter.formatIpAddress(ip) + ":11311", true);
                    }
                } else {
                    setURI(getIntent().getStringExtra("URL"), getIntent().getBooleanExtra("MASTER", false));
                }
            }
        }
        Log.e("ONRESUME END", "PAUSE_STATE : " + Integer.toString(getPAUSE_STATE()) + ", dialog : " + Boolean.valueOf(resumeDialog));
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.e("ONPAUSE START", "PAUSE_STATE : " + Integer.toString(getPAUSE_STATE()) + ", dialog : " + Boolean.valueOf(resumeDialog));
        if (getPAUSE_STATE() == PAUSE_WITH_STOP) {
            if (cTimer != null) {
                cTimer.shutDown();
                cTimer = null;
            }
        }
        Log.e("ONPAUSE END", "PAUSE_STATE : " + Integer.toString(getPAUSE_STATE()) + ", dialog : " + Boolean.valueOf(resumeDialog));
    }
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        outState.putBoolean("resumeDialog",resumeDialog);
//        super.onSaveInstanceState(outState);
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        this.resumeDialog = savedInstanceState.getBoolean("resumeDialog",false);
//    }

    private void onTimerFinished(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    connectionState.setImageResource(R.drawable.disconnected);
                    setStateConnect(STATE_DISCONNECTED);
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void initData() {
        sonarValues = new float[8];
        sonarMinAngle = new int[8];
        sonarDrawAngle = new int[8];
        for (int i = 0; i < 8; ++i) {
            sonarValues[i] = 0.0f;
            sonarMinAngle[i] = 194 + 19 * i;
//            sonarMinAngle[i] = 14 + 19 * i;
            sonarDrawAngle[i] = 19;
        }
    }

    private void addEventForHorrizontalArrows() {
        horizontalLeftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int viewPoint = innerScroll.getWidth();
                int currentPoint = horizontalScroll.getScrollX();
                for (int i = 0; i < innerScroll.getChildCount(); ++i) {
                    viewPoint = viewPoint - horizontalScroll.getWidth();
                    if (viewPoint < currentPoint) {
                        horizontalScroll.smoothScrollTo(viewPoint, 0);
                        break;
                    }
                }
            }
        });
        horizontalRightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int viewPoint = 0;
                int currentPoint = horizontalScroll.getScrollX();
                for (int i = 0; i < innerScroll.getChildCount(); ++i) {
                    if (viewPoint > currentPoint) {
                        horizontalScroll.smoothScrollTo(viewPoint, 0);
                        break;
                    }
                    viewPoint = viewPoint + horizontalScroll.getWidth();
                }
            }
        });
    }

    /**
     * method for change layout
     *
     * @param flag
     */
    private void initViews(int flag) {
        sonarView = new SonarSensorView(this, sonarValues, sonarMinAngle, sonarDrawAngle);
        sonarView.setScale(SonarSensorView.AROUND_ROBOT);
        laserView = new LaserSensorView(this) {
            @Override
            public void onMaxValChanged(float val) {
            }
        };
        cameraView = new CameraView(this);
        if (flag == CONTROLLER_HORIZONTAL_DOUBLELEVER || flag == CONTROLLER_HORIZONTAL_STEER) {
            Log.e("BEFORE CHANGE", "PAUSE_STATE : " + Integer.toString(getPAUSE_STATE()) + ", dialog : " + Boolean.valueOf(resumeDialog));
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            Log.e("AFTER CHANGE", "PAUSE_STATE : " + Integer.toString(getPAUSE_STATE()) + ", dialog : " + Boolean.valueOf(resumeDialog));
            setContentView(R.layout.robotcontroller_horizontal);
            leftCtrLayout = (LinearLayout) findViewById(R.id.left_control_layout);
            rightCtrLayout = (LinearLayout) findViewById(R.id.right_control_layout);
        } else {
            Log.e("BEFORE CHANGE", "PAUSE_STATE : " + Integer.toString(getPAUSE_STATE()) + ", dialog : " + Boolean.valueOf(resumeDialog));
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            Log.e("AFTER CHANGE", "PAUSE_STATE : " + Integer.toString(getPAUSE_STATE()) + ", dialog : " + Boolean.valueOf(resumeDialog));
            setContentView(R.layout.robotcontroller_vertical);
            joystickLayout = (LinearLayout) findViewById(R.id.robotController_joystickLayout);
        }
        viewContents = (LinearLayout) findViewById(R.id.view_contents);
        robotNameTxt = (TextView) findViewById(R.id.controllerRobotName);
        velocityDisplayLayout = (LinearLayout) findViewById(R.id.velocity_display_layout);
        robotNameTxt.setText(robotNameStr);
        userOption = (ImageView) findViewById(R.id.controllerUserOption);
        userOption.setOnClickListener(optionClickListener);
        connectionState = (ImageView) findViewById(R.id.connection_state);
        velocityDisplayer = new VelocityDisplay(RobotController.this);

        horizontalScroll = (HorizontalScrollView) findViewById(R.id.horizontalScroll);
        horizontalLeftArrow = (ImageView) findViewById(R.id.horizontalLeftArrow);
        horizontalRightArrow = (ImageView) findViewById(R.id.horizontalRightArrow);
        addEventForHorrizontalArrows();
    }

    private void addSensorViews(HorizontalScrollView scroll, int flag, Context context) {
        /**********************************************     연결 상태    *********************************************************/
        switch (getStateConnect()) {
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
        /*********************************************   아이콘  뷰   ********************************************************/
        ImageView camera_icon = new ImageView(viewContents.getContext());
        camera_icon.setImageResource(R.drawable.camera);
        camera_icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        camera_icon.setLayoutParams(new ViewGroup.LayoutParams(viewContents.getHeight(), viewContents.getHeight()));
        viewContents.addView(camera_icon);
        ImageView sonar_icon = new ImageView(viewContents.getContext());
        sonar_icon.setImageResource(R.drawable.sonar);
        sonar_icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        sonar_icon.setLayoutParams(new ViewGroup.LayoutParams(viewContents.getHeight(), viewContents.getHeight()));
        viewContents.addView(sonar_icon);
        ImageView laser_icon = new ImageView(viewContents.getContext());
        laser_icon.setImageResource(R.drawable.laser);
        laser_icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        laser_icon.setLayoutParams(new ViewGroup.LayoutParams(viewContents.getHeight(), viewContents.getHeight()));
        viewContents.addView(laser_icon);
        /*********************************************   속도 표시 뷰   ********************************************************/
        velocityDisplayLayout.removeAllViews();
        velocityDisplayLayout.addView(velocityDisplayer);
        /*********************************************   Container 뷰   ********************************************************/
        innerScroll = new LinearLayout(context);
        if (flag == CONTROLLER_VERTICAL_RTHETA || flag == CONTROLLER_VERTICAL_YTHETA)
            innerScroll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        else
            innerScroll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        /**********************************************    카메라 뷰    *********************************************************/
        if (cameraView.getParent() != null)
            ((ViewGroup) cameraView.getParent()).removeAllViews();
        cameraView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        cameraView.setLayoutParams(new LinearLayout.LayoutParams(scroll.getWidth(), scroll.getHeight()));
        innerScroll.addView(cameraView);
        /**********************************************     Sonar 뷰    *********************************************************/
        FrameLayout sonarFrame = new FrameLayout(context);
        sonarFrame.setLayoutParams(new FrameLayout.LayoutParams(scroll.getWidth(), scroll.getHeight()));
        RelativeLayout sonaroption = (RelativeLayout) getLayoutInflater().inflate(R.layout.sonaroption, null);
        sonaroption.setLayoutParams(new RelativeLayout.LayoutParams(scroll.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT));
        Spinner sonarOption = (Spinner) sonaroption.findViewById(R.id.sonar_rangeoption);
        sonarOption.setOnItemSelectedListener(sonar_range_Selected);
        sonarView.setLayoutParams(new LinearLayout.LayoutParams(scroll.getWidth(), scroll.getHeight()));
        ImageView sonarzoomin = (ImageView) sonaroption.findViewById(R.id.sonar_zoomin);
        ImageView sonarzoomout = (ImageView) sonaroption.findViewById(R.id.sonar_zoomout);
        RelativeLayout.LayoutParams inparams = new RelativeLayout.LayoutParams(scroll.getWidth() / 4, scroll.getWidth() / 4);
        inparams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        inparams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        sonarzoomin.setLayoutParams(inparams);
        RelativeLayout.LayoutParams outparams = new RelativeLayout.LayoutParams(scroll.getWidth() / 4, scroll.getWidth() / 4);
        outparams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        outparams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        sonarzoomout.setLayoutParams(outparams);
        sonarzoomin.setOnClickListener(zoom);
        sonarzoomout.setOnClickListener(zoom);
        sonarFrame.addView(sonarView);
        sonarFrame.addView(sonaroption);
        innerScroll.addView(sonarFrame);
        /**********************************************     Laser 뷰    *********************************************************/
        FrameLayout laserFrame = new FrameLayout(context);
        laserFrame.setLayoutParams(new FrameLayout.LayoutParams(scroll.getWidth(), scroll.getHeight()));
        laserView.setLayoutParams(new LinearLayout.LayoutParams(scroll.getWidth(), scroll.getHeight()));
        laserView.setDisplayRangeMode(LaserSensorView.AROUND_ROBOT);
        laserView.setDiplayMode(LaserSensorView.POINT_CLOUD);
        RelativeLayout laseroption = (RelativeLayout) getLayoutInflater().inflate(R.layout.laseroption, null);
        laseroption.setLayoutParams(new RelativeLayout.LayoutParams(scroll.getWidth(), scroll.getHeight()));
        final ToggleButton resize = (ToggleButton) laseroption.findViewById(R.id.laser_autoresize);
        resize.setChecked(true);
        resize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                laserView.setAutoResizing(isChecked);
            }
        });
        laserView.setOnAutoResizeChangeListener(new LaserSensorView.OnAutoResizeChangeListener() {
            @Override
            public void onChange(boolean onOff) {
                resize.setChecked(onOff);
            }
        });
        Spinner visibleRangeOption = (Spinner) laseroption.findViewById(R.id.laser_displayrange);
        visibleRangeOption.setOnItemSelectedListener(laser_range_Selected);
        Spinner laserDisplayOption = (Spinner) laseroption.findViewById(R.id.laser_displaymode);
        laserDisplayOption.setOnItemSelectedListener(laser_displayMode_Selected);
        ImageView zoomin = (ImageView) laseroption.findViewById(R.id.laser_zoomin);
        ImageView zoomout = (ImageView) laseroption.findViewById(R.id.laser_zoomout);
        zoomin.setLayoutParams(inparams);
        zoomout.setLayoutParams(outparams);
        zoomin.setOnClickListener(zoom);
        zoomout.setOnClickListener(zoom);
        laserFrame.addView(laserView);
        laserFrame.addView(laseroption);

        /**********************************************     뷰 추가     *********************************************************/
        innerScroll.addView(laserFrame);
        scroll.removeAllViews();
        scroll.addView(innerScroll);
        scroll.setOnTouchListener(scrollEvent);
    }
    private void setLayout(final int flag) {
        Log.e("SETLAYOUT START", "PAUSE_STATE : " + Integer.toString(getPAUSE_STATE()) + ", dialog : " + Boolean.valueOf(resumeDialog));
        innerScroll = new LinearLayout(RobotController.this);
        setPAUSE_STATE(PAUSE_WITHOUT_STOP);
        initViews(flag);
        switch (flag) {
            case RobotController.CONTROLLER_VERTICAL_RTHETA:
            case RobotController.CONTROLLER_VERTICAL_YTHETA:
                horizontalScroll.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("SETLAYOUT POST START", "PAUSE_STATE : " + Integer.toString(getPAUSE_STATE()) + ", dialog : " + Boolean.valueOf(resumeDialog));
                        addSensorViews(horizontalScroll, flag, horizontalScroll.getContext());

                        Rect joystickArea = new Rect();
                        joystickLayout.getGlobalVisibleRect(joystickArea);
                        if (joystickLayout.getWidth() > joystickLayout.getHeight()) {
                            joystickArea = new Rect(joystickLayout.getWidth() / 2 - joystickLayout.getHeight() / 2, joystickArea.top
                                    , joystickLayout.getWidth() / 2 + joystickLayout.getHeight() / 2, joystickLayout.getHeight() + joystickArea.top);
                        } else {
                            joystickArea = new Rect(0, (joystickLayout.getHeight() / 2 - joystickLayout.getWidth() / 2) + joystickArea.top
                                    , joystickLayout.getWidth(), (joystickLayout.getHeight() / 2 + joystickLayout.getWidth() / 2) + joystickArea.top);
                        }
                        if (flag == RobotController.CONTROLLER_VERTICAL_RTHETA) {
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

                        } else {
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
                        setPAUSE_STATE(PAUSE_WITH_STOP);
                        if (resumeDialog)
                            resumeDialog = false;
                        Log.e("SETLAYOUT POST END", "PAUSE_STATE : " + Integer.toString(getPAUSE_STATE()) + ", dialog : " + Boolean.valueOf(resumeDialog));
                    }

                });

                break;
            case RobotController.CONTROLLER_HORIZONTAL_STEER:
            case RobotController.CONTROLLER_HORIZONTAL_DOUBLELEVER:
                horizontalScroll.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("SETLAYOUT POST START", "PAUSE_STATE : " + Integer.toString(getPAUSE_STATE()) + ", dialog : " + Boolean.valueOf(resumeDialog));
                        addSensorViews(horizontalScroll, flag, horizontalScroll.getContext());
                        leftCtrLayout.removeAllViews();
                        rightCtrLayout.removeAllViews();
                        if (flag == RobotController.CONTROLLER_HORIZONTAL_STEER) {
                            leftCtrLayout.addView(new ControlLever(RobotController.this) {
                                @Override
                                public void onProgressChanged(int progress, boolean fromUser) {
                                    velocity = progress == 0 ? 0 : -1 * progress / 100f;
                                    velocityDisplayer.setVel(Math.abs(progress));
                                }
                            });
                            rightCtrLayout.addView(new ControlWheel(RobotController.this) {
                                @Override
                                public void onAngleChanged(int angle, boolean fromUser) {
                                    angular = angle == 0 ? 0 : angle / -180f;
                                }
                            });
                        } else {
                            final DoubleLeverCalculator cal = new DoubleLeverCalculator() {
                                @Override
                                public void valueChangeListener(float velocity, float angular) {
                                    RobotController.this.velocity = -1 * velocity;
                                    RobotController.this.angular = angular;
                                    velocityDisplayer.setVel(Math.abs(Math.round(velocity * 100)));
                                }
                            };
                            leftCtrLayout.addView(new ControlLever(RobotController.this) {
                                @Override
                                public void onProgressChanged(int progress, boolean fromUser) {
                                    cal.setLeftWheelVel(progress);

                                }
                            });
                            rightCtrLayout.addView(new ControlLever(RobotController.this) {
                                @Override
                                public void onProgressChanged(int progress, boolean fromUser) {
                                    cal.setRightWheelVel(progress);
                                }
                            });
                        }
                        setPAUSE_STATE(PAUSE_WITH_STOP);
                        if (resumeDialog)
                            resumeDialog = false;
                        Log.e("SETLAYOUT POST END", "PAUSE_STATE : " + Integer.toString(getPAUSE_STATE()) + ", dialog : " + Boolean.valueOf(resumeDialog));
                    }
                });
                break;
            default:
                break;
        }
        Log.e("SETLAYOUT END", "PAUSE_STATE : " + Integer.toString(getPAUSE_STATE()) + ", dialog : " + Boolean.valueOf(resumeDialog));
    }

    private void getUserOption(int idx) {
        DbOpenHelper mDbOpenHelper = new DbOpenHelper(RobotController.this);
        mDbOpenHelper.open();
        Cursor c = mDbOpenHelper.getAllColumns();
        if (c.getCount() != 0) {
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
        } else {
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
        Log.e("INIT START", "PAUSE_STATE : " + Integer.toString(getPAUSE_STATE()) + ", dialog : " + Boolean.valueOf(resumeDialog));
        NodeConfiguration nodeConfiguration =
                NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
        nodeConfiguration.setMasterUri(getMasterUri());

        AndroidNode androidNode = new AndroidNode("robotics") {
            @Override
            public void onError(Node node, Throwable throwable) {
                super.onError(node, throwable);
                Log.d("NodeExecutor", "onerror");
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


        CustomPublisher velocityPublisher = new CustomPublisher("cmd_vel",
//        CustomPublisher velocityPublisher = new CustomPublisher("mobile_base/commands/velocity",
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

        for (int i = 1; i < 9; ++i) {
            final int in = i;
            CustomSubscriber sonarSubscriber = new CustomSubscriber("p1_sonar_" + String.valueOf(i),
                    sensor_msgs.Range._TYPE) {
                @Override
                public void subscribingRoutine(Message message) {
                    sensor_msgs.Range msg = (sensor_msgs.Range) message;
                    sonarValues[in - 1] = msg.getRange();
                    android.os.Message hdmsg = new android.os.Message();
                    hdmsg.what = 1;
                    hdmsg.arg1 = 1;
                    handler.sendMessage(hdmsg);
                }
            };
            androidNode.addSubscriber(sonarSubscriber);
        }

        CustomSubscriber laserSubscriber = new CustomSubscriber("scan", sensor_msgs.LaserScan._TYPE) {
            @Override
            public void subscribingRoutine(Message message) {
                sensor_msgs.LaserScan msg = (sensor_msgs.LaserScan) message;
                android.os.Message hdmsg = new android.os.Message();
                hdmsg.what = 1;
                hdmsg.arg1 = 2;
                hdmsg.obj = msg;
                handler.sendMessage(hdmsg);
            }
        };
        androidNode.addSubscriber(laserSubscriber);

        CustomSubscriber cameraSubscriber = new CustomSubscriber("image/compressed", sensor_msgs.CompressedImage._TYPE) {
            //        CustomSubscriber cameraSubscriber = new CustomSubscriber("camera/rgb/image_raw/compressed", sensor_msgs.CompressedImage._TYPE) {
            @Override
            public void subscribingRoutine(Message message) {
                BitmapFromCompressedImage bfci = new BitmapFromCompressedImage();
                Bitmap data = bfci.call((sensor_msgs.CompressedImage) message);
                android.os.Message hdmsg = new android.os.Message();
                hdmsg.what = 1;
                hdmsg.arg1 = 0;
                hdmsg.obj = data;
                handler.sendMessage(hdmsg);
            }
        };
        androidNode.addSubscriber(cameraSubscriber);

        CustomSubscriber timerSubscriber = new CustomSubscriber("clock", rosgraph_msgs.Clock._TYPE){
            @Override
            public void subscribingRoutine(Message message) {
                if(cTimer != null)
                    cTimer.getHeartBeat();
            }
        };

        androidNode.addSubscriber(timerSubscriber);
        nodeMainExecutor.execute(androidNode, nodeConfiguration);
        if (!getIs_Master()) {
            if (cTimer == null) {
                cTimer = new ConnectionTimer(10) {
                    @Override
                    public void onTimerFinished() {
                        RobotController.this.onTimerFinished();
                    }
                };
                cTimer.start();
            }
        }
    }

}
