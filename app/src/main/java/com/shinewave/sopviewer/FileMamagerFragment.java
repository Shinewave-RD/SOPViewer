package com.shinewave.sopviewer;

import android.app.Activity;
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
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.shinewave.sopviewer.dummy.DummyContent;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

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
    private SimpleAdapter mAdapter;
    private ArrayList<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();

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

        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // TODO: Change Adapter to display your content
        /*
        mAdapter = new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
                android.R.layout.simple_list_item_activated_1, DummyContent.ITEMS);
        */
        setupFileList(list, null);

        mAdapter = new SimpleAdapter(
                getActivity(),
                list,
                R.layout.file_info_view,
                new String[] {FV_IMAGE,
                        FV_FILE_NAME,
                        FV_CONNECTION,
                        FV_UPDATE_DT},
                new int[] {R.id.fv_imageView,
                        R.id.fv_textViewFile,
                        R.id.fv_textViewConn,
                        R.id.fv_textViewLastestDT}
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
            try
            {
                HashMap<String,Object> o = (HashMap<String,Object>) mAdapter.getItem(position);
                File sf = (File) o.get(FV_File);
                if (sf.isDirectory()) {
                    setupFileList(list, sf.getAbsolutePath());

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

    private void setupFileList(ArrayList<HashMap<String,Object>> list, String path) {
        //clear before setup
        list.clear();

        HashMap<String, Object> fItem = null;

        if ((null != path) && !path.isEmpty()
                && !path.equalsIgnoreCase(Environment.getExternalStorageDirectory().toString())) {
            //use the provided path
            fItem = new HashMap<String, Object>();
            fItem.put(FV_IMAGE,R.drawable.forder_back);
            fItem.put(FV_FILE_NAME,"BACK");
            fItem.put(FV_CONNECTION, "");
            fItem.put(FV_UPDATE_DT,"");
            fItem.put(FV_File,new File(path).getParentFile());

            list.add(fItem);
        } else {
            path = Environment.getExternalStorageDirectory().toString();
        }

        //query localfile
        try {
            Log.d(TAG, "Path: " + path);
            File f = new File(path);
            File file[] = f.listFiles();
            Log.d(TAG, "Size: " + file.length);

            for (int i = 0; i < file.length; i++) {
                Log.d(TAG, "FileName:" + file[i].getName());
                File f1 = file[i];
                if (!f1.canRead())
                    continue;
                fItem = new HashMap<String, Object>();

                fItem.put(FV_IMAGE, f1.isDirectory() ? R.drawable.folder_pdf : R.drawable.pdf);
                fItem.put(FV_FILE_NAME, f1.getName());
                fItem.put(FV_CONNECTION, getString(R.string.label_source) + "Local");
                fItem.put(FV_UPDATE_DT, getString(R.string.label_last_update) +
                        sdf.format(new Date(f1.lastModified())));
                fItem.put(FV_File, f1);

                list.add(fItem);
            }
        } catch (Exception e) {
            Log.w(TAG,e.getMessage());
        }
    }

}
