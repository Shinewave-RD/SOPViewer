package com.shinewave.sopviewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
//import android.app.Fragment;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.artifex.mupdfdemo.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.CollationKey;
import java.text.Collator;
import java.text.RuleBasedCollator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPFile;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link IFragmentInteraction}
 * interface.
 */
public class RemoteFileManagerFragment extends Fragment implements AbsListView.OnItemClickListener {

    private static final String TAG = "RemoteFileMamagerFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private int mParam1;
    private String mParam2;

    private static final String FV_IMAGE = "image";
    private static final String FV_FILE_NAME = "fileName";
    private static final String FV_SIZE = "size";
    private static final String FV_MODIFY_DT = "modify_time";
    private static final String FV_File = "fileObject";
    private static final String FV_SMB_PATH = "smb_path";
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private IFragmentInteraction mListener;
    private AbsListView mListView;
    private ConnectionInfo info;
    private String tmpPath = "/";
    private RuleBasedCollator collator = (RuleBasedCollator) Collator.getInstance(Locale.getDefault());
    private FlieAdapte mAdapter;
    private ArrayList<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
    private HashMap<String,Object> o;

    // TODO: Rename and change types of parameters
    public static RemoteFileManagerFragment newInstance(int param1, String param2) {
        RemoteFileManagerFragment fragment = new RemoteFileManagerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RemoteFileManagerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getActivity();
        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        info = DBManager.getConnection(mParam2);

        setupFileList(list, info, tmpPath);

        mAdapter = new FlieAdapte(
                getActivity(),
                list,
                R.layout.remote_file_info_view,
                new String[] {FV_IMAGE,
                        FV_FILE_NAME,
                        FV_SIZE,
                        FV_MODIFY_DT},
                new int[] {R.id.fv_imageView,
                        R.id.fv_textViewFile,
                        R.id.fv_textViewConn,
                        R.id.fv_textViewLastestDT},
                FileMamagerFragment.FROM_REMOTE
        );


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_remotefilemanager, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
        // Set SINGLE CHOICE MODE
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        Button btnDownload = (Button) view.findViewById(R.id.downloadBtn);
        btnDownload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                doDownload();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (IFragmentInteraction) activity;
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_PARAM1));
            ((MainActivity) activity).restoreActionBar();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
