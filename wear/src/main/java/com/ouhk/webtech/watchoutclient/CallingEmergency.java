package com.ouhk.webtech.watchoutclient;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class CallingEmergency extends Activity implements DelayedConfirmationView.DelayedConfirmationListener {

    private SharedPreferences CallEmergencyPref;

    private AnnData annData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Vibrator myVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
        //myVibrator.vibrate(3000);
        CallEmergencyPref = getSharedPreferences("CallEmergency", Activity.MODE_PRIVATE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.round_activity_calling_emergency);
        Intent intent = getIntent();
        annData = (AnnData) intent.getSerializableExtra("annData");
        DelayedConfirmationView mDelayedView = (DelayedConfirmationView) findViewById(R.id.delayed_confirm);
        mDelayedView.setTotalTimeMs(10000);
        // Start the timer
        mDelayedView.start();
        mDelayedView.setListener(this);
        SharedPreferences.Editor editor = CallEmergencyPref.edit();
        editor.putString("Running", "true");
        editor.apply();
    }

    @Override
    public void onTimerFinished(View view) {
        // User didn't cancel, perform the action
        SharedPreferences.Editor editor = CallEmergencyPref.edit();
        editor.putString("Call", "true");
        editor.putString("Running", "false");

        editor.apply();
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, getString(R.string.request_sent));
        startActivity(intent);
        Vibrator myVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
        myVibrator.vibrate(500);
        Log.d(CommonData.LOGCATAG, "Activity Result: " + Boolean.toString(annData.getCallEmergency()));
        this.finish();
    }

    @Override
    public void onTimerSelected(View view) {
        // User canceled, abort the action

        SharedPreferences.Editor editor = CallEmergencyPref.edit();
        editor.putString("Call", "false");
        editor.putString("Running", "false");
        editor.apply();
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, getString(R.string.request_notsent));
        startActivity(intent);
        Vibrator myVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
        myVibrator.vibrate(200);
        ((DelayedConfirmationView) view).setListener(null);
        Log.d(CommonData.LOGCATAG, "Activity Result: " + Boolean.toString(annData.getCallEmergency()));
        this.finish();

    }


}
