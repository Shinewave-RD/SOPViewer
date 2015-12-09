package com.shinewave.sopviewer;

/**
 * Created by user on 2015/11/16.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FlieAdapte extends BaseAdapter {
    private ArrayList<HashMap<String, Object>> mAppList;
    private LayoutInflater mInflater;
    private Context mContext;
    private String[] keyString;
    private int[] valueViewID;
    private int resource;

    private ItemView itemView;
    private int nowPosition;

    private int fromType;

    private class ItemView {
        public ImageView FV_IMG;
        public TextView FV_FileName;
        public TextView FV_CONN;
        public TextView FV_UPDATE_Date;
        public Button viewBtn_Sync;
        public Button viewBtn_Del;
    }

    public FlieAdapte(Context c, ArrayList<HashMap<String, Object>> appList, int resource, String[] from, int[] to, int type) {
        mAppList = appList;
        mContext = c;
        fromType = type;
        this.resource = resource;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            convertView = mInflater.inflate(resource, null);
            itemView = new ItemView();
            if (fromType == FileMamagerFragment.FROM_MAIN_ACTIVITY || fromType == FileMamagerFragment.FROM_PLAY_ITEM) {
                itemView.FV_IMG = (ImageView) convertView.findViewById(valueViewID[0]);
                itemView.FV_FileName = (TextView) convertView.findViewById(valueViewID[1]);
                itemView.FV_CONN = (TextView) convertView.findViewById(valueViewID[2]);
                itemView.FV_UPDATE_Date = (TextView) convertView.findViewById(valueViewID[3]);
                itemView.viewBtn_Sync = (Button) convertView.findViewById(valueViewID[4]);
                itemView.viewBtn_Del = (Button) convertView.findViewById(valueViewID[5]);
            } else {
                itemView.FV_IMG = (ImageView) convertView.findViewById(valueViewID[0]);
                itemView.FV_FileName = (TextView) convertView.findViewById(valueViewID[1]);
                itemView.FV_CONN = (TextView) convertView.findViewById(valueViewID[2]);
                itemView.FV_UPDATE_Date = (TextView) convertView.findViewById(valueViewID[3]);
            }
            convertView.setTag(itemView);
        }

        HashMap<String, Object> appInfo = mAppList.get(position);
        if (appInfo != null) {

            int mid = (Integer) appInfo.get(keyString[0]);
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
            if (fromType == FileMamagerFragment.FROM_MAIN_ACTIVITY) {
                if (!name.equals("BACK") && !info.endsWith(mContext.getString(R.string.label_local))) {
                    itemView.viewBtn_Sync.setVisibility(View.VISIBLE);
                    itemView.viewBtn_Del.setVisibility(View.VISIBLE);
                    itemView.viewBtn_Sync.setOnClickListener(new ItemButtonSync_Click(position));
                    itemView.viewBtn_Del.setOnClickListener(new ItemButtonDel_Click(position));
                } else {
                    itemView.viewBtn_Sync.setVisibility(View.INVISIBLE);
                    itemView.viewBtn_Del.setVisibility(View.INVISIBLE);
                }
            } else if (fromType == FileMamagerFragment.FROM_PLAY_ITEM) {
                itemView.viewBtn_Sync.setVisibility(View.INVISIBLE);
                itemView.viewBtn_Del.setVisibility(View.INVISIBLE);
            }
            //else {
            //    itemView.viewBtn_Sync.setVisibility(View.INVISIBLE);
            //    itemView.viewBtn_Del.setVisibility(View.INVISIBLE);
            //}
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
            nowPosition = position;
            showSyncMessageDialog();
        }
    }

    class ItemButtonDel_Click implements OnClickListener {
        private int position;

        ItemButtonDel_Click(int pos) {
            position = pos;
        }

        @Override
        public void onClick(View v) {
            nowPosition = position;
            showDeleteMessageDialog();
        }
    }

    private boolean doDeleteRecur(File dir) {
        try {
            if (dir.isDirectory()) {
                for (File f : dir.listFiles()) {
                    if (f.isDirectory()) {
                        doDeleteRecur(f);
                    } else {
                        if (!f.delete())
                            break;
                    }
                }
            }
            return dir.delete();
        } catch (Exception e) {
            return false;
        }
    }

    private void showDeleteMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        LayoutInflater factory = LayoutInflater.from(mContext);
        final View textEntryView = factory.inflate(R.layout.message_dialog, null);
        builder.setTitle(mContext.getString(R.string.dialog_alter));
        builder.setView(textEntryView);

        final TextView msg = (TextView) textEntryView.findViewById(R.id.lblMassage);
        msg.setText(mContext.getString(R.string.dialog_folder_delete));

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                HashMap<String, Object> appInfo = mAppList.get(nowPosition);
                String path = FileMamagerFragment.nowPath + File.separator + appInfo.get(keyString[1]);

                File dir = new File(path);
                boolean success = doDeleteRecur(dir);
                if (success) {
                    FileMamagerFragment.deleteFileInfo(path);
                    FileMamagerFragment.resetListViewData();
                    notifyDataSetChanged();
                    Toast.makeText(mContext, mContext.getString(R.string.dialog_delete_succeed), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.dialog_delete_failed), Toast.LENGTH_SHORT).show();
                }


            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        builder.create().show();
    }

    private void showSyncMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        LayoutInflater factory = LayoutInflater.from(mContext);
        final View textEntryView = factory.inflate(R.layout.message_dialog, null);
        builder.setTitle(mContext.getString(R.string.dialog_alter));
        builder.setView(textEntryView);

        final TextView msg = (TextView) textEntryView.findViewById(R.id.lblMassage);
        msg.setText(mContext.getString(R.string.dialog_sure_sync));

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                HashMap<String, Object> appInfo = mAppList.get(nowPosition);
                String path = FileMamagerFragment.nowPath + File.separator + appInfo.get(keyString[1]);
                String failName = FileMamagerFragment.doSyncAll(path);
                if (failName.equals("")) {
                    FileMamagerFragment.resetListViewData();
                    notifyDataSetChanged();
                    Toast.makeText(mContext, mContext.getString(R.string.dialog_sync_succeed), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.dialog_sync_failed) + ":" + failName, Toast.LENGTH_LONG).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        builder.create().show();
    }
}

