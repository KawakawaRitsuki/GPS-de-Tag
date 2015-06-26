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
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Timer;
import java.util.TimerTask;

public class SendGetService extends Service implements LocationListener {
    static final String TAG="LocalService";

    private Timer timer = new Timer();
    private double lat = 0.0;
    private double lon = 0.0;
    static public String myId ;
    static public String mem[];
    private LocationManager locationManager;    // ロケーションマネージャ
    private NotificationManager nm;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");

        SharedPreferences pref = getSharedPreferences("loginpref", Activity.MODE_MULTI_PROCESS );
        myId = pref.getString("loginid","");
        mem = pref.getString("mem","").split(",");

        Parse.initialize(this, "GGhf5EisfvSx54MFMOYhF1Kugk2qTHeeEvCg5ymV", "mmaiRNaqOsqbQe5FqwA4M28EttAG3TOW43OfVXcw");
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this); // 位置情報リスナー

//ここに行いたいイベントを記載する
        Intent _intent = new Intent(this,MapsActivity.class);
        _intent.putExtra("name",myId);

        //②Activityを起動させる為。
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, _intent,PendingIntent.FLAG_UPDATE_CURRENT);

        //NotificationBuilderをセット
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(contentIntent);
        builder.setTicker("GPS de 鬼ごっこは実行中です");
        builder.setSmallIcon(R.mipmap.ic_launcher);//アイコン
        builder.setContentTitle("GPS de 鬼ごっこ");
        builder.setContentText("GPS鬼ごっこは実行中です。マップを表示。");
        builder.setOngoing(true);

        //通知するタイミング
        builder.setWhen(System.currentTimeMillis());

        // NotificationManagerをセット
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //通知
        int nId = R.string.app_name;
        nm.notify(nId, builder.build());

//        nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
//
//        Notification n;
//        n = new Notification(R.mipmap.ic_launcher, "GPS鬼ごっこは実行中です", System.currentTimeMillis());
//        n.flags = Notification.FLAG_ONGOING_EVENT;
//
//        Intent i = new Intent(this, com.kawakawaplanning.gpsdetag.MapsActivity.class);
//        i.putExtra("name",myId);
//        PendingIntent pi;
//        pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
//
//        n.setLatestEventInfo(this, "GPS鬼ごっこ", "GPS鬼ごっこは実行中です。マップを見る場合はタップしてください。", pi);
//        int nId = R.string.app_name;
//
//        nm.notify(nId, n);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand Received start id " + startId + ": " + intent);
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

        //明示的にサービスの起動、停止が決められる場合の返り値
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
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
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("TestObject");//ParseObject型のParseQueryを取得する。
                    query.whereEqualTo("USERID", myId);//そのクエリの中でReceiverがname変数のものを抜き出す。

                    ParseACL acl = new ParseACL();
                    acl.setPublicReadAccess(true);
                    acl.setPublicWriteAccess(true);

                    try {
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
                    } catch (ParseException e) {

                    }
                }
            }).start();
        }


    }
}



//    Timer timer = new Timer();
//    private Handler handler;
//
//    public double hereLat;
//    public double hereLou;
//    private LocationManager locationManager;
//
//    String myId = "kaoru0920";
//
//    public SendGetService() {
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        Parse.initialize(this, "GGhf5EisfvSx54MFMOYhF1Kugk2qTHeeEvCg5ymV", "mmaiRNaqOsqbQe5FqwA4M28EttAG3TOW43OfVXcw");
//        // ロケーションマネージャのインスタンスを取得する
//        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        handler = new Handler();
//        timer.scheduleAtFixedRate(new TimerTask(){
//            @Override
//            public void run() {
//
//
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
////                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, // プロバイダ
////                                0, // 通知のための最小時間間隔
////                                0, // 通知のための最小距離間隔
////                                SendGetService.this); // 位置情報リスナー
//                        sendLocate(MapsActivity.googleMap.getMyLocation().getLatitude(), MapsActivity.googleMap.getMyLocation().getLongitude());
//                    }
//                });
//            }
//        }, 10000, 5000);
//        return START_STICKY;
//    }
//
//    @Override public void onDestroy() {
//        super.onDestroy();
//
//        if(timer != null){
//            timer.cancel();
//        }
//    }//ログを見てみる
//

//
//    @Override
//    public void onLocationChanged(Location location) {
//        Log.v("kp", location.getLatitude() + "," + location.getLongitude());
//        locationManager.removeUpdates(this);
//    }
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//
//    }
//
//    @Override
//    public void onProviderDisabled(String provider) {
//
//    }
//}
