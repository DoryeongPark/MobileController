package com.wonikrobotics.ros;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.google.common.base.Preconditions;

import org.ros.android.NodeMainExecutorService;
import org.ros.android.NodeMainExecutorServiceListener;
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

    private final ServiceConnection nodeMainExecutorServiceConnection;
    private boolean serviceConnection = false;
    private URI MasterUri;
    protected CustomNodeMainExecutorService nodeMainExecutorService;
    private boolean is_Master = false;

    private final class NodeMainExecutorServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            serviceConnection = true;
            nodeMainExecutorService = ((CustomNodeMainExecutorService.LocalBinder) binder).getService();
            nodeMainExecutorService.addListener(new CustomNodeMainExecutorServiceListener() {
                @Override
                public void onShutdown(CustomNodeMainExecutorService nodeMainExecutorService) {
                    if ( !isFinishing() ) {
                        CustomRosActivity.this.finish();
                    }
                }
            });
            nodeMainExecutorService.setMasterUri(MasterUri);
            if(is_Master) {
                AsyncTask<Void, Void, URI> task = new AsyncTask<Void, Void, URI>() {
                    @Override
                    protected URI doInBackground(Void... params) {
                        CustomRosActivity.this.nodeMainExecutorService.startMaster();
                        return CustomRosActivity.this.nodeMainExecutorService.getMasterUri();
                    }
                };
                task.execute();
                try {
                    CustomRosActivity.this.MasterUri=task.get();
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
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("service disconnected",name.toString());
            serviceConnection = false;
        }
    };

    protected CustomRosActivity() {
        super();
        nodeMainExecutorServiceConnection = new NodeMainExecutorServiceConnection();
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
        Intent intent = new Intent(this, CustomNodeMainExecutorService.class);
        intent.setAction(CustomNodeMainExecutorService.ACTION_START);
        startService(intent);
        Preconditions.checkState(
                bindService(intent, nodeMainExecutorServiceConnection, BIND_AUTO_CREATE),
                "Failed to bind NodeMainExecutorService.");

    }
    @Override
    public void onPause(){
        try{
            AsyncTask<Void,Void,Void> task = new AsyncTask<Void, Void, Void>() {

                @Override
                protected void onPreExecute(){
                    super.onPreExecute();
                }
                @Override
                protected Void doInBackground(Void... params) {
                    disConnect();
                    return null;
                }
                @Override
                protected void onPostExecute(Void result){
                    super.onPostExecute(result);

                }
            };
            task.execute();
        }catch(Exception e){
            Log.e("timeout","success");
        }
        super.onPause();
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

}