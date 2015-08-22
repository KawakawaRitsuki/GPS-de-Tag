package com.kawakawaplanning.gpsdetag;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kawakawaplanning.gpsdetag.http.HttpConnector;

import java.io.IOException;

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
        pref = getSharedPreferences("loginpref", Activity.MODE_MULTI_PROCESS );

        gcm = GoogleCloudMessaging.getInstance(this);
        registerInBackground();

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
                signUp();
                break;
        }
    }

    public void autoLogin(){
        if(pref.getBoolean("AutoLogin", false)){
            Wait("自動ログイン");

            HttpConnector httpConnector = new HttpConnector("login","{\"user_id\":\""+mIdEt.getText().toString()+"\",\"user_name\":\"文字列\",\"password\":\""+mPwEt.getText().toString()+"\"}");
            httpConnector.setOnHttpResponseListener((String message) -> {
                Log.v("tag", message);
                waitDialog.dismiss();
                if (Integer.parseInt(message) == 0) {
                    editor = pref.edit();
                    editor.putString("loginid", mIdEt.getText().toString());
                    editor.apply();
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, SelectGroupActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    alert("ログインエラー", "IDまたはパスワードが違います。もう一度試してください。エラーコード:1");
                }

            });
            httpConnector.post();

        }
    }

    public void login(){

        String putId = mIdEt.getText().toString();
        if (putId == null){
            alert("入力エラー","IDが入力されていません。入力してください。");
        }else{
            Wait("ログイン");

            HttpConnector httpConnector = new HttpConnector("login","{\"user_id\":\""+mIdEt.getText().toString()+"\",\"user_name\":\"文字列\",\"password\":\""+mPwEt.getText().toString()+"\"}");
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
                    finish();
                }else{
                    alert("ログインエラー","IDまたはパスワードが違います。もう一度試してください。エラーコード:1");
                }


            });
            httpConnector.post();
        }
    }

    public void signUp(){

        if( mIdEt.getText().toString().length() <= 3 && mPwEt.getText().toString().length() <= 5){
            alert("入力エラー","IDは4文字以上・PWは6文字以上にしてください。");
        } else {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(
                    LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.dialog_et1,
                    (ViewGroup) findViewById(R.id.dialog_layout));

            final EditText et1 = (EditText) view.findViewById(R.id.editText1);
            final TextView tv1 = (TextView) view.findViewById(R.id.dig_tv1);

            tv1.setText("ニックネームを設定します。設定したいニックネームを入力してください！");

            alertDialogBuilder.setTitle("ニックネーム設定");
            alertDialogBuilder.setView(view);
            alertDialogBuilder.setPositiveButton("OK", null);
            alertDialogBuilder.setCancelable(true);
            final AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            Button buttonOK = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            buttonOK.setOnClickListener((View v1) -> {
                final String str = et1.getEditableText().toString();
                if (str.length() != 0) {
                    HttpConnector httpConnector = new HttpConnector("signup", "{\"user_id\":\"" + mIdEt.getText().toString() + "\",\"user_name\":\"文字列\",\"password\":\"" + mPwEt.getText().toString() + "\",\"user_name\":\"" + str +"\"}");
                    httpConnector.setOnHttpResponseListener((String message) -> {
                        Log.v("tag", message);

                        if (Integer.parseInt(message) == 0) {
                            alert("登録完了", "会員登録が完了しました。早速ログインボタンを押して鬼ごっこをしよう！");
                        } else {
                            alert("ログインエラー", "IDが重複しています。もう一度試してください。エラーコード:1");
                        }
                        alertDialog.dismiss();
                    });
                    httpConnector.post();
                } else {
                    et1.setError("ニックネームを入力してください");
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

    private GoogleCloudMessaging gcm;
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(MainActivity.this);
                    }
                    String regid = gcm.register("386036556837");
                    msg = "Device registered, registration ID=" + regid;
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.v("kp",msg);
            }
        }.execute(null, null, null);
    }
}