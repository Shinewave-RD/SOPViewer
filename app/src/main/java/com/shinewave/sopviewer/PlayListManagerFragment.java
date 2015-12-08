package com.shinewave.sopviewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.shinewave.sopviewer.dummy.DummyContent;

import java.util.ArrayList;
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
public class PlayListManagerFragment extends Fragment implements AbsListView.OnItemClickListener {

    protected static final String TAG = PlayItemFragment.class.getSimpleName();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private int mParam1;
    private String mParam2;

    private IFragmentInteraction mListener;

    private static final String FV_NAME = "PlayListName";
    //private static final String FV_PLAYLIST = "fileObject";
    private String info;
    private ArrayList<HashMap<String, Object>> list_Play = new ArrayList<HashMap<String, Object>>();
    private static Context ctext;
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
    public static PlayListManagerFragment newInstance(int param1, String param2) {
        PlayListManagerFragment fragment = new PlayListManagerFragment();
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
    public PlayListManagerFragment() {
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
        //mAdapter = new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
        //        android.R.layout.simple_list_item_1, android.R.id.text1, DummyContent.ITEMS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlistmanager, container, false);

        getPlayListName();

        mAdapter = new SimpleAdapter(
                getActivity(),
                list_Play,
                R.layout.play_list_info_view,
                new String[]{FV_NAME},
                new int[]{R.id.lblPlayListName}
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
                showPlayItemFragment(true);
            }
        });

        Button btnEdit = (Button) view.findViewById(R.id.editBtn);
        btnEdit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showPlayItemFragment(false);
            }
        });

        Button btnDelete = (Button) view.findViewById(R.id.deleteBtn);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(ctext.getString(R.string.diolog_alter));
                builder.setMessage(ctext.getString(R.string.diolog_delete_record));
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        deletePlayList(info);
                        getPlayListName();
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

        Button btnPlay = (Button) view.findViewById(R.id.playBtn);
        btnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                doPlay(info);
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
            //mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
            HashMap<String, Object> o = (HashMap<String, Object>) mAdapter.getItem(position);
            info = (String) o.get(FV_NAME);
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

    private void showPlayItemFragment(boolean isCreate) {
        String name = "";
        if (!isCreate)
            name = info;
        MainActivity ma = (MainActivity) getActivity();
        ma.onFragmentInteraction(PlayItemFragment.FROM_PLAY_LIST + name);
        ma.onNavigationDrawerItemSelected(5);
    }

    private void doPlay(String pListName) {
        Intent intent = new Intent(getActivity(),
                PDFPlayActivity.class);
        intent.putExtra("PlayListName", pListName);
        startActivity(intent);
    }

    private void getPlayListName() {
        list_Play.clear();
        List<String> list = DBManager.getPlayListName();
        try {
            for (int i = 0; i < list.size(); i++) {

                String name = list.get(i);
                HashMap<String, Object> cItem = new HashMap<String, Object>();

                cItem.put(FV_NAME, name);

                list_Play.add(cItem);
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public static boolean deletePlayList(String pListName) {
        return DBManager.deletePlayList(pListName);
    }

}
