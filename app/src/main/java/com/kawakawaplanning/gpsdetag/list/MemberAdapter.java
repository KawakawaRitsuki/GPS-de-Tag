package com.kawakawaplanning.gpsdetag.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kawakawaplanning.gpsdetag.R;

import java.util.List;

//項目をタッチした時のハイライト表示をキャンセルするためのArrayAdapter継承クラス
public class MemberAdapter extends ArrayAdapter<MemberData> {
    private LayoutInflater layoutInflater_;
    public MemberAdapter(Context context, int textViewResourceId, List<MemberData> objects) {
        super(context, textViewResourceId, objects);
        layoutInflater_ = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    //以下2つをfalseで返すと選択が行えなくなる
    public boolean areAllItemsEnabled() {
            return false;
        }
    public boolean isEnabled(int position) {
            return false;
        }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 特定の行(position)のデータを得る
        MemberData item = (MemberData)getItem(position);

        // convertViewは使い回しされている可能性があるのでnullの時だけ新しく作る
        if (null == convertView) {
            convertView = layoutInflater_.inflate(R.layout.simple_list, null);
        }

        TextView textView;
        textView = (TextView)convertView.findViewById(R.id.text1);
        textView.setText(item.getTextData());
        textView.setTextColor(item.getColorData());

        return convertView;
    }

}