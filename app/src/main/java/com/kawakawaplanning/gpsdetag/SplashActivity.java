package com.kawakawaplanning.gpsdetag;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;


public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        TextView tv = (TextView)findViewById(R.id.textView);
        tv.setTypeface(Typeface.createFromAsset(getAssets(), "mplus1cthin.ttf"));

        Timer timer = new Timer();
        timer.schedule(
                new TimerTask() {
                    public void run() {
                        startActivity(new Intent().setClass(SplashActivity.this,MainActivity.class));
                        finish();
                    }
                }, 1000);

    }

}
