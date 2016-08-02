package com.wonikrobotics.ros;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
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
public abstract class CustomRosActivity  extends Activity {

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
    private URI MasterUri;
    private boolean is_Master = false;


    protected CustomRosActivity() {
        super();
        nodeMainExecutorServiceConnection = new NodeMainExecutorServiceConnection();
    }

    public int getState() {
        return this.STATE;
    }

    protected void setURI(String uri,boolean master){
        try {
            CustomRosActivity.this.MasterUri = new URI(uri);
            CustomRosActivity.this.is_Master = master;
        }catch (URISyntaxException e) {
            throw new RosRuntimeException(e);
        }
        startNodeMainExecutorService();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void startNodeMainExecutorService() {
        this.STATE = STATE_CONNECTING;
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
            try {
                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        STATE = STATE_UNREGISTERING;
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
        PAUSE_STATE = PAUSE_WITH_STOP;
        if (STATE == STATE_UNREGISTERING) {
            Toast.makeText(this, "steel unregistering on master", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void setPAUSE_STATE(int state) {
        PAUSE_STATE = state;
        Log.e("set pause state", String.valueOf(PAUSE_STATE));
    }

    public void disConnect(){
        if (nodeMainExecutorService != null) {
            nodeMainExecutorService.shutdown();
            if (this.serviceConnection) {
                try {
                    unbindService(nodeMainExecutorServiceConnection);
                }catch (Exception e){
                    Log.e("error","on disconnect of rosactivity");
                }
            }

            // NOTE(damonkohler): The activity could still be restarted. In that case,
            // nodeMainExectuorService needs to be null for everything to be started
            // up again.
            nodeMainExecutorService = null;
        }
    }

    /**
     * This method is called in a background thread once this {@link Activity} has
     * been initialized with a master {@link URI} via the
     * * and a {@link NodeMainExecutorService} has started. Your {@link NodeMain}s
     * should be started here using the provided {@link NodeMainExecutor}.
     *
     * @param nodeMainExecutor
     *          the {@link NodeMainExecutor} created for this {@link Activity}
     */
    protected abstract void init(NodeMainExecutor nodeMainExecutor);

    public URI getMasterUri() {
        Preconditions.checkNotNull(nodeMainExecutorService);
        return nodeMainExecutorService.getMasterUri();
    }

    private final class NodeMainExecutorServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            serviceConnection = true;
            nodeMainExecutorService = ((CustomNodeMainExecutorService.LocalBinder) binder).getService();
            nodeMainExecutorService.addListener(new CustomNodeMainExecutorServiceListener() {
                @Override
                public void onShutdown(CustomNodeMainExecutorService nodeMainExecutorService) {
                    if (!isFinishing()) {
                        CustomRosActivity.this.finish();
                    }
                }
            });
            nodeMainExecutorService.setMasterUri(MasterUri);
            if (is_Master) {
                AsyncTask<Void, Void, URI> task = new AsyncTask<Void, Void, URI>() {
                    @Override
                    protected URI doInBackground(Void... params) {
                        CustomRosActivity.this.nodeMainExecutorService.startMaster();
                        return CustomRosActivity.this.nodeMainExecutorService.getMasterUri();
                    }
                };
                task.execute();
                try {
                    CustomRosActivity.this.MasterUri = task.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    CustomRosActivity.this.init(nodeMainExecutorService);
                    return null;
                }
            }.execute();
            STATE = STATE_CONNECTED;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("service disconnected", name.toString());
            serviceConnection = false;
            STATE = STATE_DISCONNECTED;
        }
    }

}