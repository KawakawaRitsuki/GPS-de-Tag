package com.kawakawaplanning.gpsdetag;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class MainActivity extends ActionBarActivity {

    private EditText mIdEt;
    private EditText mPwEt;
    private CheckBox mCheckBox;
    private Vibrator mVib;
    private ProgressDialog waitDialog;
    private AlertDialog ad;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Parse.initialize(this, "GGhf5EisfvSx54MFMOYhF1Kugk2qTHeeEvCg5ymV", "mmaiRNaqOsqbQe5FqwA4M28EttAG3TOW43OfVXcw");
        pref = getSharedPreferences("loginpref", Activity.MODE_MULTI_PROCESS );

        findView();
        autoLogin();
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
                signup();
                break;
        }
    }

    public void autoLogin(){
        if(pref.getBoolean("AutoLogin", false)){
            Wait("自動ログイン");

            ParseUser.logInInBackground(pref.getString("username", ""), pref.getString("password", ""), new LogInCallback() {
                public void done(ParseUser user, ParseException e) {
                    if (e == null) {
                        editor = pref.edit();
                        editor.putString("loginid", pref.getString("username", ""));
                        editor.commit();

                        waitDialog.dismiss();

                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, SelectGroupActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        switch (e.getCode()){
                            case 100:
                                alert("接続エラー","サーバーに接続できません。インターネット状態を確認してください。エラーコード:100");
                                break;
                            default:
                                alert("エラー","エラーが発生しました。少し時間を空けてお試しください。それでも直らない際はサポートに連絡してください。エラーコード:" + e.getCode());
                                break;
                        }
                    }
                }
            });

        }
    }

    public void login(){
        Wait("ログイン");
        String putId = mIdEt.getText().toString();
        if (putId == null){
            alert("入力エラー","IDが入力されていません。入力してください。");
        }else{
            ParseUser.logInInBackground(mIdEt.getText().toString(), mPwEt.getText().toString(), new LogInCallback() {
                public void done(ParseUser user, ParseException e) {
                    waitDialog.dismiss();
                    if (user != null) {

                        editor = pref.edit();
                        editor.putString("loginid", mIdEt.getText().toString());
                        editor.commit();
                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, SelectGroupActivity.class);
                        startActivity(intent);
                        finish();

                        if (mCheckBox.isChecked()) {
                            editor = pref.edit();
                            editor.putString("username", mIdEt.getText().toString());
                            editor.putString("password", mPwEt.getText().toString());
                            editor.putBoolean("AutoLogin", true);
                            editor.commit();
                        }

                        finish();

                    } else {
                        switch (e.getCode()){
                            case 101:
                                alert("ログインエラー","IDまたはパスワードが違います。もう一度試してください。エラーコード:101");
                                break;
                            case 100:
                                alert("接続エラー","サーバーに接続できません。インターネット状態を確認してください。エラーコード:100");
                                break;
                            default:
                                alert("エラー","エラーが発生しました。少し時間を空けてお試しください。それでも直らない際はサポートに連絡してください。エラーコード:" + e.getCode());
                                break;
                        }
                    }
                }

            });
        }
    }

    public void signup(){

        if( mIdEt.getText().toString().length() <= 3 && mPwEt.getText().toString().length() <= 5){
            alert("入力エラー","IDは4文字以上・PWは6文字以上にしてください。");
        } else {
            Wait("サインアップ");
            ParseUser user = new ParseUser();

            user.setUsername(mIdEt.getText().toString());
            user.setPassword(mPwEt.getText().toString());

            user.signUpInBackground(new SignUpCallback() {
                public void done(ParseException e) {

                    waitDialog.dismiss();

                    if (e == null) {
                        alert("登録完了","会員登録が完了しました。早速ログインボタンを押して鬼ごっこをしよう！");
                    } else {
                        switch (e.getCode()){
                            case 202:
                                alert("登録エラー","このIDは既に登録されています。別のIDで登録してください。エラーコード:202");
                                break;
                            case 100:
                                alert("接続エラー","サーバーに接続できません。インターネット状態を確認してください。エラーコード:100");
                                break;
                            default:
                                alert("エラー","エラーが発生しました。少し時間を空けてお試しください。それでも直らない際はサポートに連絡してください。エラーコード:" + e.getCode());
                                break;
                        }
                    }
                }
            });
        }
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
        ad = adb.create();
        ad.show();
    }
}