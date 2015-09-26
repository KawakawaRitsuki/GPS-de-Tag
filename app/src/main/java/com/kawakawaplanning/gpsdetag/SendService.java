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
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

import com.kawakawaplanning.gpsdetag.http.HttpConnector;
import com.kawakawaplanning.gpsdetag.http.OnHttpResponseListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class SendService extends Service implements LocationListener {

    private LocationManager locationManager;

    static public String myId ;
    static public String groupId;
    Timer mTimer;

    HashMap<String,Integer> mMap;
    NotificationManager mNm;
    @Override
    public void onCreate() {
        mMap = new HashMap<>();
        SharedPreferences pref = getSharedPreferences("loginpref", Activity.MODE_MULTI_PROCESS );
        myId = pref.getString("loginid", "");

        mNm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this); // 位置情報リスナー
        groupId = pref.getString("groupId", "");

        mTimer = new Timer();
        mTimer.schedule(
                new TimerTask() {
                    public void run() {
                        new Thread(SendService.this::loginCheck).start();
                    }
                }, 1000, 1000);
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mTimer.cancel();
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

    public void loginCheck() {
        HttpConnector httpConnector = new HttpConnector("logincheck","{\"group_id\":\"" + groupId + "\"}");
        httpConnector.setOnHttpResponseListener(new OnHttpResponseListener() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    JSONArray data = json.getJSONArray("data");

                    for (int i = 0; i != data.length(); i++) {
                        JSONObject object = data.getJSONObject(i);
                        if (mMap.containsKey(object.getString("user_id"))) {
                            if (!object.getString("user_id").equals(myId) && mMap.get(object.getString("user_id")) != object.getInt("login")) {
                                if (object.getInt("login") == 1)
                                    notification(object.getString("user_name"), "ログアウト");
                                else
                                    notification(object.getString("user_name"), "ログイン");
                            }
                        }
                        mMap.put(object.getString("user_id"), object.getInt("login"));
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        httpConnector.postNoHandler();
    }

    public void notification(String who,String what){
        Intent _intent = new Intent(this, MapsActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 520, _intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(contentIntent);
        builder.setTicker(who + "さんが" + what + "しました。");
        builder.setSmallIcon(R.drawable.ic_stat_name);//アイコン
        builder.setContentTitle("集まれ！");
        builder.setContentText(who + "さんが" + what + "しました。");
        builder.setWhen(System.currentTimeMillis());

        int nId = R.string.app_name + 10;
        mNm.notify(nId, builder.build());

    }
}