package com.shinewave.sopviewer;

import android.os.Environment;

import com.shinewave.sopviewer.ServerConnectionInfo;
import com.artifex.mupdfdemo.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

/**
 * Created by user on 2015/12/2.
 */
public class ServerDownloadSmbFile extends AsyncTask<ServerConnectionInfo, Integer, String> {
    private SmbFile smbFile;
    private ServerConnectionInfo info;
    private String localPath;

    @Override
    protected String doInBackground(ServerConnectionInfo... params)
    {
        try {
            info = params[0];
            NtlmPasswordAuthentication authentication = new NtlmPasswordAuthentication("", info.id, info.password); // domain, user, password

            if(info.fullFilePath.startsWith("/"))
                smbFile = new SmbFile("smb://" + info.url + info.fullFilePath, authentication);
            else
                smbFile = new SmbFile("smb://" + info.url + File.separator + info.fullFilePath, authentication);

            if (smbFile != null) {
                if(info.fileSavePath.endsWith("/"))
                    localPath = info.fileSavePath + smbFile.getName();
                else
                    localPath = info.fileSavePath + File.separator + smbFile.getName();

                FileOutputStream fileOutputStream = new FileOutputStream(localPath);
                InputStream fileInputStream = smbFile.getInputStream();
                byte[] buf = new byte[16 * 1024 * 1024];
                int len;
                while ((len = fileInputStream.read(buf)) > 0) {
                    fileOutputStream.write(buf, 0, len);
                }
                fileInputStream.close();
                fileOutputStream.close();
            }
            return localPath;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result)
    {
        super.onPostExecute(result);

        if(result == null)
            return;

        ConnectionInfo connInfo = new ConnectionInfo();
        connInfo.connectionName = info.connectionName;
        connInfo.protocol = info.protocol;
        connInfo.url = info.url;
        connInfo.id = info.id;
        connInfo.password = info.password;
        ConnectionInfo tmpConn = DBManager.getConnection(info.connectionName);
        if(tmpConn != null)
            DBManager.updateConnection(connInfo);
        else
            DBManager.insertConnection(connInfo);

        FileInfo fileInfo = new FileInfo();
        fileInfo.localFullFilePath = result;
        fileInfo.remoteFullFilePath = smbFile.getPath();
        fileInfo.connectionName = info.connectionName;
        fileInfo.updateTime = new Date(System.currentTimeMillis());
        fileInfo.syncSucceed = true;
        fileInfo.size = (int)result.length();
        try {
            fileInfo.remoteTimeStamp = new Date(smbFile.lastModified());
        }
        catch(Exception e)
        {}

        FileInfo tmp = DBManager.getSingleFileInfo(fileInfo.localFullFilePath);
        if(tmp != null && tmp.localFullFilePath != null)
            DBManager.updateFileInfo(fileInfo);
        else
            DBManager.insertFileInfo(fileInfo);
    }

}
