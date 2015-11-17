package com.shinewave.sopviewer;

/**
 * Created by user on 2015/11/16.
 */

import java.util.ArrayList;
import java.util.HashMap;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class FlieAdapte extends BaseAdapter {
    private ArrayList<HashMap<String, Object>> mAppList;
    private LayoutInflater mInflater;
    private Context mContext;
    private String[] keyString;
    private int[] valueViewID;

    private ItemView itemView;

    private class ItemView {
        public ImageView FV_IMG;
        public TextView FV_FileName;
        public TextView FV_CONN;
        public TextView FV_UPDATE_Date;
        public Button viewBtn_Sync;
        public Button viewBtn_Del;
    }

    public FlieAdapte(Context c, ArrayList<HashMap<String, Object>> appList, int resource, String[] from, int[] to) {
        mAppList = appList;
        mContext = c;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        keyString = new String[from.length];
        valueViewID = new int[to.length];
        System.arraycopy(from, 0, keyString, 0, from.length);
        System.arraycopy(to, 0, valueViewID, 0, to.length);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        //return 0;
        return mAppList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        //return null;
        return mAppList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        //return 0;
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        //return null;

        if (convertView != null) {
            itemView = (ItemView) convertView.getTag();
        } else {
            convertView = mInflater.inflate(R.layout.file_info_view, null);
            itemView = new ItemView();
            itemView.FV_IMG = (ImageView)convertView.findViewById(valueViewID[0]);
            itemView.FV_FileName = (TextView)convertView.findViewById(valueViewID[1]);
            itemView.FV_CONN = (TextView)convertView.findViewById(valueViewID[2]);
            itemView.FV_UPDATE_Date = (TextView)convertView.findViewById(valueViewID[3]);
            itemView.viewBtn_Sync = (Button)convertView.findViewById(valueViewID[4]);
            itemView.viewBtn_Del = (Button)convertView.findViewById(valueViewID[5]);
            convertView.setTag(itemView);
        }

        HashMap<String, Object> appInfo = mAppList.get(position);
        if (appInfo != null) {

            int mid = (Integer)appInfo.get(keyString[0]);
            String name = (String) appInfo.get(keyString[1]);
            String info = (String) appInfo.get(keyString[2]);
            String infoDT = (String) appInfo.get(keyString[3]);
            //int bid = (Integer)appInfo.get(keyString[3]);
            itemView.FV_FileName.setText(name);
            itemView.FV_CONN.setText(info);
            itemView.FV_UPDATE_Date.setText(infoDT);
            //itemView.File = (File) appInfo.get(keyString[6]);
            itemView.FV_IMG.setImageDrawable(itemView.FV_IMG.getResources().getDrawable(mid));
            //itemView.viewBtn.setBackgroundDrawable(itemView.ItemButton.getResources().getDrawable(bid));
            if(name!="BACK") {
                itemView.viewBtn_Sync.setVisibility(View.VISIBLE);
                itemView.viewBtn_Del.setVisibility(View.VISIBLE);
                itemView.viewBtn_Sync.setOnClickListener(new ItemButtonSync_Click(position));
                itemView.viewBtn_Del.setOnClickListener(new ItemButtonDel_Click(position));
            } else {
                itemView.viewBtn_Sync.setVisibility(View.INVISIBLE);
                itemView.viewBtn_Del.setVisibility(View.INVISIBLE);
            }
        }

        return convertView;
    }

    class ItemButtonSync_Click implements OnClickListener {
        private int position;

        ItemButtonSync_Click(int pos) {
            position = pos;
        }

        @Override
        public void onClick(View v) {
            int vid=v.getId();
            if (vid == itemView.viewBtn_Sync.getId())
                Log.v("ola_log",String.valueOf(position) );
        }
    }

    class ItemButtonDel_Click implements OnClickListener {
        private int position;

        ItemButtonDel_Click(int pos) {
            position = pos;
        }

        @Override
        public void onClick(View v) {
            int vid=v.getId();
            if (vid == itemView.viewBtn_Del.getId())
                Log.v("ola_log",String.valueOf(position) );
        }
    }
}

