package com.ouhk.webtech.watchoutclient;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;


/**
 * Created by Jacky Li on 20/4/2015.
 */

class AnnUtility {
    //public static String file_dir = getAssets()+"MotionTemplate";
    private static final String walk_sample_x = "aData(walking)_x_";
    private static final String walk_sample_y = "aData(walking)_y_";
    private static final String walk_sample_z = "aData(walking)_z_";
    private static final String fall_sample_x = "aData(FrontFall)_x_";
    private static final String fall_sample_y = "aData(FrontFall)_y_";
    private static final String fall_sample_z = "aData(FrontFall)_z_";
    private final float[] Ax = new float[100];
    private final float[] Ay = new float[100];
    private final float[] Az = new float[100];
    private final double mFTime = 60000.0D;
    private float mAccuracy = 1000.0F;
    private double mAge = 40.0D;
    private int mCunter = 0;
    private long mFallTime;
    private double mHeight = 165.0D;
    private String mHerHis;
    private boolean mIsFall = false;
    private boolean mIsPrimaryFall = false;
    private String mLocationText = "";
    private long mPrimaryFallTime;
    private double mSex = -1.0D;
    private long mSysTime;
    private double mWeight = 65.0D;
    private JSONObject watchMotionData = new JSONObject();
    private JSONObject watchMotionDevice = new JSONObject();
    private JSONArray watchMotionDataObj = new JSONArray();
    private double mLongitude;
    private double mLatitude;
    private String mM;

    private static void getResult(DtwUtility dtwUtility, DtwData dtwData) {
        String result = dtwUtility.evaluate_result(dtwData.getFinal_walk_dist(), dtwData.getFinal_fall_dist());
        Log.d(CommonData.LOGCATAG, "mean_walk: " + dtwData.getFinal_walk_dist());
        Log.d(CommonData.LOGCATAG, "mean_fall: " + dtwData.getFinal_fall_dist());
        Log.d(CommonData.LOGCATAG, "result: " + result);
        dtwData.setDTWResult(result);
        dtwData.setSeq2_x(null);
        dtwData.setSeq2_y(null);
        dtwData.setSeq2_z(null);
    }

    private static void getFinalDistance(DtwUtility dtwUtility, DtwData dtwData) {
        dtwData.setFinal_walk_dist(dtwUtility.mean_distance(dtwData.getWalk_distance()));
        dtwData.setFinal_fall_dist(dtwUtility.mean_distance(dtwData.getFall_distance()));
    }

    private static void prepareTemplate(DtwUtility dtwUtility, DtwData dtwData, AssetManager assetMgr) {
        dtwData.setWalk_data_x(dtwUtility.scan_data(assetMgr, walk_sample_x));
        dtwData.setWalk_data_y(dtwUtility.scan_data(assetMgr, walk_sample_y));
        dtwData.setWalk_data_z(dtwUtility.scan_data(assetMgr, walk_sample_z));

        dtwData.setFall_data_x(dtwUtility.scan_data(assetMgr, fall_sample_x));
        dtwData.setFall_data_y(dtwUtility.scan_data(assetMgr, fall_sample_y));
        dtwData.setFall_data_z(dtwUtility.scan_data(assetMgr, fall_sample_z));
    }

    private static void prepareSample(float[] Ax, float[] Ay, float[] Az, DtwData dtwData) {
        dtwData.setAx(Ax);
        dtwData.setAy(Ay);
        dtwData.setAz(Az);
    }

    private static void fallDetection(DtwUtility dtwUtility, DtwData dtwData) {
        Double[] TMPfall_distance = new Double[dtwData.getFall_data_x().size()];
        for (int i = 0; i < dtwData.getFall_data_x().size(); i++) {

            /* Loop through each template and assign to global. */
            dtwData.setSeq1_x(dtwData.getFall_data_x().get(i).toArray(new Float[dtwData.getFall_data_x().size()]));
            dtwData.setSeq1_y(dtwData.getFall_data_y().get(i).toArray(new Float[dtwData.getFall_data_y().size()]));
            dtwData.setSeq1_z(dtwData.getFall_data_z().get(i).toArray(new Float[dtwData.getFall_data_z().size()]));

            /* Assign input data for seq2 then compare. */
            dtwData.setSeq2_x(dtwData.getAx());
            dtwData.setSeq2_y(dtwData.getAy());
            dtwData.setSeq2_z(dtwData.getAz());
            dtwUtility.prepare_variables(dtwData);
            dtwUtility.compute(dtwData);
            TMPfall_distance[i] = dtwUtility.getDistance();
        }
        dtwData.setFall_distance(TMPfall_distance);
    }

