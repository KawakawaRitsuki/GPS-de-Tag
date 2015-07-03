package com.kawakawaplanning.gpsdetag;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Timer;
import java.util.TimerTask;

public class SendService extends Service implements LocationListener {

    private Timer timer = new Timer();
    private double lat = 0.0;
    private double lon = 0.0;
    private LocationManager locationManager;

    static public String myId ;
    static public String mem[];


    @Override
    public void onCreate() {

        SharedPreferences pref = getSharedPreferences("loginpref", Activity.MODE_MULTI_PROCESS );
        myId = pref.getString("loginid","");
        mem = pref.getString("mem","").split(",");

        Parse.initialize(this, "GGhf5EisfvSx54MFMOYhF1Kugk2qTHeeEvCg5ymV", "mmaiRNaqOsqbQe5FqwA4M28EttAG3TOW43OfVXcw");
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this); // 位置情報リスナー

    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final Handler handler = new Handler();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        sendLocate(lat,lon);
                    }
                });
            }
        }, 0, 5000);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        locationManager.removeUpdates(this);


    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lon = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override
    public void onProviderEnabled(String provider){}
    @Override
    public void onProviderDisabled(String provider){}


    public void sendLocate(final double lat ,final double lon){

        if (lat != 0.0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("TestObject");
                    query.whereEqualTo("USERID", myId);

                    try {
                        if (query.find().size() != 0) {
                            ParseObject testObject = query.find().get(0);
                            testObject.put("Latitude", lat);
                            testObject.put("Longiutude", lon);
                            testObject.saveInBackground();
                        } else {
                            ParseObject testObject = new ParseObject("TestObject");
                            testObject.put("USERID", myId);
                            testObject.put("Latitude", lat);
                            testObject.put("Longiutude", lon);
                            testObject.saveInBackground();
                        }
                    } catch (ParseException e) {

                    }
                }
            }).start();
        }
    }
}