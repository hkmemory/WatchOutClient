package com.ouhk.webtech.watchoutclient;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

/**
 * Created by Jacky Li on 27/4/2015.
 */
public class BootReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        SharedPreferences serviceStatusPref = context.getSharedPreferences("Service", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = serviceStatusPref.edit();
        editor.putString("Service", "True");
        editor.apply();
        Toast.makeText(context, "Booting Completed", Toast.LENGTH_LONG).show();
        Toast.makeText(context, "Fall_DetectionService Start", Toast.LENGTH_LONG).show();
        context.startService(new Intent(context, SocketUtility.class));
    }
}