    private static void walkDetection(DtwUtility dtwUtility, DtwData dtwData) {
         /* Loop through the number of total template. */
        Double[] TMPwalk_distance = new Double[dtwData.getWalk_data_x().size()];
        for (int i = 0; i < dtwData.getWalk_data_x().size(); i++) {
            dtwData.setSeq1_x(dtwData.getWalk_data_x().get(i).toArray(new Float[dtwData.getWalk_data_x().size()]));
            dtwData.setSeq1_y(dtwData.getWalk_data_y().get(i).toArray(new Float[dtwData.getWalk_data_y().size()]));
            dtwData.setSeq1_z(dtwData.getWalk_data_z().get(i).toArray(new Float[dtwData.getWalk_data_z().size()]));
            dtwData.setSeq2_x(dtwData.getAx());
            dtwData.setSeq2_y(dtwData.getAy());
            dtwData.setSeq2_z(dtwData.getAz());
            dtwUtility.prepare_variables(dtwData);
            dtwUtility.compute(dtwData);
            TMPwalk_distance[i] = dtwUtility.getDistance();

        }
        dtwData.setWalk_distance(TMPwalk_distance);
    }

    private double ANN(double[] paramArrayOfDouble) {
        double[] arrayOfDouble1 = {-1.495256509D, 0.189159898D, -1.745627963D, -0.136768115D, 0.343453265D, -1.099452714D, -0.008261038D, 0.319077332D, -1.514411266D, 0.764200433D, 0.872964737D};
        double[] arrayOfDouble2 = {2.280646265D, 0.967741057D, 2.087885364D, -0.141277674D, 0.536584108D, 0.446628999D, 0.040650166D, 0.73195341D, 1.321347553D, -1.932924615D, 1.600606284D};
        double[] arrayOfDouble3 = {0.739285384D, 1.181761609D, 0.989122626D, 1.165804538D, -1.520534741D, 0.150345993D, 0.361739402D, -0.315164389D, 0.905024674D, 0.177932131D, 0.5298672840000001D};
        double[] arrayOfDouble4 = {-0.942639787D, 0.00578126D, 0.003607329D, 0.013187709D};
        double d1 = arrayOfDouble1[0] + paramArrayOfDouble[0] * arrayOfDouble1[1] + paramArrayOfDouble[1] * arrayOfDouble1[2] + paramArrayOfDouble[2] * arrayOfDouble1[3] + paramArrayOfDouble[3] * arrayOfDouble1[4] + paramArrayOfDouble[4] * arrayOfDouble1[5] + paramArrayOfDouble[5] * arrayOfDouble1[6] + paramArrayOfDouble[6] * arrayOfDouble1[7] + paramArrayOfDouble[7] * arrayOfDouble1[8] + paramArrayOfDouble[8] * arrayOfDouble1[9] + paramArrayOfDouble[9] * arrayOfDouble1[10];
        double d2 = arrayOfDouble2[0] + paramArrayOfDouble[0] * arrayOfDouble2[1] + paramArrayOfDouble[1] * arrayOfDouble2[2] + paramArrayOfDouble[2] * arrayOfDouble2[3] + paramArrayOfDouble[3] * arrayOfDouble2[4] + paramArrayOfDouble[4] * arrayOfDouble2[5] + paramArrayOfDouble[5] * arrayOfDouble2[6] + paramArrayOfDouble[6] * arrayOfDouble2[7] + paramArrayOfDouble[7] * arrayOfDouble2[8] + paramArrayOfDouble[8] * arrayOfDouble2[9] + paramArrayOfDouble[9] * arrayOfDouble2[10];
        double d3 = arrayOfDouble3[0] + paramArrayOfDouble[0] * arrayOfDouble3[1] + paramArrayOfDouble[1] * arrayOfDouble3[2] + paramArrayOfDouble[2] * arrayOfDouble3[3] + paramArrayOfDouble[3] * arrayOfDouble3[4] + paramArrayOfDouble[4] * arrayOfDouble3[5] + paramArrayOfDouble[5] * arrayOfDouble3[6] + paramArrayOfDouble[6] * arrayOfDouble3[7] + paramArrayOfDouble[7] * arrayOfDouble3[8] + paramArrayOfDouble[8] * arrayOfDouble3[9] + paramArrayOfDouble[9] * arrayOfDouble3[10];
        return arrayOfDouble4[0] + d1 * arrayOfDouble4[1] + d2 * arrayOfDouble4[2] + d3 * arrayOfDouble4[3];

    }

