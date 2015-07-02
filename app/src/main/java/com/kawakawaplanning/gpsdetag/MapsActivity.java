package com.kawakawaplanning.gpsdetag;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.NotificationCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity {

    static public GoogleMap googleMap;
    static public String myId;
    private String groupId;
    private Handler handler; //ThreadUI操作用
    private Map<Integer, Marker> marker= new HashMap<Integer, Marker>();
    static public String[] mem;
    Timer timer;
    private NotificationManager nm;
    LinearLayout chatLl;
    ImageView chatIv;
    TextView chatTv;
    EditText chatEt;
    Spinner spinner;

    int i ;

    boolean finish = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        chatLl = (LinearLayout)findViewById(R.id.chatLL);
        chatIv = (ImageView)findViewById(R.id.chatCloseBtn);
        chatEt = (EditText)findViewById(R.id.chatEt);
        chatTv = (TextView)findViewById(R.id.chatTv);

        chatEt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && keyCode == KeyEvent.KEYCODE_ENTER) {
                    try {
                        ParseObject groupObject = new ParseObject(groupId);
                        groupObject.put("Message", chatEt.getEditableText().toString());
                        groupObject.put("From", myId);
                        groupObject.put("To", spinner.getSelectedItem().toString());
                        groupObject.save();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    chatEt.setText(null);
                    return true;
                }
                return false;
            }
        });

        chatIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatIv.setVisibility(View.INVISIBLE);
                chatLl.setVisibility(View.INVISIBLE);
            }
        });

        Parse.initialize(this, "GGhf5EisfvSx54MFMOYhF1Kugk2qTHeeEvCg5ymV", "mmaiRNaqOsqbQe5FqwA4M28EttAG3TOW43OfVXcw");

        SharedPreferences pref = getSharedPreferences("loginpref", Activity.MODE_MULTI_PROCESS );
        myId = pref.getString("loginid","");
        mem = pref.getString("mem","").split(",");
        groupId = pref.getString("groupId", "");

        spinner = (Spinner)findViewById(R.id.spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add("All");
        // アイテムを追加します
        for (String str:mem){
            if(!str.equals(myId))
                adapter.add(str);
        }
        spinner.setAdapter(adapter);


        handler = new Handler();

        googleMap =  ( (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map) ).getMap();
        mapInit();



        if(!isServiceRunning(this,SendService.class))
            startService(new Intent(this, SendService.class));

    }

    public void onClick(View v) {
        finish = true;
        ParseQuery<ParseObject> query = ParseQuery.getQuery("TestObject");//ParseObject型のParseQueryを取得する。
        query.whereEqualTo("USERID", myId);//そのクエリの中でReceiverがname変数のものを抜き出す。
        try {
            ParseObject testObject = query.find().get(0);
            testObject.put("LoginNow", "gB9xRLYJ3V4x");
            testObject.saveInBackground();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        stopService(new Intent(MapsActivity.this, SendService.class));
        ParseUser.logOutInBackground();
        finish();
    }

    public void chatBtn(View v){
        chatIv.setVisibility(View.VISIBLE);
        chatLl.setVisibility(View.VISIBLE);
    }

    String temp;

    public void receiveChat(){
        temp = "";

        ParseQuery<ParseObject> query = ParseQuery.getQuery(groupId);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> parselist, com.parse.ParseException e) {//その、name変数のものが見つかったとき
                if (e == null) {//エラーが無ければ
                    for (ParseObject parseObject : parselist) {
                        String message = parseObject.getString("Message");
                        String from = parseObject.getString("From");
                        String to = parseObject.getString("To");
                        if (to.equals(myId) || to.equals("All") || from.equals(myId))
                            temp = temp + from + ":" + message + "\n";
                    }
                    chatTv.setText(temp);
                }
            }
        });
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
                                receiveChat();
                            }
                        }).start();

                    }
                }, 5000, 5000);
        
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int nId = R.string.app_name;
        nm.cancel(nId);
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
            if(!id.equals(myId)) {//リリース時ここのコメントアウトを外す
                try {
                    ParseQuery<ParseObject> q = query.whereEqualTo("USERID", id);//そのクエリの中でReceiverがname変数のものを抜き出す。
                    ParseObject po = q.find().get(0);
                    final String obId = po.getObjectId();
                    final ParseQuery<ParseObject> que = ParseQuery.getQuery("TestObject");//その、ObjectIDで参照できるデータの内容をParseObject型のParseQueryで取得
                    if(que.get(obId).getString("LoginNow").equals(groupId)) {
                        try {
                            setMarker(i, que.get(obId).getDouble("Latitude"), que.get(obId).getDouble("Longiutude"));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }else{
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                marker.get(i).remove();
                                AlertDialog.Builder adb = new AlertDialog.Builder(MapsActivity.this);
                                adb.setCancelable(true);
                                adb.setTitle("通知");
                                adb.setMessage(id + "さんが退出しました");
                                adb.setPositiveButton("OK", null);
                                AlertDialog ad = adb.create();
                                ad.show();
                                List<String> list = new ArrayList<String>(Arrays.asList(mem)); // 新インスタンスを生成
                                list.remove(id);
                                mem = (String[]) list.toArray(new String[list.size()]);
                            }
                        });
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            i++;
            }
        }
    }

    public void setMarker(final int id,final double lat,final double lon){
        handler.post(
                new Runnable() {
                    @Override
                    public void run() {

                        if (marker.get(id) == null) {
                            LatLng latLng = new LatLng(lat, lon);
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(latLng);
                            marker.put(id, googleMap.addMarker(markerOptions));
                        }else{
                            LatLng latLng = new LatLng(lat, lon);
                            marker.get(id).setPosition(latLng);
                        }

                    }
                }
        );
    }


    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
        notificate();
    }

    public void notificate(){
        if(!finish) {
            Intent _intent = new Intent(this, MapsActivity.class);
            _intent.putExtra("name", myId);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, _intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setContentIntent(contentIntent);
            builder.setTicker("GPS de 鬼ごっこは実行中です");
            builder.setSmallIcon(R.mipmap.ic_launcher);//アイコン
            builder.setContentTitle("GPS de 鬼ごっこ");
            builder.setContentText("GPS鬼ごっこは実行中です。マップを表示。");
            builder.setOngoing(true);
            builder.setWhen(System.currentTimeMillis());


            int nId = R.string.app_name;
            nm.notify(nId, builder.build());
        }
    }
}