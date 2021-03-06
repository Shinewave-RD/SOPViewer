package com.shinewave.sopviewer;

import com.shinewave.sopviewer.ServerConnectionInfo;
import com.artifex.mupdfdemo.AsyncTask;

import java.io.File;
import java.util.Date;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPFile;

/**
 * Created by user on 2015/12/2.
 */
public class ServerDownloadFtpFile extends AsyncTask<ServerConnectionInfo, Integer, ServerConnectionInfo> {
    private FTPClient ftpClient;
    private FTPFile ftpFile;
    private ServerConnectionInfo info;
    private File file = null;

    @Override
    protected ServerConnectionInfo doInBackground(ServerConnectionInfo... params)
    {
        try {
            info = params[0];
            int lastSlashPos = info.fullFilePath.lastIndexOf('/');
            String tmpPath = new String(lastSlashPos == -1
                    ? "/"
                    : info.fullFilePath.substring(0, lastSlashPos));
            String fileName = new String(lastSlashPos == -1
                    ? info.fullFilePath
                    : info.fullFilePath.substring(lastSlashPos+1));

            ftpClient = new FTPClient();
            ftpClient.connect(info.url);
            ftpClient.login(info.id, info.password);
            ftpClient.changeDirectory(tmpPath);
            FTPFile[] list = ftpClient.list();
            if(list != null) {
                for(int i = 0; i < list.length; i++)
                {
                    FTPFile f = list[i];
                    if(f.getName() != null && f.getName().equals(fileName))
                    {
                        ftpFile = f;
                        file = new File(info.fileSavePath+fileName);
                        ftpClient.download(fileName, file);
                        info.downloadSuccessed = true;
                        break;
                    }
                }

                if (ftpClient != null)
                    ftpClient.disconnect(true);

                return info;
            }
            else
            {
                return info;
            }
        } catch (Exception e) {
            return info;
        }
    }

    @Override
    protected void onPostExecute(ServerConnectionInfo result)
    {
        super.onPostExecute(result);

        if(!result.downloadSuccessed)
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
        fileInfo.localFullFilePath = file.getAbsolutePath();
        fileInfo.remoteFullFilePath = info.fullFilePath;
        fileInfo.connectionName = info.connectionName;
        fileInfo.updateTime = new Date(System.currentTimeMillis());
        fileInfo.syncSucceed = true;
        fileInfo.size = (int)file.length();
        fileInfo.remoteTimeStamp = ftpFile.getModifiedDate();
        FileInfo tmp = DBManager.getSingleFileInfo(fileInfo.localFullFilePath);
        if(tmp != null && tmp.localFullFilePath != null)
            DBManager.updateFileInfo(fileInfo);
        else
            DBManager.insertFileInfo(fileInfo);
    }

}