    private double[] Feature(float[] paramArrayOfDouble1, float[] paramArrayOfDouble2, float[] paramArrayOfDouble3, double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4) {
        double[] arrayOfDouble1 = new double[100];
        double[] arrayOfDouble2 = new double[10];
        double tmp = 0.0;
        double std;
        double variance;
        double mean;
        double tmpAvg = 0.0;
        double max = arrayOfDouble1[0];
        double min = arrayOfDouble1[0];
        double range;
        for (int j = 0; j < arrayOfDouble1.length; j++) {
            arrayOfDouble1[j] = Math.pow(Math.pow(paramArrayOfDouble1[j], 2.0D)
                    + Math.pow(paramArrayOfDouble2[j], 2.0D)
                    + Math.pow(paramArrayOfDouble3[j], 2.0D), 0.5D);
        }

        for (int j = 1; j < arrayOfDouble1.length; j++) {
            if (arrayOfDouble1[j] > max) {
                max = arrayOfDouble1[j];
            }
        }

        for (int j = 1; j < arrayOfDouble1.length; j++) {
            if (arrayOfDouble1[j] < min) {
                min = arrayOfDouble1[j];
            }
        }

        for (double anArrayOfDouble1 : arrayOfDouble1) {
            tmp += Math.sqrt(anArrayOfDouble1);
            tmpAvg += anArrayOfDouble1;
        }

        variance = tmp / arrayOfDouble1.length;
        mean = tmpAvg / arrayOfDouble1.length;
        std = Math.sqrt(variance);
        range = max - min;
        arrayOfDouble2[0] = paramDouble4;
        arrayOfDouble2[1] = paramDouble1;
        arrayOfDouble2[2] = paramDouble3;
        arrayOfDouble2[3] = paramDouble2;
        arrayOfDouble2[4] = max;
        arrayOfDouble2[5] = min;
        arrayOfDouble2[6] = mean;
        arrayOfDouble2[7] = range;
        arrayOfDouble2[8] = variance;
        arrayOfDouble2[9] = std;
        return arrayOfDouble2;
    }

    private void FallDetected(AnnData annData) {
        this.mIsFall = true;
        this.mIsPrimaryFall = false;
        this.mFallTime = this.mSysTime;
        try {
            transmitDetectionResult("Fall detected!", annData);

        } catch (Exception e) {
            Log.d(CommonData.LOGCATAG, e.toString());
        }
    }

    public void GoNormal(AnnData annData) {
        String myAge = "56";
        String myHeight = "160";
        String myWeight = "70";
        this.mIsFall = false;
        this.mIsPrimaryFall = false;
        this.mCunter = 0;
        this.mLocationText = "";
        this.mSex = -1.0D;
        for (this.mHerHis = "Her"; ; this.mHerHis = "His") {
            if (myAge.length() < 0)
                this.mAge = Double.parseDouble(myAge);
            if (myHeight.length() < 0)
                this.mHeight = Double.parseDouble(myHeight);
            if (myWeight.length() < 0)
                this.mWeight = Double.parseDouble(myWeight);
            try {

                transmitDetectionResult("Normal situation.", annData);
            } catch (Exception e) {
                Log.d(CommonData.LOGCATAG, e.toString());
            }
            this.mSex = -1.0D;
            return;
        }
    }

    private void GoPrimaryFall(AnnData annData, DtwData dtwData) throws JSONException {
        this.mIsPrimaryFall = true;
        this.mPrimaryFallTime = this.mSysTime;
        Log.d(CommonData.LOGCATAG, "ANN: Potential Fall Detected! DTW: " + dtwData.getDTWResult());
        transmitDetectionResult("Potential Fall Detected!", annData);
    }

    private Location getLocation(AnnData annData) {
        return annData.getLastLocation();
    }

