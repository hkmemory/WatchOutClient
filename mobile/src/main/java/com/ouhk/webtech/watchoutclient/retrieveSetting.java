package com.ouhk.webtech.watchoutclient;

import android.os.Handler;

import org.json.JSONObject;

/**
 * Created by Jacky Li on 6/5/2015.
 */
public class retrieveSetting {
    JSONParser jParser = new JSONParser();
    JSONObject json = jParser.getJSONFromUrl(url);
    private Handler mHandler;
    mHandler=new

    Handler(mSensorThread.getLooper()

    )
}
