/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ouhk.webtech.watchoutclient;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Listens for a message telling it to start the Wearable MainActivity.
 */
public class WearableMessageListenerService extends WearableListenerService {

    private static final String START_SERVICE_PATH = "/start-service";
    private static final String STOP_SERVICE_PATH = "/stop-service";

    @Override
    public void onMessageReceived(MessageEvent event) {
        SharedPreferences serviceStatusPref = getSharedPreferences("Service", Activity.MODE_PRIVATE);
        Log.d(CommonData.LOGCATAG, "Listner: " + event.getPath());

        if (event.getPath().equals(START_SERVICE_PATH)) {

            SharedPreferences.Editor editor = serviceStatusPref.edit();
            editor.putString("Service", "True");
            editor.apply();
            Toast.makeText(getApplication(), "Service Start.....", Toast.LENGTH_LONG).show();
            Intent Fall_DetectionService = new Intent(getApplication(), Fall_DetectionService.class);
            startService(Fall_DetectionService);


        }
        if (event.getPath().equals(STOP_SERVICE_PATH)) {

            SharedPreferences.Editor editor = serviceStatusPref.edit();
            editor.putString("Service", "False");
            editor.apply();
            Toast.makeText(getApplication(), "Service Stop.....", Toast.LENGTH_LONG).show();
            Intent Fall_DetectionService = new Intent(getApplication(), Fall_DetectionService.class);
            Fall_DetectionService.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Fall_DetectionService.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            stopService(Fall_DetectionService);

        }
    }
}
