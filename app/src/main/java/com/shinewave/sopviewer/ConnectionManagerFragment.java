package com.shinewave.sopviewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
//import android.app.Fragment;
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
import android.widget.Spinner;
import android.widget.TextView;

import com.shinewave.sopviewer.dummy.DummyContent;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
public class ConnectionManagerFragment extends Fragment implements AbsListView.OnItemClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private int mParam1;
    private String mParam2;

    private static final String FV_NAME= "connectionName";
    private static final String FV_URL = "url";
    private static final String FV_CONNECTION = "fileObject";
    private static final int CONNECT_OVERTIME = 5;

    private IFragmentInteraction mListener;
    private ArrayList<HashMap<String,Object>> connList = new ArrayList<HashMap<String,Object>>();
    private ConnectionInfo info;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private SimpleAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static ConnectionManagerFragment newInstance(int param1, String param2) {
        ConnectionManagerFragment fragment = new ConnectionManagerFragment();
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
    public ConnectionManagerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // TODO: Change Adapter to display your content
        //mAdapter = new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
        //        android.R.layout.simple_list_item_activated_1, DummyContent.ITEMS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connectionmanager, container, false);

        getConnectionList();

        mAdapter = new SimpleAdapter(
                getActivity(),
                connList,
                R.layout.connection_info_view,
                new String[] {FV_NAME,
                        FV_URL},
                new int[] {R.id.fv_textViewConn,
                        R.id.fv_textViewUrl}
        );

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        Button btnCreate = (Button) view.findViewById(R.id.createBtn);
        btnCreate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showConnectionSettingDialog(true);
            }
        });

        Button btnEdit = (Button) view.findViewById(R.id.editBtn);
        btnEdit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showConnectionSettingDialog(false);
            }
        });

        Button btnDelete = (Button) view.findViewById(R.id.deleteBtn);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Alert!!");
                builder.setMessage("Are you sure to delete record");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        deleteConnection(info.connectionName);
                        getConnectionList();
                        mListView.clearChoices();
                        mAdapter.notifyDataSetChanged();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

                builder.create().show();
            }
        });

        Button btnConnect = (Button) view.findViewById(R.id.connectBtn);
        btnConnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                doConnection(info);
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
        if (null != mAdapter)
        {
            HashMap<String, Object> o = (HashMap<String, Object>) mAdapter.getItem(position);
            info = (ConnectionInfo) o.get(FV_CONNECTION);
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

    private void getConnectionList()
    {
        connList.clear();
        List<ConnectionInfo> list = DBManager.getConnectionList();
        try {
            for (int i = 0; i < list.size(); i++) {

                ConnectionInfo info = list.get(i);
                HashMap<String, Object> cItem = new HashMap<String, Object>();

                cItem.put(FV_NAME, info.connectionName);
                cItem.put(FV_URL, info.protocolType+"://"+info.url);
                cItem.put(FV_CONNECTION, info);

                connList.add(cItem);
            }
        } catch (Exception e) {
        }
    }

    private boolean saveConnection(ConnectionInfo conn)
    {
        return DBManager.insertConnection(conn);
    }

    private boolean deleteConnection(String connName)
    {
        return DBManager.deleteConnection(connName);
    }

    private boolean updateConnection(ConnectionInfo conn)
    {
        return DBManager.updateConnection(conn);
    }

    private void doConnection(ConnectionInfo conn)
    {
        if(conn != null)
        {
            if(ConnectionInfo.ProtocolType.FTP.equals(ConnectionInfo.ProtocolType.valueOf(conn.protocolType)))
            {
                try {
                    FTPClient ftpClient = new FTPClient();
                    ftpClient.getConnector().setConnectionTimeout(CONNECT_OVERTIME);
                    ftpClient.connect(conn.url);
                    ftpClient.login(conn.id, conn.password);
                    ftpClient.changeDirectory("/");
                    FTPFile[] ftpFiles = ftpClient.list();
                    if (ftpFiles != null) {
                        System.out.println(ftpFiles.length);
                    }
                }
                catch(Exception e)
                {
                    Log.e("doConnection", e.toString());
                }
            }
            else if(ConnectionInfo.ProtocolType.SMB.equals(ConnectionInfo.ProtocolType.valueOf(conn.protocolType)))
            {
                try {
                    NtlmPasswordAuthentication authentication = new NtlmPasswordAuthentication("", conn.id, conn.password); // domain, user, password
                    SmbFile currentFolder = new SmbFile("smb://" + conn.url, authentication);
                    SmbFile[] listFiles = currentFolder.listFiles();
                    if (listFiles != null) {
                        System.out.println(listFiles.length);
                    }
                }
                catch(Exception e)
                {
                    Log.e("doConnection", e.toString());
                }

            }
        }
    }

    private void showConnectionSettingDialog(final boolean isCreate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View textEntryView = factory.inflate(R.layout.connection_setting, null);
        builder.setTitle("Connection Setting");
        builder.setView(textEntryView);

        ArrayAdapter protocolAdapter = new ArrayAdapter<ConnectionInfo.ProtocolType>(getActivity(),
                android.R.layout.simple_spinner_item, ConnectionInfo.ProtocolType.values());

        final EditText name = (EditText) textEntryView.findViewById(R.id.editConnName);
        final Spinner sp = (Spinner) textEntryView.findViewById(R.id.spinProtocol);
        final EditText url = (EditText) textEntryView.findViewById(R.id.editUrl);
        final EditText id = (EditText) textEntryView.findViewById(R.id.editId);
        final EditText pw = (EditText) textEntryView.findViewById(R.id.editPassword);
        sp.setAdapter(protocolAdapter);
        if(isCreate)
        {
            name.setEnabled(true);
            name.setText("");
            sp.setSelection(0);
            url.setText("");
            id.setText("");
            pw.setText("");
        }
        else
        {
            name.setEnabled(false);

                if(info != null)
                {
                    name.setText(info.connectionName);
                    sp.setSelection(info.protocol);
                    url.setText(info.url);
                    id.setText(info.id);
                    pw.setText(info.password);
                }
        }

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                ConnectionInfo info = new ConnectionInfo();
                info.connectionName = name.getText().toString();
                info.protocol = ((ConnectionInfo.ProtocolType)sp.getSelectedItem()).toInt();
                info.url = url.getText().toString();
                info.id = id.getText().toString();
                info.password = pw.getText().toString();
                if(isCreate)
                {
                    saveConnection(info);
                    getConnectionList();
                    mListView.clearChoices();
                    mAdapter.notifyDataSetChanged();
                }
                else
                {
                    updateConnection(info);
                    getConnectionList();
                    mListView.clearChoices();
                    mAdapter.notifyDataSetChanged();
                }


            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        builder.create().show();
    }
}
