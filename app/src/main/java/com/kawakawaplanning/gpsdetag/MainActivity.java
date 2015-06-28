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

    private EditText idEt;
    private EditText pwEt;
    private CheckBox checkBox;
    private String TAG = "KP";
    private Vibrator vib;
    private ProgressDialog waitDialog;
    private AlertDialog.Builder alertDialogBuilder;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        idEt =      (EditText) findViewById     (R.id.idedit);
        pwEt =      (EditText) findViewById     (R.id.pwedit);
        checkBox =  (CheckBox) findViewById     (R.id.checkBox1);
        vib =       (Vibrator) getSystemService (VIBRATOR_SERVICE);

        Parse.initialize(this, "GGhf5EisfvSx54MFMOYhF1Kugk2qTHeeEvCg5ymV", "mmaiRNaqOsqbQe5FqwA4M28EttAG3TOW43OfVXcw");

        pref = getSharedPreferences("loginpref", Activity.MODE_MULTI_PROCESS );

        autoLogin();

    }

    public void onClick(View v){
        vib.vibrate(50);
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
        if(pref.getBoolean("flag", false)){
            Wait("自動ログイン");

            ParseUser.logInInBackground(pref.getString("username", ""), pref.getString("password", ""), new LogInCallback() {
                public void done(ParseUser user, ParseException e) {
                    if (e == null) {
                        editor = pref.edit();
                        editor.putString("loginid", pref.getString("username", ""));
                        editor.commit();

                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, SelectGroupActivity.class);
//                        intent.putExtra("name", pref.getString("username", ""));
                        startActivity(intent);
                        finish();
                    } else if (e.getCode() == 100) {
                        alert("接続エラー", "サーバーに接続できません。インターネット状態を確認してください。エラーコード:100");
                    } else {
                        alert("エラー", "エラーが発生しました。少し時間を空けてお試しください。それでも直らない際はサポートに連絡してください。エラーコード:" + e.getCode());
                    }
                }
            });

        }
    }

    public void login(){
        Wait("ログイン");
        String putId = idEt.getText().toString();
        if (putId == null){
            alert("入力エラー","IDが入力されていません。入力してください。");
        }else{
            ParseUser.logInInBackground(idEt.getText().toString(), pwEt.getText().toString(), new LogInCallback() {
                public void done(ParseUser user, ParseException e) {
                    waitDialog.dismiss();
                    if (user != null) {
                        editor = pref.edit();
                        editor.putString("loginid", idEt.getText().toString());
                        Intent intent=new Intent();
                        intent.setClass(MainActivity.this, SelectGroupActivity.class);
//                        intent.putExtra("name", idEt.getText().toString());
                        startActivity(intent);
                        if (checkBox.isChecked()){
                            editor = pref.edit();
                            editor.putString("username", idEt.getText().toString());
                            editor.putString("password", pwEt.getText().toString());
                            editor.putBoolean("flag",true);
                            editor.commit();
                        }
                        finish();

                    } else if (e.getCode() == 101){
                        alert("ログインエラー","IDまたはパスワードが違います。もう一度試してください。エラーコード:101");
                    } else if (e.getCode() == 100){
                        alert("接続エラー","サーバーに接続できません。インターネット状態を確認してください。エラーコード:100");
                    }else{
                        alert("エラー","エラーが発生しました。少し時間を空けてお試しください。それでも直らない際はサポートに連絡してください。エラーコード:" + e.getCode());

                    }
                }

            });
        }
    }

    public void signup(){

        if( idEt.getText().toString().length() <= 3 && pwEt.getText().toString().length() <= 5){
            alert("入力エラー","IDは4文字以上・PWは6文字以上にしてください。");
        } else {
            Wait("サインアップ");
            ParseUser user = new ParseUser();
            user.setUsername(idEt.getText().toString());
            user.setPassword(pwEt.getText().toString());
            user.signUpInBackground(new SignUpCallback() {
                public void done(ParseException e) {
                    waitDialog.dismiss();
                    if (e == null) {
                        alert("登録完了","会員登録が完了しました。早速ログインボタンを押して鬼ごっこをしよう！");
                    } else if (e.getCode() == 202){
                        alert("登録エラー","このIDは既に登録されています。別のIDで登録してください。エラーコード:202");
                    } else if (e.getCode() == 100){
                        alert("接続エラー","サーバーに接続できません。インターネット状態を確認してください。エラーコード:100");
                    }else{
                        alert("エラー","エラーが発生しました。少し時間を空けてお試しください。それでも直らない際はサポートに連絡してください。エラーコード:" + e.getCode());
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
        alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle(til);
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setPositiveButton("OK",null);
        alertDialogBuilder.show();
    }
}