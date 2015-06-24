package com.kawakawaplanning.gpsdetag;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity {

    private GoogleMap googleMap;

    public String myId;
    private Handler handler; //ThreadUI操作用

    private Map<String, Marker> marker= new HashMap<String, Marker>();

    private String[] mem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        finish();

        Parse.initialize(this, "GGhf5EisfvSx54MFMOYhF1Kugk2qTHeeEvCg5ymV", "mmaiRNaqOsqbQe5FqwA4M28EttAG3TOW43OfVXcw");

        myId = getIntent().getStringExtra("name");



        mem = getIntent().getStringArrayExtra("selectGroup");

        TextView tv = (TextView) findViewById(R.id.textView);
        tv.setText("ようこそ！" + myId + "さん");

        handler = new Handler();

        googleMap =  ( (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map) ).getMap();
        mapInit();

    }


    @Override
    protected void onResume() {
        super.onResume();

        TimerTask task = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        sendLocate(googleMap);
                    }
                });
                getLocate(mem);
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 10000, 5000);

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

//                    if(!id.equals(myId)) {

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
//                    }

                }
            }
        }).start();
    }



    public void sendLocate(final GoogleMap googleMap){

        final double lat = googleMap.getMyLocation().getLatitude();
        final double lon = googleMap.getMyLocation().getLongitude();


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
    protected void onStop() {
        super.onStop();
        Log.d("FinishTest", "onStop");
    }

    @Override
    protected void onDestroy () {
        Log.d("FinishTest", "onDestroy");
        super.onDestroy();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("TestObject");//ParseObject型のParseQueryを取得する。
        query.whereEqualTo("USERID", myId);//そのクエリの中でReceiverがname変数のものを抜き出す。
        try {
            ParseObject testObject = query.find().get(0);
            testObject.put("USERID", myId);
            testObject.put("LoginNow", false);
            testObject.saveInBackground();
        } catch (ParseException e) {
            e.printStackTrace();
        }




        ParseUser.logOutInBackground();
//        finish();

    }

}