package com.kawakawaplanning.gpsdetag;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class WaitMemberActivity extends ActionBarActivity {

    private ImageView iv[];
    private LinearLayout ll[];
    private TextView tv[];
    
    private String myId;
    private String mem[];
    private String groupId;
    private boolean log[];
    private Timer timer;
    private Handler mHandler;

    private void findView() {
        iv = new ImageView[5];
        iv[0] = (ImageView) findViewById(R.id.iv1);
        iv[1] = (ImageView) findViewById(R.id.iv2);
        iv[2] = (ImageView) findViewById(R.id.iv3);
        iv[3] = (ImageView) findViewById(R.id.iv4);
        iv[4] = (ImageView) findViewById(R.id.iv5);
        
        ll = new LinearLayout[5];
        ll[0] = (LinearLayout) findViewById(R.id.ll1);
        ll[1] = (LinearLayout) findViewById(R.id.ll2);
        ll[2] = (LinearLayout) findViewById(R.id.ll3);
        ll[3] = (LinearLayout) findViewById(R.id.ll4);
        ll[4] = (LinearLayout) findViewById(R.id.ll5);

        tv = new TextView[5];
        tv[0] = (TextView) findViewById(R.id.tv1);
        tv[1] = (TextView) findViewById(R.id.tv2);
        tv[2] = (TextView) findViewById(R.id.tv3);
        tv[3] = (TextView) findViewById(R.id.tv4);
        tv[4] = (TextView) findViewById(R.id.tv5);
        
        for(int i=0; i != mem.length;i++)
            ll[i].setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences pref = getSharedPreferences("loginpref", Activity.MODE_MULTI_PROCESS );
        myId = pref.getString("loginid", "");
        mem = pref.getString("mem", "").split(",");
        groupId = pref.getString("groupId", "");
        mHandler = new Handler();

        setContentView(R.layout.activity_member_wait);
        findView();

        for(int i = 0; i != mem.length;i++){
            tv[i].setText(mem[i]);
        }

        log = new boolean[mem.length];

        Parse.initialize(this, "GGhf5EisfvSx54MFMOYhF1Kugk2qTHeeEvCg5ymV", "mmaiRNaqOsqbQe5FqwA4M28EttAG3TOW43OfVXcw");


        ParseQuery<ParseObject> query = ParseQuery.getQuery("TestObject");//ParseObject型のParseQueryを取得する。
        query.whereEqualTo("USERID", myId);//そのクエリの中でReceiverがname変数のものを抜き出す。
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> parselist, com.parse.ParseException e) {//その、name変数のものが見つかったとき
                if (e == null) {//エラーが無ければ
                    if (parselist.size() != 0) {
                        ParseObject testObject = parselist.get(0);
                        testObject.put("USERID", myId);
                        testObject.put("LoginNow", groupId);
                        testObject.saveInBackground();
                    } else {
                        ParseObject testObject = new ParseObject("TestObject");
                        testObject.put("USERID", myId);
                        testObject.put("LoginNow", groupId);
                        testObject.saveInBackground();
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        timer = new Timer();
        final Handler handler = new Handler();
        TimerTask task = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (int a = 0; a != mem.length; a++) {
                            if (loginNow(mem[a])) {
                                iv[a].setImageResource(R.drawable.icon_success);
                                log[a] = true;
                            } else {
                                iv[a].setImageResource(R.drawable.icon_error);
                                log[a] = false;
                            }
                        }
                    }
                });

                boolean frag = true;
                for (boolean b : log) {
                    if (!b)
                        frag = false;
                }
                if (frag) {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery(groupId);
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> list, ParseException e) {
                            for (ParseObject po:list){
                                po.deleteInBackground();
                            }
                        }
                    });

                    Intent intent = new Intent();
                    intent.setClassName("com.kawakawaplanning.gpsdetag", "com.kawakawaplanning.gpsdetag.MapsActivity");
                    startActivity(intent);
                    timer.cancel();
                    finish();
                }
            }
        };
        timer.schedule(task, 500, 1000);
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
                if(groupId.equals(que.get(obId).getString("LoginNow"))){
                    return true;
                }else{
                    return false;
                }

            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }
}