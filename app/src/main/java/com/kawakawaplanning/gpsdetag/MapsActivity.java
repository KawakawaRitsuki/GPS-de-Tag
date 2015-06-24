package com.kawakawaplanning.gpsdetag;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements LocationListener {

    private GoogleMap googleMap;

    private LocationManager mLocationManager;
    public double hereLat;
    public double hereLou;

    public String myId;
    private Handler handler; //ThreadUI操作用

    private Map<String, Marker> marker= new HashMap<String, Marker>();

    private String[] mem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Parse.initialize(this, "GGhf5EisfvSx54MFMOYhF1Kugk2qTHeeEvCg5ymV", "mmaiRNaqOsqbQe5FqwA4M28EttAG3TOW43OfVXcw");

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        final boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            enableLocationSettings();
        }

        myId = getIntent().getStringExtra("name");
        mem = getIntent().getStringArrayExtra("selectGroup");

        TextView tv = (TextView) findViewById(R.id.textView);
        tv.setText("ようこそ！" + myId + "さん");

        handler = new Handler();

//        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        googleMap =  ( (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map) ).getMap();
//        mapFragment.setRetainInstance(true);
        mapInit();
    }


    @Override
    protected void onStart() {
        super.onStart();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("TestObject");//ParseObject型のParseQueryを取得する。
        query.whereEqualTo("USERID", myId);//そのクエリの中でReceiverがname変数のものを抜き出す。
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> parselist, com.parse.ParseException e) {//その、name変数のものが見つかったとき
                if (e == null) {//エラーが無ければ
                    if (parselist.size() != 0) {
                        ParseObject testObject = parselist.get(0);

                        testObject.put("USERID", myId);
                        testObject.put("LoginNow", true);
                        testObject.saveInBackground();
                    } else {
                        ParseObject testObject = new ParseObject("TestObject");

                        testObject.put("USERID", myId);
                        testObject.put("LoginNow", true);
                        testObject.saveInBackground();
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    private void enableLocationSettings() {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();



        TimerTask task = new TimerTask() {
            public void run() {
                sendLocate();
                getLocate(mem);
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 10000, 10000);

    }

    private void mapInit() {

        // 地図タイプ設定
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // 現在位置ボタンの表示を行なう
        googleMap.setMyLocationEnabled(true);

        // 東京駅の位置、ズーム設定
        CameraPosition camerapos = new CameraPosition.Builder()
                .target(new LatLng(35.681382, 139.766084)).zoom(15.5f).build();

        // 地図の中心の変更する
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camerapos));
    }

    public void getLocate(final String name[]){

        new Thread(new Runnable() {
            @Override
            public void run() {
                for(final String id:name) {

                    if(!name.equals(myId)) {

                        try {

                            ParseQuery<ParseObject> query = ParseQuery.getQuery("TestObject");//ParseObject型のParseQueryを取得する。
                            query.whereEqualTo("USERID", id);//そのクエリの中でReceiverがname変数のものを抜き出す。
                            ParseObject po = query.find().get(0);
                            final String obId = po.getObjectId();
                            final ParseQuery<ParseObject> que = ParseQuery.getQuery("TestObject");//その、ObjectIDで参照できるデータの内容をParseObject型のParseQueryで取得

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (marker.get(id) != null)
                                            marker.get(id).remove();
                                        marker.put(id, googleMap.addMarker(new MarkerOptions().position(new LatLng(que.get(obId).getDouble("Latitude"), que.get(obId).getDouble("Longiutude")))));
                                    } catch (ParseException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            });

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }).start();

    }



    public void sendLocate(){

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
                        testObject.put("Latitude", hereLat);
                        testObject.put("Longiutude", hereLou);
                        testObject.setACL(acl);
                        testObject.saveInBackground();
                    } else {
                        ParseObject testObject = new ParseObject("TestObject");
                        testObject.put("USERID", myId);
                        testObject.put("Latitude", hereLat);
                        testObject.put("Longiutude", hereLou);
                        testObject.setACL(acl);
                        testObject.saveInBackground();
                    }
                }catch (ParseException e){

                }
            }
        }).start();


    }

    @Override
    protected void onStop() {
        super.onStop();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("TestObject");//ParseObject型のParseQueryを取得する。
                query.whereEqualTo("USERID", myId);//そのクエリの中でReceiverがname変数のものを抜き出す。
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> parselist, com.parse.ParseException e) {//その、name変数のものが見つかったとき
                        if (e == null) {//エラーが無ければ
                            if (parselist.size() != 0) {
                                ParseObject testObject = parselist.get(0);

                                testObject.put("USERID", myId);
                                testObject.put("LoginNow", false);
                                testObject.saveInBackground();
                            } else {
                                ParseObject testObject = new ParseObject("TestObject");

                                testObject.put("USERID", myId);
                                testObject.put("LoginNow", false);
                                testObject.saveInBackground();
                            }
                        } else {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();

        ParseUser.logOutInBackground();
//        finish();

    }

    @Override
    public void onLocationChanged(Location location) {
        hereLat = location.getLatitude();
        hereLou = location.getLongitude();
        System.out.println("a");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}
}