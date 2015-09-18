package com.kawakawaplanning.gpsdetag;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kawakawaplanning.gpsdetag.http.HttpConnector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity {

    static public GoogleMap mGoogleMap;
    static public String mMyId;
    private String mGroupId;
    private Handler mHandler;
    private Map<Integer, Marker> mMarker= new HashMap<>();
    Timer mTimer;
    private NotificationManager mNm;
    SharedPreferences mPref;

    boolean finish = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mPref = getSharedPreferences("loginpref", Activity.MODE_MULTI_PROCESS);
        mMyId = mPref.getString("loginid", "");
        mGroupId = mPref.getString("groupId", "");

        SharedPreferences.Editor editor = mPref.edit();
        editor.putBoolean("loginnow", true);
        editor.apply();

        mHandler = new Handler();
        mGoogleMap =  ( (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map) ).getMap();

        mapInit();

        if(!isServiceRunning(this,SendService.class))
            startService(new Intent(this, SendService.class));

    }

    public void onClick(View v) {
        AlertDialog.Builder adb = new AlertDialog.Builder(MapsActivity.this);
        adb.setTitle("確認");
        adb.setMessage("終了しますか？");
        adb.setPositiveButton("OK", (DialogInterface dialog, int which) -> {
            finish = true;
            HttpConnector httpConnector = new HttpConnector("grouplogout", "{\"user_id\":\"" + mMyId + "\"}");
            httpConnector.setOnHttpResponseListener((String message) -> {
                if (Integer.parseInt(message) == 0) {
                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putBoolean("loginnow", false);
                    editor.apply();
                    stopService(new Intent(this, SendService.class));
                    finish();
                } else {
                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putBoolean("loginnow", false);
                    editor.apply();
                    Toast.makeText(MapsActivity.this, "サーバーエラーが発生しました。強制的に終了しました。", Toast.LENGTH_SHORT).show();
                    stopService(new Intent(this, SendService.class));
                    finish();
                }
            });
            httpConnector.setOnHttpErrorListener((int error) -> {
                android.support.v7.app.AlertDialog.Builder adbs = new android.support.v7.app.AlertDialog.Builder(MapsActivity.this);
                adbs.setTitle("接続エラー");
                adbs.setMessage("接続エラーが発生しました。インターネットの接続状態を確認して下さい。");
                adbs.setPositiveButton("OK", null);
                adbs.setCancelable(true);
                adbs.show();
            });
            httpConnector.post();
        });
        adb.setNegativeButton("Cancel", null);
        adb.show();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            onClick(null);
            return super.dispatchKeyEvent(event);
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mTimer = new Timer();
        mTimer.schedule(
                new TimerTask() {
                    public void run() {
                        new Thread(MapsActivity.this::setLocate).start();
                    }
                }, 1000, 1000);

        mNm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int nId = R.string.app_name;
        mNm.cancel(nId);
        mNm.cancel(nId + 1);
        mMyId = mPref.getString("loginid", "");
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
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.setMyLocationEnabled(true);
        CameraPosition cameraPos = new CameraPosition.Builder().target(new LatLng(38.2586, 137.6850)).zoom(4.5f).build();
        mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
    }

    public void setLocate(){
        mHandler.post(() -> {
            HttpConnector httpConnector = new HttpConnector("getlocate", "{\"group_id\":\"" + mGroupId + "\"}");
            httpConnector.setOnHttpResponseListener((String message) -> {
                Log.v("kp", message);
                try {
                    JSONObject json = new JSONObject(message);
                    JSONArray data = json.getJSONArray("data");

                    for (int i = 0; i != data.length(); i++) {
                        JSONObject object = data.getJSONObject(i);
                        setMarker(i, object.getString("user_name"), object.getDouble("latitude"), object.getDouble("longitude"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            });
            httpConnector.post();
        });

    }

    public void setMarker(final int id,final String name,final double lat,final double lon){
        mHandler.post(() -> {
                    if (mMarker.get(id) == null) {
                        LatLng latLng = new LatLng(lat, lon);
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title(name);
                        mMarker.put(id, mGoogleMap.addMarker(markerOptions));
                    } else {
                        LatLng latLng = new LatLng(lat, lon);
                        mMarker.get(id).setPosition(latLng);
                    }
                }
        );
    }


    @Override
    protected void onStop() {
        super.onStop();
        mTimer.cancel();
        notification();
        mMyId = null;
    }

    public void notification(){
        if(!finish) {
            Intent _intent = new Intent(this, MapsActivity.class);
            _intent.putExtra("name", mMyId);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 510, _intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setContentIntent(contentIntent);
            builder.setTicker("集まれ！は実行中です");
            builder.setSmallIcon(R.drawable.ic_stat_name);//アイコン
            builder.setContentTitle("集まれ！");
            builder.setContentText("集まれ！は実行中です。マップを表示。");
            builder.setOngoing(true);
            builder.setWhen(System.currentTimeMillis());

            int nId = R.string.app_name;
            mNm.notify(nId, builder.build());
        }
    }
}