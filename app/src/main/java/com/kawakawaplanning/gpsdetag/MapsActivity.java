package com.kawakawaplanning.gpsdetag;

import android.app.AlertDialog;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements LocationListener {

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    public double hereLat;
    public double hereLou;

    public String myId;
    private Handler _handler;

    private Map<String, Marker> marker= new HashMap<String, Marker>();

    private ArrayList<String> members;
    ImageView iv[];

    private int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        members = new ArrayList<String>();

        Parse.initialize(this, "GGhf5EisfvSx54MFMOYhF1Kugk2qTHeeEvCg5ymV", "mmaiRNaqOsqbQe5FqwA4M28EttAG3TOW43OfVXcw");

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        myId = getIntent().getStringExtra("name");

        TextView tv = (TextView)findViewById(R.id.textView);
        tv.setText("ようこそ！" + myId + "さん");


    }

    @Override
    protected void onResume() {
        super.onResume();

        setUpMapIfNeeded();

        getGroup();

    }
    @Override
    protected void onPause() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }

        super.onPause();
    }

    public void getGroup(){

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Group");//ParseObject型のParseQueryを取得する。
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> parselist, ParseException e) {//その、name変数のものが見つかったとき
                if (e == null) {//エラーが無ければ

                    for (ParseObject po : parselist) {
                        final String obId = po.getObjectId();

                        final ParseQuery<ParseObject> que = ParseQuery.getQuery("Group");//その、ObjectIDで参照できるデータの内容をParseObject型のParseQueryで取得

                        try {
                            String member = que.get(obId).getString("Members");
                            String[] memberSpr = que.get(obId).getString("Members").split(",");

                            boolean frag = false;
                            for (String str : memberSpr) {
                                if (str.equals(myId))
                                    frag = true;
                            }

                            if (frag)
                                members.add(member);

                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                    }

                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapsActivity.this);
                    LayoutInflater inflater = (LayoutInflater) MapsActivity.this.getSystemService(
                            LAYOUT_INFLATER_SERVICE);
                    View view = inflater.inflate(R.layout.dialog_select,
                            (ViewGroup) findViewById(R.id.root));
                    final ListView lv = (ListView) view.findViewById(R.id.listView);
                    alertDialogBuilder.setTitle("グループ選択");
                    alertDialogBuilder.setView(view);
                    alertDialogBuilder.setCancelable(true);
                    final AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                    final ArrayAdapter adapter = new ArrayAdapter(MapsActivity.this,
                            android.R.layout.simple_list_item_1, members);
                    lv.setAdapter(adapter);
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                            alertDialog.dismiss();

                            final String mem[] = adapter.getItem(position).toString().split(",");
                            final boolean log[] = new boolean[mem.length];


                            AlertDialog.Builder alert = new AlertDialog.Builder(MapsActivity.this);
                            // タイトル設定
                            alert.setTitle("ログイン待ち");
                            // ページ作成.(適当に)
                            LinearLayout ll = new LinearLayout(MapsActivity.this);
                            ll.setOrientation(LinearLayout.VERTICAL);

                            TextView textTitle = new TextView(MapsActivity.this);
                            textTitle.setText("メンバーのログインを待っています");
                            textTitle.setTextSize(20);
                            ll.addView(textTitle, 0);

                            iv = new ImageView[mem.length];

                            for (int a = 0; a != iv.length; a++) {
                                LinearLayout linearLayout = new LinearLayout(MapsActivity.this);
                                iv[a] = new ImageView(MapsActivity.this);
                                if (loginNow(mem[a])) {
                                    iv[a].setImageResource(R.drawable.icon_success);
                                    log[a] = true;
                                } else {
                                    iv[a].setImageResource(R.drawable.icon_error);
                                    log[a] = false;
                                }
                                iv[a].setLayoutParams(new FrameLayout.LayoutParams(100, 100));

                                TextView tv = new TextView(MapsActivity.this);
                                tv.setText(mem[a]);
                                tv.setTextSize(20);
                                tv.setGravity(Gravity.CENTER);

                                linearLayout.addView(iv[a], 0);
                                linearLayout.addView(tv, 1);

                                ll.addView(linearLayout, a + 1);
                            }

                            // ダイアログにviewを設定
                            alert.setView(ll);
                            alert.setCancelable(false);
                            final AlertDialog alertDialog = alert.create();
                            alertDialog.show();


                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                    public void run() {

                                        for (int a = 0; a != iv.length; a++) {
                                            if (loginNow(mem[a])) {
                                                iv[a].setImageResource(R.drawable.icon_success);
                                                log[a] = true;
                                            } else {
                                                iv[a].setImageResource(R.drawable.icon_error);
                                                log[a] = false;
                                            }
                                        }
                                        boolean frag = true;
                                        for(boolean b:log){
                                            if(!b)
                                                frag = false;
                                        }
                                        if(frag)
                                            handler.removeCallbacks(this);

                                    alertDialog.dismiss();

                                    _handler = new Handler();
                                    _handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            sendLocate();
                                            getLocate(adapter.getItem(position).toString().split(","));
                                            _handler.postDelayed(this, 1000 * 10);
                                        }
                                    }, 1000 * 10);

                                    if (mLocationManager != null) {
                                        mLocationManager.requestLocationUpdates(
                                                LocationManager.GPS_PROVIDER,
                //                                      LocationManager.NETWORK_PROVIDER,
                                                0,
                                                0,
                                                MapsActivity.this);

                                    }

                                    handler.postDelayed(this, 1000 * 1);
                                }
                            }, 1000 * 1);




