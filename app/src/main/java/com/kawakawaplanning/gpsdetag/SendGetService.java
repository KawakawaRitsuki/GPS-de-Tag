package com.kawakawaplanning.gpsdetag;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Timer;
import java.util.TimerTask;

public class SendGetService extends Service implements LocationListener {

    Timer timer = new Timer();
    protected LocationManager mLocationManager;
    private Handler handler;

    public double hereLat;
    public double hereLou;

    String myId = "kaoru0920";

    public SendGetService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "GGhf5EisfvSx54MFMOYhF1Kugk2qTHeeEvCg5ymV", "mmaiRNaqOsqbQe5FqwA4M28EttAG3TOW43OfVXcw");
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler = new Handler();
        timer.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run() {
                sendLocate(hereLat,hereLou);

//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//
//                    }
//                });
            }
        }, 10000, 5000);
        return START_STICKY;
    }

    @Override public void onDestroy() {
        super.onDestroy();

        if(timer != null){
            timer.cancel();
        }
        mLocationManager.removeUpdates(this);
    }//ログを見てみる

    public void sendLocate(final double lat ,final double lon){


        new Thread(new Runnable() {
            @Override
            public void run() {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("TestObject");//ParseObject型のParseQueryを取得する。
                query.whereEqualTo("USERID", myId);//そのクエリの中でReceiverがname変数のものを抜き出す。

                ParseACL acl = new ParseACL();
                acl.setPublicReadAccess(true);
                acl.setPublicWriteAccess(true);

                try{
                    if (query.find().size() != 0) {
                        ParseObject testObject = query.find().get(0);
                        testObject.put("USERID", myId);
                        testObject.put("Latitude", lat);
                        testObject.put("Longiutude", lon);
                        testObject.setACL(acl);
                        testObject.saveInBackground();
                    } else {
                        ParseObject testObject = new ParseObject("TestObject");
                        testObject.put("USERID", myId);
                        testObject.put("Latitude", lat);
                        testObject.put("Longiutude", lon);
                        testObject.setACL(acl);
                        testObject.saveInBackground();
                    }
                }catch (ParseException e){

                }
            }
        }).start();


    }

    @Override
    public void onLocationChanged(Location location) {
        hereLat = location.getLatitude();
        hereLou = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}
}
