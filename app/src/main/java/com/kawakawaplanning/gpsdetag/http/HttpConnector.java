package com.kawakawaplanning.gpsdetag.http;

import android.os.Handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by KP on 15/08/13.
 */
public class HttpConnector {

    String host = "http://192.168.11.2:8080/";
    String message = "";
    private OnHttpResponseListener listener = null;

    //コンストラクタここから
    public HttpConnector(){}
    public HttpConnector(String message){
        this.message = message;
    }
    public HttpConnector(String host, String message){
        this.host += host;
        this.message = message;
    }
    //コンストラクタここまで

    //ゲット用コマンド
    public void get(){
        new Thread(() -> {
            try {
                URL url = new URL(host);
                URLConnection uc = url.openConnection();

                InputStream is = uc.getInputStream();//POSTした結果を取得
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                StringBuffer sb = new StringBuffer();
                String s;
                while ((s = reader.readLine()) != null) {
                    sb.append(s);
                }
                listener.onPostResponse(sb.toString());
                reader.close();
            } catch (MalformedURLException e) {
                System.err.println("Invalid URL format: " + host);
            } catch (IOException e) {
                System.err.println("Can't connect to " + host);
            }
        }).start();
    }

    //ポスト用コマンド
    public void post(){
        Handler handler = new Handler();
        new Thread(() -> {
            try {
                URL url = new URL(host);
                URLConnection uc = url.openConnection();
                uc.setDoOutput(true);
                uc.setRequestProperty("Content-type", "application/json");
                OutputStream os = uc.getOutputStream();

                PrintStream ps = new PrintStream(os);
                ps.print(message);
                ps.close();

                InputStream is = uc.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                StringBuffer sb = new StringBuffer();
                String s;
                while ((s = reader.readLine()) != null) {
                    sb.append(s);
                }
                handler.post(() -> {
                    if(listener != null)
                        listener.onPostResponse(sb.toString());
                });
                reader.close();
            } catch (MalformedURLException e) {
                System.err.println("Invalid URL format: " + host);
            } catch (IOException e) {
                System.err.println("Can't connect to " + host);
            }
        }).start();
    }

    public void setHost(String host){
        this.host = host;
    }

    public String getHost(){
        return host;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public String getMessage(){
        return message;
    }

    public void setOnHttpResponseListener(OnHttpResponseListener listener){
        this.listener = listener;
    }

    public void removeListener(){
        this.listener = null;
    }

}

/*
サンプル

HttpConnector httpConnector = new HttpConnector("outgroup","");
httpConnector.setOnHttpResponseListener((String message) -> {
if(Integer.parseInt(message) == 0){
//成功時
}else{
//失敗時
}
});
httpConnector.post();
 */