//                            Log.v("kp", "吉川:" + loginNow(adapter.getItem(position).toString().split(",")[0]));
//                            Log.v("kp", "五嶋:"+loginNow(adapter.getItem(position).toString().split(",")[1]));
//                            Log.v("kp", "荒木:"+loginNow(adapter.getItem(position).toString().split(",")[2]));

                        }
                    });
                }
            }
        });

    }

    public boolean loginNow(String userName){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("TestObject");//ParseObject型のParseQueryを取得する。
        query.whereEqualTo("USERID", userName);
        try {
            List<ParseObject> list = query.find();

            ParseObject po = list.get(0);
            final String obId = po.getObjectId();

            final ParseQuery<ParseObject> que = ParseQuery.getQuery("TestObject");//その、ObjectIDで参照できるデータの内容をParseObject型のParseQueryで取得

            try {
                return que.get(obId).getBoolean("LoginNow");
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();

        }
    }
    public void getLocate(final String name[]){

        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(name.length);
                i = 0;
                for(final String id:name) {
                    if(!name.equals(myId)) {
                        ParseQuery<ParseObject> query = ParseQuery.getQuery("TestObject");//ParseObject型のParseQueryを取得する。
                        query.whereEqualTo("USERID", id);//そのクエリの中でReceiverがname変数のものを抜き出す。
                        query.findInBackground(new FindCallback<ParseObject>() {
                            public void done(List<ParseObject> parselist, ParseException e) {//その、name変数のものが見つかったとき
                                if (e == null) {//エラーが無ければ
                                    ParseObject po = parselist.get(0);
                                    final String obId = po.getObjectId();
                                    final ParseQuery<ParseObject> que = ParseQuery.getQuery("TestObject");//その、ObjectIDで参照できるデータの内容をParseObject型のParseQueryで取得

                                    _handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                if (marker.get(id) != null)
                                                    marker.get(id).remove();
                                                marker.put(id,mMap.addMarker(new MarkerOptions().position(new LatLng(que.get(obId).getDouble("Latitude"), que.get(obId).getDouble("Longiutude"))).title("五嶋")));
                                            } catch (ParseException e1) {
                                                e1.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                    i++;
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
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> parselist, com.parse.ParseException e) {//その、name変数のものが見つかったとき
                        if (e == null) {//エラーが無ければ

                            ParseACL acl = new ParseACL();
                            acl.setPublicReadAccess(true);
                            acl.setPublicWriteAccess(true);

                            if (parselist.size() != 0) {
                                ParseObject testObject = parselist.get(0);
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
                        }
                    }
                });
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
        finish();

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
