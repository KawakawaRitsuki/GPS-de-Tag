package com.kawakawaplanning.gpsdetag;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity {

    static public GoogleMap googleMap;
    static public String myId;
    private Handler handler; //ThreadUI操作用
    private Map<Integer, Marker> marker= new HashMap<Integer, Marker>();
    static public String[] mem;
    Timer timer;
    int i ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Parse.initialize(this, "GGhf5EisfvSx54MFMOYhF1Kugk2qTHeeEvCg5ymV", "mmaiRNaqOsqbQe5FqwA4M28EttAG3TOW43OfVXcw");

        SharedPreferences pref = getSharedPreferences("loginpref", Activity.MODE_MULTI_PROCESS );
        myId = pref.getString("loginid","");
        Log.v("tag", pref.getString("mem", ""));
        mem = pref.getString("mem","").split(",");

        TextView tv = (TextView) findViewById(R.id.textView);
        tv.setText("ようこそ！" + myId + "さん");

        handler = new Handler();

        googleMap =  ( (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map) ).getMap();
        mapInit();

        if(!isServiceRunning(this,SendService.class))
            startService(new Intent(this, SendService.class));

    }

    public void onClick(View v) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("TestObject");//ParseObject型のParseQueryを取得する。
        query.whereEqualTo("USERID", myId);//そのクエリの中でReceiverがname変数のものを抜き出す。
        try {
            ParseObject testObject = query.find().get(0);
            testObject.put("LoginNow", false);
            testObject.saveInBackground();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        stopService(new Intent(MapsActivity.this, SendService.class));
        ParseUser.logOutInBackground();
        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
        timer = new Timer();
        timer.schedule(
                new TimerTask() {
                    public void run() {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                getLocate(mem);
                            }
                        }).start();

                    }
                }, 5000, 5000);

    }
    public boolean isServiceRunning(Context c, Class<?> cls) {
        ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningService = am.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo i : runningService) {
            if (cls.getName().equals(i.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    private void mapInit() {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setMyLocationEnabled(true);
        CameraPosition camerapos = new CameraPosition.Builder().target(new LatLng(38.2586, 137.6850)).zoom(4.5f).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camerapos));
    }

    public void getLocate(final String name[]){

        ParseQuery<ParseObject> query = ParseQuery.getQuery("TestObject");//ParseObject型のParseQueryを取得する。
        i = 0;
        for(final String id:name) {
//          if(!id.equals(myId)) {//リリース時ここのコメントアウトを外す

                Log.v("tag",id);
                try {
                    ParseQuery<ParseObject> q = query.whereEqualTo("USERID", id);//そのクエリの中でReceiverがname変数のものを抜き出す。
                    ParseObject po = q.find().get(0);
                    final String obId = po.getObjectId();
                    final ParseQuery<ParseObject> que = ParseQuery.getQuery("TestObject");//その、ObjectIDで参照できるデータの内容をParseObject型のParseQueryで取得
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                setMarker(i,que.get(obId).getDouble("Latitude"),que.get(obId).getDouble("Longiutude"));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            i++;
//          }
        }
    }

    public void setMarker(final int id,final double lat,final double lon){
        handler.post(
                new Runnable() {
                    @Override
                    public void run() {

                        if (marker.get(id) != null)
                            marker.get(id).remove();
                        LatLng latLng = new LatLng(lat, lon);
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        marker.put(id, googleMap.addMarker(markerOptions));

                    }
                }
        );
    }


    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
    }
}