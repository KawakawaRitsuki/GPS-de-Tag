package com.kawakawaplanning.gpsdetag;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;


public class SelectGroupActivity extends ActionBarActivity {


    private String myId;
    private ArrayList<String> members;
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_group);

        members = new ArrayList<String>();
        lv = (ListView)findViewById(R.id.listView2);
        myId = getIntent().getStringExtra("name");

    }

    @Override
    protected void onStart() {
        super.onStart();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Group");//ParseObject型のParseQueryを取得する。
        List<ParseObject> parselist= null;
        try {
            parselist = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (ParseObject po : parselist) {
            final String obId = po.getObjectId();

            final ParseQuery<ParseObject> que = ParseQuery.getQuery("Group");//その、ObjectIDで参照できるデータの内容をParseObject型のParseQueryで取得

            try {
                String member = que.get(obId).getString("Members");
                String[] memberSpr = que.get(obId).getString("Members").split(",");

                boolean frag = false;
                for (String str : memberSpr) {
                    if (str.equals(myId))
                        frag = true;
                }

                if (frag) //そのグループに自分がいたら
                    members.add(member);

            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }

        final ArrayAdapter adapter = new ArrayAdapter(SelectGroupActivity.this,
                android.R.layout.simple_list_item_1, members);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent();
                intent.setClassName("com.kawakawaplanning.gpsdetag", "com.kawakawaplanning.gpsdetag.WaitMemberActivity");
                intent.putExtra("name", myId);
                intent.putExtra("selectGroup",(String)parent.getItemAtPosition(position));
                startActivity(intent);
                finish();
            }
        });
    }
}
