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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.shinewave.sopviewer.dummy.DummyContent;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link IFragmentInteraction}
 * interface.
 */
public class FileMamagerFragment extends Fragment implements AbsListView.OnItemClickListener {

    private static final String TAG = "FileMamagerFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private int mParam1;
    private String mParam2;

    private static final String FV_IMAGE = "image";
    private static final String FV_FILE_NAME = "fileName";
    private static final String FV_CONNECTION = "connection_name";
    private static final String FV_UPDATE_DT = "last_update_time";
    private static final String FV_File = "fileObject";
    private static final String FV_SyncBtn = "sync";
    private static final String FV_DelBtn = "delete";

    public static String nowPath;
    public static List<FileInfo> FileInfolist;
    private static Context ctext;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private IFragmentInteraction mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private FlieAdapte mAdapter;
    private static ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

    // TODO: Rename and change types of parameters
    public static FileMamagerFragment newInstance(int param1, String param2) {
        FileMamagerFragment fragment = new FileMamagerFragment();
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
    public FileMamagerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctext = getActivity();
        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // TODO: Change Adapter to display your content
        /*
        mAdapter = new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
                android.R.layout.simple_list_item_activated_1, DummyContent.ITEMS);
        */
        getFileList();
        setupFileList(list, FileInfolist, null);

        mAdapter = new FlieAdapte(
                getActivity(),
                list,
                R.layout.file_info_view,
                new String[]{FV_IMAGE,
                        FV_FILE_NAME,
                        FV_CONNECTION,
                        FV_UPDATE_DT,
                        FV_SyncBtn,
                        FV_DelBtn},
                new int[]{R.id.fv_imageView,
                        R.id.fv_textViewFile,
                        R.id.fv_textViewConn,
                        R.id.fv_textViewLastestDT,
                        R.id.ItemButton_Sync,
                        R.id.ItemButton_Del},
                false
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filemamager, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
        // Set SINGLE CHOICE MODE
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        Button btnCreate = (Button) view.findViewById(R.id.createBtn);
        btnCreate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showCreateFolderDialog();
            }
        });

        Button btnEdit = (Button) view.findViewById(R.id.syncAllBtn);
        btnEdit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showSyncDialog();
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
            try {
                HashMap<String, Object> o = (HashMap<String, Object>) mAdapter.getItem(position);
                File sf = (File) o.get(FV_File);
                if (sf.isDirectory()) {
                    setupFileList(list, FileInfolist, sf.getAbsolutePath());

                    mListView.clearChoices();

                    mAdapter.notifyDataSetChanged();
                }
            } catch (ClassCastException ec) {
                //normal case
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

    private static void setupFileList(ArrayList<HashMap<String, Object>> list, List<FileInfo> fInfo, String path) {
        //clear before setup
        list.clear();

        HashMap<String, Object> fItem = null;

        if ((null != path) && !path.isEmpty()
                && !path.equalsIgnoreCase(Environment.getExternalStorageDirectory().toString())) {
            //use the provided path
            fItem = new HashMap<String, Object>();
            fItem.put(FV_IMAGE, R.drawable.forder_back);
            fItem.put(FV_FILE_NAME, "BACK");
            fItem.put(FV_CONNECTION, "");
            fItem.put(FV_UPDATE_DT, "");
            fItem.put(FV_SyncBtn, "");
            fItem.put(FV_DelBtn, "");
            fItem.put(FV_File, new File(path).getParentFile());

            list.add(fItem);
        } else {
            path = Environment.getExternalStorageDirectory().toString();
        }
        nowPath = path;
        //query localfile
        try {
            Log.d(TAG, "Path: " + path);
            File f = new File(path);
            File file[] = f.listFiles();
            Log.d(TAG, "Size: " + file.length);

            for (int i = 0; i < file.length; i++) {
                Log.d(TAG, "FileName:" + file[i].getName());
                File f1 = file[i];
                if (!f1.canRead() || (!f1.isDirectory() && !f1.getName().toLowerCase().endsWith("pdf")))
                    continue;
                fItem = new HashMap<String, Object>();
                boolean isUpdate = false;
                if (fInfo != null) {
                    for (FileInfo item : fInfo) {
                        if (item.localFullFilePath.equals(f1.getAbsolutePath())) {
                            fItem.put(FV_CONNECTION, ctext.getString(R.string.label_source) + item.connectionName);
                            fItem.put(FV_UPDATE_DT, ctext.getString(R.string.label_last_update) +
                                    sdf.format(item.updateTime));
                            isUpdate = true;
                        }
                    }
                    if (!isUpdate) {
                        fItem.put(FV_CONNECTION, ctext.getString(R.string.label_source) + "Local");
                        fItem.put(FV_UPDATE_DT, ctext.getString(R.string.label_last_update) +
                                sdf.format(new Date(f1.lastModified())));
                    }
                }

                fItem.put(FV_IMAGE, f1.isDirectory() ? R.drawable.folder_pdf : R.drawable.pdf);
                fItem.put(FV_FILE_NAME, f1.getName());
                fItem.put(FV_SyncBtn, "Sync");
                fItem.put(FV_DelBtn, "Delete");
                fItem.put(FV_File, f1);

                list.add(fItem);
            }
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
        }
    }

    public static String doSyncAll(String path) {
        StringBuilder failName = new StringBuilder();

        List<FileInfo> syncList = new ArrayList<>();
        List<FileInfo> fInfoList = FileInfolist;
        for (FileInfo info : fInfoList) {
            if (info.localFullFilePath.startsWith(path)) {
                syncList.add(info);
            }
        }
        if (syncList.size() > 0) {
            List<FileInfo> resList = new ArrayList<>();
            //TODO:call ConnectionManager Sync()
            resList = ConnectionManagerFragment.doSync(syncList); //james add

            updateFileInfo(resList);

            for (FileInfo info : resList) {
                if (!info.syncSucceed)
                    failName.append(info.localFullFilePath).append(",");
            }
        }
        return failName.toString();
    }

    private void showCreateFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View textEntryView = factory.inflate(R.layout.create_folder, null);
        builder.setTitle("Create Folder");
        builder.setView(textEntryView);

        final EditText name = (EditText) textEntryView.findViewById(R.id.txtFolderName);
        name.setText("");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                File folder = new File(nowPath + File.separator + name.getText());
                boolean success;
                if (!folder.exists()) {
                    success = folder.mkdir();
                    if (success) {
                        getFileList();
                        setupFileList(list, FileInfolist, nowPath);
                        mListView.clearChoices();
                        mAdapter.notifyDataSetChanged();
                        Toast.makeText(getActivity(), "新增成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "新增失敗", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "資料夾已存在", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        builder.create().show();
    }

    private void showSyncDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View textEntryView = factory.inflate(R.layout.message_dialog, null);
        builder.setTitle("Warning");
        builder.setView(textEntryView);

        final TextView msg = (TextView) textEntryView.findViewById(R.id.lblMassage);
        msg.setText("確定要同步?");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String failName = doSyncAll(nowPath);
                if (failName.equals("")) {
                    getFileList();
                    setupFileList(list, FileInfolist, nowPath);
                    mListView.clearChoices();
                    mAdapter.notifyDataSetChanged();
                    Toast.makeText(getActivity(), "同步成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "同步失敗:" + failName, Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        builder.create().show();
    }

    public static void resetListViewData() {
        getFileList();
        setupFileList(list, FileInfolist, nowPath);
    }

    private static void getFileList() {
        FileInfolist = DBManager.getFileInfo();
    }

    public static boolean insertFileInfo(FileInfo fInfo) {
        return DBManager.insertFileInfo(fInfo);
    }

    public static void deleteFileInfo(String pathName) {
        boolean success;
        List<FileInfo> retryList = new ArrayList<>();
        for (FileInfo info : FileInfolist) {
            if (info.localFullFilePath.startsWith(pathName)) {
                success = DBManager.deleteFileInfo(info.localFullFilePath);
                if (!success) {
                    retryList.add(info);
                }
            }
        }
        //retry
        if (retryList.size() > 0) {
            for (FileInfo info : retryList) {
                DBManager.deleteFileInfo(info.localFullFilePath);
            }
        }
    }

    private static void updateFileInfo(List<FileInfo> fInfoList) {
        boolean success;
        List<FileInfo> retryList = new ArrayList<>();
        for (FileInfo info : fInfoList) {
            success = DBManager.updateFileInfo(info);
            if (!success) {
                retryList.add(info);
            }
        }
        //retry
        if (retryList.size() > 0) {
            for (FileInfo info : retryList) {
                DBManager.updateFileInfo(info);
            }
        }
    }
}
