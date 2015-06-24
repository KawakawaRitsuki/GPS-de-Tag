package com.kawakawaplanning.gpsdetag;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.widget.FrameLayout;
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
    private String myId;
    private String mem[];
    private boolean log[];
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myId = getIntent().getStringExtra("name");

        mem = getIntent().getStringExtra("selectGroup").split(",");
        log = new boolean[mem.length];

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(16, 16, 16, 16);

        TextView textTitle = new TextView(this);
        textTitle.setText("メンバーのログインを待っています");
        textTitle.setTextSize(20);
        ll.addView(textTitle, 0);



        setContentView(ll);

        iv = new ImageView[mem.length];

        for (int a = 0; a != iv.length; a++) {//グループの人数分生成
            LinearLayout linearLayout = new LinearLayout(this);
            iv[a] = new ImageView(this);
            if (loginNow(mem[a])) {
                iv[a].setImageResource(R.drawable.icon_success);
                log[a] = true;
            } else {
                iv[a].setImageResource(R.drawable.icon_error);
                log[a] = false;
            }
            iv[a].setLayoutParams(new FrameLayout.LayoutParams(100, 100));
            TextView tv = new TextView(this);
            tv.setText(mem[a]);
            tv.setTextSize(20);
            tv.setGravity(Gravity.CENTER);

            linearLayout.addView(iv[a], 0);
            linearLayout.addView(tv, 1);

            ll.addView(linearLayout, a + 1);
        }

        Parse.initialize(this, "GGhf5EisfvSx54MFMOYhF1Kugk2qTHeeEvCg5ymV", "mmaiRNaqOsqbQe5FqwA4M28EttAG3TOW43OfVXcw");


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
                        for (int a = 0; a != iv.length; a++) {
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

                    Intent intent = new Intent();
                    intent.setClassName("com.kawakawaplanning.gpsdetag", "com.kawakawaplanning.gpsdetag.MapsActivity");
                    intent.putExtra("name", myId);
                    intent.putExtra("selectGroup", mem);
                    startActivity(intent);

                    timer.cancel();

                    finish();
                }
            }
        };


        timer.schedule(task, 1000, 1000);
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
}
