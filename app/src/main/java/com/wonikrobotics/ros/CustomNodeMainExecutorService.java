package com.wonikrobotics.ros;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import org.ros.RosCore;
import org.ros.concurrent.ListenerGroup;
import org.ros.concurrent.SignalRunnable;
import org.ros.exception.RosRuntimeException;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeListener;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;

import java.net.URI;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by Notebook on 2016-08-01.
 */
public class CustomNodeMainExecutorService extends Service implements NodeMainExecutor {

    private static final String TAG = "NodeMainExecutorService";


    static final String ACTION_START = "com.wonikrobotics.ros.ACTION_START_NODE_RUNNER_SERVICE";
    static final String ACTION_SHUTDOWN = "com.wonikrobotics.ros.ACTION_SHUTDOWN_NODE_RUNNER_SERVICE";

    private final NodeMainExecutor nodeMainExecutor;
    private final IBinder binder;
    private final ListenerGroup<CustomNodeMainExecutorServiceListener> listeners;

    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;
    private RosCore rosCore;
    private URI masterUri;

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    class LocalBinder extends Binder {
        CustomNodeMainExecutorService getService() {
            return CustomNodeMainExecutorService.this;
        }
    }

    public CustomNodeMainExecutorService() {
        super();
        nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
        binder = new LocalBinder();
        listeners =
                new ListenerGroup<CustomNodeMainExecutorServiceListener>(
                        nodeMainExecutor.getScheduledExecutorService());
    }

    @Override
    public void onCreate() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wakeLock.acquire();
        int wifiLockType = WifiManager.WIFI_MODE_FULL;
        try {
            wifiLockType = WifiManager.class.getField("WIFI_MODE_FULL_HIGH_PERF").getInt(null);
        } catch (Exception e) {
            // We must be running on a pre-Honeycomb device.
            Log.w(TAG, "Unable to acquire high performance wifi lock.");
        }
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock(wifiLockType, TAG);
        wifiLock.acquire();
    }

    @Override
    public void execute(NodeMain nodeMain, NodeConfiguration nodeConfiguration,
                        Collection<NodeListener> nodeListeneners) {
        nodeMainExecutor.execute(nodeMain, nodeConfiguration, nodeListeneners);
    }

    @Override
    public void execute(NodeMain nodeMain, NodeConfiguration nodeConfiguration) {
        execute(nodeMain, nodeConfiguration, null);
    }

    @Override
    public ScheduledExecutorService getScheduledExecutorService() {
        return nodeMainExecutor.getScheduledExecutorService();
    }

    @Override
    public void shutdownNodeMain(NodeMain nodeMain) {
        nodeMainExecutor.shutdownNodeMain(nodeMain);
    }

    @Override
    public void shutdown() {
        signalOnShutdown();
        // NOTE(damonkohler): This may be called multiple times. Shutting down a
        // NodeMainExecutor multiple times is safe. It simply calls shutdown on all
        // NodeMains.
        try {
            nodeMainExecutor.shutdown();
        }catch (org.ros.internal.node.xmlrpc.XmlRpcTimeoutException e){
            Log.e("error","on shutdown");
            e.printStackTrace();
        }
        if (rosCore != null) {
            rosCore.shutdown();
        }
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
        if (wifiLock.isHeld()) {
            wifiLock.release();
        }
        stopForeground(true);
        stopSelf();
    }

    public void addListener(CustomNodeMainExecutorServiceListener listener) {
        listeners.add(listener);
    }



    private void signalOnShutdown() throws org.ros.internal.node.xmlrpc.XmlRpcTimeoutException {
        listeners.signal(new SignalRunnable<CustomNodeMainExecutorServiceListener>() {
            @Override
            public void run(CustomNodeMainExecutorServiceListener nodeMainExecutorServiceListener) {
                nodeMainExecutorServiceListener.onShutdown(CustomNodeMainExecutorService.this);
            }
        });

    }

    @Override
    public void onDestroy() {
        shutdown();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() == null) {
            return START_NOT_STICKY;
        }
        if (intent.getAction().equals(ACTION_START)) {
        }
        if (intent.getAction().equals(ACTION_SHUTDOWN)) {
            shutdown();
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public URI getMasterUri() {
        return masterUri;
    }

    public void setMasterUri(URI uri) {
        masterUri = uri;
    }

    @Deprecated
    public void startMaster() {
        rosCore = RosCore.newPublic(11311);
        rosCore.start();
        try {
            rosCore.awaitStart();
        } catch (Exception e) {
            throw new RosRuntimeException(e);
        }
        masterUri = rosCore.getUri();
    }
}