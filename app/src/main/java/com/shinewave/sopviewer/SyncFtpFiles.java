package com.shinewave.sopviewer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Environment;

import com.artifex.mupdfdemo.AsyncTask;

import java.io.File;
import java.util.Date;
import java.util.List;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPFile;

/**
 * Created by user on 2015/11/24.
 */
public class SyncFtpFiles extends AsyncTask<FileInfo, Integer, FileInfo> {

    @Override
    protected FileInfo doInBackground(FileInfo... params)
    {
        FileInfo file = params[0];
        try {
            FTPClient ftpClient = new FTPClient();
            ConnectionInfo conn = DBManager.getConnection(file.connectionName);
            if (conn != null) {
                ftpClient.connect(conn.url);
                ftpClient.login(conn.id, conn.password);
                Date date = ftpClient.modifiedDate(file.remoteFullFilePath);
                long size = ftpClient.fileSize(file.remoteFullFilePath);
                //日期不同或是大小不同就下載
                if (date.compareTo(file.remoteTimeStamp) != 0 || size != (long) file.size) {
                    File newFile = new File(file.localFullFilePath);
                    ftpClient.download(file.remoteFullFilePath, newFile);
                    file.size = (int) size;
                    file.remoteTimeStamp = date;
                    file.updateTime = new Date(System.currentTimeMillis());
                    file.syncSucceed = true;
                } else {
                    file.updateTime = new Date(System.currentTimeMillis());
                    file.syncSucceed = true; //檔案不變就不下載,算sync成功??
                }
            } else {
                file.syncSucceed = false;
            }

            if (ftpClient != null)
                ftpClient.disconnect(true);

        } catch (Exception e) {
            file.syncSucceed = false;
        }

        return file;
    }

    @Override
    protected void onPostExecute(FileInfo result)
    {
        super.onPostExecute(result);
    }
}
