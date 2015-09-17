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
import android.os.IBinder;

import com.kawakawaplanning.gpsdetag.http.HttpConnector;

import java.util.Timer;

public class SendService extends Service implements LocationListener {

    private Timer timer = new Timer();
    private LocationManager locationManager;

    static public String myId ;
    static public String groupId;
    static public String mem[];

    @Override
    public void onCreate() {

        SharedPreferences pref = getSharedPreferences("loginpref", Activity.MODE_MULTI_PROCESS );
        myId = pref.getString("loginid", "");
        mem = pref.getString("mem","").split(",");

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this); // 位置情報リスナー
        groupId = pref.getString("groupId", "");
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

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
        sendLocate(location.getLatitude(),location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override
    public void onProviderEnabled(String provider){}
    @Override
    public void onProviderDisabled(String provider){}


    public void sendLocate(final double lat ,final double lon){

        if (lat != 0.0) {
            HttpConnector httpConnector = new HttpConnector("regist","{\"user_id\":\"" + myId + "\",\"latitude\":\"" + lat + "\",\"longitude\":\"" + lon + "\"}");
            httpConnector.post();
        }
    }


}