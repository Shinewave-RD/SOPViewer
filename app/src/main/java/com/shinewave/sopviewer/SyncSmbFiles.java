package com.shinewave.sopviewer;

import android.os.Environment;

import com.artifex.mupdfdemo.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

/**
 * Created by user on 2015/11/25.
 */
public class SyncSmbFiles extends AsyncTask<FileInfo, Integer, FileInfo> {
    @Override
    protected FileInfo doInBackground(FileInfo... params)
    {
        FileInfo file = params[0];
        try {
            ConnectionInfo conn = DBManager.getConnection(file.connectionName);
            if (conn != null) {
                NtlmPasswordAuthentication authentication = new NtlmPasswordAuthentication("", conn.id, conn.password); // domain, user, password
                SmbFile smbFile = new SmbFile(file.remoteFullFilePath, authentication);
                Date date = new Date(smbFile.lastModified());
                long size = smbFile.length();
                //日期不同或是大小不同就下載
                if (date.compareTo(file.remoteTimeStamp) != 0 || size != (long) file.size) {
                    FileOutputStream fileOutputStream = new FileOutputStream(file.localFullFilePath);
                    InputStream fileInputStream = smbFile.getInputStream();
                    byte[] buf = new byte[16 * 1024 * 1024];
                    int len;
                    while ((len = fileInputStream.read(buf)) > 0) {
                        fileOutputStream.write(buf, 0, len);
                    }
                    fileInputStream.close();
                    fileOutputStream.close();
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
