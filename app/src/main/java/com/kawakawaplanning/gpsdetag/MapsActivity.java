package com.kawakawaplanning.gpsdetag;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
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
    private Handler handler;
    private Map<Integer, Marker> marker= new HashMap<>();
    static public String[] mem;
    Timer timer;
    private NotificationManager nm;
    LinearLayout chatLl;
    ImageView chatIv;
    TextView chatTv;
    EditText chatEt;
    Spinner spinner;
    SharedPreferences pref;

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

        chatEt.setOnKeyListener((View v, int keyCode, KeyEvent event) -> {
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
        });

        chatIv.setOnClickListener((View v) -> {
            chatIv.setVisibility(View.INVISIBLE);
            chatLl.setVisibility(View.INVISIBLE);
        });

        Parse.initialize(this, "GGhf5EisfvSx54MFMOYhF1Kugk2qTHeeEvCg5ymV", "mmaiRNaqOsqbQe5FqwA4M28EttAG3TOW43OfVXcw");

        pref = getSharedPreferences("loginpref", Activity.MODE_MULTI_PROCESS );
        myId = pref.getString("loginid","");
        mem = pref.getString("mem","").split(",");
        groupId = pref.getString("groupId", "");

        spinner = (Spinner)findViewById(R.id.spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add("All");
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

        if(getIntent().getBooleanExtra("from",false)){
            chatBtn(null);
        }

    }

    public void onClick(View v) {
        AlertDialog.Builder adb = new AlertDialog.Builder(MapsActivity.this);
        adb.setTitle("確認");
        adb.setMessage("本当にログアウトしますか？");
        adb.setPositiveButton("OK",(DialogInterface dialog, int which) -> {
            finish = true;
            ParseQuery<ParseObject> query = ParseQuery.getQuery("TestObject");
            query.whereEqualTo("USERID", myId);
            query.findInBackground((List<ParseObject> list, ParseException e) -> {
                ParseObject testObject = list.get(0);
                testObject.put("LoginNow", "gB9xRLYJ3V4x");
                testObject.saveInBackground();
            });
            stopService(new Intent(MapsActivity.this, SendService.class));
            ParseUser.logOutInBackground();
            finish();
        });
        adb.setNegativeButton("Cancel", null);
        adb.show();
    }

    public void chatBtn(View v){
        chatIv.setVisibility(View.VISIBLE);
        chatLl.setVisibility(View.VISIBLE);
        chatTv.setText(SendService.chatTxt);
    }



    @Override
    protected void onResume() {
        super.onResume();
        timer = new Timer();
        timer.schedule(
                new TimerTask() {
                    public void run() {
                        handler.post(() -> chatTv.setText(SendService.chatTxt));

                        new Thread(() -> getLocate(mem)).start();
                    }
                }, 5000, 5000);

        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int nId = R.string.app_name;
        nm.cancel(nId);
        nm.cancel(nId+1);
        myId = pref.getString("loginid", "");
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
        CameraPosition cameraPos = new CameraPosition.Builder().target(new LatLng(38.2586, 137.6850)).zoom(4.5f).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
    }

    public void getLocate(final String name[]){

        ParseQuery<ParseObject> query = ParseQuery.getQuery("TestObject");
        i = 0;
        for(final String id:name) {
            if(!id.equals(myId)) {
                try {
                    ParseQuery<ParseObject> q = query.whereEqualTo("USERID", id);
                    ParseObject po = q.find().get(0);
                    final String obId = po.getObjectId();
                    final ParseQuery<ParseObject> que = ParseQuery.getQuery("TestObject");
                    if(que.get(obId).getString("LoginNow").equals(groupId)) {
                        try {
                            setMarker(i, que.get(obId).getString("UserName"),que.get(obId).getDouble("Latitude"), que.get(obId).getDouble("Longiutude"));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }else{
                        handler.post(()->{
                            marker.get(i).remove();
                            AlertDialog.Builder adb = new AlertDialog.Builder(MapsActivity.this);
                            adb.setCancelable(true);
                            adb.setTitle("通知");
                            adb.setMessage(id + "さんが退出しました");
                            adb.setPositiveButton("OK", null);
                            AlertDialog ad = adb.create();
                            ad.show();
                            List<String> list = new ArrayList<>(Arrays.asList(mem)); // 新インスタンスを生成
                            list.remove(id);
                            mem = list.toArray(new String[list.size()]);
                        });
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                i++;
            }
        }
    }

    public void setMarker(final int id,final String name,final double lat,final double lon){
        handler.post(() -> {
                    if (marker.get(id) == null) {
                        LatLng latLng = new LatLng(lat, lon);
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title(name);
                        marker.put(id, googleMap.addMarker(markerOptions));
                    }else{
                        LatLng latLng = new LatLng(lat, lon);
                        marker.get(id).setPosition(latLng);
                    }
                }
        );
    }


    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
        notificate();
        myId = null;
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