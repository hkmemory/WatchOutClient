package com.ouhk.webtech.watchoutclient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity {
    private Boolean wearService = false;
    private TextView tvServiceStatus;
    private SharedPreferences ServiceStatusPref;
    private final Button.OnClickListener onImageViewListner = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            wearService = Boolean.parseBoolean(ServiceStatusPref.getString("Service", "False"));
            if (wearService) {
                Vibrator myVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
                myVibrator.vibrate(200);
                Toast.makeText(getApplicationContext(), "WatchOut Service Stop.....", Toast.LENGTH_SHORT).show();
                Intent SocketUtility = new Intent(getApplication(), SocketUtility.class);
                SocketUtility.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                SocketUtility.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                getApplication().stopService(SocketUtility);
                Toast.makeText(getApplication(), "Service Started: False", Toast.LENGTH_SHORT).show();
                tvServiceStatus.setText("Service Started: False");
                SharedPreferences.Editor editor = ServiceStatusPref.edit();
                editor.putString("Service", "False");
                editor.apply();
                finish();
            } else {
                Vibrator myVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
                myVibrator.vibrate(200);
                Toast.makeText(getApplicationContext(), "WatchOut Service Start.....", Toast.LENGTH_SHORT).show();
                Intent SocketUtility = new Intent(getApplication(), SocketUtility.class);
                SocketUtility.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                SocketUtility.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                getApplication().startService(SocketUtility);
                Toast.makeText(getApplication(), "Service Started: True", Toast.LENGTH_SHORT).show();
                tvServiceStatus.setText("Service Started: True");
                SharedPreferences.Editor editor = ServiceStatusPref.edit();
                editor.putString("Service", "True");
                editor.apply();
            }
        }

    };
    private SharedPreferences settingPref;
    private Context context;
    private JSONParser jParser = new JSONParser();
    private JSONObject json;
    private JSONArray android = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(com.ouhk.webtech.watchoutclient.R.layout.activity_main);
        ServiceStatusPref = getSharedPreferences("Service", Activity.MODE_PRIVATE);
        settingPref = getSharedPreferences("setting", Activity.MODE_PRIVATE);
        wearService = Boolean.parseBoolean(ServiceStatusPref.getString("Service", "False"));
        context = this;
        new fetchSetting().execute();
        initialUI();
    }

    public void onDestroy() {
        super.onDestroy();
        //stopService(new Intent(this, SocketUtility.class));
    }

    public void fetchSettingStart() {
        try {
            JSONObject json = jParser.getJSONFromUrl(CommonData.FETCHSETTINGURL);
            JSONObject settingOBJ = json.getJSONObject("android");
            Log.d(CommonData.LOGCATAG, "Fetch Setting: " + settingOBJ);
            String userName = settingOBJ.getString("username");
            String elderlyname = settingOBJ.getString("elderlyname");
            String elderlydob = settingOBJ.getString("elderlydob");
            Boolean elderlygender = Boolean.parseBoolean(settingOBJ.getString("elderlygender"));
            String elderlyheight = settingOBJ.getString("elderlyheight");
            String elderlyweight = settingOBJ.getString("elderlyweight");
            String phonenumber = settingOBJ.getString("phonenumber");
            Boolean emServices = Boolean.parseBoolean(settingOBJ.getString("emServices"));
            Log.d(CommonData.LOGCATAG, Boolean.toString(emServices));
            Log.d(CommonData.LOGCATAG, phonenumber);
            SharedPreferences.Editor editor = settingPref.edit();
            editor.putString("username", userName);
            editor.putString("elderlyname", elderlyname);
            editor.putString("elderlydob", elderlydob);
            editor.putBoolean("elderlygender", elderlygender);
            editor.putString("elderlyheight", elderlyheight);
            editor.putString("elderlyweight", elderlyweight);
            editor.putString("phonenumber", phonenumber);
            editor.putBoolean("emServices", emServices);
            editor.apply();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initialUI() {

        RelativeLayout myLayout = new RelativeLayout(this);
        LinearLayout linerLayout = new LinearLayout(this);
        tvServiceStatus = new TextView(this);
        TextView tvTitle = new TextView(this);
        TextView tvElderlyName = new TextView(this);
        linerLayout.setOrientation(LinearLayout.VERTICAL);

        tvTitle.setTextSize(30);
        tvElderlyName.setTextSize(30);
        tvTitle.setGravity(Gravity.CENTER);
        tvElderlyName.setGravity(Gravity.CENTER);
        tvTitle.setText("Pressed Icon activate service:");
        tvElderlyName.setText("Hello, " + settingPref.getString("elderlyname", "User"));
        tvServiceStatus.setTextSize(28);
        tvServiceStatus.setGravity(Gravity.CENTER);
        tvServiceStatus.setText("Service Started: " + wearService);
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.app_icon);
        imageView.setOnClickListener(onImageViewListner);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(800, 800);
        llp.setMargins(400, 50, 400, 50); // llp.setMargins(left, top, right, bottom);
        imageView.setLayoutParams(llp);
        linerLayout.addView(tvElderlyName);
        linerLayout.addView(tvTitle);
        linerLayout.addView(imageView);
        linerLayout.addView(tvServiceStatus);
        linerLayout.setGravity(Gravity.CENTER);

        myLayout.addView(linerLayout);
        myLayout.setGravity(Gravity.CENTER);
        setContentView(myLayout);
    }

    class fetchSetting extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(context);
            dialog.setMessage("Loading...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            fetchSettingStart();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (result != null) {
                dialog.dismiss();
            } else {
                Toast.makeText(context, "Successfully Fetch Setting", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        }
    }
}