//            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
            try
            {
                o = (HashMap<String,Object>) mAdapter.getItem(position);
                if(ConnectionInfo.ProtocolType.FTP.equals(ConnectionInfo.ProtocolType.valueOf(info.protocolType))) {
                    final FTPFile sf = (FTPFile) o.get(FV_File);
                    if (sf.getType() == FTPFile.TYPE_DIRECTORY) {
                        String newTmpPath = tmpPath + sf.getName() + "/";
                        setupFileList(list, info, newTmpPath);
                        mListView.clearChoices();
                        mAdapter.notifyDataSetChanged();
                    } else if (sf.getType() == FTPFile.TYPE_LINK) {
                        String newTmpPath = sf.getName();
                        setupFileList(list, info, newTmpPath);
                        mListView.clearChoices();
                        mAdapter.notifyDataSetChanged();
                    }
                }
                else if(ConnectionInfo.ProtocolType.SMB.equals(ConnectionInfo.ProtocolType.valueOf(info.protocolType)))
                {
                    final SmbFile sf = (SmbFile) o.get(FV_File);
                    if(sf == null) {
                        String newTmpPath = (String) o.get(FV_SMB_PATH);
                        setupFileList(list, info, newTmpPath);
                        mListView.clearChoices();
                        mAdapter.notifyDataSetChanged();
                    }
                    else if (sf.isDirectory()) {
                        String newTmpPath = tmpPath + sf.getName();
                        setupFileList(list, info, newTmpPath);
                        mListView.clearChoices();
                        mAdapter.notifyDataSetChanged();
                    }
                }
            } catch (Exception ec) {
                Log.e("doConnection", ec.toString());
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Error!!");
                builder.setMessage("Connect Error!! Please check the Connection setting.");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
                builder.create().show();
            }
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }


    private void setupFileList(ArrayList<HashMap<String,Object>> list, ConnectionInfo conn, String path) {
        list.clear();
        HashMap<String, Object> fItem = null;
        if(conn != null) {
            if (ConnectionInfo.ProtocolType.FTP.equals(ConnectionInfo.ProtocolType.valueOf(conn.protocolType))) {
                try {
                    FTPClient ftpClient = new FTPClient();
                    ftpClient.getConnector().setConnectionTimeout(5);
                    ftpClient.connect(conn.url);
                    ftpClient.login(conn.id, conn.password);
                    ftpClient.changeDirectory(path);
                    FTPFile[] ftpFiles = ftpClient.list();
                    if (ftpFiles != null) {
                        int ftpFileCount = ftpFiles.length;
                        int folderNum = 0;
                        for (int ftpFileIndex = 0; ftpFileIndex < ftpFileCount; ++ftpFileIndex) {
                            FTPFile ftpFile = ftpFiles[ftpFileIndex];
                            int type = ftpFile.getType();
                            int fileInfoCount = list.size();
                            if (type == FTPFile.TYPE_DIRECTORY ) {
                                boolean addedFlag = false;
                                fItem = new HashMap<String, Object>();
                                fItem.put(FV_IMAGE, R.drawable.folder_pdf);
                                fItem.put(FV_FILE_NAME, ftpFile.getName());
                                fItem.put(FV_File, ftpFile);

                                for (int fileInfoIndex = 0; fileInfoIndex < folderNum; ++fileInfoIndex) {
                                    if (compare(fItem.get(FV_FILE_NAME).toString(),
                                            list.get(fileInfoIndex).get(FV_FILE_NAME).toString()) < 0) {
                                        list.add(fileInfoIndex, fItem);
                                        addedFlag = true;
                                        break;
                                    }
                                }

                                if (!addedFlag) {
                                    if (folderNum == fileInfoCount) {
                                        list.add(fItem);
                                    } else {
                                        list.add(folderNum, fItem);
                                    }
                                }
                                folderNum++;
                            }
                            else if(type == FTPFile.TYPE_FILE && ftpFile.getName().toLowerCase(Locale.getDefault()).endsWith("pdf"))
                            {
                                boolean addedFlag = false;
                                fItem = new HashMap<String, Object>();
                                fItem.put(FV_SIZE, getString(R.string.label_size) + String.valueOf(ftpFile.getSize()));
                                fItem.put(FV_MODIFY_DT, getString(R.string.label_modify) + sdf.format(ftpFile.getModifiedDate()));
                                fItem.put(FV_IMAGE, R.drawable.pdf);
                                fItem.put(FV_FILE_NAME, ftpFile.getName());
                                fItem.put(FV_File, ftpFile);
                                for (int fileInfoIndex = folderNum; fileInfoIndex < fileInfoCount; ++fileInfoIndex) {
                                    if (compare(fItem.get(FV_FILE_NAME).toString(),
                                            list.get(fileInfoIndex).get(FV_FILE_NAME).toString()) < 0) {
                                        list.add(fileInfoIndex, fItem);
                                        addedFlag = true;
                                        break;
                                    }
                                }
                                if (!addedFlag) {
                                    list.add(fItem);
                                }
                            }
                            else {
                                continue;
                            }
                        }
                    }
                    ftpClient.disconnect(true);

                    if (null != path && !path.isEmpty() && !path.equalsIgnoreCase("/")) {
                        //use the provided path
                        fItem = new HashMap<String, Object>();
                        fItem.put(FV_IMAGE,R.drawable.forder_back);
                        fItem.put(FV_FILE_NAME,"BACK");
                        fItem.put(FV_SIZE, "");
                        fItem.put(FV_MODIFY_DT,"");
                        FTPFile f = new FTPFile();
                        String[] strs = path.split("/");
                        if (strs.length >= 2) {
                            String backPath = "/";
                            for (int i = 0; i < strs.length - 1; ++i) {
                                if (!strs[i].equals("") && strs[i] != null)
                                    backPath = backPath + strs[i] + "/";
                            }
                            f.setName(backPath);
                        }
                        else
                        {
                            f.setName("/");
                        }

                        f.setType(FTPFile.TYPE_LINK);
                        fItem.put(FV_File, f);

                        list.add(0,fItem);
                    }
                } catch (Exception e) {
                    Log.e("doConnection", e.toString());
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Error!!");
                    builder.setMessage("Connect Error!! Please check the Connection setting.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    });
                    builder.create().show();
                }

            } else if (ConnectionInfo.ProtocolType.SMB.equals(ConnectionInfo.ProtocolType.valueOf(conn.protocolType))) {
                try {
                    NtlmPasswordAuthentication authentication = new NtlmPasswordAuthentication("", conn.id, conn.password); // domain, user, password
                    SmbFile currentFolder = new SmbFile("smb://" + conn.url + path, authentication);
                    SmbFile[] smbFiles = currentFolder.listFiles();
                    if (smbFiles != null) {
                        int ftpFileCount = smbFiles.length;
                        int folderNum = 0;
                        for (int ftpFileIndex = 0; ftpFileIndex < ftpFileCount; ++ftpFileIndex) {
                            SmbFile smbFile = smbFiles[ftpFileIndex];
                            int fileInfoCount = list.size();
                            if (smbFile.isDirectory()) {
                                boolean addedFlag = false;
                                fItem = new HashMap<String, Object>();
                                fItem.put(FV_IMAGE, R.drawable.folder_pdf);
                                fItem.put(FV_FILE_NAME, smbFile.getName());
                                fItem.put(FV_File, smbFile);

                                for (int fileInfoIndex = 0; fileInfoIndex < folderNum; ++fileInfoIndex) {
                                    if (compare(fItem.get(FV_FILE_NAME).toString(),
                                            list.get(fileInfoIndex).get(FV_FILE_NAME).toString()) < 0) {
                                        list.add(fileInfoIndex, fItem);
                                        addedFlag = true;
                                        break;
                                    }
                                }

                                if (!addedFlag) {
                                    if (folderNum == fileInfoCount) {
                                        list.add(fItem);
                                    } else {
                                        list.add(folderNum, fItem);
                                    }
                                }
                                folderNum++;
                            }
                            else if(smbFile.isFile() && smbFile.getName().toLowerCase(Locale.getDefault()).endsWith("pdf"))
                            {
                                boolean addedFlag = false;
                                fItem = new HashMap<String, Object>();
                                fItem.put(FV_SIZE, getString(R.string.label_size) + String.valueOf(smbFile.length()));
                                fItem.put(FV_MODIFY_DT, getString(R.string.label_modify) + sdf.format(smbFile.lastModified()));
                                fItem.put(FV_IMAGE, R.drawable.pdf);
                                fItem.put(FV_FILE_NAME, smbFile.getName());
                                fItem.put(FV_File, smbFile);
                                for (int fileInfoIndex = folderNum; fileInfoIndex < fileInfoCount; ++fileInfoIndex) {
                                    if (compare(fItem.get(FV_FILE_NAME).toString(),
                                            list.get(fileInfoIndex).get(FV_FILE_NAME).toString()) < 0) {
                                        list.add(fileInfoIndex, fItem);
                                        addedFlag = true;
                                        break;
                                    }
                                }
                                if (!addedFlag) {
                                    list.add(fItem);
                                }
                            }
                            else {
                                continue;
                            }
                        }

                        if (null != path && !path.isEmpty() && !path.equalsIgnoreCase("/")) {
                            //use the provided path
                            fItem = new HashMap<String, Object>();
                            fItem.put(FV_IMAGE,R.drawable.forder_back);
                            fItem.put(FV_FILE_NAME,"BACK");
                            fItem.put(FV_SIZE, "");
                            fItem.put(FV_MODIFY_DT,"");
                            String[] strs = path.split("/");
                            if (strs.length >= 2) {
                                String backPath = "/";
                                for (int i = 0; i < strs.length - 1; ++i) {
                                    if (!strs[i].equals("") && strs[i] != null)
                                        backPath = backPath + strs[i] + "/";
                                }
                                fItem.put(FV_SMB_PATH,backPath);
                            }
                            else
                            {
                                fItem.put(FV_SMB_PATH,"/");
                            }
                            list.add(0,fItem);
                        }
                    }
                } catch (Exception e) {
                    Log.e("doConnection", e.toString());
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Error!!");
                    builder.setMessage("Connect Error!! Please check the Connection setting.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    });
                    builder.create().show();
                }

            }
            tmpPath = path;
        }
    }

    private int compare(String obj1, String obj2) {

        CollationKey c1 = collator.getCollationKey(obj1);
        CollationKey c2 = collator.getCollationKey(obj2);

        return collator.compare(((CollationKey) c1).getSourceString(),
                ((CollationKey) c2).getSourceString());
    }

    private void doDownload() {
        if (null != o) {
            try {
                if (ConnectionInfo.ProtocolType.FTP.equals(ConnectionInfo.ProtocolType.valueOf(info.protocolType))) {
                    final FTPFile sf = (FTPFile) o.get(FV_File);
                    if(sf.getType() == FTPFile.TYPE_FILE) {
                        File file = new File(Environment.getExternalStorageDirectory()
                                + File.separator + sf.getName());
                        if (!file.exists()) {
                            new DownloadFtpFile().execute(sf);
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Alert!!");
                            builder.setMessage("File exist !! Are you sure to overwrite?");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    new DownloadFtpFile().execute(sf);
                                }
                            });

                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                }
                            });

                            builder.create().show();
                        }
                    }
                } else if (ConnectionInfo.ProtocolType.SMB.equals(ConnectionInfo.ProtocolType.valueOf(info.protocolType))) {
                    final SmbFile sf = (SmbFile) o.get(FV_File);
                    if(sf.isFile()) {
                        File file = new File(Environment.getExternalStorageDirectory()
                                + File.separator + sf.getName());
                        if (!file.exists()) {
                            new DownloadSmbFile().execute(sf);
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Alert!!");
                            builder.setMessage("File exist !! Are you sure to overwrite?");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    new DownloadSmbFile().execute(sf);
                                }
                            });

                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                }
                            });

                            builder.create().show();
                        }
                    }
                }
            } catch (Exception ec) {
                Log.e("doConnection", ec.toString());
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Error!!");
                builder.setMessage("Download Error!! Please check the Connection setting.");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
                builder.create().show();
            }

        }
    }

    private class DownloadFtpFile extends AsyncTask<FTPFile, Integer, File> {
        private FTPClient ftpClient;
        private FTPFile ftpFile;

        @Override
        protected File doInBackground(FTPFile... params)
        {
            try {
                ftpFile = params[0];
                ftpClient = new FTPClient();
                ftpClient.connect(info.url);
                ftpClient.login(info.id, info.password);
                ftpClient.changeDirectory(tmpPath);
                File file = new File(Environment.getExternalStorageDirectory() + File.separator + ftpFile.getName());
                ftpClient.download(ftpFile.getName(), file);
                if (ftpClient != null)
                    ftpClient.disconnect(true);

                return file;
            } catch (Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Error!!");
                builder.setMessage("Download Error!! Please check the Connection setting.");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
                builder.create().show();
                return null;
            }
        }

        @Override
        protected void onPostExecute(File result)
        {
            super.onPostExecute(result);

            if(result == null)
                return;

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Download Finish.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
            builder.create().show();

            FileInfo fileInfo = new FileInfo();
            fileInfo.localFullFilePath = result.getAbsolutePath();
            fileInfo.remoteFullFilePath = tmpPath + result.getName();
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

    private class DownloadSmbFile extends AsyncTask<SmbFile, Integer, String> {
        private SmbFile smbFile;

        @Override
        protected String doInBackground(SmbFile... params)
        {
            try {
                smbFile = params[0];
                String localPath = Environment.getExternalStorageDirectory() + File.separator + smbFile.getName();
                FileOutputStream fileOutputStream = new FileOutputStream(localPath);
                InputStream fileInputStream = smbFile.getInputStream();
                byte[] buf = new byte[16 * 1024 * 1024];
                int len;
                while ((len = fileInputStream.read(buf)) > 0) {
                    fileOutputStream.write(buf, 0, len);
                }
                fileInputStream.close();
                fileOutputStream.close();

                return localPath;
            } catch (Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Error!!");
                builder.setMessage("Download Error!! Please check the Connection setting.");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
                builder.create().show();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            if(result == null)
                return;

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Download Finish.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
            builder.create().show();

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
}
