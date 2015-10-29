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

    private static DataBaseHelper mydbAccess;

    public static List<FileInfo> getFileInfo(DataBaseHelper dbAccess) {

        mydbAccess = dbAccess;  //keep dbhelper instance
        SQLiteDatabase db = dbAccess.getReadableDatabase();

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

    public static boolean insertFileInfo(DataBaseHelper dbAccess, FileInfo info) {

        mydbAccess = dbAccess;  //keep dbhelper instance
        SQLiteDatabase db = dbAccess.getReadableDatabase();

        Cursor cr = null;
        cr.close();
        db.close();
        return true;
    }

    public static boolean deleteFileInfo(DataBaseHelper dbAccess, String localFullFilePath) {

        mydbAccess = dbAccess;  //keep dbhelper instance
        SQLiteDatabase db = dbAccess.getReadableDatabase();

        Cursor cr = null;
        cr.close();
        db.close();
        return true;
    }

    public static boolean updateFileInfo(FileInfo info) {

        SQLiteDatabase db = mydbAccess.getWritableDatabase();

        for (int count = 0; count < 1; count++) {
            String name = info.localFullFilePath;
            ContentValues ctv = new ContentValues();
            ctv.put("remoteFullFilePath", info.remoteFullFilePath);
            ctv.put("connectionName", info.connectionName);
            ctv.put("size", info.size);
            ctv.put("remoteTimeStamp", info.remoteTimeStamp.toString());
            ctv.put("updateTime", info.updateTime.toString());
            try {
                db.update("SOPViewer_FileInfo", ctv, "command=?", new String[]{name});
            } catch (Exception e) {
                //
            }
        }
        db.close();

        return true;
    }
}
