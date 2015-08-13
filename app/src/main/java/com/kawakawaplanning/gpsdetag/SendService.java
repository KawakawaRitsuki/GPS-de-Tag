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

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SendService extends Service implements LocationListener {

    private Timer timer = new Timer();
    private double lat = 0.0;
    private double lon = 0.0;
    private LocationManager locationManager;

    static public String myId ;
    static public String groupId;
    static public String mem[];

    int chatNum = 0;


    @Override
    public void onCreate() {

        SharedPreferences pref = getSharedPreferences("loginpref", Activity.MODE_MULTI_PROCESS );
        myId = pref.getString("loginid","");
        mem = pref.getString("mem","").split(",");

        Parse.initialize(this, "GGhf5EisfvSx54MFMOYhF1Kugk2qTHeeEvCg5ymV", "mmaiRNaqOsqbQe5FqwA4M28EttAG3TOW43OfVXcw");
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, this); // 位置情報リスナー
        groupId = pref.getString("groupId", "");
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final Handler handler = new Handler();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                handler.post(() -> {
                    sendLocate(lat, lon);
                    receiveChat();
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
            new Thread(() -> {
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
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public static String chatTxt;
    public void receiveChat(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery(groupId);
        query.findInBackground((List<ParseObject> parselist, com.parse.ParseException e) -> {
            if (e == null) {//エラーが無ければ
                if (chatNum == 0) {
                    chatTxt = "";
                    for (ParseObject parseObject : parselist) {
                        String message = parseObject.getString("Message");
                        String from = parseObject.getString("From");
                        String to = parseObject.getString("To");
                        if (to.equals(myId) || to.equals("All") || from.equals(myId))
                            chatTxt = chatTxt + from + ":" + message + "\n";
                    }
                    chatNum = parselist.size();
                } else if(parselist.size() > chatNum){
                    chatTxt = "";
                    String message = "";
                    String from = "";
                    String to;
                    for (ParseObject parseObject : parselist) {
                        message = parseObject.getString("Message");
                        from = parseObject.getString("From");
                        to = parseObject.getString("To");
                        if (to.equals(myId) || to.equals("All") || from.equals(myId))
                            chatTxt = chatTxt + from + ":" + message + "\n";
                    }
                    chatNum = parselist.size();

                    if(MapsActivity.myId == null) {
                        Intent _intent = new Intent(SendService.this, MapsActivity.class);
                        _intent.putExtra("name", myId);
                        _intent.putExtra("from", true);
                        PendingIntent contentIntent = PendingIntent.getActivity(SendService.this, 0, _intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(SendService.this);
                        builder.setContentIntent(contentIntent);
                        builder.setTicker(from + "からチャットを受信しました！");
                        builder.setSmallIcon(R.mipmap.ic_launcher);//アイコン
                        builder.setContentTitle("チャットを受信しました");
                        builder.setContentText(from + "からチャットを受信しました。タップで確認。");
                        builder.setWhen(System.currentTimeMillis());
                        long[] vib = {100, 200, 300};
                        builder.setVibrate(vib);

                        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle(builder);
                        inboxStyle.setBigContentTitle("チャットを受信しました");
                        inboxStyle.setSummaryText("GPS de 鬼ごっこ");
                        inboxStyle.addLine(from + ":" + message);

                        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        int nId = R.string.app_name + 1;
                        nm.cancel(nId);
                        nm.notify(nId, builder.build());
                    }
                }
            }
        });
    }

}