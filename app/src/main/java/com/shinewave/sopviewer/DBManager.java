package com.shinewave.sopviewer;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2015/10/28.
 */
public class DBManager {

    private static DataBaseHelper sopDBAccess;

    public static void initDBHelp(DataBaseHelper dbAccess) {
        sopDBAccess = dbAccess; //keep dbhelper instance
    }

    public static List<FileInfo> getFileInfo() {
        SQLiteDatabase db = sopDBAccess.getReadableDatabase();

        Cursor cr = null;
        try {
            cr = db.rawQuery("SELECT * FROM SOPViewer_FileInfo", null);
        } catch (Exception e) {
            //
        }
        if (cr != null) {
            cr.moveToFirst();
            for (int i = 0; i < cr.getCount(); i++) {
                cr.moveToNext();
            }
            cr.close();
        }
        db.close();
        List<FileInfo> aa = new ArrayList<>();
        return aa;
    }

    public static boolean insertFileInfo(FileInfo info) {
        boolean res = false;
        SQLiteDatabase db = sopDBAccess.getReadableDatabase();

        ContentValues ctv = new ContentValues();
        ctv.put("localFullFilePath", info.localFullFilePath);
        ctv.put("remoteFullFilePath", info.remoteFullFilePath);
        ctv.put("connectionName", info.connectionName);
        ctv.put("size", info.size);
        ctv.put("remoteTimeStamp", info.remoteTimeStamp.toString());
        ctv.put("updateTime", info.updateTime.toString());
        try {
            long resLong = db.insert("SOPViewer_FileInfo", "", ctv);
            if (resLong >= 0)
                res = true;
        } catch (Exception e) {
            //
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
            //
        }

        db.close();
        return res;
    }

    public static boolean updateFileInfo(FileInfo info) {

        boolean res = false;
        SQLiteDatabase db = sopDBAccess.getWritableDatabase();

        String name = info.localFullFilePath;
        ContentValues ctv = new ContentValues();
        ctv.put("remoteFullFilePath", info.remoteFullFilePath);
        ctv.put("connectionName", info.connectionName);
        ctv.put("size", info.size);
        ctv.put("remoteTimeStamp", info.remoteTimeStamp.toString());
        ctv.put("updateTime", info.updateTime.toString());
        try {
            long resLong = db.update("SOPViewer_FileInfo", ctv, "localFullFilePath=?", new String[]{name});
            if (resLong >= 0)
                res = true;
        } catch (Exception e) {
            //
        }

        db.close();
        return res;
    }

    public static List<ConnectionInfo> getConnectionList() {
        SQLiteDatabase db = sopDBAccess.getReadableDatabase();

        Cursor cr = null;
        try {
            cr = db.rawQuery("SELECT * FROM SOPViewer_ConnectionInfo", null);
        } catch (Exception e) {
            //
        }
        if (cr != null) {
            cr.moveToFirst();
            for (int i = 0; i < cr.getCount(); i++) {
                cr.moveToNext();
            }
            cr.close();
        }
        db.close();
        List<ConnectionInfo> aa = new ArrayList<>();
        return aa;
    }

    public static boolean insertConnection(ConnectionInfo conn) {
        boolean res = false;
        SQLiteDatabase db = sopDBAccess.getReadableDatabase();

        ContentValues ctv = new ContentValues();
        try {
            long resLong = db.insert("SOPViewer_ConnectionInfo", "", ctv);
            if (resLong >= 0)
                res = true;
        } catch (Exception e) {
            //
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
            //
        }

        db.close();
        return res;
    }

    public static boolean updateConnection(ConnectionInfo conn) {

        boolean res = false;
        SQLiteDatabase db = sopDBAccess.getWritableDatabase();

        String name = conn.connectionName;
        ContentValues ctv = new ContentValues();
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

        Cursor cr = null;
        try {
            cr = db.rawQuery("SELECT * FROM SOPViewer_PlayList", null);
        } catch (Exception e) {
            //
        }
        if (cr != null) {
            cr.moveToFirst();
            for (int i = 0; i < cr.getCount(); i++) {
                cr.moveToNext();
            }
            cr.close();
        }
        db.close();
        List<PlayList> aa = new ArrayList<>();
        return aa;
    }

    public static boolean insertPlayList(PlayList pList) {
        boolean res = false;
        SQLiteDatabase db = sopDBAccess.getReadableDatabase();

        ContentValues ctv = new ContentValues();
        try {
            long resLong = db.insert("SOPViewer_PlayList", "", ctv);
            if (resLong >= 0)
                res = true;
        } catch (Exception e) {
            //
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
