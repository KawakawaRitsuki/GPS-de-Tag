package com.kawakawaplanning.gpsdetag;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SelectGroupActivity extends ActionBarActivity {


    private String myId;
    private ListView lv;
    private ProgressDialog waitDialog;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_group);

        SharedPreferences pref = getSharedPreferences("loginpref", Activity.MODE_MULTI_PROCESS );
        myId = pref.getString("loginid", "");
        mHandler = new Handler();

        findView();
        listLoad();
    }

    private void findView(){
        lv = (ListView)findViewById(R.id.listView2);
    }


    Map<String, String> members;
    List<Map<String, String>> list;

    private void listLoad(){
        Wait("グループ読み込み");
        members = new HashMap();
        list = new ArrayList();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Group");//ParseObject型のParseQueryを取得する。
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseList, ParseException e) {
                for (ParseObject po : parseList) {
                    final String obId = po.getObjectId();

                    final ParseQuery<ParseObject> que = ParseQuery.getQuery("Group");//その、ObjectIDで参照できるデータの内容をParseObject型のParseQueryで取得

                    try {
                        String member = que.get(obId).getString("Members");
                        String name = que.get(obId).getString("GroupName");
                        String[] memberSpr = que.get(obId).getString("Members").split(",");

                        boolean frag = false;
                        for (String str : memberSpr) {
                            if (str.equals(myId))
                                frag = true;
                        }

                        if (frag) { //そのグループに自分がいたら
                            Map<String, String> conMap = new HashMap<>();
                            members.put(name, member);
                            conMap.put("Name", name);
                            conMap.put("Member", "グループID:" + obId);
                            list.add(conMap);
                        }

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                final SimpleAdapter adapter = new SimpleAdapter(SelectGroupActivity.this, list, android.R.layout.simple_list_item_2, new String[]{"Name", "Member"}, new int[]{android.R.id.text1, android.R.id.text2});
                                lv.setAdapter(adapter);
                                lv.setOnItemClickListener(onItem);
                                lv.setOnItemLongClickListener(onItemLong);
                                waitDialog.dismiss();
                            }
                        });

                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    private AdapterView.OnItemClickListener onItem = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Map<String, String> map = (Map<String, String>) parent.getAdapter().getItem(position);

            SharedPreferences pref = getSharedPreferences("loginpref", Activity.MODE_MULTI_PROCESS);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("mem", members.get(map.get("Name")));//グループ名からメンバーを抜き出す
            editor.putString("groupId", map.get("Member").substring(7));
            editor.commit();

            Intent intent = new Intent();
            intent.setClassName("com.kawakawaplanning.gpsdetag", "com.kawakawaplanning.gpsdetag.WaitMemberActivity");
            startActivity(intent);
            finish();
        }
    };

    private AdapterView.OnItemLongClickListener onItemLong = new AdapterView.OnItemLongClickListener(){

        @Override
        public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
            AlertDialog.Builder adb = new AlertDialog.Builder(SelectGroupActivity.this);
            adb.setCancelable(true);
            adb.setTitle("確認");
            adb.setMessage("このグループを削除しますか？");
            adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Wait("処理");
                    Map<String, String> map = (Map<String, String>) parent.getAdapter().getItem(position);
                    String mem[] = members.get(map.get("Name")).split(",");
                    ArrayList<String> arrayList = new ArrayList<String>();

                    for (String str : mem) {
                        if (!str.equals(myId))
                            arrayList.add(str);
                    }

                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Group");//ParseObject型のParseQueryを取得する。
                    query.whereEqualTo("GroupName", map.get("Name"));//そのクエリの中でReceiverがname変数のものを抜き出す。

                    try {
                        ParseObject testObject = query.find().get(0);
                        String str = "";
                        boolean a = true;
                        for (String s : arrayList) {
                            if (a) {
                                str = s + ",";
                                a = false;
                            } else {
                                str = str + s + ",";
                            }
                        }
                        str = str.substring(0, str.length() - 1);
                        testObject.put("Members", str);
                        testObject.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                listLoad();
                                waitDialog.dismiss();
                            }
                        });
                    } catch (ParseException e) {

                    }
                }
            });
            adb.setNegativeButton("Cancel", null);
            AlertDialog ad = adb.create();
            ad.show();
            return false;
        }
    };

    public void makeGroup(View v){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(
                LAYOUT_INFLATER_SERVICE);
        View view =  inflater.inflate(R.layout.dialog_et1,
                (ViewGroup)findViewById(R.id.dialog_layout));

        final EditText et1 = (EditText)view.findViewById(R.id.editText1);
        final TextView tv1 = (TextView)view.findViewById(R.id.dig_tv1);

        tv1.setText("作成するグループ名を入力してください");

        alertDialogBuilder.setTitle("グループ作成");
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String str = et1.getEditableText().toString();
                        if (str.equals("gB9xRLYJ3V4x")) {
                            AlertDialog.Builder adb = new AlertDialog.Builder(SelectGroupActivity.this);
                            adb.setCancelable(true);
                            adb.setTitle("グループ作成失敗");
                            adb.setMessage("システムエラーが発生しました。グループ名を変えてもう一度作成してください。");
                            adb.setPositiveButton("OK", null);
                            AlertDialog ad = adb.create();
                            ad.show();
                        } else if (!str.isEmpty()) {

                            ParseQuery<ParseObject> query = ParseQuery.getQuery("Group");//ParseObject型のParseQueryを取得する。
                            query.whereEqualTo("GroupName", str);//そのクエリの中でReceiverがname変数のものを抜き出す。

                            try {
                                if (query.count() == 0) {
                                    ParseObject groupObject = new ParseObject("Group");
                                    groupObject.put("GroupName", str);
                                    groupObject.put("Members", myId);
                                    groupObject.save();

                                    AlertDialog.Builder adb = new AlertDialog.Builder(SelectGroupActivity.this);
                                    adb.setCancelable(true);
                                    adb.setTitle("グループ作成完了");
                                    adb.setMessage("グループの作成が完了しました。友達を早速誘おう！グループIDは「" + groupObject.getObjectId() + "」です。");
                                    adb.setPositiveButton("OK", null);
                                    AlertDialog ad = adb.create();
                                    ad.show();
                                    listLoad();
                                } else {
                                    AlertDialog.Builder adb = new AlertDialog.Builder(SelectGroupActivity.this);
                                    adb.setCancelable(true);
                                    adb.setTitle("グループ作成失敗");
                                    adb.setMessage("グループの作成に失敗しました。すでに同じグループ名が存在しています。グループ名を変えてもう一度作成してください。");
                                    adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            listLoad();
                                        }
                                    });
                                    AlertDialog ad = adb.create();
                                    ad.show();
                                }

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                });
        alertDialogBuilder.setCancelable(true);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void inGroup(View v){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(
                LAYOUT_INFLATER_SERVICE);
        View view =  inflater.inflate(R.layout.dialog_et1,
                (ViewGroup)findViewById(R.id.dialog_layout));

        final EditText et1 = (EditText)view.findViewById(R.id.editText1);
        final TextView tv1 = (TextView)view.findViewById(R.id.dig_tv1);

        tv1.setText("ログインしたいグループIDを入力してください");

        alertDialogBuilder.setTitle("グループログイン");
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setPositiveButton("OK", null);

        alertDialogBuilder.setCancelable(true);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        Button buttonOK = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String str = et1.getEditableText().toString();

                if (str.length() == 10) {

                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Group");//ParseObject型のParseQueryを取得する。

                    try {
                        ParseObject po = query.get(str);

                        String[] st = po.getString("Members").split(",");
                        String string = po.getString("Members");

                        if(st.length > 5) {
                            boolean f = true;
                            for (String s : st) {
                                if (s.equals(myId))
                                    f = false;
                            }

                            if (f) {
                                string = string + "," + myId;
                                po.put("Members", string);
                                po.saveInBackground();
                                alertDialog.dismiss();
                                listLoad();
                            } else {
                                et1.setError("すでにログインしています");
                            }
                        }else{
                            et1.setError("一つのグループには5人までしかログインできません。");
                        }

                    } catch (ParseException e) {
                        et1.setError("グループIDが見つかりませんでした。IDを確認して下さい。");
                    }
                } else {
                    et1.setError("IDは10文字で入力してください");
                }
            }
        });
    }
    private void Wait(String what){
        waitDialog = new ProgressDialog(this);
        waitDialog.setMessage(what + "中...");
        waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        waitDialog.setCanceledOnTouchOutside(false);
        waitDialog.show();
    }
}
