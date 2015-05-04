package com.ouhk.webtech.watchoutclient;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private TextView tvServiceStatus;
    private SharedPreferences servicePref;
    private Boolean wearService = false;
    private final Button.OnClickListener onImageViewListner = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            wearService = Boolean.parseBoolean(servicePref.getString("Service", "false"));
            Toast.makeText(getApplication(), "Service Stop.....", Toast.LENGTH_LONG).show();
            Intent Fall_DetectionService = new Intent(getApplication(), Fall_DetectionService.class);
            SharedPreferences.Editor editor = servicePref.edit();
            editor.putString("Service", "False");
            editor.apply();
            stopService(Fall_DetectionService);
            tvServiceStatus.setText(Boolean.toString(wearService));
            finish();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        servicePref = getSharedPreferences("Service", Activity.MODE_PRIVATE);
        wearService = Boolean.parseBoolean(servicePref.getString("Service", "false"));
        initialUI();
    }

    private void initialUI() {
        android.support.wearable.view.BoxInsetLayout boxInsertLayout = new android.support.wearable.view.BoxInsetLayout(this);
        LinearLayout linerLayout = new LinearLayout(this);
        tvServiceStatus = new TextView(this);
        linerLayout.setOrientation(LinearLayout.VERTICAL);
        tvServiceStatus.setTextSize(20);
        tvServiceStatus.setGravity(Gravity.CENTER);
        tvServiceStatus.setText("Service Started: " + wearService);
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.app_icon);
        imageView.setOnClickListener(onImageViewListner);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(150, 150);
        llp.setMargins(150, 30, 150, 30); // llp.setMargins(left, top, right, bottom);
        imageView.setLayoutParams(llp);
        linerLayout.addView(tvServiceStatus);
        linerLayout.addView(imageView);
        linerLayout.setGravity(Gravity.CENTER);
        boxInsertLayout.addView(linerLayout);
        setContentView(boxInsertLayout);

    }
}
