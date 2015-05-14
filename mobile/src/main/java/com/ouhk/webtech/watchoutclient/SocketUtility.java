package com.ouhk.webtech.watchoutclient;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;

/**
 * Created by Jacky Li on 4/2/2015.
 */
public class SocketUtility extends WearableListenerService implements DataApi.DataListener, GoogleApiClient.ConnectionCallbacks {
    private static final String START_SERVICE_PATH = "/start-service";
    private static final String STOP_SERVICE_PATH = "/stop-service";
    private final String logCatTag = "mobileLogCat";
    private final Runnable runnable;
    private GoogleApiClient mGoogleApiClient;
    private String timeStamp;
    private SocketData socketData = new SocketData();
    private TelephonyManager manager;
    private StatePhoneReceiver myPhoneStateListener;
    private boolean callFromApp = false; // To control the call has been made from the application
    private boolean callFromOffHook = false; // To control the change to idle state is from the app call

    {
        runnable = new Runnable() {
            @Override
            public void run() {
                // mSocket.connect();
                try {
                    getSocketConnection();
                } catch (Exception e) {
                    Log.d(logCatTag, e.toString());
                    getSocketConnection();
                }
            }
        };
    }

    private static void writeFile(String str, File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
            buf.append(str);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        Log.d(logCatTag, "onCreate method invoked");
        Log.d(logCatTag, "The new Service was Created");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(logCatTag, "onStartCommand method invoked");
        Log.d(logCatTag, "Service Started");
        //the socket thread start
        new Thread(runnable).start();
        //the google api invoke
        initGoogleApi();
        myPhoneStateListener = new StatePhoneReceiver(this);
        manager = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
        //android wear connected

        return START_STICKY;
    }

    //Socket connection method
    private void getSocketConnection() {
        try {
            SocketData.setServerIP();
            SocketData.setServerPort();
            SocketData.setSocketObj(new java.net.Socket());
            SocketData.setSocketTimeout();
            InetAddress serverAddr = InetAddress.getByName(SocketData.getServerIP());
            SocketAddress sc_add = new InetSocketAddress(serverAddr, SocketData.getServerPort());
            SocketData.getSocketObj().connect(sc_add, SocketData.getSocketTimeout());
            SocketData.setOutStream(SocketData.getSocketObj().getOutputStream());
            SocketData.setDataOut(new DataOutputStream(SocketData.getSocketObj().getOutputStream()));
            Log.d(logCatTag, "Socket Running");

        } catch (IOException e1) {
            e1.printStackTrace();
            Log.d(logCatTag, e1.toString());
        }
    }

    @Override
    public void onDestroy() {
        try {
            Log.d(logCatTag, "Service Destroyed");
            if (SocketData.getDataOut() != null) {
                SocketData.getDataOut().close();
            }
            if (SocketData.getOutStream() != null) {
                SocketData.getOutStream().close();
            }
            if (SocketData.getSocketObj() != null) {
                SocketData.getSocketObj().close();
            }
            WearableService(false);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(logCatTag, e.toString());
        }

    }

