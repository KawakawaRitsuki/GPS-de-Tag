package com.kawakawaplanning.gpsdetag;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.support.v7.app.NotificationCompat;

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
    private NotificationManager nm;

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

        notificate();

    }

    public void notificate(){
        Intent _intent = new Intent(this,MapsActivity.class);
        _intent.putExtra("name",myId);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, _intent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(contentIntent);
        builder.setTicker("GPS de 鬼ごっこは実行中です");
        builder.setSmallIcon(R.mipmap.ic_launcher);//アイコン
        builder.setContentTitle("GPS de 鬼ごっこ");
        builder.setContentText("GPS鬼ごっこは実行中です。マップを表示。");
        builder.setOngoing(true);
        builder.setWhen(System.currentTimeMillis());

        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int nId = R.string.app_name;
        nm.notify(nId, builder.build());
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
        int nId = R.string.app_name;
        nm.cancel(nId);
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