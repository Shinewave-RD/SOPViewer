package com.shinewave.sopviewer;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2015/10/28.
 */
public class DBManager {

    private static final String TAG = "DBManager";
    private static DataBaseHelper sopDBAccess;

    public static void initDBHelp(DataBaseHelper dbAccess) {
        sopDBAccess = dbAccess; //keep dbhelper instance
    }

    public static List<FileInfo> getFileInfo() {
        SQLiteDatabase db = sopDBAccess.getReadableDatabase();
        List<FileInfo> list = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Cursor cr = null;
        try {
            cr = db.rawQuery("SELECT * FROM SOPViewer_FileInfo", null);
        } catch (Exception e) {
            Log.d("TAG", e.getMessage());
        }
        if (cr != null) {
            cr.moveToFirst();
            for (int i = 0; i < cr.getCount(); i++) {
                try {
                    FileInfo info = new FileInfo();
                    info.localFullFilePath = cr.getString(0);
                    info.remoteFullFilePath = cr.getString(1);
                    info.connectionName = cr.getString(2);
                    info.updateTime = format.parse(cr.getString(3));
                    info.size = cr.getInt(4);
                    info.remoteTimeStamp = format.parse(cr.getString(5));
                    list.add(info);
                    cr.moveToNext();
                } catch (ParseException e) {
                    Log.d("TAG", e.getMessage());
                }
            }
            cr.close();
        }
        db.close();

        return list;
    }

    public static boolean insertFileInfo(FileInfo info) {
        boolean res = false;
        SQLiteDatabase db = sopDBAccess.getReadableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String updateString = "";
        String remoteDateString = "";

        try {
            updateString = sdf.format(info.updateTime);
            remoteDateString = sdf.format(info.remoteTimeStamp);
        } catch (Exception e) {
            Log.d("TAG", e.getMessage());
        }

        ContentValues ctv = new ContentValues();
        ctv.put("localFullFilePath", info.localFullFilePath);
        ctv.put("remoteFullFilePath", info.remoteFullFilePath);
        ctv.put("connectionName", info.connectionName);
        ctv.put("updateTime", updateString);
        ctv.put("size", info.size);
        ctv.put("remoteTimeStamp", remoteDateString);

        try {
            long resLong = db.insert("SOPViewer_FileInfo", "", ctv);
            if (resLong >= 0)
                res = true;
        } catch (Exception e) {
            Log.d("TAG", e.getMessage());
        }
        db.close();
        return res;
    }

    public static boolean deleteFileInfo(String localFullFilePath) {
        boolean res = false;
        SQLiteDatabase db = sopDBAccess.getReadableDatabase();

        try {
            long resLong = db.delete("SOPViewer_FileInfo", "localFullFilePath=?", new String[]{localFullFilePath});
            if (resLong >= 0)
                res = true;
        } catch (Exception e) {
            Log.d("TAG", e.getMessage());
        }

        db.close();
        return res;
    }

    public static boolean updateFileInfo(FileInfo info) {
        boolean res = false;
        SQLiteDatabase db = sopDBAccess.getWritableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String updateString = "";
        String remoteDateString = "";

        try {
            updateString = sdf.format(info.updateTime);
            remoteDateString = sdf.format(info.remoteTimeStamp);
        } catch (Exception e) {
            Log.d("TAG", e.getMessage());
        }

        String name = info.localFullFilePath;
        ContentValues ctv = new ContentValues();
        ctv.put("remoteFullFilePath", info.remoteFullFilePath);
        ctv.put("connectionName", info.connectionName);
        ctv.put("updateTime", updateString);
        ctv.put("size", info.size);
        ctv.put("remoteTimeStamp", remoteDateString);

        try {
            long resLong = db.update("SOPViewer_FileInfo", ctv, "localFullFilePath=?", new String[]{name});
            if (resLong >= 0)
                res = true;
        } catch (Exception e) {
            Log.d("TAG", e.getMessage());
        }

        db.close();
        return res;
    }

