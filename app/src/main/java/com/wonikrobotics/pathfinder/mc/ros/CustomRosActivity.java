package com.wonikrobotics.pathfinder.mc.ros;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.Preconditions;

import org.ros.android.NodeMainExecutorService;
import org.ros.exception.RosRuntimeException;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

/**
 * Created by Notebook on 2016-08-01.
 */
public abstract class CustomRosActivity extends Activity {

    public static final int PAUSE_WITHOUT_STOP = 5;
    public static final int PAUSE_WITH_STOP = 4;
    public static final int STATE_CONNECTED = 1;
    public static final int STATE_UNREGISTERING = 2;
    public static final int STATE_CONNECTING = 3;
    public static final int STATE_DISCONNECTED = 0;

    private final ServiceConnection nodeMainExecutorServiceConnection;
    public int STATE = 0;
    public int PAUSE_STATE = 4;
    protected CustomNodeMainExecutorService nodeMainExecutorService;
    private boolean serviceConnection = false;
    private URI MasterUri = null;
    private boolean is_Master = false;
    private IBinder bundleBinder = null;


    protected CustomRosActivity() {
        super();
        nodeMainExecutorServiceConnection = new NodeMainExecutorServiceConnection();
    }

    public int getState() {
        return this.STATE;
    }

    protected void setURI(String uri, boolean master) {
        Log.e("CustomRosActivity", "set URI" + uri);
        try {
            CustomRosActivity.this.MasterUri = new URI(uri);
            CustomRosActivity.this.is_Master = master;
        } catch (URISyntaxException e) {
            throw new RosRuntimeException(e);
        }
        startNodeMainExecutorService();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private boolean checkRunningServices() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.wonikrobotics.pathfinder.mc.ros.CustomNodeMainExecutorService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    private void startNodeMainExecutorService() {
        Intent intent = new Intent(this, CustomNodeMainExecutorService.class);
        intent.setAction(CustomNodeMainExecutorService.ACTION_START);
        startService(intent);
        Preconditions.checkState(
                bindService(intent, nodeMainExecutorServiceConnection, BIND_AUTO_CREATE),
                "Failed to bind NodeMainExecutorService.");

    }

    @Override
    public void onPause() {
        Log.e("on pause state", String.valueOf(PAUSE_STATE));
        if (PAUSE_STATE == PAUSE_WITH_STOP) {
            Log.e("on pause state", "in pause with stop");
            try {
                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        STATE = STATE_UNREGISTERING;
                        onStateChangeListener(STATE);
                    }

                    @Override
                    protected Void doInBackground(Void... params) {
                        disConnect();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        super.onPostExecute(result);
                        STATE = STATE_DISCONNECTED;
                        onStateChangeListener(STATE);
                    }
                };
                task.execute();
            } catch (Exception e) {
                Log.e("timeout", "success");
            }

        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("resume pause state", String.valueOf(PAUSE_STATE));
        setPAUSE_STATE(PAUSE_WITH_STOP);
        if (STATE == STATE_UNREGISTERING) {
            Toast.makeText(this, "steel unregistering on master", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("CONNECTION", serviceConnection);
        outState.putBinder("BINDER", bundleBinder);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceConnection = savedInstanceState.getBoolean("CONNECTION", false);
        bundleBinder = savedInstanceState.getBinder("BINDER");
    }

    public int getPAUSE_STATE() {
        return this.PAUSE_STATE;
    }

    public void setPAUSE_STATE(int state) {
        PAUSE_STATE = state;
        Log.e("set pause state", String.valueOf(PAUSE_STATE));
    }

    public boolean getIs_Master() {
        return this.is_Master;
    }

    public int getStateConnect() {
        return this.STATE;
    }

    public void setStateConnect(int state) {
        this.STATE = state;
    }

    public void disConnect() {
        if (nodeMainExecutorService != null) {
            nodeMainExecutorService.shutdown();
            if (this.serviceConnection) {
                try {
                    unbindService(nodeMainExecutorServiceConnection);
                } catch (Exception e) {
                    Log.e("error", "on disconnect of rosactivity");
                }
            }

            // NOTE(damonkohler): The activity could still be restarted. In that case,
            // nodeMainExectuorService needs to be null for everything to be started
            // up again.
            nodeMainExecutorService = null;
            MasterUri = null;
            serviceConnection = false;
        }
    }

    /**
     * This method is called in a background thread once this {@link Activity} has
     * been initialized with a master {@link URI} via the
     * * and a {@link NodeMainExecutorService} has started. Your {@link NodeMain}s
     * should be started here using the provided {@link NodeMainExecutor}.
     *
     * @param nodeMainExecutor the {@link NodeMainExecutor} created for this {@link Activity}
     */
    protected abstract void init(NodeMainExecutor nodeMainExecutor);

    protected abstract void onStateChangeListener(int state);

    public URI getMasterUri() {
        Preconditions.checkNotNull(nodeMainExecutorService);
        return nodeMainExecutorService.getMasterUri();
    }

    private final class NodeMainExecutorServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            if (bundleBinder == null)
                bundleBinder = binder;
            else
                Log.e("BUNDLE", Boolean.toString(bundleBinder.pingBinder()));
            nodeMainExecutorService = ((CustomNodeMainExecutorService.LocalBinder) bundleBinder).getService();
            nodeMainExecutorService.addListener(new CustomNodeMainExecutorServiceListener() {
                @Override
                public void onShutdown(CustomNodeMainExecutorService nodeMainExecutorService) {
                    if (!isFinishing()) {
                        CustomRosActivity.this.finish();
                    }
                }
            });
            nodeMainExecutorService.setMasterUri(MasterUri);
            if (is_Master && !serviceConnection) {
                if (!nodeMainExecutorService.hasMaster()) {
                    try {
                        AsyncTask<Void, Void, URI> task = new AsyncTask<Void, Void, URI>() {
                            @Override
                            protected URI doInBackground(Void... params) {
                                CustomRosActivity.this.nodeMainExecutorService.startMaster();
                                return CustomRosActivity.this.nodeMainExecutorService.getMasterUri();
                            }
                        };
                        task.execute();
                        CustomRosActivity.this.MasterUri = task.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    CustomRosActivity.this.init(nodeMainExecutorService);
                    return null;
                }
            }.execute();
            serviceConnection = true;
            STATE = STATE_CONNECTING;
            onStateChangeListener(STATE);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("service disconnected", name.toString());
        }
    }

}