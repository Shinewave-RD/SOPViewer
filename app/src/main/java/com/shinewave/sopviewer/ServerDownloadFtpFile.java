package com.shinewave.sopviewer;

import com.ServerConnectionInfo;
import com.artifex.mupdfdemo.AsyncTask;

import java.io.File;
import java.util.Date;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPFile;

/**
 * Created by user on 2015/12/2.
 */
public class ServerDownloadFtpFile extends AsyncTask<ServerConnectionInfo, Integer, File> {
    private FTPClient ftpClient;
    private FTPFile ftpFile;
    private ServerConnectionInfo info;

    @Override
    protected File doInBackground(ServerConnectionInfo... params)
    {
        try {
            info = params[0];
            String tmpPath = info.fullFilePath.substring(0, info.fullFilePath.lastIndexOf("/"));
            String fileName = info.fullFilePath.substring(info.fullFilePath.lastIndexOf("/"));
            ftpClient = new FTPClient();
            ftpClient.connect(info.url);
            ftpClient.login(info.id, info.password);
            ftpClient.changeDirectory(tmpPath);
            FTPFile[] list = ftpClient.list();
            if(list != null) {
                File file = null;
                for(int i = 0; i < list.length; i++)
                {
                    FTPFile f = list[i];
                    if(f.getName() == fileName)
                    {
                        ftpFile = f;
                        file = new File(info.fileSavePath);
                        ftpClient.download(fileName, file);
                        break;
                    }
                }

                if (ftpClient != null)
                    ftpClient.disconnect(true);

                return file;
            }
            else
            {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(File result)
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
        fileInfo.localFullFilePath = result.getAbsolutePath();
        fileInfo.remoteFullFilePath = info.fullFilePath;
        fileInfo.connectionName = info.connectionName;
        fileInfo.updateTime = new Date(System.currentTimeMillis());
        fileInfo.syncSucceed = true;
        fileInfo.size = (int)result.length();
        fileInfo.remoteTimeStamp = ftpFile.getModifiedDate();
        FileInfo tmp = DBManager.getSingleFileInfo(fileInfo.localFullFilePath);
        if(tmp != null && tmp.localFullFilePath != null)
            DBManager.updateFileInfo(fileInfo);
        else
            DBManager.insertFileInfo(fileInfo);
    }

}
