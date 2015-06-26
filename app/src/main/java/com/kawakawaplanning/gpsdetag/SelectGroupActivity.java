package com.kawakawaplanning.gpsdetag;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
    ProgressDialog waitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_group);

        lv = (ListView)findViewById(R.id.listView2);
        myId = getIntent().getStringExtra("name");

        listLoad();
    }

    public void listLoad(){
        final Map<String, String> members = new HashMap<String, String>();
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Group");//ParseObject型のParseQueryを取得する。
        List<ParseObject> parseList= null;
        try {
            parseList = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
                    Map<String, String> conMap = new HashMap<String, String>();
                    members.put(name,member);
                    conMap.put("Name", name);
                    conMap.put("Member", "グループID:"+obId);
                    list.add(conMap);
                }

            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }

        final SimpleAdapter adapter = new SimpleAdapter(this,list,
                android.R.layout.simple_list_item_2, new String[] { "Name",
                "Member" }, new int[] { android.R.id.text1,
                android.R.id.text2 });

        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, String> map = (Map<String, String>)parent.getAdapter().getItem(position);

                SharedPreferences pref = getSharedPreferences("loginpref", Activity.MODE_MULTI_PROCESS);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("mem", members.get(map.get("Name")));//グループ名からメンバーを抜き出す
                editor.commit();

                Wait("メンバー一覧読み込み");

                Intent intent = new Intent();
                intent.setClassName("com.kawakawaplanning.gpsdetag", "com.kawakawaplanning.gpsdetag.WaitMemberActivity");
                intent.putExtra("name", myId);
                intent.putExtra("selectGroup", members.get(map.get("Name")));
                startActivity(intent);
                finish();
            }
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view,final int position, long id) {
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
                return true;
            }
        });
    }

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
                        if (!str.isEmpty()) {

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
                                }else{
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

                    } catch (ParseException e) {
                        et1.setError("グループIDがありませんでした。IDを確認して下さい。");
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