    private void onLocationChanged(AnnData annData) {
        String userName = "Emma";
        if (getLocation(annData) != null) {
            this.mLatitude = getLocation(annData).getLatitude();
            this.mLongitude = getLocation(annData).getLongitude();
            if (this.mAccuracy >= getLocation(annData).getAccuracy()) {
                Log.d(CommonData.LOGCATAG, "The Accuracy is = " + getLocation(annData).getAccuracy());
                this.mLocationText = (this.mHerHis + " current location is: " + "http://www.google.com/maps?q=" + this.mLatitude + "," + this.mLongitude);
            }
        }
        while (true) {
            String str = "It appears that " + userName + " has Fallen and may require assistance." + this.mLocationText;
            Log.d(CommonData.LOGCATAG, "mSYS: " + Double.toString(this.mSysTime - this.mFallTime));
            Log.d(CommonData.LOGCATAG, "mSYS>: " + Double.toString(this.mFTime));
            if ((this.mLocationText.length() > 0) && (this.mSysTime - this.mFallTime > this.mFTime)) {

                //LocationServices.FusedLocationApi.removeLocationUpdates(annData.getGMS(), (com.google.android.gms.location.LocationListener) this.context);
                //sendSMS(this.mPhoneNo, str);
                Log.d(CommonData.LOGCATAG, str);
                this.mLocationText = "";
            }
            this.mAccuracy = getLocation(annData).getAccuracy();
            return;
        }
    }

    private void fallDetection(float accelerometerValues[], AnnData annData, DtwUtility dtwUtility, DtwData dtwData, AssetManager assetMgr) throws JSONException {
        float axisX = accelerometerValues[0];
        float axisY = accelerometerValues[1];
        float axisZ = accelerometerValues[2];

        this.mSysTime = System.currentTimeMillis();
        double mMean = Math.pow(Math.pow(axisX, 2.0D) + Math.pow(axisY, 2.0D) + Math.pow(axisZ, 2.0D), 0.5D);

        if ((!this.mIsFall))
            this.mM = String.valueOf(mMean);
        if ((this.mIsFall) && (this.mSysTime - this.mFallTime < 1000.0D + this.mFTime)) {
            this.mM = String.valueOf((int) (this.mFTime / 1000.0D) - (this.mSysTime - this.mFallTime) / 1000L);
            if (this.mSysTime - this.mFallTime > this.mFTime) {
                this.mFallTime -= 1000L;
            }
        }
        while (true) {

            //eg 3<30000
            double mLTime = 30000.0D;
            if ((this.mIsPrimaryFall) && (this.mSysTime - this.mPrimaryFallTime < mLTime)) {
                Log.d(CommonData.LOGCATAG, dtwData.getDTWResult());
                //eg 6>5
                Log.d(CommonData.LOGCATAG, "no movement Time: " + Long.toString(this.mSysTime - this.mPrimaryFallTime));
                Log.d(CommonData.LOGCATAG, "Thesthold: " + Double.toString(mLTime - 25000.D));

                //Duo to limited template of DTW, therefore we set or case in this if prevent the FallTime Loop
                if (this.mSysTime - this.mPrimaryFallTime > mLTime - 27000.D) {
                    dtwData.setDTWResult("Fall");
                    if ((this.mSysTime - this.mPrimaryFallTime > mLTime - 25000.D) && (this.mSysTime - this.mPrimaryFallTime < 6000.D)) {
                        if (Objects.equals(dtwData.getDTWResult(), "Fall")) {
                            Log.d(CommonData.LOGCATAG, "Call:" + annData.getCallEmergency());
                            SharedPreferences CallEmergencyPref = annData.getAnnUtilityContext().getSharedPreferences("CallEmergency", 0);
                            Log.d(CommonData.LOGCATAG, CallEmergencyPref.getString("Running", "false"));
                            if (CallEmergencyPref.getString("Running", "false").equalsIgnoreCase("false")) {
                                Log.d(CommonData.LOGCATAG, "Intent Running");
                                Intent CallingEmergency = new Intent(annData.getAnnUtilityContext(), CallingEmergency.class);
                                CallingEmergency.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Bundle mBundle = new Bundle();
                                mBundle.putSerializable("annData", annData);
                                CallingEmergency.putExtras(mBundle);
                                annData.getAnnUtilityContext().startActivity(CallingEmergency);
                            }
                            while (Objects.equals(CallEmergencyPref.getString("Call", ""), "")) {
                                if (!Objects.equals(CallEmergencyPref.getString("Call", "false"), "")) {
                                    if (Boolean.parseBoolean(CallEmergencyPref.getString("Call", "false"))) {
                                        annData.setCallEmergency(Boolean.parseBoolean(CallEmergencyPref.getString("Call", "false")));
                                        onLocationChanged(annData);
                                        FallDetected(annData);
                                        Log.d(CommonData.LOGCATAG, "ANN: FallDected " + "DTW: " + dtwData.getDTWResult());
                                        break;
                                    } else if ((!Boolean.parseBoolean(CallEmergencyPref.getString("Call", "false")))) {
                                        GoNormal(annData);
                                        break;
                                    }

                                }
                            }
                            break;
                        } else {
                            GoNormal(annData);
                        }
                    }
                }
                if ((mMean > 11.0D) && (this.mSysTime - this.mPrimaryFallTime > 3000L)) {
                    GoNormal(annData);
                    Log.d(CommonData.LOGCATAG, "ANN: GoNormal " + "DTW: " + dtwData.getDTWResult());
                    dtwData.setDTWResult("");
                }


            }
            if ((!this.mIsFall) && (!this.mIsPrimaryFall))  //True True
            {
                this.Ax[this.mCunter] = (float) (237.00277800000001D * axisX / 10.0159442D);
                this.Ay[this.mCunter] = (float) (237.00277800000001D * axisY / 10.0159442D);
                this.Az[this.mCunter] = (float) (237.00277800000001D * axisZ / 10.0159442D);
                this.mCunter = (1 + this.mCunter);
                if (this.mCunter == 100) {
                    this.mCunter = 50;
                    System.arraycopy(this.Ax, 50, this.Ax, 0, -50 + this.Ax.length);
                    System.arraycopy(this.Ay, 50, this.Ay, 0, -50 + this.Ay.length);
                    System.arraycopy(this.Az, 50, this.Az, 0, -50 + this.Az.length);
                    //readFile(dtwUtility, dtwData);

                    if (dtwData.getFall_data_x() == null) {
                        prepareTemplate(dtwUtility, dtwData, assetMgr);
                    }
                    if (ANN(Feature(this.Ax, this.Ay, this.Az, this.mAge, this.mHeight, this.mWeight, this.mSex)) > -0.1D) {
                        if (dtwData.getFall_data_x() != null) {
                            prepareSample(this.Ax, this.Ay, this.Az, dtwData);
                            walkDetection(dtwUtility, dtwData);
                            fallDetection(dtwUtility, dtwData);
                            getFinalDistance(dtwUtility, dtwData);
                            getResult(dtwUtility, dtwData);
                        }
                        GoPrimaryFall(annData, dtwData);
                    }
                }
            }

            if ((!this.mIsFall) || (this.mSysTime - this.mFallTime >= this.mFTime) || (mMean <= 15.0D))
                break;
            GoNormal(annData);
        }
    }

