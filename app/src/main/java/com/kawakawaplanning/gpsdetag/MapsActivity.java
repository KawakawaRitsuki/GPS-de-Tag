package com.kawakawaplanning.gpsdetag;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

public class MapsActivity extends FragmentActivity implements LocationListener {

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    public double hereLat;
    public double hereLou;
    private Marker marker;

    public String myId = "KAWAKAWA";
    private Handler _handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Parse.initialize(this, "GGhf5EisfvSx54MFMOYhF1Kugk2qTHeeEvCg5ymV", "mmaiRNaqOsqbQe5FqwA4M28EttAG3TOW43OfVXcw");

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setUpMapIfNeeded();

        _handler = new Handler();
        _handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                sendLocate();
                getLocate("KAWAKAWA");
                _handler.postDelayed(this, 1000 * 10);
            }
        }, 1000 * 10);

        if (mLocationManager != null) {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
//                LocationManager.NETWORK_PROVIDER,
                    0,
                    0,
                    this);
        }
    }
    @Override
    protected void onPause() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }

        super.onPause();
    }



    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        getLocate("ABCD");

        mMap.addMarker(new MarkerOptions().position(new LatLng(34.413362, 131.393002)).title("門田"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(34.403209, 131.402953)).title("荒木"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(34.393724, 131.406778)).title("吉川"));
    }

    public void getLocate(final String name){
        new Thread(new Runnable() {
            @Override
            public void run() {

                ParseQuery<ParseObject> query = ParseQuery.getQuery("TestObject");//ParseObject型のParseQueryを取得する。
                query.whereEqualTo("USERID", name);//そのクエリの中でReceiverがname変数のものを抜き出す。
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> parselist, ParseException e) {//その、name変数のものが見つかったとき
                        if (e == null) {//エラーが無ければ

                            ParseObject po = parselist.get(0);
                            final String obId = po.getObjectId();

                            final ParseQuery<ParseObject> que = ParseQuery.getQuery("TestObject");//その、ObjectIDで参照できるデータの内容をParseObject型のParseQueryで取得

                                _handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try{
                                            if (marker != null)
                                            marker.remove();
                                            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(que.get(obId).getDouble("Latitude"), que.get(obId).getDouble("Longiutude"))).title("五嶋"));
                                        } catch (ParseException e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                });


                        }
                    }
                });
            }
        }).start();

    }



    public void sendLocate(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("TestObject");//ParseObject型のParseQueryを取得する。
                query.whereEqualTo("USERID", myId);//そのクエリの中でReceiverがname変数のものを抜き出す。
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> parselist, com.parse.ParseException e) {//その、name変数のものが見つかったとき
                        if (e == null) {//エラーが無ければ

                            ParseACL acl = new ParseACL();
                            acl.setPublicReadAccess(true);
                            acl.setPublicWriteAccess(true);

                            ParseObject testObject = parselist.get(0);

                            testObject.put("USERID", myId);
                            testObject.put("Latitude", hereLat);
                            testObject.put("Longiutude", hereLou);
                            testObject.setACL(acl);
                            testObject.saveInBackground();

                        }
                    }
                });
            }
        }).start();



    }

    @Override
    public void onLocationChanged(Location location) {
        hereLat = location.getLatitude();
        hereLou = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}
}
