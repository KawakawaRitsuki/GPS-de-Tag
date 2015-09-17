package com.kawakawaplanning.gpsdetag;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.kawakawaplanning.gpsdetag.http.HttpConnector;

public class MainActivity extends AppCompatActivity {

    private EditText mIdEt;
    private EditText mPwEt;
    private CheckBox mCheckBox;
    private Vibrator mVib;
    private ProgressDialog waitDialog;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = getSharedPreferences("loginpref", Activity.MODE_MULTI_PROCESS );

        if(pref.getBoolean("loginnow",false)) {
            Intent intent = new Intent();
            intent.setClassName("com.kawakawaplanning.gpsdetag", "com.kawakawaplanning.gpsdetag.MapsActivity");
            startActivity(intent);
//            finish();
        }else{
            autoLogin();
        }

        findView();

        mPwEt.setOnKeyListener((View v, int keyCode, KeyEvent event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                login();
                return true;
            }
            return false;
        });
    }

    private void findView(){
        mIdEt =      (EditText) findViewById     (R.id.idedit);
        mPwEt =      (EditText) findViewById     (R.id.pwedit);
        mCheckBox =  (CheckBox) findViewById     (R.id.checkBox1);
        mVib =       (Vibrator) getSystemService (VIBRATOR_SERVICE);
    }

    public void onClick(View v){
        mVib.vibrate(50);
        switch (v.getId()) {
            case R.id.loginbutton:
                login();
                break;

            case R.id.signinbutton:
                signUp();
                break;
        }
    }

    public void autoLogin(){
        if(pref.getBoolean("AutoLogin", false)){
            Wait("自動ログイン");

            HttpConnector httpConnector = new HttpConnector("login","{\"user_id\":\""+pref.getString("username", "")+"\",\"password\":\""+pref.getString("password", "")+"\"}");
            httpConnector.setOnHttpResponseListener((String message) -> {
                Log.v("tag", message);
                waitDialog.dismiss();
                if (Integer.parseInt(message) == 0) {
                    editor = pref.edit();
                    editor.putString("loginid", pref.getString("username", ""));
                    editor.apply();
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, SelectGroupActivity.class);
                    startActivity(intent);
//                    finish();
                } else {
                    alert("ログインエラー", "IDまたはパスワードが違います。もう一度試してください。エラーコード:1");
                }

            });
            httpConnector.setOnHttpErrorListener((int error) -> {
                if(waitDialog != null)
                waitDialog.dismiss();
                android.support.v7.app.AlertDialog.Builder adb = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
                adb.setTitle("接続エラー");
                adb.setMessage("接続エラーが発生しました。インターネットの接続状態を確認して下さい。");
                adb.setPositiveButton("OK", null);
                adb.setCancelable(true);
                adb.show();
            });
            httpConnector.post();

        }
    }

    public void login(){

        if ( mIdEt.getText().toString().length() == 0 || mPwEt.getText().toString().length() == 0){
            alert("入力エラー","ID/PWが入力されていません。入力してください。");
        }else{
            Wait("ログイン");

            HttpConnector httpConnector = new HttpConnector("login","{\"user_id\":\""+mIdEt.getText().toString()+"\",\"password\":\""+mPwEt.getText().toString()+"\"}");
            httpConnector.setOnHttpResponseListener((String message) -> {
                Log.v("tag", message);
                waitDialog.dismiss();
                if(Integer.parseInt(message) == 0){
                    editor = pref.edit();
                    editor.putString("loginid", mIdEt.getText().toString());
                    editor.apply();
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, SelectGroupActivity.class);
                    startActivity(intent);

                    if (mCheckBox.isChecked()) {
                        editor = pref.edit();
                        editor.putString("username", mIdEt.getText().toString());
                        editor.putString("password", mPwEt.getText().toString());
                        editor.putBoolean("AutoLogin", true);
                        editor.apply();
                    }
//                    finish();
                }else{
                    alert("ログインエラー","IDまたはパスワードが違います。もう一度試してください。エラーコード:1");
                }


            });
            httpConnector.setOnHttpErrorListener((int error) -> {
                waitDialog.dismiss();
                android.support.v7.app.AlertDialog.Builder adb = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
                adb.setTitle("接続エラー");
                adb.setMessage("接続エラーが発生しました。インターネットの接続状態を確認して下さい。");
                adb.setPositiveButton("OK", null);
                adb.setCancelable(true);
                adb.show();
            });
            httpConnector.post();
        }
    }

    public void signUp(){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    private void Wait(String what){
        waitDialog = new ProgressDialog(this);
        waitDialog.setMessage(what + "中...");
        waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        waitDialog.setCanceledOnTouchOutside(false);
        waitDialog.show();
    }
    private void alert(String til,String msg){
        AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
        adb.setTitle(til);
        adb.setMessage(msg);
        adb.setPositiveButton("OK", null);
        adb.show();
    }

}