    private void transmitDetectionResult(String paramString, AnnData annData) throws JSONException {
        SharedPreferences CallEmergencyPref = annData.getAnnUtilityContext().getSharedPreferences("CallEmergency", Activity.MODE_PRIVATE);
        annData.setFallDataState(paramString);
        Log.d(CommonData.LOGCATAG, paramString);
        if (annData.getWearConnectionState()) {
            Log.d(CommonData.LOGCATAG, "aData Before Sent: ");
            watchMotionData.put("id", "aData");
            watchMotionDevice.put("func", "1");
            watchMotionData.put("tStamp", Long.toString(System.currentTimeMillis() / 1000));
            watchMotionData.put("status", paramString);
            watchMotionData.put("latitude", Double.toString(annData.getLastLocation().getLatitude()));
            watchMotionData.put("longitude", Double.toString(annData.getLastLocation().getLongitude()));
            watchMotionData.put("callEmergency", Boolean.toString(annData.getCallEmergency()));
            watchMotionDataObj.put(watchMotionDevice);
            watchMotionDataObj.put(watchMotionData);
            annData.setSentaDataJSON(true);
            annData.setaData(watchMotionDataObj.toString());
            watchMotionDevice = new JSONObject();
            watchMotionData = new JSONObject();
            watchMotionDataObj = new JSONArray();
            annData.setCallEmergency(false);
            SharedPreferences.Editor editor = CallEmergencyPref.edit();
            editor.putString("Call", "");
            editor.apply();
        }
    }

    public void getAccelerometerData(float accelerometerValues[], AnnData annData, DtwUtility dtwUtility, DtwData dtwData, AssetManager assetMgr) throws JSONException {
        fallDetection(accelerometerValues, annData, dtwUtility, dtwData, assetMgr);
    }


}
