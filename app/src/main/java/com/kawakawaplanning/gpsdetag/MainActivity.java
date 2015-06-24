package com.kawakawaplanning.gpsdetag;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.List;

public class MainActivity extends ActionBarActivity {

    EditText idet;
    EditText pwet;
    Button loginButton;
    Button signinButton;
    CheckBox checkBox;
    View loginView;
    View signinView;
    String TAG;
    Boolean flag;
    Vibrator vib ;
    ProgressDialog waitDialog;
    AlertDialog.Builder alertDialogBuilder;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        idet = (EditText)findViewById(R.id.idedit);
        pwet = (EditText)findViewById(R.id.pwedit);
        loginView = findViewById(R.id.loginbutton);
        checkBox = (CheckBox)findViewById(R.id.checkBox1);
        vib = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        TAG = "KP";
        flag = false;

        Parse.initialize(this, "GGhf5EisfvSx54MFMOYhF1Kugk2qTHeeEvCg5ymV", "mmaiRNaqOsqbQe5FqwA4M28EttAG3TOW43OfVXcw");

        pref = getSharedPreferences("loginpref", Activity.MODE_PRIVATE);
        if(pref.getBoolean("flag", false)){
            Wait("自動ログイン");

            ParseUser.logInInBackground(pref.getString("username", ""), pref.getString("password", ""), new LogInCallback() {
                public void done(ParseUser user, ParseException e) {
                    waitDialog.dismiss();
                    if (e == null) {
                        Log.d(TAG, "ログイン 成功");

                        ParseQuery<ParseObject> query = ParseQuery.getQuery("TestObject");//ParseObject型のParseQueryを取得する。
                        query.whereEqualTo("USERID", pref.getString("username", ""));//そのクエリの中でReceiverがname変数のものを抜き出す。
                        query.findInBackground(new FindCallback<ParseObject>() {
                            public void done(List<ParseObject> parselist, com.parse.ParseException e) {//その、name変数のものが見つかったとき
                                if (e == null) {//エラーが無ければ
                                    if (parselist.size() != 0) {
                                        ParseObject testObject = parselist.get(0);

                                        testObject.put("USERID", pref.getString("username", ""));
                                        testObject.put("LoginNow", true);
                                        testObject.saveInBackground();
                                    } else {
                                        ParseObject testObject = new ParseObject("TestObject");

                                        testObject.put("USERID", pref.getString("username", ""));
                                        testObject.put("LoginNow", true);
                                        testObject.saveInBackground();
                                    }
                                } else {
                                    e.printStackTrace();
                                }
                            }
                        });

                        Intent intent=new Intent();
                        intent.setClassName("com.kawakawaplanning.gpsdetag","com.kawakawaplanning.gpsdetag.SelectGroupActivity");
                        intent.putExtra("name",pref.getString("username", "") );
                        startActivity(intent);
                        finish();
                    }else if (e.getCode() == 100){
                        alert("接続エラー","サーバーに接続できません。インターネット状態を確認してください。エラーコード:100");

                    }else{
                        alert("エラー","エラーが発生しました。少し時間を空けてお試しください。それでも直らない際はサポートに連絡してください。エラーコード:" + e.getCode());
                    }
                }
            });

        }
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

    public void login(){
        Wait("ログイン");
        String a = null;
        a = idet.getText().toString();
        if (a == null){
            alert("入力エラー","IDが入力されていません。入力してください。");
        }else{
            ParseUser.logInInBackground(idet.getText().toString(), pwet.getText().toString(), new LogInCallback() {
                public void done(ParseUser user, ParseException e) {
                    waitDialog.dismiss();
                    if (user != null) {
                        Log.d(TAG, "ログイン 成功");



                        Intent intent=new Intent();
                        intent.setClassName("com.kawakawaplanning.gpsdetag","com.kawakawaplanning.gpsdetag.SelectGroupActivity");
                        intent.putExtra("name",idet.getText().toString() );
                        startActivity(intent);
                        if (checkBox.isChecked()){
                            editor = pref.edit();
                            // Editor に値を代入
                            editor.putString("username",idet.getText().toString());
                            editor.putString("password",pwet.getText().toString());
                            editor.putBoolean("flag",true);
                            // データの保存
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


        if( flag == false && idet.getText().toString().length() == 0 && pwet.getText().toString().length() == 0){
            alert("入力エラー","ID・PWが入力されていません。");
            flag = true;
        }
        if( flag == false && idet.getText().toString().length() == 0 && pwet.getText().toString().length() != 0){
            alert("入力エラー","IDが入力されていません。");
            flag = true;
        }
        if( flag == false && idet.getText().toString().length() != 0 && pwet.getText().toString().length() == 0){
            alert("入力エラー","PWが入力されていません。");
            flag = true;
        }
        if( flag == false && idet.getText().toString().length() <= 3 && pwet.getText().toString().length() > 5){
            alert("入力エラー","IDは4文字以上にしてください。");
            flag = true;
        }
        if( flag == false && idet.getText().toString().length() > 3 && pwet.getText().toString().length() <= 5){
            alert("入力エラー","PWは6文字以上にしてください。");
            flag = true;
        }
        if( flag == false && idet.getText().toString().length() <= 3 && pwet.getText().toString().length() <= 5){
            alert("入力エラー","IDは4文字以上・PWは6文字以上にしてください。");
            flag = true;
        }
        if( flag == false ){
            Wait("サインアップ");
            ParseUser user = new ParseUser();
            user.setUsername(idet.getText().toString());
            user.setPassword(pwet.getText().toString());
            user.signUpInBackground(new SignUpCallback() {
                public void done(ParseException e) {
                    waitDialog.dismiss();
                    if (e == null) {
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
        flag = false;
    }

    private void Wait(String what){
        // プログレスダイアログの設定
        waitDialog = new ProgressDialog(this);
        // プログレスダイアログのメッセージを設定します
        waitDialog.setMessage(what + "中...");
        // 円スタイル（くるくる回るタイプ）に設定します
        waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        waitDialog.setCanceledOnTouchOutside(false);
        // プログレスダイアログを表示
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