    private void initGoogleApi() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(logCatTag, "onConnectionFailed: " + result);
                    }
                })
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(logCatTag, "DataIncome");
        /* Sample Json Struture
                [[{"device":"1"}],
                [{"id":"aData","tStamp":"12345678","aX":"0.45644333","aY":"-2.333333123","aZ":"-9.474747744","status":"Fall Decteced"},
                {"id":"aData","tStamp":"12345678","aX":"0.45644333","aY":"-2.333333123","aZ":"-9.474747744","status":"Fall Decteced"}]]
                */
        for (DataEvent event : dataEvents) {
            try {
                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                String watchMotionDataObj = dataMapItem.getDataMap().getString("Data");

                Log.d(logCatTag, "Length " + Integer.toString(watchMotionDataObj.length()));
                Log.d(logCatTag, "Data " + watchMotionDataObj);
                JSONObject deviceJson = new JSONArray(watchMotionDataObj).getJSONObject(0);
                JSONObject dataJson = new JSONArray(watchMotionDataObj).getJSONObject(1);
                String latitude = "";
                String longitude = "";
                String status = "";
                String tStamp;
                //tStampArray = DataJson.getJSONArray("tStamp");
                //statusArray = DataJson.getJSONArray("status");;
                //String status = statusArray.getString(0);
                //Log.d(logCatTag,status);
                int func = Integer.parseInt(deviceJson.getString("func"));
                Log.d(logCatTag, Integer.toString(func));

                switch (func) {
                    case 1:
                        Log.d(logCatTag, "Case 1 Entry");
                        try {
                            Log.d(logCatTag, "Case 1 try_block");
                            Boolean callEmergency = Boolean.parseBoolean(dataJson.getString("callEmergency"));
                            latitude = dataJson.getString("latitude");
                            longitude = dataJson.getString("longitude");
                            status = dataJson.getString("status");
                            tStamp = dataJson.getString("tStamp");
                            Log.d(CommonData.LOGCATAG, "Status: " + status);
                            Log.d(CommonData.LOGCATAG, "Emergency: " + callEmergency.toString());
                            Log.d(CommonData.LOGCATAG, "ts True: " + tStamp + (!Objects.equals(tStamp, timeStamp)) + timeStamp);
                            SharedPreferences CallEmergencyPref = getSharedPreferences("CallEmergency", Activity.MODE_PRIVATE);
                            if (!Objects.equals(tStamp, timeStamp)) {
                                File accelerometerFile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), "aData.txt");
                                writeFile(watchMotionDataObj, accelerometerFile);
                                //displayNotification(status);
                                Intent intentCall = new Intent(Intent.ACTION_CALL);


                                if (callEmergency) {
                                    intentCall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intentCall.setData(Uri.parse("tel:98255575"));
                                    startActivity(intentCall);
                                    manager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE); // start listening to the phone changes
                                    callFromApp = true;
                                }
                                SocketData.getDataOut().write((watchMotionDataObj + "\r").getBytes("UTF-8"));
                                timeStamp = tStamp;
                            }

                        } catch (Exception e) {
                            Log.e(CommonData.LOGCATAG, "Exception: " + e.toString());
                            Log.d(CommonData.LOGCATAG, "Catch Status: " + status);
                            if (status.equalsIgnoreCase("Fall detected!")) {
                                sendSMS(latitude, longitude);
                            }

                        }

                }


            } catch (JSONException e) {
                Log.e(CommonData.LOGCATAG, e.toString());
            }
        }

    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        WearableService(true);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("Tag", "WatchDissconnected");
    }

    private void sendSMS(String lat, String lng) {
        String phoneNo = "+85265773427";
        String warningMSG = "It appears that Emma has Fellen and may require assistance. Her current location is: " + "http://www.google.com/maps?q=" + lat + "," + lng;
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, warningMSG, null, null);
            Toast.makeText(getApplicationContext(), "SMS sent.",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "SMS faild, please try again.",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void WearableService(boolean state) {
        if (state) {
            Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(
                    new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                        @Override
                        public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                            for (final Node node : getConnectedNodesResult.getNodes()) {
                                Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(),
                                        START_SERVICE_PATH, new byte[0]).setResultCallback(
                                        getSendMessageResultCallback());
                            }
                        }
                    });
        } else {
            Log.d(CommonData.LOGCATAG, "abc");
            Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(
                    new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                        @Override
                        public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                            for (final Node node : getConnectedNodesResult.getNodes()) {
                                Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(),
                                        STOP_SERVICE_PATH, new byte[0]).setResultCallback(
                                        getSendMessageResultCallback());
                            }
                        }
                    });
        }
    }

    private ResultCallback<MessageApi.SendMessageResult> getSendMessageResultCallback() {
        return new ResultCallback<MessageApi.SendMessageResult>() {
            @Override
            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                if (!sendMessageResult.getStatus().isSuccess()) {
                    Log.e(CommonData.LOGCATAG, "Failed to connect to Google Api Client with status "
                            + sendMessageResult.getStatus());
                }
            }
        };
    }

    public class StatePhoneReceiver extends PhoneStateListener {
        final Context context;

        public StatePhoneReceiver(Context context) {
            this.context = context;
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            Log.d(CommonData.LOGCATAG, "onCAll:" + Integer.toString(state));
            switch (state) {

                case TelephonyManager.CALL_STATE_OFFHOOK: //Call is established
                    if (callFromApp) {
                        callFromApp = false;
                        callFromOffHook = true;

                        try {
                            Thread.sleep(500); // Delay 0,5 seconds to handle better turning on loudspeaker
                        } catch (InterruptedException ignored) {
                        }

                        //Activate loudspeaker
                        AudioManager audioManager = (AudioManager)
                                context.getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setMode(AudioManager.MODE_IN_CALL);
                        audioManager.setSpeakerphoneOn(true);
                    }
                    break;

                case TelephonyManager.CALL_STATE_IDLE: //Call is finished
                    if (callFromOffHook) {
                        callFromOffHook = false;
                        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setMode(AudioManager.MODE_NORMAL); //Deactivate loudspeaker
                        manager.listen(myPhoneStateListener, // Remove listener
                                PhoneStateListener.LISTEN_NONE);
                    }
                    break;
            }
        }
    }


}
