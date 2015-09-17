package com.kawakawaplanning.gpsdetag;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.kawakawaplanning.gpsdetag.http.HttpConnector;

import java.util.regex.Pattern;


public class SignUpActivity extends AppCompatActivity {

    Pattern p;
    private ProgressDialog waitDialog;
    Vibrator mVib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mVib = (Vibrator) getSystemService (VIBRATOR_SERVICE);
        p = Pattern.compile("^[0-9a-zA-Z_.]+$");
        assignViews();

        idEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() < 4) {
                    idEt.setError("4文字以上で入力してください");
                } else if (p.matcher(s.toString()).find()) {
                    idEt.setError(null);
                } else {
                    idEt.setError("半角英数字で入力してください");
                }
            }
        });
        pwEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() < 6){
                    pwEt.setError("6文字以上で入力してください");
                } else if(p.matcher(s.toString()).find()) {
                    pwEt.setError(null);
                }else{
                    pwEt.setError("半角英数字で入力してください");
                }

                if(!pw2Et.getText().toString().equals(s.toString())){
                    pw2Et.setError("1度目の入力と違います");
                } else if(p.matcher(s.toString()).find()) {
                    pw2Et.setError(null);
                }else{
                    pw2Et.setError("半角英数字で入力してください");
                }
            }
        });
        pw2Et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(!pwEt.getText().toString().equals(s.toString())){
                    pw2Et.setError("1度目の入力と違います");
                } else if(p.matcher(s.toString()).find()) {
                    pw2Et.setError(null);
                }else{
                    pw2Et.setError("半角英数字で入力してください");
                }
            }
        });
        nnEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() < 1){
                    nnEt.setError("文字を入力してください");
                }else{
                    nnEt.setError(null);
                }
            }
        });
        nnEt.setOnKeyListener((View v, int keyCode, KeyEvent event) -> {
            if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                signUp();
                return true;
            }
            return false;
        });

    }



    EditText idEt;
    EditText pwEt;
    EditText pw2Et;
    EditText nnEt;

    public void assignViews(){
        idEt = (EditText)findViewById(R.id.signIdEt);
        pwEt = (EditText)findViewById(R.id.signPwEt);
        pw2Et = (EditText)findViewById(R.id.signPw2Et);
        nnEt = (EditText)findViewById(R.id.signNickNameEt);
    }

    public void onClick(View v){
        mVib.vibrate(50);
        signUp();
    }

    public void signUp(){
        if (idEt.getText().length() < 4) {
            idEt.setError("4文字以上で入力してください");
        } else if (p.matcher(idEt.getText().toString()).find()) {
            idEt.setError(null);
        } else {
            idEt.setError("半角英数字で入力してください");
        }

        if(pwEt.getText().length() < 6){
            pwEt.setError("6文字以上で入力してください");
        } else if(p.matcher(pwEt.getText().toString()).find()) {
            pwEt.setError(null);
        }else{
            pwEt.setError("半角英数字で入力してください");
        }

        if(!pw2Et.getText().toString().equals(pwEt.getText().toString())){
            pw2Et.setError("1度目の入力と違います");
        } else if (p.matcher(pw2Et.getText().toString()).find()) {
            pw2Et.setError(null);
        }else{
            pw2Et.setError("半角英数字で入力してください");
        }

        if(nnEt.getText().length() < 1){
            nnEt.setError("文字を入力してください");
        }else{
            nnEt.setError(null);
        }

        if (idEt.getError() == null&&pwEt.getError() == null&&pw2Et.getError() == null&&nnEt.getError() == null){
            Wait("登録");
            HttpConnector httpConnector = new HttpConnector("signup", "{\"user_id\":\"" + idEt.getText().toString() + "\",\"password\":\"" + pwEt.getText().toString() + "\",\"user_name\":\"" + nnEt.getText().toString() +"\"}");
            httpConnector.setOnHttpResponseListener((String message) -> {
                waitDialog.dismiss();
                Log.v("tag", message);
                if (Integer.parseInt(message) == 0) {
                    alert("登録完了", "会員登録が完了しました。早速ログインボタンを押して始めよう！", null);
                } else {
                    alert("エラー", "このIDは既に使用されています。エラーコード:1",null);
                }
            });
            httpConnector.setOnHttpErrorListener((int error) -> {
                waitDialog.dismiss();
                android.support.v7.app.AlertDialog.Builder adb = new android.support.v7.app.AlertDialog.Builder(SignUpActivity.this);
                adb.setTitle("接続エラー");
                adb.setMessage("接続エラーが発生しました。インターネットの接続状態を確認して下さい。");
                adb.setPositiveButton("OK", null);
                adb.setCancelable(true);
                adb.show();
            });
            httpConnector.post();
        } else {
            Toast.makeText(getApplicationContext(), "入力に不備があります", Toast.LENGTH_SHORT).show();
        }
    }

    private void alert(String til,String msg, DialogInterface.OnClickListener onclick){
        AlertDialog.Builder adb = new AlertDialog.Builder(SignUpActivity.this);
        adb.setTitle(til);
        adb.setMessage(msg);
        adb.setPositiveButton("OK", onclick);
        adb.show();
    }

    private void Wait(String what){
        waitDialog = new ProgressDialog(this);
        waitDialog.setMessage(what + "中...");
        waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        waitDialog.setCanceledOnTouchOutside(false);
        waitDialog.show();
    }
}
