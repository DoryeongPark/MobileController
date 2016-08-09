package com.wonikrobotics.pathfinder.mc.mobilecontroller;

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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
    /**
     * define layout and views for scroll the sensor views
     **/
    private ScrollView verticalScroll;
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
    AdapterView.OnItemSelectedListener sonar_range_Selected = new AdapterView.OnItemSelectedListener() {
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
    private LaserSensorView laserView;
    AdapterView.OnItemSelectedListener laser_range_Selected = new AdapterView.OnItemSelectedListener() {
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
    AdapterView.OnItemSelectedListener laser_displayMode_Selected = new AdapterView.OnItemSelectedListener() {
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
    private CameraView cameraView;
    private ImageView connectionState;
    private ImageView verticalLeftArrow;
    private ImageView verticalRightArrow;
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
    private boolean resumeDialog = false;
    private String robotNameStr;
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
                                    try {
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
            return event.getPointerCount() > 1;
        }
    };
    public RobotController() {
    }

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
        if (!resumeDialog) {
            if (instance.hasExtra("URL") && instance.hasExtra("MASTER"))
                setURI(getIntent().getStringExtra("URL"), getIntent().getBooleanExtra("MASTER", false));
        }
    }

    private void initData() {
        sonarValues = new float[8];
        sonarMinAngle = new int[8];
        sonarDrawAngle = new int[8];
        for (int i = 0; i < 8; ++i) {
            sonarValues[i] = 0.0f;
            sonarMinAngle[i] = 194 + 19 * i;
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

    private void addEventForVerticalArrows() {
        verticalLeftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int viewPoint = innerScroll.getHeight();
                int currentPoint = verticalScroll.getScrollY();
                for (int i = 0; i < innerScroll.getChildCount(); ++i) {
                    viewPoint = viewPoint - verticalScroll.getHeight();
                    if (viewPoint < currentPoint) {
                        verticalScroll.smoothScrollTo(0, viewPoint);
                        break;
                    }
                }
            }
        });

        verticalRightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int viewPoint = 0;
                int currentPoint = verticalScroll.getScrollY();
                for (int i = 0; i < innerScroll.getChildCount(); ++i) {
                    if (viewPoint > currentPoint) {
                        verticalScroll.smoothScrollTo(0, viewPoint);
                        break;
                    }
                    viewPoint = viewPoint + verticalScroll.getHeight();
                }
            }
        });
    }

    /**
     * method for change layout
     *
     * @param flag
     */
    private void setLayout(final int flag) {
        innerScroll = new LinearLayout(RobotController.this);
        setPAUSE_STATE(PAUSE_WITHOUT_STOP);
        switch (flag) {
            case RobotController.CONTROLLER_VERTICAL_RTHETA:
            case RobotController.CONTROLLER_VERTICAL_YTHETA:
                sonarView = new SonarSensorView(this, sonarValues, sonarMinAngle, sonarDrawAngle);
                sonarView.setScale(SonarSensorView.AROUND_ROBOT);
                laserView = new LaserSensorView(this) {
                    @Override
                    public void onMaxValChanged(float val) {
                    }
                };
                cameraView = new CameraView(this);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                setContentView(R.layout.robotcontroller_vertical);
                viewContents = (LinearLayout) findViewById(R.id.view_contents);
                robotNameTxt = (TextView) findViewById(R.id.controllerRobotName);
                userOption = (ImageView) findViewById(R.id.controllerUserOption);
                userOption.setOnClickListener(optionClickListener);
                connectionState = (ImageView) findViewById(R.id.connection_state);
                velocityDisplayLayout = (LinearLayout) findViewById(R.id.velocity_display_layout);
                joystickLayout = (LinearLayout) findViewById(R.id.robotController_joystickLayout);
                velocityDisplayer = new VelocityDisplay(RobotController.this);
                robotNameTxt.setText(robotNameStr);
                horizontalScroll = (HorizontalScrollView) findViewById(R.id.horizontalScroll);
                horizontalLeftArrow = (ImageView) findViewById(R.id.horizontalLeftArrow);
                horizontalRightArrow = (ImageView) findViewById(R.id.horizontalRightArrow);
                horizontalScroll.post(new Runnable() {
                    @Override
                    public void run() {
                        velocityDisplayLayout.removeAllViews();
                        velocityDisplayLayout.addView(velocityDisplayer);
                        innerScroll = new LinearLayout(horizontalScroll.getContext());
                        innerScroll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        if (cameraView.getParent() != null)
                            ((ViewGroup) cameraView.getParent()).removeAllViews();
                        cameraView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        cameraView.setLayoutParams(new LinearLayout.LayoutParams(horizontalScroll.getWidth(), horizontalScroll.getHeight()));
                        innerScroll.addView(cameraView);
                        ImageView camera_icon = new ImageView(viewContents.getContext());
                        camera_icon.setImageResource(R.drawable.camera);
                        camera_icon.setLayoutParams(new ViewGroup.LayoutParams(viewContents.getHeight(), viewContents.getHeight()));
                        viewContents.addView(camera_icon);
                        FrameLayout sonarFrame = new FrameLayout(horizontalScroll.getContext());
                        sonarFrame.setLayoutParams(new FrameLayout.LayoutParams(horizontalScroll.getWidth(), horizontalScroll.getHeight()));
                        LinearLayout sonaroption = (LinearLayout) getLayoutInflater().inflate(R.layout.sonaroption, null);
                        sonaroption.setLayoutParams(new LinearLayout.LayoutParams(horizontalScroll.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT));
                        Spinner sonarOption = (Spinner) sonaroption.findViewById(R.id.sonar_rangeoption);
                        sonarOption.setOnItemSelectedListener(sonar_range_Selected);
                        sonarView.setLayoutParams(new LinearLayout.LayoutParams(horizontalScroll.getWidth(), horizontalScroll.getHeight()));
                        sonarFrame.addView(sonarView);
                        sonarFrame.addView(sonaroption);
                        innerScroll.addView(sonarFrame);
                        ImageView sonar_icon = new ImageView(viewContents.getContext());
                        sonar_icon.setImageResource(R.drawable.sonar);
                        sonar_icon.setLayoutParams(new ViewGroup.LayoutParams(viewContents.getHeight(), viewContents.getHeight()));
                        viewContents.addView(sonar_icon);
                        FrameLayout laserFrame = new FrameLayout(horizontalScroll.getContext());
                        laserFrame.setLayoutParams(new FrameLayout.LayoutParams(horizontalScroll.getWidth(), horizontalScroll.getHeight()));
                        laserView.setLayoutParams(new LinearLayout.LayoutParams(horizontalScroll.getWidth(), horizontalScroll.getHeight()));
                        laserView.setDisplayRangeMode(LaserSensorView.AROUND_ROBOT);
                        laserView.setDiplayMode(LaserSensorView.POINT_CLOUD);
                        LinearLayout laseroption = (LinearLayout) getLayoutInflater().inflate(R.layout.laseroption, null);
                        laseroption.setLayoutParams(new LinearLayout.LayoutParams(horizontalScroll.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT));
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
                        laserFrame.addView(laserView);
                        laserFrame.addView(laseroption);
                        innerScroll.addView(laserFrame);
                        ImageView laser_icon = new ImageView(viewContents.getContext());
                        laser_icon.setImageResource(R.drawable.laser);
                        laser_icon.setLayoutParams(new ViewGroup.LayoutParams(viewContents.getHeight(), viewContents.getHeight()));
                        viewContents.addView(laser_icon);
                        horizontalScroll.removeAllViews();
                        horizontalScroll.addView(innerScroll);
                        horizontalScroll.setOnTouchListener(scrollEvent);
                        addEventForHorrizontalArrows();
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
                        if (!resumeDialog)
                            setPAUSE_STATE(PAUSE_WITH_STOP);
                        else
                            resumeDialog = false;
                    }

                });

                break;
            case RobotController.CONTROLLER_HORIZONTAL_STEER:
            case RobotController.CONTROLLER_HORIZONTAL_DOUBLELEVER:
                sonarView = new SonarSensorView(this, sonarValues, sonarMinAngle, sonarDrawAngle);
                sonarView.setScale(SonarSensorView.AROUND_ROBOT);
                laserView = new LaserSensorView(this) {
                    @Override
                    public void onMaxValChanged(float val) {
                    }
                };
                cameraView = new CameraView(this);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                setContentView(R.layout.robotcontroller_horizontal);
                viewContents = (LinearLayout) findViewById(R.id.view_contents);
                robotNameTxt = (TextView) findViewById(R.id.controllerRobotName);
                velocityDisplayLayout = (LinearLayout) findViewById(R.id.velocity_display_layout);
                leftCtrLayout = (LinearLayout) findViewById(R.id.left_control_layout);
                rightCtrLayout = (LinearLayout) findViewById(R.id.right_control_layout);
                robotNameTxt.setText(robotNameStr);
                userOption = (ImageView) findViewById(R.id.controllerUserOption);
                userOption.setOnClickListener(optionClickListener);
                connectionState = (ImageView) findViewById(R.id.connection_state);
                velocityDisplayer = new VelocityDisplay(RobotController.this);
                verticalScroll = (ScrollView) findViewById(R.id.verticalScroll);
                verticalLeftArrow = (ImageView) findViewById(R.id.verticalLeftArrow);
                verticalRightArrow = (ImageView) findViewById(R.id.verticalRightArrow);
                verticalScroll.setOnTouchListener(scrollEvent);
                verticalScroll.post(new Runnable() {
                    @Override
                    public void run() {
                        velocityDisplayLayout.removeAllViews();
                        velocityDisplayLayout.addView(velocityDisplayer);
                        innerScroll = new LinearLayout(verticalScroll.getContext());
                        innerScroll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        innerScroll.setOrientation(LinearLayout.VERTICAL);
                        if (cameraView.getParent() != null)
                            ((ViewGroup) cameraView.getParent()).removeAllViews();
                        cameraView.setLayoutParams(new LinearLayout.LayoutParams(verticalScroll.getWidth(), verticalScroll.getHeight()));
                        innerScroll.addView(cameraView);
                        ImageView camera_icon = new ImageView(viewContents.getContext());
                        camera_icon.setImageResource(R.drawable.camera);
                        camera_icon.setLayoutParams(new ViewGroup.LayoutParams(viewContents.getHeight(), viewContents.getHeight()));
                        viewContents.addView(camera_icon);
                        sonarView.setLayoutParams(new LinearLayout.LayoutParams(verticalScroll.getWidth(), verticalScroll.getHeight()));
                        innerScroll.addView(sonarView);
                        ImageView sonar_icon = new ImageView(viewContents.getContext());
                        sonar_icon.setImageResource(R.drawable.sonar);
                        sonar_icon.setLayoutParams(new ViewGroup.LayoutParams(viewContents.getHeight(), viewContents.getHeight()));
                        viewContents.addView(sonar_icon);
                        FrameLayout laserFrame = new FrameLayout(verticalScroll.getContext());
                        laserFrame.setLayoutParams(new FrameLayout.LayoutParams(verticalScroll.getWidth(), verticalScroll.getHeight()));
                        laserView.setLayoutParams(new LinearLayout.LayoutParams(verticalScroll.getWidth(), verticalScroll.getHeight()));
                        laserView.setDisplayRangeMode(LaserSensorView.FRONT_OF_ROBOT);
                        laserView.setDiplayMode(LaserSensorView.POINT_CLOUD);
                        final ToggleButton resize = new ToggleButton(verticalScroll.getContext());
                        resize.setChecked(true);
                        resize.setText("resize");
                        resize.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
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
                        laserFrame.addView(laserView);
                        laserFrame.addView(resize);
                        innerScroll.addView(laserFrame);
                        ImageView laser_icon = new ImageView(viewContents.getContext());
                        laser_icon.setImageResource(R.drawable.laser);
                        laser_icon.setLayoutParams(new ViewGroup.LayoutParams(viewContents.getHeight(), viewContents.getHeight()));
                        viewContents.addView(laser_icon);
                        verticalScroll.removeAllViews();
                        verticalScroll.addView(innerScroll);
                        addEventForVerticalArrows();
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
                        if (!resumeDialog)
                            setPAUSE_STATE(PAUSE_WITH_STOP);
                        else
                            resumeDialog = false;
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
        setPAUSE_STATE(PAUSE_WITHOUT_STOP);
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


        CustomPublisher velocityPublisher = new CustomPublisher("mobile_base/commands/velocity",
//        CustomPublisher velocityPublisher = new CustomPublisher("cmd_vel",
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

        CustomSubscriber cameraSubscriber = new CustomSubscriber("camera/rgb/image_raw/compressed", sensor_msgs.CompressedImage._TYPE) {
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

        nodeMainExecutor.execute(androidNode, nodeConfiguration);

    }

}
