package com.ouhk.webtech.watchoutclient;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Set;

/**
 * Created by Ravic on 3/4/15.
 */

public class Fall_DetectionService extends Service implements SensorEventListener,
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, MessageApi.MessageListener,
        CapabilityApi.CapabilityListener {
    private static final String CONFIRMATION_HANDLER_CAPABILITY_NAME = "confirmation_handler";
    private final DtwData dtwData = new DtwData();
    private final DtwUtility dtwUtility = new DtwUtility();
    private final AnnUtility annUtility = new AnnUtility();
    private final AnnData annData = new AnnData();
    private float[] accelerometerValues = new float[3];
    private SensorManager SensorManager;
    private GoogleApiClient client;
    private AssetManager assetMgr;
    private Handler mHandler;

    public void onCreate() {
        super.onCreate();

        HandlerThread mSensorThread = new HandlerThread("sensor_thread");
        mSensorThread.start();
        mHandler = new Handler(mSensorThread.getLooper());
        SensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor aSensor = SensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        assetMgr = getAssets();
        if (aSensor != null) {
            SensorManager.registerListener(this, aSensor, android.hardware.SensorManager.SENSOR_DELAY_UI, mHandler);
            client = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addApi(Wearable.API)  // used for data layer API
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            client.connect();

            Log.d(CommonData.LOGCATAG, "Connection: " + Boolean.toString(client.isConnected()));
            Log.d(CommonData.LOGCATAG, "The Fall detection service is Running");
            Toast.makeText(getApplication(), "The Fall detection service is Running", Toast.LENGTH_LONG).show();
        }
    }

    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(client, this);
        setupConfirmationHandlerNode();
        invokeANNService();
        // Vibrator myVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
        // myVibrator.vibrate(new long[]{1000, 500, 1000, 500, 1000, 500}, -1);
        Log.d(CommonData.LOGCATAG, "onConnected: " + client.isConnected());
        Toast.makeText(getApplication(), "onConnected: " + client.isConnected(), Toast.LENGTH_SHORT).show();
    }

    public void onConnectionSuspended(int i) {
        Log.d(CommonData.LOGCATAG, "onConnectionSuspended: " + i);
        Toast.makeText(getApplication(), "onConnectionSuspended: " + i, Toast.LENGTH_SHORT).show();

    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(CommonData.LOGCATAG, "Connection failed");
        Toast.makeText(getApplication(), "Connection failed", Toast.LENGTH_SHORT).show();
    }

    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(CommonData.LOGCATAG, "DataIncome");

        for (DataEvent event : dataEvents)
            try {
                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                String watchMotionDataObj = dataMapItem.getDataMap().getString("Data");
                Log.d(CommonData.LOGCATAG, "Length " + Integer.toString(watchMotionDataObj.length()));
                JSONArray json = new JSONArray(watchMotionDataObj);
                for (int i = 0; i < json.length(); i++) {
                    Log.d(CommonData.LOGCATAG, watchMotionDataObj);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
    }

    public void onAccuracyChanged(Sensor paramSensor, int paramInt) {
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            accelerometerValues = event.values;
        mHandler.post(new fallDetect());

    }

    public void onLocationChanged(Location location) {
        annData.setLastLocation(location);
    }

    public void onMessageReceived(MessageEvent messageEvent) {

    }

    private void setupConfirmationHandlerNode() {
        Wearable.CapabilityApi.addCapabilityListener(
                client, this, CONFIRMATION_HANDLER_CAPABILITY_NAME);

        Wearable.CapabilityApi.getCapability(
                client, CONFIRMATION_HANDLER_CAPABILITY_NAME,
                CapabilityApi.FILTER_REACHABLE).setResultCallback(
                new ResultCallback<CapabilityApi.GetCapabilityResult>() {
                    @Override
                    public void onResult(CapabilityApi.GetCapabilityResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Log.e(CommonData.LOGCATAG, "setupConfirmationHandlerNode() Failed to get capabilities, "
                                    + "status: " + result.getStatus().getStatusMessage());
                            return;
                        }
                        updateConfirmationCapability(result.getCapability());
                    }
                });
    }

    private void updateConfirmationCapability(CapabilityInfo capabilityInfo) {
        Set<Node> connectedNodes = capabilityInfo.getNodes();
        Node mConfirmationHandlerNode;
        if (connectedNodes.isEmpty()) {
            Log.d(CommonData.LOGCATAG, "Node is Empty");
        } else {
            Log.d(CommonData.LOGCATAG, "Node Connected");
            mConfirmationHandlerNode = pickBestNode(connectedNodes);
        }
    }

    private Node pickBestNode(Set<Node> connectedNodes) {
        Node best = null;
        if (connectedNodes != null) {
            for (Node node : connectedNodes) {
                if (node.isNearby()) {
                    return node;
                }
                best = node;
            }
        }
        return best;
    }

    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
        updateConfirmationCapability(capabilityInfo);
    }

    private void invokeANNService() {
        annData.setWearConnectionState();
        annData.setGMS(client);
        annData.setAnnUtilityContext(getApplicationContext());
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(60000)
                .setFastestInterval(30000);
        LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        Location LastLocation = LocationServices.FusedLocationApi.getLastLocation(client);
        if (LastLocation != null) {
            annData.setLastLocation(LastLocation);
        }
        StartANN(annUtility, annData, dtwData);
    }

    private void StartANN(AnnUtility annUtility, AnnData annData, DtwData dtwData) {
        Log.d(CommonData.LOGCATAG, "ANN START......");
        annUtility.GoNormal(annData);
    }

    private void StopANN(AnnUtility annUtility, AnnData annData) {
        Log.d(CommonData.LOGCATAG, "ANN STOP......");
        annUtility.GoNormal(annData);
        SensorManager.unregisterListener(this);
        client.disconnect();
    }

    private void sendMessage(final String message) {
        PutDataMapRequest dataMap = PutDataMapRequest.create("/SensorData");
        dataMap.getDataMap().putString("Data", message);
        PutDataRequest request = dataMap.asPutDataRequest();
        Wearable.DataApi.putDataItem(client, request);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(CommonData.LOGCATAG, "onStartCommand method invoked");

        return START_STICKY;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        StopANN(annUtility, annData);

    }

    private class fallDetect implements Runnable {
        @Override
        public void run() {
            try {
                annData.setAnnUtilityContext(getApplicationContext());
                annUtility.getAccelerometerData(accelerometerValues, annData, dtwUtility, dtwData, assetMgr);
                if (annData.getSentaDataJSON()) {
                    sendMessage(annData.getaData());
                    annData.setaData("");
                    annData.setSentaDataJSON(false);
                }

            } catch (JSONException e1) {
                e1.printStackTrace();
            }

        }
    }


}