    public static FileInfo getSingleFileInfo(String fullPath) {
        SQLiteDatabase db = sopDBAccess.getReadableDatabase();
        FileInfo info = new FileInfo();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Cursor cr = null;
        try {
            cr = db.rawQuery("SELECT * FROM SOPViewer_FileInfo WHERE localFullFilePath=?", new String[]{fullPath});
        } catch (Exception e) {
            Log.d("TAG", e.getMessage());
        }
        if (cr != null) {
            cr.moveToFirst();
            for (int i = 0; i < cr.getCount(); i++) {
                try {
                    info.localFullFilePath = cr.getString(0);
                    info.remoteFullFilePath = cr.getString(1);
                    info.connectionName = cr.getString(2);
                    info.updateTime = format.parse(cr.getString(3));
                    info.size = cr.getInt(4);
                    info.remoteTimeStamp = format.parse(cr.getString(5));
                    cr.moveToNext();
                } catch (ParseException e) {
                    Log.d("TAG", e.getMessage());
                }
            }
            cr.close();
        }
        db.close();

        return info;
    }

    public static ConnectionInfo getConnection(String connName) {
        SQLiteDatabase db = sopDBAccess.getReadableDatabase();
        ConnectionInfo info = null;
        Cursor cr = null;
        try {
            cr = db.rawQuery("SELECT * FROM SOPViewer_ConnectionInfo where connectionName=" + connName, null);
        } catch (Exception e) {
            Log.d("TAG", e.getMessage());
        }
        if (cr != null) {
            cr.moveToFirst();
            info = new ConnectionInfo();
            info.connectionName = cr.getString(0);
            info.protocol = cr.getInt(1);
            info.protocolType = ConnectionInfo.ProtocolType.values()[cr.getInt(1)].toString();
            info.url = cr.getString(2);
            info.id = cr.getString(3);
            info.password = cr.getString(4);
            cr.close();
        }
        db.close();

        return info;
    }

    public static List<ConnectionInfo> getConnectionList() {
        SQLiteDatabase db = sopDBAccess.getReadableDatabase();
        List<ConnectionInfo> list = new ArrayList<>();
        Cursor cr = null;
        try {
            cr = db.rawQuery("SELECT * FROM SOPViewer_ConnectionInfo", null);
        } catch (Exception e) {
            Log.d("TAG", e.getMessage());
        }
        if (cr != null) {
            cr.moveToFirst();
            for (int i = 0; i < cr.getCount(); i++) {
                ConnectionInfo info = new ConnectionInfo();
                info.connectionName = cr.getString(0);
                info.protocol = cr.getInt(1);
                info.protocolType = ConnectionInfo.ProtocolType.values()[cr.getInt(1)].toString();
                info.url = cr.getString(2);
                info.id = cr.getString(3);
                info.password = cr.getString(4);
                list.add(info);
                cr.moveToNext();
            }
            cr.close();
        }
        db.close();

        return list;
    }

    public static boolean insertConnection(ConnectionInfo conn) {
        boolean res = false;
        SQLiteDatabase db = sopDBAccess.getReadableDatabase();

        ContentValues ctv = new ContentValues();
        ctv.put("connectionName", conn.connectionName);
        ctv.put("protocol", conn.protocol);
        ctv.put("url", conn.url);
        ctv.put("id", conn.id);
        ctv.put("password", conn.password);

        try {
            long resLong = db.insert("SOPViewer_ConnectionInfo", "", ctv);
            if (resLong >= 0)
                res = true;
        } catch (Exception e) {
            Log.d("TAG", e.getMessage());
        }
        db.close();
        return res;
    }

    public static boolean deleteConnection(String connName) {
        boolean res = false;
        SQLiteDatabase db = sopDBAccess.getReadableDatabase();

        try {
            long resLong = db.delete("SOPViewer_ConnectionInfo", "connectionName=?", new String[]{connName});
            if (resLong >= 0)
                res = true;
        } catch (Exception e) {
            Log.d("TAG", e.getMessage());
        }

        db.close();
        return res;
    }

