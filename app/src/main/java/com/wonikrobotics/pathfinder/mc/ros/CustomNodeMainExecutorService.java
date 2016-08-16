package com.wonikrobotics.pathfinder.mc.ros;

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


    static final String ACTION_START = "com.wonikrobotics.pathfinder.mc.ros.ACTION_START_NODE_RUNNER_SERVICE";
    static final String ACTION_SHUTDOWN = "com.wonikrobotics.pathfinder.mc.ros.ACTION_SHUTDOWN_NODE_RUNNER_SERVICE";
    private static final String TAG = "NodeMainExecutorService";
    private final NodeMainExecutor nodeMainExecutor;
    private final IBinder binder;
    private final ListenerGroup<CustomNodeMainExecutorServiceListener> listeners;

    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;
    private RosCore rosCore = null;
    private URI masterUri;

    public CustomNodeMainExecutorService() {
        super();
        /*
            Create new DefaultNodeMainExecutor on constructor.
            DefaultNodeMainExecutor is on rosjava-0.2.1
         */
        nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
        binder = new LocalBinder();
        listeners =
                new ListenerGroup<CustomNodeMainExecutorServiceListener>(
                        nodeMainExecutor.getScheduledExecutorService());
    }

    @Override
    public void onCreate() {
        /*
            wake lock
            wifi lock
         */
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
                        Collection<NodeListener> nodeListeners) {
        nodeMainExecutor.execute(nodeMain, nodeConfiguration, nodeListeners);
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
        } catch (org.ros.internal.node.xmlrpc.XmlRpcTimeoutException e) {
            Log.e("error", "on shutdown");
            e.printStackTrace();
        }
        if (masterUri != null)
            masterUri = null;
        if (rosCore != null) {
            rosCore.shutdown();
            rosCore = null;
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

    public boolean hasMaster() {
        return rosCore != null;
    }

    public URI getMasterUri() {
        return this.masterUri;
    }

    public void setMasterUri(URI uri) {
        this.masterUri = uri;
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


    @Deprecated
    public void startMaster() {
        /*
            Start new master node on roscore.
         */
        rosCore = RosCore.newPublic(11311);
        try {
            rosCore.start();
            rosCore.awaitStart();
        } catch (Exception e) {
            throw new RosRuntimeException(e);
        }

        masterUri = rosCore.getUri();
    }

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    class LocalBinder extends Binder {
        CustomNodeMainExecutorService getService() {
            return CustomNodeMainExecutorService.this;
        }
    }
}