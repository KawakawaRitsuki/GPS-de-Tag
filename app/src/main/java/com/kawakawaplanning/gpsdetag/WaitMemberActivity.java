package com.kawakawaplanning.gpsdetag;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.widget.ListView;
import android.widget.Toast;

import com.kawakawaplanning.gpsdetag.http.HttpConnector;
import com.kawakawaplanning.gpsdetag.list.CustomAdapter;
import com.kawakawaplanning.gpsdetag.list.CustomData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class WaitMemberActivity extends ActionBarActivity {
    
    private String myId;
    private String groupId;

    private Timer mTimer;
    private Handler mHandler;
    private ListView mListView;

    private Boolean mIntentFlag = false;

    private void findView() {
        mListView = (ListView)findViewById(R.id.listView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences pref = getSharedPreferences("loginpref", Activity.MODE_MULTI_PROCESS );
        myId = pref.getString("loginid", "");
        groupId = pref.getString("groupId", "");

        mHandler = new Handler();

        setContentView(R.layout.activity_member_wait);
        findView();

        HttpConnector httpConnector = new HttpConnector("grouplogin","{\"user_id\":\""+myId+"\",\"group_id\":\""+groupId+"\"}");
        httpConnector.setOnHttpResponseListener((String message) -> {
            if (Integer.parseInt(message) == 1) {
                Toast.makeText(WaitMemberActivity.this, "サーバーエラーが発生しました。時間を開けてお試しください。", Toast.LENGTH_SHORT).show();
            }
        });
        httpConnector.post();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            startActivity(new Intent(getApplicationContext(),SelectGroupActivity.class));
            finish();
            return true;
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();

        mTimer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                if(!mIntentFlag) {
                    mHandler.post(() -> loginCheck(groupId));
                }
            }
        };
        mTimer.schedule(task, 0, 1000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTimer.cancel();
        if(!mIntentFlag) {
            HttpConnector httpConnector = new HttpConnector("grouplogin", "{\"user_id\":\"" + myId + "\",\"group_id\":\"\"}");
            httpConnector.setOnHttpResponseListener((String message) -> {
                if (Integer.parseInt(message) == 1) {
                    Toast.makeText(WaitMemberActivity.this, "サーバーエラーが発生しました。時間を開けてお試しください。", Toast.LENGTH_SHORT).show();
                }
            });
            httpConnector.post();
        }
    }


    public void loginCheck(String groupId){
        HttpConnector httpConnector = new HttpConnector("loginstate","{\"group_id\":\"" + groupId + "\"}");
        httpConnector.setOnHttpResponseListener((String message) -> {
            Bitmap successImage = BitmapFactory.decodeResource(getResources(), R.drawable.icon_success);
            Bitmap errorImage = BitmapFactory.decodeResource(getResources(), R.drawable.icon_error);

            List<CustomData> objects = new ArrayList<>();

            try {
                JSONObject json = new JSONObject(message);
                JSONArray data = json.getJSONArray("data");

                Boolean flag = true;
                for (int i = 0; i != data.length(); i++) {

                    JSONObject object = data.getJSONObject(i);
                    CustomData item = new CustomData();
                    if(object.getString("login_now").equals(groupId)) {
                        item.setImagaData(successImage);
                    } else {
                        item.setImagaData(errorImage);
                        flag=false;
                    }

                    item.setTextData(object.getString("user_name") + "(" + object.getString("user_id") + ")");
                    objects.add(item);
                }
                if(flag) {
                    mIntentFlag = true;
                    Intent intent = new Intent();
                    intent.setClassName("com.kawakawaplanning.gpsdetag", "com.kawakawaplanning.gpsdetag.MapsActivity");
                    startActivity(intent);
                    finish();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            CustomAdapter customAdapter = new CustomAdapter(this, 0, objects);
            mListView.setAdapter(customAdapter);

        });
        httpConnector.post();
    }
}