    public static boolean updateConnection(ConnectionInfo conn) {

        boolean res = false;
        SQLiteDatabase db = sopDBAccess.getWritableDatabase();

        String name = conn.connectionName;
        ContentValues ctv = new ContentValues();
        ctv.put("protocol", conn.protocol);
        ctv.put("url", conn.url);
        ctv.put("id", conn.id);
        ctv.put("password", conn.password);
        try {
            long resLong = db.update("SOPViewer_ConnectionInfo", ctv, "connectionName=?", new String[]{name});
            if (resLong >= 0)
                res = true;
        } catch (Exception e) {
            //
        }

        db.close();
        return res;
    }

    public static List<PlayList> getPlayList() {
        SQLiteDatabase db = sopDBAccess.getReadableDatabase();
        List<PlayList> list = new ArrayList<>();
        PlayList info = new PlayList();
        PlayListItem item = new PlayListItem();
        Cursor cr = null;
        try {
            cr = db.rawQuery("SELECT * FROM SOPViewer_PlayList ORDER BY playListName", null);
        } catch (Exception e) {
            //
        }
        if (cr != null) {
            cr.moveToFirst();
            String tmpName = "";
            int guard = 0;
            for (int i = 0; i < cr.getCount(); i++) {
                try {
                    guard++;
                    if (!tmpName.equals(cr.getString(0))) {
                        if (guard > 1)
                            list.add(info);
                        info = new PlayList();
                        info.playListName = cr.getString(0);
                        info.loop = cr.getInt(1);
                        item.seq = cr.getInt(2);
                        item.localFullFilePath = cr.getString(3);
                        item.strPages = cr.getString(4);
                        item.sec = cr.getInt(5);
                        info.playListItem.add(item);
                        tmpName = info.playListName;

                    } else {
                        item.seq = cr.getInt(2);
                        item.localFullFilePath = cr.getString(3);
                        item.strPages = cr.getString(4);
                        item.sec = cr.getInt(5);
                        info.playListItem.add(item);
                    }
                    cr.moveToNext();
                } catch (Exception e) {
                    //
                }
            }
            cr.close();
        }
        db.close();

        return list;
    }

    public static boolean insertPlayList(PlayList pList) {
        boolean res = false;
        SQLiteDatabase db = sopDBAccess.getReadableDatabase();

        ContentValues ctv = new ContentValues();
        for (PlayListItem item : pList.playListItem) {
            ctv.put("playListName", pList.playListName);
            ctv.put("loop", pList.loop);
            ctv.put("seq", item.seq);
            ctv.put("localFullFilePath", item.localFullFilePath);
            ctv.put("strPages", item.strPages);
            ctv.put("sec", item.sec);
            try {
                long resLong = db.insert("SOPViewer_PlayList", "", ctv);
                if (resLong >= 0)
                    res = true;
            } catch (Exception e) {
                //
            }
        }
        db.close();
        return res;
    }

    public static boolean deletePlayList(String pListName) {
        boolean res = false;
        SQLiteDatabase db = sopDBAccess.getReadableDatabase();

        try {
            long resLong = db.delete("SOPViewer_PlayList", "playListName=?", new String[]{pListName});
            if (resLong >= 0)
                res = true;
        } catch (Exception e) {
            //
        }

        db.close();
        return res;
    }

    public static boolean updatePlayList(PlayList pList) {
        //每次update都先delete再insert，所以暫時用不到。
        boolean res = false;
        SQLiteDatabase db = sopDBAccess.getWritableDatabase();

        String name = "";
        ContentValues ctv = new ContentValues();
        try {
            long resLong = db.update("SOPViewer_PlayList", ctv, "playListName=?", new String[]{name});
            if (resLong >= 0)
                res = true;
        } catch (Exception e) {
            //
        }

        db.close();
        return res;
    }
}
